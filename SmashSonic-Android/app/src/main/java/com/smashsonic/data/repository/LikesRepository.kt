package com.smashsonic.data.repository

import com.smashsonic.data.local.LikedSongDao
import com.smashsonic.data.local.LikedSongEntity
import com.smashsonic.data.model.Album
import com.smashsonic.data.model.Artist
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

    // Album & artist favorites: server is the source of truth, held in memory
    // (this is a @Singleton, so the state survives for the whole app process).
    private val _starredAlbums = MutableStateFlow<List<Album>>(emptyList())
    val starredAlbums: StateFlow<List<Album>> = _starredAlbums.asStateFlow()

    private val _starredArtists = MutableStateFlow<List<Artist>>(emptyList())
    val starredArtists: StateFlow<List<Artist>> = _starredArtists.asStateFlow()

    val starredAlbumIds: Flow<Set<String>> = _starredAlbums.map { list -> list.map { it.id }.toSet() }
    val starredArtistIds: Flow<Set<String>> = _starredArtists.map { list -> list.map { it.id }.toSet() }

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

    // MARK: - Album & artist favorites (server-backed, optimistic UI)

    fun isAlbumStarred(albumId: String): Boolean = _starredAlbums.value.any { it.id == albumId }

    fun isArtistStarred(artistId: String): Boolean = _starredArtists.value.any { it.id == artistId }

    suspend fun toggleAlbumStar(album: Album) {
        val wasStarred = isAlbumStarred(album.id)
        // Optimistic update.
        _starredAlbums.value = if (wasStarred) {
            _starredAlbums.value.filterNot { it.id == album.id }
        } else {
            listOf(album) + _starredAlbums.value.filterNot { it.id == album.id }
        }
        try {
            if (wasStarred) subsonicRepository.unstarAlbum(album.id)
            else subsonicRepository.starAlbum(album.id)
        } catch (_: Exception) {
            // Keep optimistic state; the next sync reconciles with the server.
        }
    }

    suspend fun toggleArtistStar(artist: Artist) {
        val wasStarred = isArtistStarred(artist.id)
        _starredArtists.value = if (wasStarred) {
            _starredArtists.value.filterNot { it.id == artist.id }
        } else {
            listOf(artist) + _starredArtists.value.filterNot { it.id == artist.id }
        }
        try {
            if (wasStarred) subsonicRepository.unstarArtist(artist.id)
            else subsonicRepository.starArtist(artist.id)
        } catch (_: Exception) {
        }
    }

    /**
     * Loads starred albums & artists from the server (the source of truth for
     * those) and replaces the in-memory lists. Called on app start and refresh.
     */
    suspend fun loadStarredAlbumsAndArtists() {
        try {
            val (albums, artists) = subsonicRepository.getStarredAlbumsAndArtists()
            _starredAlbums.value = albums
            _starredArtists.value = artists
        } catch (_: Exception) {
        }
    }

    suspend fun syncFromServer() {
        _isSyncing.value = true
        try {
            // Songs: merged additively into the local Room store (unchanged behavior).
            val starredSongs = subsonicRepository.getStarred()
            val existingIds = dao.getAllIdsOnce().toSet()
            for (song in starredSongs) {
                if (song.id !in existingIds) {
                    dao.insert(LikedSongEntity.from(song))
                }
            }
            // Albums & artists: replaced wholesale from the authoritative server list.
            loadStarredAlbumsAndArtists()
        } catch (_: Exception) {
        } finally {
            _isSyncing.value = false
        }
    }
}
