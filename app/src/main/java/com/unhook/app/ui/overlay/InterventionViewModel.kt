// UnHook — ViewModel managing intervention overlay state and countdown
package com.unhook.app.ui.overlay

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unhook.app.data.db.AppDatabase
import com.unhook.app.data.model.PointEvent
import com.unhook.app.service.UnHookAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InterventionUiState(
    val appName: String = "",
    val packageName: String = "",
    val myName: String = "",
    val myEmoji: String = "",
    val myPoints: Int = 200,
    val partnerName: String = "",
    val partnerEmoji: String = "",
    val partnerPoints: Int = 200,
    val reminderMessage: String = "",
    val countdownSeconds: Int = 5,
    val canLetIn: Boolean = false,
    val isVisible: Boolean = false,
    // true = user resisted → send them to Home, not back to the blocked app
    val navigateHome: Boolean = false,
)

class InterventionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val _uiState = MutableStateFlow(InterventionUiState())
    val uiState: StateFlow<InterventionUiState> = _uiState

    fun showIntervention(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = db.userDao().getMeOnce()
            val partner = db.partnerDao().getPartnerOnce()
            val appName = db.blockedAppDao().getAppName(packageName) ?: packageName
            val message = db.reminderMessageDao().getRandom()

            val prefs = getApplication<Application>()
                .getSharedPreferences("unhook_prefs", Context.MODE_PRIVATE)
            val countdown = prefs.getInt("countdown_seconds", 5)

            _uiState.value = InterventionUiState(
                appName = appName,
                packageName = packageName,
                myName = user?.name ?: "You",
                myEmoji = user?.emojiAvatar ?: "😊",
                myPoints = user?.weeklyPoints ?: 200,
                partnerName = partner?.name ?: "Partner",
                partnerEmoji = partner?.emojiAvatar ?: "❓",
                partnerPoints = partner?.weeklyPoints ?: 200,
                reminderMessage = message?.text ?: "Is the scroll really worth it?",
                countdownSeconds = countdown,
                canLetIn = false,
                isVisible = true,
            )

            // Start countdown
            for (i in (countdown - 1) downTo 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    countdownSeconds = i,
                    canLetIn = i == 0,
                )
            }
        }
    }

    fun onResist() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val user = db.userDao().getMeOnce() ?: return@launch

            // Award +10 points
            db.pointEventDao().insert(
                PointEvent(
                    userId = user.id,
                    points = 10,
                    reason = "Resisted ${state.appName}",
                    appPackageName = state.packageName,
                ),
            )
            // Update streak: increment if resisted today
            val newStreak = user.currentStreak + 1

            db.userDao().update(
                user.copy(
                    weeklyPoints = user.weeklyPoints + 10,
                    totalResists = user.totalResists + 1,
                    currentStreak = newStreak,
                ),
            )

            // Bonus: 3-day streak = +50 points
            if (newStreak > 0 && newStreak % 3 == 0) {
                db.pointEventDao().insert(
                    PointEvent(
                        userId = user.id,
                        points = 50,
                        reason = "$newStreak-day streak bonus!",
                        appPackageName = "",
                    ),
                )
                db.userDao().update(
                    db.userDao().getMeOnce()!!.copy(
                        weeklyPoints = db.userDao().getMeOnce()!!.weeklyPoints + 50,
                    ),
                )
            }

            _uiState.value = _uiState.value.copy(isVisible = false, navigateHome = true)
        }
    }

    fun onLetMeIn() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val user = db.userDao().getMeOnce() ?: return@launch

            // Read configurable grace period from prefs
            val prefs = getApplication<Application>()
                .getSharedPreferences("unhook_prefs", Context.MODE_PRIVATE)
            val gracePeriodMs = prefs.getInt("grace_period_minutes", 10) * 60 * 1000L
            val reminderIntervalMs = prefs.getInt("reminder_interval_seconds", 120) * 1000L

            // Start grace period with periodic reminder notifications
            val service = UnHookAccessibilityService.instance
            if (service != null) {
                service.startGracePeriod(state.packageName, gracePeriodMs, reminderIntervalMs)
            } else {
                // Fallback: set grace period without reminders
                UnHookAccessibilityService.tempAllowedPackages[state.packageName] =
                    System.currentTimeMillis() + gracePeriodMs
            }

            // Deduct -15 points
            db.pointEventDao().insert(
                PointEvent(
                    userId = user.id,
                    points = -15,
                    reason = "Scrolled past ${state.appName}",
                    appPackageName = state.packageName,
                ),
            )
            db.userDao().update(
                user.copy(
                    weeklyPoints = user.weeklyPoints - 15,
                    currentStreak = 0,
                ),
            )

            _uiState.value = _uiState.value.copy(isVisible = false)
        }
    }

    fun dismiss() {
        _uiState.value = _uiState.value.copy(isVisible = false)
    }
}
