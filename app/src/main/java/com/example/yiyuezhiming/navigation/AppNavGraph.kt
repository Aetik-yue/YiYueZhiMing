package com.example.yiyuezhiming.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.yiyuezhiming.ui.components.KawaiiBottomBar
import com.example.yiyuezhiming.ui.screens.addmemory.AddMemoryScreen
import com.example.yiyuezhiming.ui.screens.album.AlbumScreen
import com.example.yiyuezhiming.ui.screens.home.HomeScreen
import com.example.yiyuezhiming.ui.screens.music.MusicPlayerScreen
import com.example.yiyuezhiming.ui.screens.reminders.DateReminderScreen
import com.example.yiyuezhiming.ui.screens.settings.SettingsScreen
import com.example.yiyuezhiming.ui.screens.splash.SplashScreen
import com.example.yiyuezhiming.ui.screens.toolbox.ToolboxScreen

@Composable
fun AppNavGraph(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf(
        Route.Home.path,
        Route.Album.path,
        Route.Reminders.path,
        Route.Toolbox.path,
        Route.Music.path,
        Route.Settings.path
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                KawaiiBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { target ->
                        navController.navigate(target.path) {
                            popUpTo(Route.Toolbox.path) { saveState = true }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Splash.path,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn() + slideInVertically { it / 8 } },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() + slideOutVertically { it / 8 } }
        ) {
            composable(Route.Splash.path) {
                SplashScreen {
                    navController.navigate(Route.Toolbox.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                }
            }
            composable(Route.Toolbox.path) {
                ToolboxScreen(
                    onOpenMemories = { navController.navigate(Route.Home.path) },
                    onOpenReminders = { navController.navigate(Route.Reminders.path) }
                )
            }
            composable(Route.Home.path) {
                HomeScreen(onAddMemory = { navController.navigate(Route.AddMemory.path) })
            }
            composable(
                Route.AddMemory.path,
                enterTransition = { fadeIn() + slideInVertically { it } },
                exitTransition = { ExitTransition.None },
                popExitTransition = { fadeOut() + slideOutVertically { it } }
            ) {
                AddMemoryScreen(onBack = { navController.popBackStack() })
            }
            composable(Route.Album.path) {
                AlbumScreen()
            }
            composable(Route.Music.path) {
                MusicPlayerScreen()
            }
            composable(Route.Reminders.path) {
                DateReminderScreen()
            }
            composable(Route.Settings.path) {
                SettingsScreen(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange
                )
            }
        }
    }
}
