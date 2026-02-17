package com.smashsonic.di

import com.smashsonic.credential.CredentialManager
import com.smashsonic.data.remote.SubsonicApi
import com.smashsonic.data.remote.SubsonicInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideSubsonicInterceptor(
        credentialManager: CredentialManager,
    ): SubsonicInterceptor = SubsonicInterceptor(credentialManager)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        subsonicInterceptor: SubsonicInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(subsonicInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
        credentialManager: CredentialManager,
    ): Retrofit {
        val config = credentialManager.loadServerConfig()
        val baseUrl = config?.baseURL?.let { "$it/" } ?: "https://placeholder.local/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideSubsonicApi(retrofit: Retrofit): SubsonicApi =
        retrofit.create(SubsonicApi::class.java)
}
