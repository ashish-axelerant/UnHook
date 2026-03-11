// UnHook — Partner entity representing the paired partner (local-only data)
package com.unhook.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "partners")
data class Partner(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val emojiAvatar: String,
    val pairingCode: String,
    val weeklyPoints: Int = 200,
    val totalResists: Int = 0,
    val currentStreak: Int = 0,
)
