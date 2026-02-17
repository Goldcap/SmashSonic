package com.smashsonic.ui.browse

import androidx.compose.foundation.Image
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

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smashsonic.R
import com.smashsonic.data.model.Album
import com.smashsonic.data.model.Song
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
fun AlbumDetailScreen(
    albumId: String,
    navController: NavController,
    onBack: () -> Unit,
    viewModel: BrowseViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    likesViewModel: LikesViewModel = hiltViewModel(),
    downloadsViewModel: DownloadsViewModel = hiltViewModel(),
    urlBuilder: SubsonicUrlBuilder = hiltViewModel<HomeScreenUrlHelper>().urlBuilder,
) {
    var album by remember { mutableStateOf<Album?>(null) }
    val currentSong by playerViewModel.currentSong.collectAsState()
    val likedSongIds by likesViewModel.likedSongIds.collectAsState()

    LaunchedEffect(albumId) {
        album = viewModel.loadAlbum(albumId)
    }

    val sortedSongs = remember(album) {
        album?.songs?.sortedWith(compareBy<Song> { it.track ?: Int.MAX_VALUE }.thenBy { it.title }) ?: emptyList()
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
            if (album == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val displayAlbum = album!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    // Header
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CoverArtImage(
                                url = displayAlbum.coverArt?.let { urlBuilder.coverArtUrl(it, 600) },
                                modifier = Modifier.size(250.dp).clip(RoundedCornerShape(8.dp)),
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(displayAlbum.name, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                            displayAlbum.artist?.let {
                                Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                displayAlbum.year?.let { Text("$it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
                                displayAlbum.genre?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
                                displayAlbum.songCount?.let { Text("$it tracks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
                            }

                            // Play/Shuffle/Download buttons
                            if (sortedSongs.isNotEmpty()) {
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.height(44.dp),
                                ) {
                                    IconButton(onClick = { playerViewModel.play(sortedSongs.first(), sortedSongs) }) {
                                        Image(painterResource(R.drawable.pixel_play_button), "Play", modifier = Modifier.fillMaxSize())
                                    }
                                    IconButton(onClick = {
                                        val shuffled = sortedSongs.shuffled()
                                        playerViewModel.play(shuffled.first(), shuffled)
                                    }) {
                                        Image(painterResource(R.drawable.pixel_shuffle_button), "Shuffle", modifier = Modifier.fillMaxSize())
                                    }
                                    IconButton(onClick = { /* TODO: download all */ }) {
                                        Image(painterResource(R.drawable.pixel_download_button), "Download", modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                    }

                    // Track list
                    if (sortedSongs.isNotEmpty()) {
                        item { HorizontalDivider() }
                        itemsIndexed(sortedSongs, key = { _, song -> song.id }) { _, song ->
                            SongRow(
                                song = song,
                                songs = sortedSongs,
                                showTrackNumber = true,
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
