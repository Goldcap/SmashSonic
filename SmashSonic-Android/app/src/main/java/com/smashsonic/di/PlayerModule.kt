package com.smashsonic.di

import android.content.Context
import com.smashsonic.data.remote.SubsonicUrlBuilder
import com.smashsonic.data.repository.DownloadRepository
import com.smashsonic.data.repository.SubsonicRepository
import com.smashsonic.player.SmashSonicPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideSmashSonicPlayer(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        urlBuilder: SubsonicUrlBuilder,
        repository: SubsonicRepository,
        downloadRepository: DownloadRepository,
    ): SmashSonicPlayer = SmashSonicPlayer(context, okHttpClient, urlBuilder, repository).also {
        it.setLocalFileResolver { songId -> downloadRepository.localPath(songId) }
    }
}
