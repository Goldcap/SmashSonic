package com.smashsonic.ui.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.smashsonic.data.remote.SubsonicUrlBuilder
import com.smashsonic.player.PlayMode
import com.smashsonic.ui.components.CoverArtImage
import com.smashsonic.ui.home.HomeScreenUrlHelper
import com.smashsonic.ui.likes.LikesViewModel
import com.smashsonic.ui.theme.Cyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBack: () -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    likesViewModel: LikesViewModel = hiltViewModel(),
    urlBuilder: SubsonicUrlBuilder = hiltViewModel<HomeScreenUrlHelper>().urlBuilder,
) {
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val isLoading by playerViewModel.isLoading.collectAsState()
    val progress by playerViewModel.progress.collectAsState()
    val currentTime by playerViewModel.currentTime.collectAsState()
    val duration by playerViewModel.duration.collectAsState()
    val currentTimeFormatted by playerViewModel.currentTimeFormatted.collectAsState()
    val durationFormatted by playerViewModel.durationFormatted.collectAsState()
    val playMode by playerViewModel.playMode.collectAsState()
    val isTrackRadioActive by playerViewModel.isTrackRadioActive.collectAsState()
    val trackRadioSeedSong by playerViewModel.trackRadioSeedSong.collectAsState()
    val upcomingSongs by playerViewModel.upcomingSongs.collectAsState()
    val likedSongIds by likesViewModel.likedSongIds.collectAsState()
    val isCurrentSongLiked = currentSong?.id?.let { it in likedSongIds } ?: false

    val coverArtUrl = remember(currentSong) {
        currentSong?.coverArt?.let { urlBuilder.coverArtUrl(it, 600) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Blurred background
        if (coverArtUrl != null) {
            AsyncImage(
                model = coverArtUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(50.dp),
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
        } else {
            Box(Modifier.fillMaxSize().background(Color.Black))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Back handle
            IconButton(onClick = onBack) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = Color.White)
            }

            Spacer(Modifier.height(16.dp))

            // Album Art
            CoverArtImage(
                url = coverArtUrl,
                modifier = Modifier.size(280.dp).clip(RoundedCornerShape(12.dp)),
            )

            Spacer(Modifier.height(24.dp))

            // Track Info
            Text(
                currentSong?.title ?: "Not Playing",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                currentSong?.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
            )
            currentSong?.album?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.5f), maxLines = 1)
            }

            if (isTrackRadioActive) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = Cyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Radio, contentDescription = null, modifier = Modifier.size(14.dp), tint = Cyan)
                        Text("Radio", style = MaterialTheme.typography.labelSmall, color = Cyan)
                        trackRadioSeedSong?.let {
                            Text("· ${it.title}", style = MaterialTheme.typography.labelSmall, color = Cyan, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // Like button
            Spacer(Modifier.height(16.dp))
            IconButton(onClick = { currentSong?.let { likesViewModel.toggleLike(it) } }) {
                Icon(
                    if (isCurrentSongLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isCurrentSongLiked) "Unlike" else "Like",
                    tint = if (isCurrentSongLiked) Color.Red else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.height(8.dp))

            // Progress Bar (seekable)
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                var isSeeking by remember { mutableStateOf(false) }
                var seekPosition by remember { mutableFloatStateOf(0f) }

                Slider(
                    value = if (isSeeking) seekPosition else progress,
                    onValueChange = {
                        isSeeking = true
                        seekPosition = it
                    },
                    onValueChangeFinished = {
                        playerViewModel.seekTo((seekPosition * duration).toLong())
                        isSeeking = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Cyan,
                        activeTrackColor = Cyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                    ),
                    modifier = Modifier.fillMaxWidth().height(24.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(currentTimeFormatted, style = MaterialTheme.typography.labelSmall, color = Color.White, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Text(durationFormatted, style = MaterialTheme.typography.labelSmall, color = Color.White, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Playback Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { playerViewModel.previous() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                IconButton(
                    onClick = { playerViewModel.togglePlayPause() },
                    modifier = Modifier.size(70.dp).clip(CircleShape).background(Color.White),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(30.dp), color = Color.Black, strokeWidth = 3.dp)
                    } else {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
                IconButton(onClick = { playerViewModel.next() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Secondary controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Play mode
                IconButton(onClick = { playerViewModel.cyclePlayMode() }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            when (playMode) {
                                PlayMode.PLAY_ONCE -> Icons.Default.ArrowForward
                                PlayMode.LOOP -> Icons.Default.Repeat
                                PlayMode.SHUFFLE -> Icons.Default.Shuffle
                            },
                            contentDescription = playMode.displayName,
                            tint = if (playMode == PlayMode.PLAY_ONCE) Color.White.copy(alpha = 0.7f) else Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(playMode.displayName, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.White.copy(alpha = 0.7f))
                    }
                }
                IconButton(onClick = { playerViewModel.skipBackward() }) {
                    Icon(Icons.Default.Replay10, contentDescription = "Skip back", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { playerViewModel.skipForward() }) {
                    Icon(Icons.Default.Forward10, contentDescription = "Skip forward", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
