package com.smashsonic.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedSongDao {
    @Query("SELECT * FROM liked_songs ORDER BY likedAt DESC")
    fun getAll(): Flow<List<LikedSongEntity>>

    @Query("SELECT id FROM liked_songs")
    fun getAllIds(): Flow<List<String>>

    @Query("SELECT id FROM liked_songs")
    suspend fun getAllIdsOnce(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: LikedSongEntity)

    @Query("DELETE FROM liked_songs WHERE id = :id")
    suspend fun delete(id: String)
}
