package com.smashsonic.data.model

data class ArtistDetail(
    val id: String,
    val name: String,
    val albumCount: Int? = null,
    val coverArt: String? = null,
    val albums: List<Album> = emptyList(),
)
