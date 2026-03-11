// UnHook — WorkManager worker to reset points every Monday at midnight
package com.unhook.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.unhook.app.data.db.AppDatabase

class WeeklyResetWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)

        val user = db.userDao().getMeOnce()
        if (user != null) {
            db.userDao().update(user.copy(weeklyPoints = STARTING_POINTS, currentStreak = 0))
        }

        val partner = db.partnerDao().getPartnerOnce()
        if (partner != null) {
            db.partnerDao().update(partner.copy(weeklyPoints = STARTING_POINTS, currentStreak = 0))
        }

        return Result.success()
    }

    companion object {
        const val STARTING_POINTS = 200
        const val WORK_NAME = "weekly_reset"
    }
}
