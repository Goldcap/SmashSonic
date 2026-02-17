package com.smashsonic.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smashsonic.R
import com.smashsonic.ui.browse.BrowseScreen
import com.smashsonic.ui.components.SmashSonicBackground
import com.smashsonic.ui.likes.LikedSongsScreen
import com.smashsonic.ui.navigation.Route
import com.smashsonic.ui.player.MiniPlayer
import com.smashsonic.ui.player.PlayerViewModel
import com.smashsonic.ui.search.SearchScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    val currentSong by playerViewModel.currentSong.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Content
        when (selectedTab) {
            0 -> HomeScreen(navController = navController, onMenuClick = { showMenu = true })
            1 -> BrowseScreen(navController = navController, onMenuClick = { showMenu = true })
            2 -> LikedSongsScreen(navController = navController, onMenuClick = { showMenu = true })
            3 -> SearchScreen(navController = navController, onMenuClick = { showMenu = true })
        }

        // Mini player + Tab bar at bottom
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            if (currentSong != null) {
                MiniPlayer(
                    onTap = { navController.navigate(Route.NowPlaying.route) },
                )
            }
            SmashSonicTabBar(
                selectedTab = selectedTab,
                onTabSelect = { selectedTab = it },
                onRandomTap = { playerViewModel.startRandomPlayback() },
                onQueueTap = { navController.navigate(Route.Queue.route) },
            )
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
                    navController.navigate(Route.Queue.route)
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
                .padding(horizontal = 8.dp)
                .padding(bottom = 20.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TabIcon(R.drawable.pixel_home, "Home", selectedTab == 0) { onTabSelect(0) }
            TabIcon(R.drawable.pixel_browse, "Browse", selectedTab == 1) { onTabSelect(1) }
            TabIcon(R.drawable.pixel_heart, "Liked", selectedTab == 2) { onTabSelect(2) }
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
        MenuRow(R.drawable.pixel_heart, "Liked Songs") { onNavigate(2) }
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
