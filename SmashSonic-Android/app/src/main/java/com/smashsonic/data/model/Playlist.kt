package com.smashsonic.data.model

data class Playlist(
    val id: String,
    val name: String,
    val comment: String? = null,
    val owner: String? = null,
    val songCount: Int? = null,
    val duration: Int? = null,
    val created: String? = null,
    val changed: String? = null,
    val coverArt: String? = null,
    val songs: List<Song>? = null,
)
