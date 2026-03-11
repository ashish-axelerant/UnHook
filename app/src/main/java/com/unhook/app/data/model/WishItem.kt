// UnHook — Wish item entity for weekly winner rewards
package com.unhook.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wish_items")
data class WishItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val createdByMe: Boolean = true,
    val isGranted: Boolean = false,
)
