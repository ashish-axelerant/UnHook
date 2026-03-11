// UnHook — Navigation graph with bottom navigation bar
package com.unhook.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unhook.app.R
import com.unhook.app.data.db.BlockedAppDao
import com.unhook.app.data.db.ChoreItemDao
import com.unhook.app.data.db.WishItemDao
import com.unhook.app.data.repository.PointsRepository
import com.unhook.app.data.repository.UserRepository
import com.unhook.app.ui.screens.BlockedAppsScreen
import com.unhook.app.ui.screens.ChoreWishScreen
import com.unhook.app.ui.screens.DashboardScreen
import com.unhook.app.ui.screens.DuelScreen
import com.unhook.app.ui.screens.ReportScreen
import com.unhook.app.ui.screens.SettingsScreen

sealed class Screen(val route: String, val labelRes: Int, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", R.string.nav_dashboard, Icons.Filled.Home)
    data object Duel : Screen("duel", R.string.nav_duel, Icons.Filled.Shield)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings)
}

private val bottomNavItems = listOf(Screen.Dashboard, Screen.Duel, Screen.Settings)

@Composable
fun UnHookNavGraph(
    userRepository: UserRepository,
    pointsRepository: PointsRepository,
    blockedAppDao: BlockedAppDao,
    choreItemDao: ChoreItemDao,
    wishItemDao: WishItemDao,
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Hide bottom bar on sub-screens
            if (currentRoute in bottomNavItems.map { it.route }) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(screen.labelRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    userRepository = userRepository,
                    pointsRepository = pointsRepository,
                    onNavigateToReport = { navController.navigate("report") },
                )
            }
            composable(Screen.Duel.route) {
                DuelScreen(
                    userRepository = userRepository,
                    onNavigateToChoreWish = { navController.navigate("chore_wish") },
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToBlockedApps = { navController.navigate("blocked_apps") },
                )
            }
            composable("blocked_apps") {
                BlockedAppsScreen(
                    blockedAppDao = blockedAppDao,
                    onBack = { navController.popBackStack() },
                )
            }
            composable("chore_wish") {
                ChoreWishScreen(
                    choreItemDao = choreItemDao,
                    wishItemDao = wishItemDao,
                    onBack = { navController.popBackStack() },
                )
            }
            composable("report") {
                ReportScreen(
                    userRepository = userRepository,
                    pointsRepository = pointsRepository,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
