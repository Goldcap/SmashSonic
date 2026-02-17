package com.smashsonic.ui.components

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
import com.smashsonic.data.model.Song

@Composable
fun SongRow(
    song: Song,
    songs: List<Song>,
    showTrackNumber: Boolean = false,
    isCurrentSong: Boolean = false,
    coverArtUrl: String? = null,
    onPlay: (Song, List<Song>) -> Unit,
    onPlayNow: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onPlayLast: (Song) -> Unit,
    onStartRadio: (Song) -> Unit,
    onDownload: ((Song) -> Unit)? = null,
    onToggleLike: ((Song) -> Unit)? = null,
    isLiked: Boolean = false,
    isDownloaded: Boolean = false,
    isDownloading: Boolean = false,
    downloadProgress: Float = 0f,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPlay(song, songs) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showTrackNumber) {
                song.track?.let { track ->
                    Text(
                        "$track",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.width(24.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                song.artist?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            if (isDownloading) {
                CircularProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else if (isDownloaded) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Downloaded",
                    tint = Color.Green,
                    modifier = Modifier.size(20.dp),
                )
            }

            Text(
                song.formattedDuration,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Play") }, onClick = { showMenu = false; onPlay(song, songs) }, leadingIcon = { Icon(Icons.Default.PlayArrow, null) })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Play Now") }, onClick = { showMenu = false; onPlayNow(song) }, leadingIcon = { Icon(Icons.Default.PlayCircle, null) })
                    DropdownMenuItem(text = { Text("Play Next") }, onClick = { showMenu = false; onPlayNext(song) }, leadingIcon = { Icon(Icons.Default.QueuePlayNext, null) })
                    DropdownMenuItem(text = { Text("Play Last") }, onClick = { showMenu = false; onPlayLast(song) }, leadingIcon = { Icon(Icons.Default.AddToQueue, null) })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Start Radio") }, onClick = { showMenu = false; onStartRadio(song) }, leadingIcon = { Icon(Icons.Default.Radio, null) })
                    HorizontalDivider()
                    onToggleLike?.let {
                        DropdownMenuItem(
                            text = { Text(if (isLiked) "Unlike" else "Like") },
                            onClick = { showMenu = false; it(song) },
                            leadingIcon = { Icon(if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null) },
                        )
                    }
                    onDownload?.let {
                        if (!isDownloaded && !isDownloading) {
                            DropdownMenuItem(
                                text = { Text("Download") },
                                onClick = { showMenu = false; it(song) },
                                leadingIcon = { Icon(Icons.Default.Download, null) },
                            )
                        }
                    }
                }
            }
        }
    }
}
