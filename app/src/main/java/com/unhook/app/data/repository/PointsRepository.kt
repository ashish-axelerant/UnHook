// UnHook — Repository for PointEvent DB operations
package com.unhook.app.data.repository

import com.unhook.app.data.db.PointEventDao
import com.unhook.app.data.model.PointEvent
import kotlinx.coroutines.flow.Flow

class PointsRepository(
    private val pointEventDao: PointEventDao,
) {
    fun getRecentEvents(userId: Int, limit: Int = 5): Flow<List<PointEvent>> =
        pointEventDao.getRecentEvents(userId, limit)

    fun getAllEvents(userId: Int): Flow<List<PointEvent>> =
        pointEventDao.getAllEvents(userId)

    suspend fun logEvent(userId: Int, points: Int, reason: String, appPackageName: String): Long {
        val event = PointEvent(
            userId = userId,
            points = points,
            reason = reason,
            appPackageName = appPackageName,
        )
        return pointEventDao.insert(event)
    }
}
