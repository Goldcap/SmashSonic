package com.smashsonic.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smashsonic.R
import com.smashsonic.ui.browse.AlbumDetailScreen
import com.smashsonic.ui.browse.ArtistDetailScreen
import com.smashsonic.ui.browse.BrowseScreen
import com.smashsonic.ui.browse.PlaylistDetailScreen
import com.smashsonic.ui.components.SmashSonicBackground
import com.smashsonic.ui.likes.FavoritesScreen
import com.smashsonic.ui.likes.LikesViewModel
import com.smashsonic.ui.navigation.Route
import com.smashsonic.ui.player.MiniPlayer
import com.smashsonic.ui.player.NowPlayingScreen
import com.smashsonic.ui.player.PlayerViewModel
import com.smashsonic.ui.player.QueueScreen
import com.smashsonic.ui.search.SearchScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    likesViewModel: LikesViewModel = hiltViewModel(),
) {
    // Inner NavHost for detail/full-screen content (album, artist, playlist, now playing,
    // queue). Routing these through a SEPARATE controller means opening them overlays the
    // tab content instead of replacing it, so the tab screens (Home etc.) are never
    // disposed and returning is instant rather than a multi-second rebuild.
    // Settings/Downloads still use the OUTER navController. See [[android-back-nav-stall]].
    val contentNav = rememberNavController()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    val currentSong by playerViewModel.currentSong.collectAsState()

    // Load starred albums & artists once on entry so favorite state is correct
    // on detail screens before the user opens the Favorites tab (mirrors iOS).
    LaunchedEffect(Unit) { likesViewModel.loadStarred() }

    Box(modifier = Modifier.fillMaxSize()) {
        // ---- Persistent tab layer (never disposed by detail navigation) ----
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> HomeScreen(navController = contentNav, onMenuClick = { showMenu = true })
                1 -> BrowseScreen(navController = contentNav, onMenuClick = { showMenu = true })
                2 -> FavoritesScreen(navController = contentNav, onMenuClick = { showMenu = true })
                3 -> SearchScreen(navController = contentNav, onMenuClick = { showMenu = true })
            }

            // Mini player + Tab bar at bottom
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                if (currentSong != null) {
                    MiniPlayer(
                        onTap = { contentNav.navigate(Route.NowPlaying.route) },
                    )
                }
                SmashSonicTabBar(
                    selectedTab = selectedTab,
                    onTabSelect = { selectedTab = it },
                    onRandomTap = { playerViewModel.startRandomPlayback() },
                    onQueueTap = { contentNav.navigate(Route.Queue.route) },
                )
            }
        }

        // ---- Detail overlay layer: pushed on top of the tab layer ----
        NavHost(
            navController = contentNav,
            startDestination = Route.ContentRoot.route,
        ) {
            // Transparent root: the tab layer behind shows through and stays interactive.
            composable(Route.ContentRoot.route) {}

            composable(
                route = Route.AlbumDetail.route,
                arguments = listOf(navArgument("albumId") { type = NavType.StringType }),
            ) { entry ->
                val albumId = entry.arguments?.getString("albumId") ?: return@composable
                AlbumDetailScreen(
                    albumId = albumId,
                    navController = contentNav,
                    onBack = { contentNav.popBackStack() },
                )
            }

            composable(
                route = Route.ArtistDetail.route,
                arguments = listOf(navArgument("artistId") { type = NavType.StringType }),
            ) { entry ->
                val artistId = entry.arguments?.getString("artistId") ?: return@composable
                ArtistDetailScreen(
                    artistId = artistId,
                    navController = contentNav,
                    onBack = { contentNav.popBackStack() },
                )
            }

            composable(
                route = Route.PlaylistDetail.route,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
            ) { entry ->
                val playlistId = entry.arguments?.getString("playlistId") ?: return@composable
                PlaylistDetailScreen(
                    playlistId = playlistId,
                    navController = contentNav,
                    onBack = { contentNav.popBackStack() },
                )
            }

            composable(Route.NowPlaying.route) {
                NowPlayingScreen(onBack = { contentNav.popBackStack() })
            }

            composable(Route.Queue.route) {
                QueueScreen(onBack = { contentNav.popBackStack() })
            }
        }
    }

    // Menu bottom sheet
    if (showMenu) {
        ModalBottomSheet(
            onDismissRequest = { showMenu = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            MenuContent(
                onNavigate = { tab ->
                    selectedTab = tab
                    showMenu = false
                },
                onRandomPlay = {
                    playerViewModel.startRandomPlayback()
                    showMenu = false
                },
                onQueueTap = {
                    showMenu = false
                    contentNav.navigate(Route.Queue.route)
                },
                onDownloads = {
                    showMenu = false
                    navController.navigate(Route.Downloads.route)
                },
                onSettings = {
                    showMenu = false
                    navController.navigate(Route.Settings.route)
                },
            )
        }
    }
}

@Composable
fun SmashSonicTabBar(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    onRandomTap: () -> Unit,
    onQueueTap: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TabIcon(R.drawable.pixel_home, "Home", selectedTab == 0) { onTabSelect(0) }
            TabIcon(R.drawable.pixel_browse, "Browse", selectedTab == 1) { onTabSelect(1) }
            TabIcon(R.drawable.pixel_heart, "Favorites", selectedTab == 2) { onTabSelect(2) }
            TabIcon(R.drawable.pixel_search, "Search", selectedTab == 3) { onTabSelect(3) }
            TabIcon(R.drawable.pixel_queue, "Queue", false, isAction = true) { onQueueTap() }
            TabIcon(R.drawable.pixel_random, "Random", false, isAction = true) { onRandomTap() }
        }
    }
}

@Composable
fun TabIcon(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    isAction: Boolean = false,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(36.dp)
                    .then(
                        if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(4.dp))
                        else Modifier
                    ),
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isAction) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun MenuContent(
    onNavigate: (Int) -> Unit,
    onRandomPlay: () -> Unit,
    onQueueTap: () -> Unit,
    onDownloads: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text("Menu", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        HorizontalDivider()

        Text("Navigation", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        MenuRow(R.drawable.pixel_home, "Home") { onNavigate(0) }
        MenuRow(R.drawable.pixel_browse, "Browse") { onNavigate(1) }
        MenuRow(R.drawable.pixel_heart, "Favorites") { onNavigate(2) }
        MenuRow(R.drawable.pixel_search, "Search") { onNavigate(3) }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Playback", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        MenuRow(R.drawable.pixel_random, "Play Random") { onRandomPlay() }
        MenuRow(R.drawable.pixel_queue, "Play Queue") { onQueueTap() }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Library", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        MenuRow(R.drawable.pixel_downloads, "Downloads") { onDownloads() }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Settings", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        MenuRow(R.drawable.pixel_settings, "Server Settings") { onSettings() }
    }
}

@Composable
fun MenuRow(iconRes: Int, label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
