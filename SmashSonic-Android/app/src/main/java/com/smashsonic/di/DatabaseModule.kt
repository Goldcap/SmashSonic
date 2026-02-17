package com.smashsonic.di

import android.content.Context
import androidx.room.Room
import com.smashsonic.data.local.DownloadedSongDao
import com.smashsonic.data.local.LikedSongDao
import com.smashsonic.data.local.SmashSonicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): SmashSonicDatabase = Room.databaseBuilder(
        context,
        SmashSonicDatabase::class.java,
        "smashsonic.db",
    ).build()

    @Provides
    fun provideDownloadedSongDao(db: SmashSonicDatabase): DownloadedSongDao = db.downloadedSongDao()

    @Provides
    fun provideLikedSongDao(db: SmashSonicDatabase): LikedSongDao = db.likedSongDao()
}
