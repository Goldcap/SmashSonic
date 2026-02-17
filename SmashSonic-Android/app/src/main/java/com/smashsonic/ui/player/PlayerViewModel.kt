package com.smashsonic.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smashsonic.data.model.Song
import com.smashsonic.player.PlayMode
import com.smashsonic.player.SmashSonicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val player: SmashSonicPlayer,
) : ViewModel() {

    val currentSong: StateFlow<Song?> = player.currentSong
    val queue: StateFlow<List<Song>> = player.queue
    val currentIndex: StateFlow<Int> = player.currentIndex
    val isPlaying: StateFlow<Boolean> = player.isPlaying
    val currentTime: StateFlow<Long> = player.currentTime
    val duration: StateFlow<Long> = player.duration
    val isLoading: StateFlow<Boolean> = player.isLoading
    val playMode: StateFlow<PlayMode> = player.playMode
    val autoAddRandomSongs: StateFlow<Boolean> = player.autoAddRandomSongs
    val trackRadioSessionId: StateFlow<String?> = player.trackRadioSessionId
    val trackRadioSeedSong: StateFlow<Song?> = player.trackRadioSeedSong

    val isTrackRadioActive: StateFlow<Boolean> = player.trackRadioSessionId
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val progress: StateFlow<Float> = combine(player.currentTime, player.duration) { current, dur ->
        if (dur > 0) current.toFloat() / dur.toFloat() else 0f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val currentTimeFormatted: StateFlow<String> = player.currentTime.map { formatTime(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0:00")

    val durationFormatted: StateFlow<String> = player.duration.map { formatTime(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0:00")

    val upcomingSongs: StateFlow<List<Song>> = combine(player.queue, player.currentIndex) { q, idx ->
        if (idx < q.size) q.drop(idx + 1) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playedSongs: StateFlow<List<Song>> = combine(player.queue, player.currentIndex) { q, idx ->
        if (idx > 0) q.take(idx).takeLast(20) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun formatTime(ms: Long): String {
        val totalSeconds = (ms / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    fun play(song: Song, queue: List<Song>? = null) = player.playSong(song, queue)
    fun togglePlayPause() = player.togglePlayPause()
    fun next() = player.next()
    fun previous() = player.previous()
    fun seekTo(ms: Long) = player.seekTo(ms)
    fun skipForward() = player.skipForward()
    fun skipBackward() = player.skipBackward()
    fun cyclePlayMode() = player.cyclePlayMode()
    fun playNow(song: Song) = player.playNow(song)
    fun playNext(song: Song) = player.playNext(song)
    fun playLast(song: Song) = player.playLast(song)
    fun addToQueue(song: Song) = player.addToQueue(song)
    fun removeFromQueue(index: Int) = player.removeFromQueue(index)
    fun playFromQueue(index: Int) = player.playFromQueue(index)
    fun clearQueue() = player.clearQueue()
    fun setAutoAdd(enabled: Boolean) = player.setAutoAdd(enabled)

    fun startRandomPlayback() = player.startRandomPlayback()

    fun startTrackRadio(song: Song) = player.startTrackRadio(song)

    fun addRandomSongsToQueue(count: Int = 10) {
        viewModelScope.launch { player.addRandomSongsToQueue(count) }
    }
}
