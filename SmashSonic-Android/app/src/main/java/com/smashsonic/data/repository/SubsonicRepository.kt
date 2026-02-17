package com.smashsonic.data.repository

import com.smashsonic.data.model.*
import com.smashsonic.data.remote.SubsonicApi
import com.smashsonic.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubsonicRepository @Inject constructor(
    private val api: SubsonicApi,
) {
    suspend fun ping(): Boolean {
        return try {
            val response = api.ping()
            response.subsonicResponse.status == "ok"
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getArtists(): List<Artist> {
        val response = api.getArtists()
        checkError(response)
        return response.subsonicResponse.artists?.index
            ?.flatMap { it.artist?.map { a -> a.toModel() } ?: emptyList() }
            ?: emptyList()
    }

    suspend fun getArtist(id: String): ArtistDetail {
        val response = api.getArtist(id)
        checkError(response)
        return response.subsonicResponse.artist?.toModel()
            ?: throw SubsonicException("Empty response")
    }

    suspend fun getAlbum(id: String): Album {
        val response = api.getAlbum(id)
        checkError(response)
        return response.subsonicResponse.album?.toModel()
            ?: throw SubsonicException("Empty response")
    }

    suspend fun getAlbumList(type: String = "alphabeticalByName", size: Int = 50, offset: Int = 0): List<Album> {
        val response = api.getAlbumList2(type, size, offset)
        checkError(response)
        return response.subsonicResponse.albumList2?.album?.map { it.toModel() } ?: emptyList()
    }

    suspend fun getPlaylists(): List<Playlist> {
        val response = api.getPlaylists()
        checkError(response)
        return response.subsonicResponse.playlists?.playlist?.map { it.toModel() } ?: emptyList()
    }

    suspend fun getPlaylist(id: String): Playlist {
        val response = api.getPlaylist(id)
        checkError(response)
        return response.subsonicResponse.playlist?.toModel()
            ?: throw SubsonicException("Empty response")
    }

    suspend fun search(query: String, artistCount: Int = 20, albumCount: Int = 20, songCount: Int = 50): SearchResult {
        val response = api.search3(query, artistCount, albumCount, songCount)
        checkError(response)
        return response.subsonicResponse.searchResult3?.toModel() ?: SearchResult()
    }

    suspend fun star(songId: String) {
        val response = api.star(songId)
        checkError(response)
    }

    suspend fun unstar(songId: String) {
        val response = api.unstar(songId)
        checkError(response)
    }

    suspend fun getStarred(): List<Song> {
        val response = api.getStarred2()
        checkError(response)
        return response.subsonicResponse.starred2?.song?.map { it.toModel() } ?: emptyList()
    }

    suspend fun getRandomSongs(size: Int = 50): List<Song> {
        val response = api.getRandomSongs(size)
        checkError(response)
        return response.subsonicResponse.randomSongs?.song?.map { it.toModel() } ?: emptyList()
    }

    suspend fun startTrackRadio(songId: String, count: Int = 20): Pair<String, List<Song>> {
        val response = api.startTrackRadio(songId, count)
        checkError(response)
        val session = response.subsonicResponse.trackRadioSession
            ?: throw SubsonicException("Empty response")
        val sessionId = session.sessionId ?: throw SubsonicException("No session ID")
        val songs = session.song?.map { it.toModel() } ?: emptyList()
        return sessionId to songs
    }

    suspend fun getTrackRadioSongs(sessionId: String, count: Int = 10): List<Song> {
        val response = api.getTrackRadioSongs(sessionId, count)
        checkError(response)
        return response.subsonicResponse.trackRadioSongs?.song?.map { it.toModel() } ?: emptyList()
    }

    suspend fun stopTrackRadio(sessionId: String) {
        try {
            val response = api.stopTrackRadio(sessionId)
            checkError(response)
        } catch (_: Exception) { }
    }

    private fun checkError(wrapper: SubsonicResponseWrapper) {
        val response = wrapper.subsonicResponse
        response.error?.let {
            throw SubsonicException(it.message, it.code)
        }
        if (response.status != "ok") {
            throw SubsonicException("Invalid response from server")
        }
    }
}

class SubsonicException(message: String, val code: Int? = null) : Exception(message)
