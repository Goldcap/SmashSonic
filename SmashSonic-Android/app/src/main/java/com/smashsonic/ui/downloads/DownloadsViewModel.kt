package com.smashsonic.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smashsonic.data.local.DownloadedSongEntity
import com.smashsonic.data.model.Song
import com.smashsonic.data.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
) : ViewModel() {

    val downloadedSongs: StateFlow<List<DownloadedSongEntity>> = downloadRepository.downloadedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDownloads: StateFlow<Map<String, Float>> = downloadRepository.activeDownloads

    fun download(song: Song) {
        viewModelScope.launch { downloadRepository.download(song) }
    }

    fun deleteDownload(songId: String) {
        viewModelScope.launch { downloadRepository.deleteDownload(songId) }
    }

    val totalSize: Long
        get() = downloadedSongs.value.mapNotNull { it.fileSize?.toLong() }.sum()
}
