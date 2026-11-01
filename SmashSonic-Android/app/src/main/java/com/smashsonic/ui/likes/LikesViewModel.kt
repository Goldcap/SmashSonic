package com.smashsonic.ui.likes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smashsonic.data.local.LikedSongEntity
import com.smashsonic.data.model.Album
import com.smashsonic.data.model.Artist
import com.smashsonic.data.model.Song
import com.smashsonic.data.repository.LikesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikesViewModel @Inject constructor(
    private val likesRepository: LikesRepository,
) : ViewModel() {

    val likedSongs: StateFlow<List<LikedSongEntity>> = likesRepository.likedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val likedSongIds: StateFlow<Set<String>> = likesRepository.likedSongIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val starredAlbums: StateFlow<List<Album>> = likesRepository.starredAlbums
    val starredArtists: StateFlow<List<Artist>> = likesRepository.starredArtists

    val starredAlbumIds: StateFlow<Set<String>> = likesRepository.starredAlbumIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    val starredArtistIds: StateFlow<Set<String>> = likesRepository.starredArtistIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val isSyncing: StateFlow<Boolean> = likesRepository.isSyncing

    fun toggleLike(song: Song) {
        viewModelScope.launch { likesRepository.toggleLike(song) }
    }

    fun unlike(songId: String) {
        viewModelScope.launch { likesRepository.unlike(songId) }
    }

    fun toggleAlbumStar(album: Album) {
        viewModelScope.launch { likesRepository.toggleAlbumStar(album) }
    }

    fun toggleArtistStar(artist: Artist) {
        viewModelScope.launch { likesRepository.toggleArtistStar(artist) }
    }

    /** Loads starred albums & artists from the server (startup + refresh). */
    fun loadStarred() {
        viewModelScope.launch { likesRepository.loadStarredAlbumsAndArtists() }
    }

    fun syncFromServer() {
        viewModelScope.launch { likesRepository.syncFromServer() }
    }

    fun isLiked(songId: String): Boolean = likedSongIds.value.contains(songId)
}
