// UnHook — Restarts monitoring service after device reboot
package com.unhook.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, MonitoringForegroundService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
