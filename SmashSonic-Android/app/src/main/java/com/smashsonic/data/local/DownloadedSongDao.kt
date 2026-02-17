package com.smashsonic.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedSongDao {
    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getAll(): Flow<List<DownloadedSongEntity>>

    @Query("SELECT * FROM downloaded_songs WHERE id = :id")
    suspend fun getById(id: String): DownloadedSongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: DownloadedSongEntity)

    @Query("DELETE FROM downloaded_songs WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_songs WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT localPath FROM downloaded_songs WHERE id = :id")
    suspend fun getLocalPath(id: String): String?
}
