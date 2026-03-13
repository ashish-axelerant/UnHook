// UnHook — AccessibilityService that detects when a blocked app is opened
package com.unhook.app.service

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.unhook.app.R
import com.unhook.app.data.db.AppDatabase
import com.unhook.app.ui.overlay.InterventionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UnHookAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var blockedPackages = setOf<String>()
    private var lastDetectedPackage: String? = null
    private var interventionShowing = false
    private val reminderJobs = mutableMapOf<String, Job>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "AccessibilityService connected")
        refreshBlockedApps()
        ensureReminderChannel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Ignore our own app and system UI
        if (packageName == "com.unhook.app" || packageName == "com.android.systemui") return

        // Purge expired grace periods and reset lastDetectedPackage for expired ones
        val now = System.currentTimeMillis()
        val expired = tempAllowedPackages.entries.filter { it.value < now }.map { it.key }
        expired.forEach { pkg ->
            tempAllowedPackages.remove(pkg)
            reminderJobs.remove(pkg)?.cancel()
            if (lastDetectedPackage == pkg) lastDetectedPackage = null
            Log.d(TAG, "Grace period expired for $pkg")
        }

        // Grace period active for this package
        if (packageName in tempAllowedPackages) {
            // User returned to the blocked app during grace period — resume reminders
            // if no job is running (they were paused when they left).
            if (!reminderJobs.containsKey(packageName)) {
                val remaining = tempAllowedPackages[packageName]!! - System.currentTimeMillis()
                if (remaining > 0) {
                    val prefs = getSharedPreferences("unhook_prefs", MODE_PRIVATE)
                    val intervalMs = prefs.getInt("reminder_interval_seconds", 120) * 1000L
                    scheduleReminders(packageName, remaining, intervalMs)
                    Log.d(TAG, "User returned to $packageName — reminders resumed ($remaining ms left)")
                }
            }
            lastDetectedPackage = packageName
            return
        }

        if (packageName in blockedPackages && packageName != lastDetectedPackage) {
            lastDetectedPackage = packageName
            interventionShowing = true
            Log.d(TAG, "Blocked app detected: $packageName")
            launchIntervention(packageName)
        } else if (packageName !in blockedPackages) {
            // User left a blocked/grace-period app — pause reminders and clear
            // the notification, but keep tempAllowedPackages so they can return
            // without being re-intervened while the grace period is still valid.
            val leavingPkg = lastDetectedPackage
            if (leavingPkg != null && leavingPkg in tempAllowedPackages) {
                pauseReminders(leavingPkg)
                Log.d(TAG, "User left $leavingPkg — reminders paused, grace period preserved")
            }
            lastDetectedPackage = null
            interventionShowing = false
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "AccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        reminderJobs.values.forEach { it.cancel() }
        scope.cancel()
    }

    fun refreshBlockedApps() {
        scope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            blockedPackages = db.blockedAppDao().getEnabledPackageNames().toSet()
            Log.d(TAG, "Refreshed blocked apps: $blockedPackages")
        }
    }

    /**
     * Pauses reminder notifications when the user leaves a grace-period app.
     * The grace period expiry timestamp is preserved so they can return without
     * being re-intervened while time remains.
     */
    private fun pauseReminders(packageName: String) {
        reminderJobs.remove(packageName)?.cancel()
        getSystemService(NotificationManager::class.java)?.cancel(REMINDER_NOTIFICATION_ID)
    }

    /**
     * Grants a grace period for [packageName] and starts periodic reminder
     * notifications for as long as the user is inside the app.
     */
    fun startGracePeriod(packageName: String, durationMs: Long, reminderIntervalMs: Long) {
        tempAllowedPackages[packageName] = System.currentTimeMillis() + durationMs
        Log.d(TAG, "Grace period started for $packageName (${durationMs / 1000}s)")
        scheduleReminders(packageName, durationMs, reminderIntervalMs)
    }

    /**
     * Schedules the reminder loop for [packageName] to run for [durationMs] ms.
     * When the loop ends naturally (grace period expired) the user is sent Home.
     * If cancelled early (user left the app), the loop simply stops — the expiry
     * timestamp in [tempAllowedPackages] is not touched.
     */
    private fun scheduleReminders(packageName: String, durationMs: Long, reminderIntervalMs: Long) {
        reminderJobs[packageName]?.cancel()
        reminderJobs[packageName] = scope.launch {
            val expiresAt = System.currentTimeMillis() + durationMs
            delay(reminderIntervalMs)
            while (System.currentTimeMillis() < expiresAt) {
                sendReminderNotification(packageName)
                delay(reminderIntervalMs)
            }
            // Grace period expired naturally — clean up and send user Home
            tempAllowedPackages.remove(packageName)
            if (lastDetectedPackage == packageName) lastDetectedPackage = null
            reminderJobs.remove(packageName)
            getSystemService(NotificationManager::class.java)?.cancel(REMINDER_NOTIFICATION_ID)
            startActivity(
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
            Log.d(TAG, "Grace period expired for $packageName — sent to Home")
        }
    }

    private suspend fun sendReminderNotification(packageName: String) {
        val db = AppDatabase.getInstance(applicationContext)
        val message = db.reminderMessageDao().getRandom() ?: return
        val appName = db.blockedAppDao().getAppName(packageName) ?: packageName

        // Full-screen intent — re-launches the intervention screen mid-scroll
        val fullScreenIntent = Intent(applicationContext, InterventionActivity::class.java).apply {
            putExtra(InterventionActivity.EXTRA_PACKAGE_NAME, packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            applicationContext,
            packageName.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Vibrate directly — guaranteed regardless of notification channel/DND settings
        val vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 600)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VibratorManager::class.java)
            vm?.defaultVibrator?.vibrate(
                VibrationEffect.createWaveform(vibrationPattern, -1),
            )
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)?.vibrate(
                VibrationEffect.createWaveform(vibrationPattern, -1),
            )
        }

        val nm = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
            .setContentTitle("⛔ $appName — Put the phone down.")
            .setContentText(message.text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("\n${message.text}\n\nYou are losing points every time you ignore this.")
                    .setBigContentTitle("⛔ $appName — Put the phone down."),
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()

        nm.notify(REMINDER_NOTIFICATION_ID, notification)
        Log.d(TAG, "Sent reminder for $packageName: ${message.text}")
    }

    private fun ensureReminderChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        // Always recreate so vibration/sound settings take effect immediately
        nm.deleteNotificationChannel(REMINDER_CHANNEL_ID)
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Scroll Reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Interrupts you while using a blocked app"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400)
            setBypassDnd(false)
        }
        nm.createNotificationChannel(channel)
    }

    private fun launchIntervention(packageName: String) {
        val intent = Intent(this, InterventionActivity::class.java).apply {
            putExtra(InterventionActivity.EXTRA_PACKAGE_NAME, packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "UnHookAccessibility"
        private const val REMINDER_CHANNEL_ID = "unhook_reminders"
        private const val REMINDER_NOTIFICATION_ID = 2001

        // Exposed so the ViewModel can call startGracePeriod without a broadcast
        var instance: UnHookAccessibilityService? = null

        // packageName -> allow until timestamp (ms)
        val tempAllowedPackages = mutableMapOf<String, Long>()
    }
}
