// UnHook — Application class, initializes Room database and seeds default blocked apps
package com.unhook.app

import android.app.Application
import com.unhook.app.data.db.AppDatabase
import com.unhook.app.data.model.BlockedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UnHookApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        seedDefaultBlockedApps()
    }

    private fun seedDefaultBlockedApps() {
        appScope.launch {
            val dao = database.blockedAppDao()
            if (dao.count() == 0) {
                defaultBlockedApps.forEach { dao.insert(it) }
            }
        }
    }

    companion object {
        val defaultBlockedApps = listOf(
            BlockedApp("com.twitter.android", "Twitter / X"),
            BlockedApp("com.facebook.katana", "Facebook"),
            BlockedApp("com.instagram.android", "Instagram"),
            BlockedApp("com.google.android.youtube", "YouTube"),
            BlockedApp("com.zhiliaoapp.musically", "TikTok"),
            BlockedApp("com.snapchat.android", "Snapchat"),
        )
    }
}
