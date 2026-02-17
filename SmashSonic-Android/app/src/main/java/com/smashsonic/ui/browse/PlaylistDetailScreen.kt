package com.smashsonic.ui.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smashsonic.data.model.Playlist
import com.smashsonic.data.remote.SubsonicUrlBuilder
import com.smashsonic.ui.components.CoverArtImage
import com.smashsonic.ui.components.SmashSonicBackground
import com.smashsonic.ui.components.SongRow
import com.smashsonic.ui.downloads.DownloadsViewModel
import com.smashsonic.ui.home.HomeScreenUrlHelper
import com.smashsonic.ui.likes.LikesViewModel
import com.smashsonic.ui.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    navController: NavController,
    onBack: () -> Unit,
    viewModel: BrowseViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    likesViewModel: LikesViewModel = hiltViewModel(),
    downloadsViewModel: DownloadsViewModel = hiltViewModel(),
    urlBuilder: SubsonicUrlBuilder = hiltViewModel<HomeScreenUrlHelper>().urlBuilder,
) {
    var playlist by remember { mutableStateOf<Playlist?>(null) }
    val currentSong by playerViewModel.currentSong.collectAsState()
    val likedSongIds by likesViewModel.likedSongIds.collectAsState()

    LaunchedEffect(playlistId) {
        playlist = viewModel.loadPlaylist(playlistId)
    }

    SmashSonicBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            if (playlist == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val displayPlaylist = playlist!!
                val songs = displayPlaylist.songs ?: emptyList()

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CoverArtImage(
                                url = displayPlaylist.coverArt?.let { urlBuilder.coverArtUrl(it, 600) },
                                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp)),
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(displayPlaylist.name, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                            displayPlaylist.songCount?.let {
                                Text("$it tracks", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }

                            if (songs.isNotEmpty()) {
                                Spacer(Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(onClick = { playerViewModel.play(songs.first(), songs) }) {
                                        Text("Play")
                                    }
                                    OutlinedButton(onClick = {
                                        val shuffled = songs.shuffled()
                                        playerViewModel.play(shuffled.first(), shuffled)
                                    }) {
                                        Text("Shuffle")
                                    }
                                }
                            }
                        }
                    }

                    if (songs.isNotEmpty()) {
                        item { HorizontalDivider() }
                        itemsIndexed(songs, key = { _, song -> song.id }) { _, song ->
                            SongRow(
                                song = song,
                                songs = songs,
                                isCurrentSong = currentSong?.id == song.id,
                                onPlay = { s, q -> playerViewModel.play(s, q) },
                                onPlayNow = { playerViewModel.playNow(it) },
                                onPlayNext = { playerViewModel.playNext(it) },
                                onPlayLast = { playerViewModel.playLast(it) },
                                onStartRadio = { playerViewModel.startTrackRadio(it) },
                                onToggleLike = { likesViewModel.toggleLike(it) },
                                isLiked = song.id in likedSongIds,
                                onDownload = { downloadsViewModel.download(it) },
                            )
                        }
                    }

                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }
}
