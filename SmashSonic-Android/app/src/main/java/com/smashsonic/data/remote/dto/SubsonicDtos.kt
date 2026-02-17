package com.smashsonic.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Root wrapper
@JsonClass(generateAdapter = true)
data class SubsonicResponseWrapper(
    @Json(name = "subsonic-response") val subsonicResponse: SubsonicResponse,
)

@JsonClass(generateAdapter = true)
data class SubsonicResponse(
    val status: String,
    val version: String? = null,
    val error: SubsonicError? = null,

    // Various response fields
    val artists: ArtistsDto? = null,
    val artist: ArtistDetailDto? = null,
    val album: AlbumDto? = null,
    val albumList2: AlbumListDto? = null,
    val playlists: PlaylistsDto? = null,
    val playlist: PlaylistDto? = null,
    val searchResult3: SearchResultDto? = null,
    val starred2: StarredDto? = null,
    val randomSongs: RandomSongsDto? = null,
    val trackRadioSession: TrackRadioSessionDto? = null,
    val trackRadioSongs: TrackRadioSongsDto? = null,
)

@JsonClass(generateAdapter = true)
data class SubsonicError(
    val code: Int,
    val message: String,
)

// Artists
@JsonClass(generateAdapter = true)
data class ArtistsDto(
    val index: List<ArtistIndexDto>? = null,
)

@JsonClass(generateAdapter = true)
data class ArtistIndexDto(
    val name: String? = null,
    val artist: List<ArtistDto>? = null,
)

@JsonClass(generateAdapter = true)
data class ArtistDto(
    val id: String,
    val name: String,
    val albumCount: Int? = null,
    val coverArt: String? = null,
)

@JsonClass(generateAdapter = true)
data class ArtistDetailDto(
    val id: String,
    val name: String,
    val albumCount: Int? = null,
    val coverArt: String? = null,
    val album: List<AlbumDto>? = null,
)

// Albums
@JsonClass(generateAdapter = true)
data class AlbumListDto(
    val album: List<AlbumDto>? = null,
)

@JsonClass(generateAdapter = true)
data class AlbumDto(
    val id: String,
    val name: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val artistId: String? = null,
    val coverArt: String? = null,
    val songCount: Int? = null,
    val year: Int? = null,
    val genre: String? = null,
    val duration: Int? = null,
    val song: List<SongDto>? = null,
)

// Songs
@JsonClass(generateAdapter = true)
data class SongDto(
    val id: String,
    val title: String,
    val album: String? = null,
    val albumId: String? = null,
    val artist: String? = null,
    val artistId: String? = null,
    val track: Int? = null,
    val duration: Int? = null,
    val coverArt: String? = null,
    val suffix: String? = null,
    val bitRate: Int? = null,
    val size: Int? = null,
    val contentType: String? = null,
    val path: String? = null,
)

// Playlists
@JsonClass(generateAdapter = true)
data class PlaylistsDto(
    val playlist: List<PlaylistDto>? = null,
)

@JsonClass(generateAdapter = true)
data class PlaylistDto(
    val id: String,
    val name: String,
    val comment: String? = null,
    val owner: String? = null,
    val songCount: Int? = null,
    val duration: Int? = null,
    val created: String? = null,
    val changed: String? = null,
    val coverArt: String? = null,
    val entry: List<SongDto>? = null,
)

// Search
@JsonClass(generateAdapter = true)
data class SearchResultDto(
    val artist: List<ArtistDto>? = null,
    val album: List<AlbumDto>? = null,
    val song: List<SongDto>? = null,
)

// Starred
@JsonClass(generateAdapter = true)
data class StarredDto(
    val song: List<SongDto>? = null,
)

// Random Songs
@JsonClass(generateAdapter = true)
data class RandomSongsDto(
    val song: List<SongDto>? = null,
)

// Track Radio
@JsonClass(generateAdapter = true)
data class TrackRadioSessionDto(
    val sessionId: String? = null,
    val song: List<SongDto>? = null,
)

@JsonClass(generateAdapter = true)
data class TrackRadioSongsDto(
    val song: List<SongDto>? = null,
)
