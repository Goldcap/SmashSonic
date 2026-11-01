package com.smashsonic.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smashsonic.ui.downloads.DownloadsScreen
import com.smashsonic.ui.home.MainScreen
import com.smashsonic.ui.settings.AppearanceSettingsScreen
import com.smashsonic.ui.settings.ServerSetupScreen
import com.smashsonic.ui.settings.ServerSetupViewModel

@Composable
fun SmashSonicNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val serverSetupViewModel: ServerSetupViewModel = hiltViewModel()
    val isConfigured by serverSetupViewModel.isConfigured.collectAsState()

    val startDestination = if (isConfigured) Route.Main.route else Route.ServerSetup.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Route.ServerSetup.route) {
            ServerSetupScreen(
                isInitialSetup = true,
                onConfigured = {
                    navController.navigate(Route.Main.route) {
                        popUpTo(Route.ServerSetup.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.Main.route) {
            MainScreen(navController = navController)
        }

        composable(Route.Settings.route) {
            ServerSetupScreen(
                isInitialSetup = false,
                onConfigured = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Route.ServerSetup.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAppearance = {
                    navController.navigate(Route.AppearanceSettings.route)
                },
            )
        }

        composable(Route.AppearanceSettings.route) {
            AppearanceSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Downloads.route) {
            DownloadsScreen(onBack = { navController.popBackStack() })
        }

        // NOTE: Album/Artist/Playlist detail, Now Playing and Queue are NOT top-level
        // destinations. They live in an inner NavHost inside MainScreen so that opening
        // them overlays the tab content instead of disposing it — see MainScreen.
    }
}
