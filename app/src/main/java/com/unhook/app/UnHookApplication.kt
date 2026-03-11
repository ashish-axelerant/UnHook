// UnHook — Application class, initializes Room database and seeds defaults
package com.unhook.app

import android.app.Application
import com.unhook.app.data.db.AppDatabase
import com.unhook.app.data.model.BlockedApp
import com.unhook.app.data.model.ReminderMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UnHookApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        seedDefaults()
    }

    private fun seedDefaults() {
        appScope.launch {
            val blockedDao = database.blockedAppDao()
            if (blockedDao.count() == 0) {
                defaultBlockedApps.forEach { blockedDao.insert(it) }
            }

            val messageDao = database.reminderMessageDao()
            if (messageDao.count() == 0) {
                defaultMessages.forEach { messageDao.insert(it) }
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

        val defaultMessages = listOf(
            ReminderMessage(text = "Is the scroll worth the chore?"),
            ReminderMessage(text = "Your partner is watching your score right now"),
            ReminderMessage(text = "Put the phone down. Go find your person."),
            ReminderMessage(text = "Future you is proud when you resist."),
            ReminderMessage(text = "Your streak is on the line!"),
            ReminderMessage(text = "Every resist makes you stronger."),
            ReminderMessage(text = "You didn't pick up the phone to scroll."),
            ReminderMessage(text = "5 minutes of scrolling = 5 fewer points."),
            ReminderMessage(text = "Think about what you could do instead."),
            ReminderMessage(text = "This is the moment that counts."),
            ReminderMessage(text = "Close it. You'll thank yourself later."),
            ReminderMessage(text = "Is this app making your life better?"),
            ReminderMessage(text = "Your real life is waiting outside this screen."),
            ReminderMessage(text = "One resist at a time. You've got this."),
            ReminderMessage(text = "Don't let an algorithm control your day."),
            ReminderMessage(text = "Resist now, celebrate later."),
            ReminderMessage(text = "Your partner just resisted. Can you?"),
            ReminderMessage(text = "Be the one with the crown this week."),
            ReminderMessage(text = "The chore list is waiting for the loser..."),
            ReminderMessage(text = "Breathe. You don't need this right now."),
        )
    }
}
