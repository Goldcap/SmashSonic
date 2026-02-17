package com.smashsonic.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smashsonic.data.model.Song

@Entity(tableName = "liked_songs")
data class LikedSongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val album: String?,
    val albumId: String?,
    val artist: String?,
    val artistId: String?,
    val track: Int?,
    val duration: Int?,
    val coverArt: String?,
    val suffix: String?,
    val likedAt: Long,
    val fileSize: Int?,
) {
    fun toSong(): Song = Song(
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
    )

    companion object {
        fun from(song: Song): LikedSongEntity = LikedSongEntity(
            id = song.id,
            title = song.title,
            album = song.album,
            albumId = song.albumId,
            artist = song.artist,
            artistId = song.artistId,
            track = song.track,
            duration = song.duration,
            coverArt = song.coverArt,
            suffix = song.suffix,
            likedAt = System.currentTimeMillis(),
            fileSize = song.size,
        )
    }
}
