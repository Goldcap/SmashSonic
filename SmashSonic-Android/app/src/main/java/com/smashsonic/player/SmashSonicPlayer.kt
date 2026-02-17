package com.smashsonic.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.smashsonic.data.model.Song
import com.smashsonic.data.remote.SubsonicUrlBuilder
import com.smashsonic.data.repository.SubsonicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

enum class PlayMode(val displayName: String) {
    PLAY_ONCE("Play Once"),
    LOOP("Loop"),
    SHUFFLE("Shuffle");

    fun next(): PlayMode = when (this) {
        PLAY_ONCE -> LOOP
        LOOP -> SHUFFLE
        SHUFFLE -> PLAY_ONCE
    }
}

@Singleton
class SmashSonicPlayer @OptIn(UnstableApi::class) @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val urlBuilder: SubsonicUrlBuilder,
    private val repository: SubsonicRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @OptIn(UnstableApi::class)
    val exoPlayer: ExoPlayer by lazy {
        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setHandleAudioBecomingNoisy(true)
            .build().also { player ->
                player.addListener(playerListener)
            }
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _playMode = MutableStateFlow(PlayMode.PLAY_ONCE)
    val playMode: StateFlow<PlayMode> = _playMode.asStateFlow()

    private val _autoAddRandomSongs = MutableStateFlow(false)
    val autoAddRandomSongs: StateFlow<Boolean> = _autoAddRandomSongs.asStateFlow()

    private val _trackRadioSessionId = MutableStateFlow<String?>(null)
    val trackRadioSessionId: StateFlow<String?> = _trackRadioSessionId.asStateFlow()

    private val _trackRadioSeedSong = MutableStateFlow<Song?>(null)
    val trackRadioSeedSong: StateFlow<Song?> = _trackRadioSeedSong.asStateFlow()

    private var isLoadingMoreSongs = false
    private val randomSongThreshold = 5
    private val randomSongsToAdd = 10
    private var positionUpdateJob: Job? = null

    private var localFileResolver: ((String) -> String?)? = null

    fun setLocalFileResolver(resolver: (String) -> String?) {
        localFileResolver = resolver
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            _isPlaying.value = playing
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _isLoading.value = false
                    val dur = exoPlayer.duration
                    if (dur > 0) {
                        _duration.value = dur
                    } else {
                        _currentSong.value?.duration?.let { _duration.value = it * 1000L }
                    }
                    startPositionUpdates()
                }
                Player.STATE_BUFFERING -> _isLoading.value = true
                Player.STATE_ENDED -> handlePlaybackEnded()
                Player.STATE_IDLE -> {}
            }
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                _currentTime.value = exoPlayer.currentPosition
                delay(500)
            }
        }
    }

    fun playSong(song: Song, queue: List<Song>? = null) {
        if (queue != null) {
            val index = queue.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
            _queue.value = queue
            _currentIndex.value = index
        } else {
            _queue.value = listOf(song)
            _currentIndex.value = 0
        }
        loadAndPlay(song)
    }

    private fun loadAndPlay(song: Song) {
        _isLoading.value = true
        _currentSong.value = song

        val localPath = localFileResolver?.invoke(song.id)
        val url = localPath ?: urlBuilder.streamUrl(song.id)
        if (url == null) {
            _isLoading.value = false
            return
        }

        val mediaItem = if (localPath != null) {
            MediaItem.fromUri("file://$localPath")
        } else {
            MediaItem.fromUri(url)
        }
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun handlePlaybackEnded() {
        when (_playMode.value) {
            PlayMode.LOOP -> {
                exoPlayer.seekTo(0)
                exoPlayer.playWhenReady = true
            }
            PlayMode.SHUFFLE -> {
                val q = _queue.value
                if (q.size > 1) {
                    var randomIndex: Int
                    do {
                        randomIndex = q.indices.random()
                    } while (randomIndex == _currentIndex.value && q.size > 1)
                    playFromQueue(randomIndex)
                } else {
                    scope.launch {
                        addRandomSongsToQueue(1)
                        if (_currentIndex.value < _queue.value.size - 1) next()
                    }
                }
            }
            PlayMode.PLAY_ONCE -> {
                if (_currentIndex.value < _queue.value.size - 1) {
                    next()
                    checkAndReplenishQueue()
                } else if (_autoAddRandomSongs.value) {
                    scope.launch {
                        addRandomSongsToQueue()
                        if (_currentIndex.value < _queue.value.size - 1) next()
                    }
                } else {
                    _isPlaying.value = false
                    _currentTime.value = 0
                }
            }
        }
    }

    private fun checkAndReplenishQueue() {
        if (!_autoAddRandomSongs.value) return
        val remaining = _queue.value.size - _currentIndex.value - 1
        if (remaining <= randomSongThreshold && !isLoadingMoreSongs) {
            scope.launch { addRandomSongsToQueue() }
        }
    }

    suspend fun addRandomSongsToQueue(count: Int? = null) {
        if (isLoadingMoreSongs) return
        isLoadingMoreSongs = true
        try {
            val toAdd = count ?: randomSongsToAdd
            val newSongs = if (_trackRadioSessionId.value != null) {
                repository.getTrackRadioSongs(_trackRadioSessionId.value!!, toAdd)
            } else {
                repository.getRandomSongs(toAdd)
            }
            _queue.value = _queue.value + newSongs
        } catch (e: Exception) {
            // Silently fail
        } finally {
            isLoadingMoreSongs = false
        }
    }

    fun startRandomPlayback() {
        scope.launch {
            _trackRadioSessionId.value?.let { repository.stopTrackRadio(it) }
            _trackRadioSessionId.value = null
            _trackRadioSeedSong.value = null
            _autoAddRandomSongs.value = true

            try {
                val songs = repository.getRandomSongs(20)
                if (songs.isNotEmpty()) {
                    _queue.value = songs
                    _currentIndex.value = 0
                    loadAndPlay(songs.first())
                }
            } catch (e: Exception) { }
        }
    }

    fun startTrackRadio(song: Song) {
        scope.launch {
            _trackRadioSessionId.value?.let { repository.stopTrackRadio(it) }
            _autoAddRandomSongs.value = false

            try {
                val (sessionId, songs) = repository.startTrackRadio(song.id, 20)
                _trackRadioSessionId.value = sessionId
                _trackRadioSeedSong.value = song
                _autoAddRandomSongs.value = true
                if (songs.isNotEmpty()) {
                    _queue.value = songs
                    _currentIndex.value = 0
                    loadAndPlay(songs.first())
                }
            } catch (e: Exception) {
                _trackRadioSessionId.value = null
                _trackRadioSeedSong.value = null
            }
        }
    }

    fun addToQueue(song: Song) { _queue.value = _queue.value + song }
    fun addToQueue(songs: List<Song>) { _queue.value = _queue.value + songs }

    fun playNow(song: Song) {
        if (_queue.value.isEmpty()) {
            playSong(song)
        } else {
            val mutable = _queue.value.toMutableList()
            mutable.add(_currentIndex.value + 1, song)
            _queue.value = mutable
            next()
        }
    }

    fun playNext(song: Song) {
        if (_queue.value.isEmpty()) {
            _queue.value = listOf(song)
        } else {
            val mutable = _queue.value.toMutableList()
            mutable.add(_currentIndex.value + 1, song)
            _queue.value = mutable
        }
    }

    fun playLast(song: Song) {
        if (_queue.value.isEmpty()) {
            _queue.value = listOf(song)
        } else {
            _queue.value = _queue.value + song
        }
    }

    fun removeFromQueue(index: Int) {
        if (index < 0 || index >= _queue.value.size || index == _currentIndex.value) return
        val mutable = _queue.value.toMutableList()
        mutable.removeAt(index)
        _queue.value = mutable
        if (index < _currentIndex.value) _currentIndex.value -= 1
    }

    fun playFromQueue(index: Int) {
        if (index < 0 || index >= _queue.value.size) return
        _currentIndex.value = index
        loadAndPlay(_queue.value[index])
    }

    fun clearQueue() {
        scope.launch {
            _trackRadioSessionId.value?.let { repository.stopTrackRadio(it) }
        }
        exoPlayer.stop()
        _queue.value = emptyList()
        _currentIndex.value = 0
        _currentSong.value = null
        _isPlaying.value = false
        _autoAddRandomSongs.value = false
        _trackRadioSessionId.value = null
        _trackRadioSeedSong.value = null
    }

    fun play() { exoPlayer.playWhenReady = true }
    fun pause() { exoPlayer.playWhenReady = false }
    fun togglePlayPause() { if (exoPlayer.isPlaying) pause() else play() }

    fun next() {
        if (_currentIndex.value < _queue.value.size - 1) {
            _currentIndex.value += 1
            loadAndPlay(_queue.value[_currentIndex.value])
        }
    }

    fun previous() {
        if (_currentTime.value > 3000) {
            seekTo(0)
        } else if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
            loadAndPlay(_queue.value[_currentIndex.value])
        } else {
            seekTo(0)
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _currentTime.value = positionMs
    }

    fun cyclePlayMode() { _playMode.value = _playMode.value.next() }
    fun skipForward(seconds: Long = 15) { seekTo(minOf(_currentTime.value + seconds * 1000, _duration.value)) }
    fun skipBackward(seconds: Long = 15) { seekTo(maxOf(_currentTime.value - seconds * 1000, 0)) }
    fun setAutoAdd(enabled: Boolean) { _autoAddRandomSongs.value = enabled }
}
