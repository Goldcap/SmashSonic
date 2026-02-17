package com.smashsonic.data.model

data class Album(
    val id: String,
    val name: String,
    val artist: String? = null,
    val artistId: String? = null,
    val coverArt: String? = null,
    val songCount: Int? = null,
    val year: Int? = null,
    val genre: String? = null,
    val duration: Int? = null,
    val songs: List<Song>? = null,
)
