package com.smashsonic.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smashsonic.data.model.*
import com.smashsonic.data.repository.SubsonicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: SubsonicRepository,
) : ViewModel() {

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadArtists() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _artists.value = repository.getArtists()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAlbums() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _albums.value = repository.getAlbumList("alphabeticalByName", 500)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun loadArtist(id: String): ArtistDetail? {
        return try {
            repository.getArtist(id)
        } catch (e: Exception) {
            _error.value = e.message
            null
        }
    }

    suspend fun loadAlbum(id: String): Album? {
        return try {
            repository.getAlbum(id)
        } catch (e: Exception) {
            _error.value = e.message
            null
        }
    }

    suspend fun loadPlaylist(id: String): Playlist? {
        return try {
            repository.getPlaylist(id)
        } catch (e: Exception) {
            _error.value = e.message
            null
        }
    }
}
