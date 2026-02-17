package com.smashsonic.ui.likes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smashsonic.R
import com.smashsonic.data.local.LikedSongEntity
import com.smashsonic.data.remote.SubsonicUrlBuilder
import com.smashsonic.ui.components.CoverArtImage
import com.smashsonic.ui.components.SmashSonicBackground
import com.smashsonic.ui.home.HomeScreenUrlHelper
import com.smashsonic.ui.player.PlayerViewModel
import com.smashsonic.util.formattedDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedSongsScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    viewModel: LikesViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    urlBuilder: SubsonicUrlBuilder = hiltViewModel<HomeScreenUrlHelper>().urlBuilder,
) {
    val likedSongs by viewModel.likedSongs.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    SmashSonicBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Liked Songs") },
                    actions = {
                        if (likedSongs.isNotEmpty() || isSyncing) {
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Sync from Server") },
                                        onClick = {
                                            showMenu = false
                                            viewModel.syncFromServer()
                                        },
                                        leadingIcon = { Icon(Icons.Default.Sync, null) },
                                    )
                                    if (likedSongs.isNotEmpty()) {
                                        HorizontalDivider()
                                        DropdownMenuItem(
                                            text = { Text("Play All") },
                                            onClick = {
                                                showMenu = false
                                                val songs = likedSongs.map { it.toSong() }
                                                songs.firstOrNull()?.let { playerViewModel.play(it, songs) }
                                            },
                                            leadingIcon = { Icon(Icons.Default.PlayArrow, null) },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Shuffle All") },
                                            onClick = {
                                                showMenu = false
                                                val songs = likedSongs.map { it.toSong() }.shuffled()
                                                songs.firstOrNull()?.let { playerViewModel.play(it, songs) }
                                            },
                                            leadingIcon = { Icon(Icons.Default.Shuffle, null) },
                                        )
                                    }
                                }
                            }
                        }
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            if (likedSongs.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.pixel_heart),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No Liked Songs", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Songs you like will appear here.\nUse the menu to sync from server.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("${likedSongs.size} songs", style = MaterialTheme.typography.labelLarge)
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                    items(likedSongs, key = { it.id }) { likedSong ->
                        LikedSongRow(
                            likedSong = likedSong,
                            isPlaying = currentSong?.id == likedSong.id,
                            coverArtUrl = likedSong.coverArt?.let { urlBuilder.coverArtUrl(it, 100) },
                            onClick = {
                                val songs = likedSongs.map { it.toSong() }
                                playerViewModel.play(likedSong.toSong(), songs)
                            },
                            onUnlike = { viewModel.unlike(likedSong.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LikedSongRow(
    likedSong: LikedSongEntity,
    isPlaying: Boolean,
    coverArtUrl: String?,
    onClick: () -> Unit,
    onUnlike: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CoverArtImage(url = coverArtUrl, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(4.dp)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    likedSong.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    likedSong.artist?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1) }
                    if (likedSong.artist != null && likedSong.album != null) Text("·", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    likedSong.album?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1) }
                }
            }
            likedSong.duration?.let {
                Text(
                    it.formattedDuration(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Image(
                painter = painterResource(R.drawable.pixel_heart),
                contentDescription = "Liked",
                modifier = Modifier.size(24.dp).clickable(onClick = onUnlike),
            )
        }
    }
}
