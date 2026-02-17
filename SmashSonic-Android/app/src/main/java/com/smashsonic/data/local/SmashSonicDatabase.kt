package com.smashsonic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DownloadedSongEntity::class, LikedSongEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SmashSonicDatabase : RoomDatabase() {
    abstract fun downloadedSongDao(): DownloadedSongDao
    abstract fun likedSongDao(): LikedSongDao
}
