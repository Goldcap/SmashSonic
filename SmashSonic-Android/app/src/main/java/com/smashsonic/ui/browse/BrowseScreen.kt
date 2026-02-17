package com.smashsonic.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.smashsonic.ui.home.AlbumCard
import com.smashsonic.ui.home.HomeScreenUrlHelper
import com.smashsonic.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    viewModel: BrowseViewModel = hiltViewModel(),
    urlBuilder: SubsonicUrlBuilder = hiltViewModel<HomeScreenUrlHelper>().urlBuilder,
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val artists by viewModel.artists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(selectedSection) {
        when (selectedSection) {
            0 -> if (artists.isEmpty()) viewModel.loadArtists()
            1 -> if (albums.isEmpty()) viewModel.loadAlbums()
        }
    }

    SmashSonicBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Browse") },
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
                // Segmented toggle
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    FilterChip(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        label = { Text("Artists") },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        label = { Text("Albums") },
                        modifier = Modifier.weight(1f),
                    )
                }

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else when (selectedSection) {
                    0 -> ArtistsList(artists, navController, urlBuilder)
                    1 -> AlbumsGrid(albums, navController, urlBuilder)
                }
            }
        }
    }
}

@Composable
fun ArtistsList(
    artists: List<Artist>,
    navController: NavController,
    urlBuilder: SubsonicUrlBuilder,
) {
    LazyColumn {
        items(artists) { artist ->
            Surface(
                onClick = { navController.navigate(Route.ArtistDetail.create(artist.id)) },
                color = Color.Black.copy(alpha = 0.5f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CoverArtImage(
                        url = artist.coverArt?.let { urlBuilder.coverArtUrl(it, 100) },
                        modifier = Modifier.size(50.dp).clip(CircleShape),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(artist.name, style = MaterialTheme.typography.bodyLarge)
                        artist.albumCount?.let {
                            Text(
                                "$it album${if (it == 1) "" else "s"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumsGrid(
    albums: List<Album>,
    navController: NavController,
    urlBuilder: SubsonicUrlBuilder,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(albums) { album ->
            AlbumCard(
                album = album,
                coverArtUrl = album.coverArt?.let { urlBuilder.coverArtUrl(it) },
                onClick = { navController.navigate(Route.AlbumDetail.create(album.id)) },
            )
        }
    }
}
