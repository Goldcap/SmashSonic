package com.smashsonic.ui.downloads

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smashsonic.data.local.DownloadedSongEntity
import com.smashsonic.data.remote.SubsonicUrlBuilder
import com.smashsonic.ui.components.CoverArtImage
import com.smashsonic.ui.components.SmashSonicBackground
import com.smashsonic.ui.home.HomeScreenUrlHelper
import com.smashsonic.ui.player.PlayerViewModel
import com.smashsonic.util.formattedDuration
import com.smashsonic.util.formattedFileSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    urlBuilder: SubsonicUrlBuilder = hiltViewModel<HomeScreenUrlHelper>().urlBuilder,
) {
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()

    SmashSonicBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Downloads") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (downloadedSongs.isNotEmpty()) {
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Play All") },
                                        onClick = {
                                            showMenu = false
                                            val songs = downloadedSongs.map { it.toSong() }
                                            songs.firstOrNull()?.let { playerViewModel.play(it, songs) }
                                        },
                                        leadingIcon = { Icon(Icons.Default.PlayArrow, null) },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Shuffle All") },
                                        onClick = {
                                            showMenu = false
                                            val songs = downloadedSongs.map { it.toSong() }.shuffled()
                                            songs.firstOrNull()?.let { playerViewModel.play(it, songs) }
                                        },
                                        leadingIcon = { Icon(Icons.Default.Shuffle, null) },
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            if (downloadedSongs.isEmpty() && activeDownloads.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text("No Downloads", style = MaterialTheme.typography.headlineMedium)
                        Text("Downloaded songs will appear here", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    // Active downloads
                    if (activeDownloads.isNotEmpty()) {
                        item {
                            Text("Downloading", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                    }

                    // Downloaded songs
                    if (downloadedSongs.isNotEmpty()) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Downloaded", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    downloadedSongs.mapNotNull { it.fileSize }.sum().formattedFileSize(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                )
                            }
                        }
                        items(downloadedSongs, key = { it.id }) { downloaded ->
                            DownloadedSongRow(
                                downloaded = downloaded,
                                isPlaying = currentSong?.id == downloaded.id,
                                coverArtUrl = downloaded.coverArt?.let { urlBuilder.coverArtUrl(it, 100) },
                                onClick = {
                                    val songs = downloadedSongs.map { it.toSong() }
                                    playerViewModel.play(downloaded.toSong(), songs)
                                },
                                onDelete = { viewModel.deleteDownload(downloaded.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadedSongRow(
    downloaded: DownloadedSongEntity,
    isPlaying: Boolean,
    coverArtUrl: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
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
                    downloaded.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    downloaded.artist?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1) }
                    if (downloaded.artist != null && downloaded.album != null) Text("·", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    downloaded.album?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1) }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                downloaded.duration?.let { Text(it.formattedDuration(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
                downloaded.fileSize?.let { Text(it.formattedFileSize(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
            }
        }
    }
}
