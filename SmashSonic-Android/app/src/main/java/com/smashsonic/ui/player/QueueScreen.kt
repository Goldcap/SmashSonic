package com.smashsonic.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smashsonic.data.model.Song
import com.smashsonic.data.remote.SubsonicUrlBuilder
import com.smashsonic.ui.components.CoverArtImage
import com.smashsonic.ui.home.HomeScreenUrlHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onBack: () -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    urlBuilder: SubsonicUrlBuilder = hiltViewModel<HomeScreenUrlHelper>().urlBuilder,
) {
    val queue by playerViewModel.queue.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val currentIndex by playerViewModel.currentIndex.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val upcomingSongs by playerViewModel.upcomingSongs.collectAsState()
    val playedSongs by playerViewModel.playedSongs.collectAsState()
    val autoAdd by playerViewModel.autoAddRandomSongs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Play Queue") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Done") }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Play Random") },
                                onClick = { showMenu = false; playerViewModel.startRandomPlayback() },
                                leadingIcon = { Icon(Icons.Default.Shuffle, null) },
                            )
                            DropdownMenuItem(
                                text = { Text("Add 10 Random Songs") },
                                onClick = { showMenu = false; playerViewModel.addRandomSongsToQueue(10) },
                                leadingIcon = { Icon(Icons.Default.AddCircle, null) },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(if (autoAdd) "Disable Auto-Add" else "Enable Auto-Add Random") },
                                onClick = { showMenu = false; playerViewModel.setAutoAdd(!autoAdd) },
                                leadingIcon = { Icon(Icons.Default.AllInclusive, null) },
                            )
                            if (queue.isNotEmpty()) {
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Clear Queue", color = MaterialTheme.colorScheme.error) },
                                    onClick = { showMenu = false; playerViewModel.clearQueue() },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.95f)),
            )
        },
        containerColor = Color.Black.copy(alpha = 0.95f),
    ) { padding ->
        if (queue.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Default.QueueMusic, contentDescription = null, modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))
                Text("Queue is Empty", style = MaterialTheme.typography.headlineMedium)
                Text("Start playing music or add random songs", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(16.dp))
                Button(onClick = { playerViewModel.startRandomPlayback() }) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Play Random")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                // Now Playing
                currentSong?.let { song ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Now Playing", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            if (autoAdd) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AllInclusive, null, modifier = Modifier.size(14.dp), tint = Color.Green)
                                    Text("Auto-Add On", style = MaterialTheme.typography.labelSmall, color = Color.Green)
                                }
                            }
                        }
                    }
                    item {
                        QueueSongRow(
                            song = song,
                            isCurrentlyPlaying = true,
                            isPlaying = isPlaying,
                            coverArtUrl = song.coverArt?.let { urlBuilder.coverArtUrl(it, 100) },
                            onClick = {},
                        )
                    }
                }

                // Up Next
                if (upcomingSongs.isNotEmpty()) {
                    item {
                        Text(
                            "Up Next (${upcomingSongs.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    itemsIndexed(upcomingSongs, key = { idx, song -> "${song.id}_up_$idx" }) { offset, song ->
                        QueueSongRow(
                            song = song,
                            isCurrentlyPlaying = false,
                            isPlaying = false,
                            coverArtUrl = song.coverArt?.let { urlBuilder.coverArtUrl(it, 100) },
                            onClick = { playerViewModel.playFromQueue(currentIndex + 1 + offset) },
                        )
                    }
                }

                // Previously Played
                if (playedSongs.isNotEmpty()) {
                    item {
                        Text(
                            "Previously Played",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    itemsIndexed(playedSongs, key = { idx, song -> "${song.id}_prev_$idx" }) { offset, song ->
                        QueueSongRow(
                            song = song,
                            isCurrentlyPlaying = false,
                            isPlaying = false,
                            coverArtUrl = song.coverArt?.let { urlBuilder.coverArtUrl(it, 100) },
                            onClick = { playerViewModel.playFromQueue(offset) },
                            alpha = 0.6f,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueSongRow(
    song: Song,
    isCurrentlyPlaying: Boolean,
    isPlaying: Boolean,
    coverArtUrl: String?,
    onClick: () -> Unit,
    alpha: Float = 1f,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clickable(onClick = onClick)
            .background(
                if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.5f)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isCurrentlyPlaying) {
            Icon(
                if (isPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }

        CoverArtImage(
            url = coverArtUrl,
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(4.dp)),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            song.artist?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
            }
        }

        Text(song.formattedDuration, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
