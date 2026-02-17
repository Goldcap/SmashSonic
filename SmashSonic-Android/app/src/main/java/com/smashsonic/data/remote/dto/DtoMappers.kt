package com.smashsonic.data.remote.dto

import com.smashsonic.data.model.*

fun SongDto.toModel(): Song = Song(
    id = id,
    title = title,
    album = album,
    albumId = albumId,
    artist = artist,
    artistId = artistId,
    track = track,
    duration = duration,
    coverArt = coverArt,
    suffix = suffix,
    bitRate = bitRate,
    size = size,
    contentType = contentType,
    path = path,
)

fun AlbumDto.toModel(): Album = Album(
    id = id,
    name = name ?: title ?: "",
    artist = artist,
    artistId = artistId,
    coverArt = coverArt,
    songCount = songCount,
    year = year,
    genre = genre,
    duration = duration,
    songs = song?.map { it.toModel() },
)

fun ArtistDto.toModel(): Artist = Artist(
    id = id,
    name = name,
    albumCount = albumCount,
    coverArt = coverArt,
)

fun ArtistDetailDto.toModel(): ArtistDetail = ArtistDetail(
    id = id,
    name = name,
    albumCount = albumCount,
    coverArt = coverArt,
    albums = album?.map { it.toModel() } ?: emptyList(),
)

fun PlaylistDto.toModel(): Playlist = Playlist(
    id = id,
    name = name,
    comment = comment,
    owner = owner,
    songCount = songCount,
    duration = duration,
    created = created,
    changed = changed,
    coverArt = coverArt,
    songs = entry?.map { it.toModel() },
)

fun SearchResultDto.toModel(): SearchResult = SearchResult(
    artists = artist?.map { it.toModel() } ?: emptyList(),
    albums = album?.map { it.toModel() } ?: emptyList(),
    songs = song?.map { it.toModel() } ?: emptyList(),
)
