package com.smashsonic.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smashsonic.data.model.Album
import com.smashsonic.data.model.Artist
import com.smashsonic.data.remote.SubsonicUrlBuilder
import com.smashsonic.ui.components.CoverArtImage
import com.smashsonic.ui.components.SmashSonicBackground
import com.smashsonic.ui.components.SongRow
import com.smashsonic.ui.downloads.DownloadsViewModel
import com.smashsonic.ui.home.HomeScreenUrlHelper
import com.smashsonic.ui.likes.LikesViewModel
import com.smashsonic.ui.navigation.Route
import com.smashsonic.ui.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    likesViewModel: LikesViewModel = hiltViewModel(),
    downloadsViewModel: DownloadsViewModel = hiltViewModel(),
    urlBuilder: SubsonicUrlBuilder = hiltViewModel<HomeScreenUrlHelper>().urlBuilder,
) {
    val query by viewModel.query.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val hasSearched by viewModel.hasSearched.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val likedSongIds by likesViewModel.likedSongIds.collectAsState()

    SmashSonicBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Search") },
                    actions = {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Search bar
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::updateQuery,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("Artists, albums, or songs") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(Modifier.height(8.dp))

                when {
                    isSearching -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    hasSearched && !viewModel.hasResults -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No results for \"$query\"", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    viewModel.hasResults -> {
                        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                            if (artists.isNotEmpty()) {
                                item {
                                    Text("Artists", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                }
                                items(artists) { artist ->
                                    SearchArtistRow(
                                        artist = artist,
                                        coverArtUrl = artist.coverArt?.let { urlBuilder.coverArtUrl(it, 100) },
                                        onClick = { navController.navigate(Route.ArtistDetail.create(artist.id)) },
                                    )
                                }
                            }
                            if (albums.isNotEmpty()) {
                                item {
                                    Text("Albums", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                }
                                item {
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        items(albums) { album ->
                                            SearchAlbumCard(
                                                album = album,
                                                coverArtUrl = album.coverArt?.let { urlBuilder.coverArtUrl(it, 200) },
                                                onClick = { navController.navigate(Route.AlbumDetail.create(album.id)) },
                                            )
                                        }
                                    }
                                }
                            }
                            if (songs.isNotEmpty()) {
                                item {
                                    Text("Songs", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                }
                                items(songs, key = { it.id }) { song ->
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
                    else -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text("Search your library", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchArtistRow(artist: Artist, coverArtUrl: String?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CoverArtImage(url = coverArtUrl, modifier = Modifier.size(50.dp).clip(CircleShape))
            Column(modifier = Modifier.weight(1f)) {
                Text(artist.name, style = MaterialTheme.typography.bodyLarge)
                artist.albumCount?.let {
                    Text("$it album${if (it == 1) "" else "s"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun SearchAlbumCard(album: Album, coverArtUrl: String?, onClick: () -> Unit) {
    Column(modifier = Modifier.width(140.dp).clickable(onClick = onClick)) {
        CoverArtImage(url = coverArtUrl, modifier = Modifier.size(140.dp).clip(RoundedCornerShape(8.dp)))
        Spacer(Modifier.height(8.dp))
        Text(album.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        album.artist?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
        }
    }
}
