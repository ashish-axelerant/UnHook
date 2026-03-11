// UnHook — Chore item entity for weekly loser obligations
package com.unhook.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chore_items")
data class ChoreItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val createdByMe: Boolean = true,
    val isCompleted: Boolean = false,
)
