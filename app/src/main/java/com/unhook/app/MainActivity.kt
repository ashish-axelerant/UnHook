// UnHook — Single activity entry point, hosts Compose UI
package com.unhook.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.unhook.app.data.repository.PointsRepository
import com.unhook.app.data.repository.UserRepository
import com.unhook.app.navigation.UnHookNavGraph
import com.unhook.app.notification.NotificationHelper
import com.unhook.app.service.MonitoringForegroundService
import com.unhook.app.ui.screens.OnboardingScreen
import com.unhook.app.ui.theme.UnHookTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = (application as UnHookApplication).database
        val userRepository = UserRepository(db.userDao(), db.partnerDao())
        val pointsRepository = PointsRepository(db.pointEventDao())
        val blockedAppDao = db.blockedAppDao()
        val choreItemDao = db.choreItemDao()
        val wishItemDao = db.wishItemDao()

        // Schedule weekly workers
        NotificationHelper.scheduleWeeklyWork(this)

        setContent {
            UnHookTheme {
                val user by userRepository.getMe().collectAsState(initial = null)
                val scope = rememberCoroutineScope()

                if (user == null) {
                    OnboardingScreen(
                        onComplete = { userName, userAvatar, partnerName, partnerAvatar, pairingCode ->
                            scope.launch(Dispatchers.IO) {
                                userRepository.createUser(userName, userAvatar)
                                userRepository.createPartner(partnerName, partnerAvatar, pairingCode)
                            }
                            startMonitoringService()
                        },
                    )
                } else {
                    UnHookNavGraph(
                        userRepository = userRepository,
                        pointsRepository = pointsRepository,
                        blockedAppDao = blockedAppDao,
                        choreItemDao = choreItemDao,
                        wishItemDao = wishItemDao,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startMonitoringService()
    }

    private fun startMonitoringService() {
        val intent = Intent(this, MonitoringForegroundService::class.java)
        startForegroundService(intent)
    }
}
