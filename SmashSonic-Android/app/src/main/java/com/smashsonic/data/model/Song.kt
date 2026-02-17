package com.smashsonic.data.model

data class Song(
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
) {
    val formattedDuration: String
        get() {
            val d = duration ?: return "--:--"
            val minutes = d / 60
            val seconds = d % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
