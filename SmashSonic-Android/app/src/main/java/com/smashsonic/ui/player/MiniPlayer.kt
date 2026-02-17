package com.smashsonic.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smashsonic.data.remote.SubsonicUrlBuilder
import com.smashsonic.player.PlayMode
import com.smashsonic.ui.components.CoverArtImage
import com.smashsonic.ui.home.HomeScreenUrlHelper
import com.smashsonic.ui.theme.Cyan

@Composable
fun MiniPlayer(
    onTap: () -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    urlBuilder: SubsonicUrlBuilder = hiltViewModel<HomeScreenUrlHelper>().urlBuilder,
) {
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val isLoading by playerViewModel.isLoading.collectAsState()
    val progress by playerViewModel.progress.collectAsState()
    val currentTimeFormatted by playerViewModel.currentTimeFormatted.collectAsState()
    val durationFormatted by playerViewModel.durationFormatted.collectAsState()
    val playMode by playerViewModel.playMode.collectAsState()
    val isTrackRadioActive by playerViewModel.isTrackRadioActive.collectAsState()

    val coverArtUrl = remember(currentSong) {
        currentSong?.coverArt?.let { urlBuilder.coverArtUrl(it, 100) }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onTap),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
    ) {
        Column {
            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(currentTimeFormatted, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(4.dp),
                    color = Cyan,
                    trackColor = Cyan.copy(alpha = 0.2f),
                )
                Text(durationFormatted, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            // Player content
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CoverArtImage(
                    url = coverArtUrl,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        currentSong?.title ?: "Not Playing",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        currentSong?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                    )
                    if (isTrackRadioActive) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Radio, contentDescription = null, modifier = Modifier.size(10.dp), tint = Cyan)
                            Text("Radio", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Cyan)
                        }
                    }
                }

                // Controls
                IconButton(onClick = { playerViewModel.previous() }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                }
                IconButton(onClick = { playerViewModel.togglePlayPause() }, modifier = Modifier.size(36.dp)) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                    }
                }
                IconButton(onClick = { playerViewModel.next() }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next")
                }
                IconButton(onClick = { playerViewModel.cyclePlayMode() }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        when (playMode) {
                            PlayMode.PLAY_ONCE -> Icons.Default.ArrowForward
                            PlayMode.LOOP -> Icons.Default.Repeat
                            PlayMode.SHUFFLE -> Icons.Default.Shuffle
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (playMode == PlayMode.PLAY_ONCE) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Bottom cyan border
            Box(
                modifier = Modifier.fillMaxWidth().height(2.dp).background(Cyan),
            )
        }
    }
}
