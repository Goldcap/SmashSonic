package com.smashsonic.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smashsonic.data.model.Album
import com.smashsonic.data.model.Artist
import com.smashsonic.data.model.Song
import com.smashsonic.data.repository.SubsonicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SubsonicRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()

    private var searchJob: Job? = null

    init {
        _query
            .debounce(300)
            .distinctUntilChanged()
            .onEach { q ->
                if (q.isBlank()) {
                    clearResults()
                } else {
                    performSearch(q)
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateQuery(value: String) {
        _query.value = value
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            try {
                val result = repository.search(query)
                _artists.value = result.artists
                _albums.value = result.albums
                _songs.value = result.songs
                _hasSearched.value = true
            } catch (e: Exception) {
                // Silently handle cancellation
            } finally {
                _isSearching.value = false
            }
        }
    }

    private fun clearResults() {
        _artists.value = emptyList()
        _albums.value = emptyList()
        _songs.value = emptyList()
        _hasSearched.value = false
    }

    val hasResults: Boolean
        get() = _artists.value.isNotEmpty() || _albums.value.isNotEmpty() || _songs.value.isNotEmpty()

    val isEmpty: Boolean
        get() = _hasSearched.value && !hasResults
}
