// UnHook — User entity representing the local phone owner
package com.unhook.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val emojiAvatar: String,
    val isMe: Boolean,
    val weeklyPoints: Int = 200,
    val totalResists: Int = 0,
    val currentStreak: Int = 0,
)
