package com.smashsonic.data.remote

import com.smashsonic.data.remote.dto.SubsonicResponseWrapper
import retrofit2.http.GET
import retrofit2.http.Query

interface SubsonicApi {

    @GET("rest/ping")
    suspend fun ping(): SubsonicResponseWrapper

    @GET("rest/getArtists")
    suspend fun getArtists(): SubsonicResponseWrapper

    @GET("rest/getArtist")
    suspend fun getArtist(@Query("id") id: String): SubsonicResponseWrapper

    @GET("rest/getAlbum")
    suspend fun getAlbum(@Query("id") id: String): SubsonicResponseWrapper

    @GET("rest/getAlbumList2")
    suspend fun getAlbumList2(
        @Query("type") type: String,
        @Query("size") size: Int = 50,
        @Query("offset") offset: Int = 0,
    ): SubsonicResponseWrapper

    @GET("rest/getPlaylists")
    suspend fun getPlaylists(): SubsonicResponseWrapper

    @GET("rest/getPlaylist")
    suspend fun getPlaylist(@Query("id") id: String): SubsonicResponseWrapper

    @GET("rest/search3")
    suspend fun search3(
        @Query("query") query: String,
        @Query("artistCount") artistCount: Int = 20,
        @Query("albumCount") albumCount: Int = 20,
        @Query("songCount") songCount: Int = 50,
    ): SubsonicResponseWrapper

    @GET("rest/star")
    suspend fun star(@Query("id") id: String): SubsonicResponseWrapper

    @GET("rest/unstar")
    suspend fun unstar(@Query("id") id: String): SubsonicResponseWrapper

    @GET("rest/getStarred2")
    suspend fun getStarred2(): SubsonicResponseWrapper

    @GET("rest/getRandomSongs")
    suspend fun getRandomSongs(@Query("size") size: Int = 50): SubsonicResponseWrapper

    @GET("rest/startTrackRadio")
    suspend fun startTrackRadio(
        @Query("id") songId: String,
        @Query("count") count: Int = 20,
    ): SubsonicResponseWrapper

    @GET("rest/getTrackRadioSongs")
    suspend fun getTrackRadioSongs(
        @Query("sessionId") sessionId: String,
        @Query("count") count: Int = 10,
    ): SubsonicResponseWrapper

    @GET("rest/stopTrackRadio")
    suspend fun stopTrackRadio(@Query("sessionId") sessionId: String): SubsonicResponseWrapper
}
