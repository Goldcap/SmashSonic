package com.smashsonic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class SmashSonicApp : Application(), SingletonImageLoader.Factory {

    @Inject lateinit var okHttpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val playback = NotificationChannel(
            PLAYBACK_CHANNEL_ID,
            "Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Music playback controls"
        }

        val downloads = NotificationChannel(
            DOWNLOADS_CHANNEL_ID,
            "Downloads",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Download progress"
        }

        manager.createNotificationChannel(playback)
        manager.createNotificationChannel(downloads)
    }

    override fun newImageLoader(context: android.content.Context): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
            }
            .build()
    }

    companion object {
        const val PLAYBACK_CHANNEL_ID = "playback"
        const val DOWNLOADS_CHANNEL_ID = "downloads"
    }
}
