package com.smashsonic.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smashsonic.ui.browse.AlbumDetailScreen
import com.smashsonic.ui.browse.ArtistDetailScreen
import com.smashsonic.ui.browse.PlaylistDetailScreen
import com.smashsonic.ui.downloads.DownloadsScreen
import com.smashsonic.ui.likes.LikedSongsScreen
import com.smashsonic.ui.home.MainScreen
import com.smashsonic.ui.player.NowPlayingScreen
import com.smashsonic.ui.player.QueueScreen
import com.smashsonic.ui.search.SearchScreen
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

        composable(
            route = Route.AlbumDetail.route,
            arguments = listOf(navArgument("albumId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
            AlbumDetailScreen(
                albumId = albumId,
                navController = navController,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Route.ArtistDetail.route,
            arguments = listOf(navArgument("artistId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId") ?: return@composable
            ArtistDetailScreen(
                artistId = artistId,
                navController = navController,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Route.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
            PlaylistDetailScreen(
                playlistId = playlistId,
                navController = navController,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Route.NowPlaying.route) {
            NowPlayingScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Queue.route) {
            QueueScreen(onBack = { navController.popBackStack() })
        }
    }
}
