package com.smashsonic.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smashsonic.data.model.Album
import com.smashsonic.data.model.Playlist
import com.smashsonic.data.repository.SubsonicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SubsonicRepository,
) : ViewModel() {

    private val _randomAlbums = MutableStateFlow<List<Album>>(emptyList())
    val randomAlbums: StateFlow<List<Album>> = _randomAlbums.asStateFlow()

    private val _recentAlbums = MutableStateFlow<List<Album>>(emptyList())
    val recentAlbums: StateFlow<List<Album>> = _recentAlbums.asStateFlow()

    private val _starredAlbums = MutableStateFlow<List<Album>>(emptyList())
    val starredAlbums: StateFlow<List<Album>> = _starredAlbums.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val random = async { repository.getAlbumList("random", 10) }
                val recent = async { repository.getAlbumList("newest", 10) }
                val starred = async { repository.getAlbumList("starred", 10) }
                val playlists = async { repository.getPlaylists() }

                _randomAlbums.value = random.await()
                _recentAlbums.value = recent.await()
                _starredAlbums.value = starred.await()
                _playlists.value = playlists.await()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshRandom() {
        viewModelScope.launch {
            try {
                _randomAlbums.value = repository.getAlbumList("random", 10)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
