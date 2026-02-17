package com.smashsonic.data.repository

import com.smashsonic.data.local.LikedSongDao
import com.smashsonic.data.local.LikedSongEntity
import com.smashsonic.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikesRepository @Inject constructor(
    private val dao: LikedSongDao,
    private val subsonicRepository: SubsonicRepository,
) {
    val likedSongs: Flow<List<LikedSongEntity>> = dao.getAll()

    val likedSongIds: Flow<Set<String>> = dao.getAllIds().map { it.toSet() }

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    suspend fun isLiked(songId: String): Boolean = dao.exists(songId)

    suspend fun toggleLike(song: Song) {
        if (dao.exists(song.id)) {
            unlike(song.id)
        } else {
            like(song)
        }
    }

    suspend fun like(song: Song) {
        dao.insert(LikedSongEntity.from(song))
        try {
            subsonicRepository.star(song.id)
        } catch (_: Exception) { }
    }

    suspend fun unlike(songId: String) {
        dao.delete(songId)
        try {
            subsonicRepository.unstar(songId)
        } catch (_: Exception) { }
    }

    suspend fun syncFromServer() {
        _isSyncing.value = true
        try {
            val starredSongs = subsonicRepository.getStarred()
            val existingIds = dao.getAllIdsOnce().toSet()
            for (song in starredSongs) {
                if (song.id !in existingIds) {
                    dao.insert(LikedSongEntity.from(song))
                }
            }
        } catch (_: Exception) {
        } finally {
            _isSyncing.value = false
        }
    }
}
