package com.smashsonic.ui.likes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smashsonic.data.local.LikedSongEntity
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

    val isSyncing: StateFlow<Boolean> = likesRepository.isSyncing

    fun toggleLike(song: Song) {
        viewModelScope.launch { likesRepository.toggleLike(song) }
    }

    fun unlike(songId: String) {
        viewModelScope.launch { likesRepository.unlike(songId) }
    }

    fun syncFromServer() {
        viewModelScope.launch { likesRepository.syncFromServer() }
    }

    fun isLiked(songId: String): Boolean = likedSongIds.value.contains(songId)
}
