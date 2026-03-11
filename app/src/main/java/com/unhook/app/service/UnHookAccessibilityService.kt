// UnHook — AccessibilityService that detects when a blocked app is opened
package com.unhook.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.unhook.app.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class UnHookAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var blockedPackages = setOf<String>()
    private var lastDetectedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "AccessibilityService connected")
        refreshBlockedApps()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Ignore our own app and system UI
        if (packageName == "com.unhook.app" || packageName == "com.android.systemui") return

        if (packageName in blockedPackages && packageName != lastDetectedPackage) {
            lastDetectedPackage = packageName
            Log.d(TAG, "Blocked app detected: $packageName")
            broadcastDetection(packageName)
        } else if (packageName !in blockedPackages) {
            lastDetectedPackage = null
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "AccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    fun refreshBlockedApps() {
        scope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            blockedPackages = db.blockedAppDao().getEnabledPackageNames().toSet()
            Log.d(TAG, "Refreshed blocked apps: $blockedPackages")
        }
    }

    private fun broadcastDetection(packageName: String) {
        val intent = Intent(ACTION_APP_DETECTED).apply {
            putExtra(EXTRA_PACKAGE_NAME, packageName)
            setPackage("com.unhook.app")
        }
        sendBroadcast(intent)
    }

    companion object {
        private const val TAG = "UnHookAccessibility"
        const val ACTION_APP_DETECTED = "com.unhook.app.APP_DETECTED"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }
}
