// UnHook — Reminder message entity for intervention screen rotation
package com.unhook.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_messages")
data class ReminderMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isBuiltIn: Boolean = true,
)
