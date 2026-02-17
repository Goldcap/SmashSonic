package com.smashsonic.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smashsonic.data.model.Album
import com.smashsonic.data.model.Playlist
import com.smashsonic.ui.components.CoverArtImage
import com.smashsonic.ui.components.SmashSonicBackground
import com.smashsonic.ui.navigation.Route
import com.smashsonic.data.remote.SubsonicUrlBuilder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    urlBuilder: SubsonicUrlBuilder = hiltViewModel<HomeScreenUrlHelper>().urlBuilder,
) {
    val randomAlbums by viewModel.randomAlbums.collectAsState()
    val recentAlbums by viewModel.recentAlbums.collectAsState()
    val starredAlbums by viewModel.starredAlbums.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    SmashSonicBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Home") },
                    actions = {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            when {
                isLoading && randomAlbums.isEmpty() -> {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null && randomAlbums.isEmpty() -> {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(error ?: "", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadHomeData() }) { Text("Retry") }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        if (randomAlbums.isNotEmpty()) {
                            item {
                                AlbumSection(
                                    title = "Random",
                                    albums = randomAlbums,
                                    showRefresh = true,
                                    onRefresh = { viewModel.refreshRandom() },
                                    onAlbumClick = { navController.navigate(Route.AlbumDetail.create(it.id)) },
                                    urlBuilder = urlBuilder,
                                )
                            }
                        }

                        if (recentAlbums.isNotEmpty()) {
                            item {
                                AlbumSection(
                                    title = "Recently Added",
                                    albums = recentAlbums,
                                    onAlbumClick = { navController.navigate(Route.AlbumDetail.create(it.id)) },
                                    urlBuilder = urlBuilder,
                                )
                            }
                        }

                        if (starredAlbums.isNotEmpty()) {
                            item {
                                AlbumSection(
                                    title = "Starred",
                                    albums = starredAlbums,
                                    onAlbumClick = { navController.navigate(Route.AlbumDetail.create(it.id)) },
                                    urlBuilder = urlBuilder,
                                )
                            }
                        }

                        if (playlists.isNotEmpty()) {
                            item {
                                PlaylistSection(
                                    title = "Playlists",
                                    playlists = playlists,
                                    onPlaylistClick = { navController.navigate(Route.PlaylistDetail.create(it.id)) },
                                    urlBuilder = urlBuilder,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumSection(
    title: String,
    albums: List<Album>,
    showRefresh: Boolean = false,
    onRefresh: () -> Unit = {},
    onAlbumClick: (Album) -> Unit,
    urlBuilder: SubsonicUrlBuilder,
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            if (showRefresh) {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(albums) { album ->
                AlbumCard(
                    album = album,
                    coverArtUrl = album.coverArt?.let { urlBuilder.coverArtUrl(it) },
                    onClick = { onAlbumClick(album) },
                )
            }
        }
    }
}

@Composable
fun AlbumCard(
    album: Album,
    coverArtUrl: String?,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.width(150.dp).clickable(onClick = onClick),
    ) {
        CoverArtImage(
            url = coverArtUrl,
            modifier = Modifier.size(150.dp).clip(RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Text(album.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            album.artist?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun PlaylistSection(
    title: String,
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    urlBuilder: SubsonicUrlBuilder,
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(12.dp))
        playlists.forEach { playlist ->
            PlaylistRow(
                playlist = playlist,
                coverArtUrl = playlist.coverArt?.let { urlBuilder.coverArtUrl(it, 100) },
                onClick = { onPlaylistClick(playlist) },
            )
        }
    }
}

@Composable
fun PlaylistRow(
    playlist: Playlist,
    coverArtUrl: String?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CoverArtImage(
                url = coverArtUrl,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(6.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(playlist.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                playlist.songCount?.let {
                    Text("$it tracks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        }
    }
}
