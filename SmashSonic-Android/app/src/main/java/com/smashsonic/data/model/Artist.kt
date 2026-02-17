package com.smashsonic.data.model

data class Artist(
    val id: String,
    val name: String,
    val albumCount: Int? = null,
    val coverArt: String? = null,
)
