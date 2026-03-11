// UnHook — Application class, initializes Room database
package com.unhook.app

import android.app.Application
import com.unhook.app.data.db.AppDatabase

class UnHookApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
