// UnHook — Point event entity tracking each point gain or loss
package com.unhook.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point_events")
data class PointEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val points: Int,
    val reason: String,
    val appPackageName: String,
    val timestamp: Long = System.currentTimeMillis(),
)
