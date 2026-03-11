// UnHook — Single activity entry point, hosts Compose UI
package com.unhook.app

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
                        },
                    )
                } else {
                    UnHookNavGraph(
                        userRepository = userRepository,
                        pointsRepository = pointsRepository,
                    )
                }
            }
        }
    }
}
