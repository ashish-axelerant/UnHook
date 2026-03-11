// UnHook — Helper to schedule weekly report and daily motivation notifications
package com.unhook.app.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.unhook.app.workers.WeeklyReportWorker
import com.unhook.app.workers.WeeklyResetWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationHelper {

    fun scheduleWeeklyWork(context: Context) {
        scheduleWeeklyReset(context)
        scheduleWeeklyReport(context)
    }

    private fun scheduleWeeklyReset(context: Context) {
        val delay = calculateDelayUntil(Calendar.MONDAY, 0, 0)

        val request = PeriodicWorkRequestBuilder<WeeklyResetWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WeeklyResetWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun scheduleWeeklyReport(context: Context) {
        val delay = calculateDelayUntil(Calendar.SUNDAY, 20, 0)

        val request = PeriodicWorkRequestBuilder<WeeklyReportWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WeeklyReportWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun calculateDelayUntil(dayOfWeek: Int, hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        return target.timeInMillis - now.timeInMillis
    }
}
