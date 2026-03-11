// UnHook — Blocked app entity representing an app to monitor
package com.unhook.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
)
