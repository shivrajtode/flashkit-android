package com.flashkit.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flashkit.app.ui.screens.*

object Routes {
    const val HOME = "home"
    const val FLASH = "flash"
    const val SIDELOAD = "sideload"
    const val REBOOT = "reboot"
    const val COMMANDS = "commands"
    const val JOB_OUTPUT = "job_output"
    const val SETTINGS = "settings"
}

@Composable
fun FlashKitNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }
        composable(Routes.FLASH) {
            FlashScreen(navController = navController)
        }
        composable(Routes.SIDELOAD) {
            SideloadScreen(navController = navController)
        }
        composable(Routes.REBOOT) {
            RebootScreen(navController = navController)
        }
        composable(Routes.COMMANDS) {
            CommandsScreen(navController = navController)
        }
        composable(Routes.JOB_OUTPUT) {
            JobOutputScreen(navController = navController)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(navController = navController)
        }
    }
}
