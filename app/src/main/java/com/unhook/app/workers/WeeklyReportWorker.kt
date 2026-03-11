// UnHook — WorkManager worker for weekly report notification (Sunday 8 PM)
package com.unhook.app.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.unhook.app.MainActivity
import com.unhook.app.R
import com.unhook.app.data.db.AppDatabase

class WeeklyReportWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val user = db.userDao().getMeOnce() ?: return Result.success()
        val partner = db.partnerDao().getPartnerOnce()

        val myPoints = user.weeklyPoints
        val partnerPoints = partner?.weeklyPoints ?: 200
        val isWinner = myPoints > partnerPoints
        val isTied = myPoints == partnerPoints

        val title = when {
            isTied -> applicationContext.getString(R.string.report_notification_tied)
            isWinner -> applicationContext.getString(R.string.report_notification_won)
            else -> applicationContext.getString(R.string.report_notification_lost)
        }

        val body = applicationContext.getString(
            R.string.report_notification_body,
            myPoints,
            partnerPoints,
            user.totalResists,
        )

        createNotificationChannel()
        showNotification(title, body)

        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            applicationContext.getString(R.string.notification_channel_reports),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = applicationContext.getString(R.string.notification_channel_reports_desc)
        }
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun showNotification(title: String, body: String) {
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "unhook_reports"
        const val NOTIFICATION_ID = 2001
        const val WORK_NAME = "weekly_report"
    }
}
