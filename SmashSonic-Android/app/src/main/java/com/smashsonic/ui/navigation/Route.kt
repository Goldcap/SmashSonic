package com.smashsonic.ui.navigation

sealed class Route(val route: String) {
    data object ServerSetup : Route("server_setup")
    data object Main : Route("main")
    data object Home : Route("home")
    data object Browse : Route("browse")
    data object Search : Route("search")
    data object LikedSongs : Route("liked_songs")
    data object Downloads : Route("downloads")
    data object Settings : Route("settings")
    data object AppearanceSettings : Route("appearance_settings")
    data object NowPlaying : Route("now_playing")
    data object Queue : Route("queue")

    data object AlbumDetail : Route("album/{albumId}") {
        fun create(albumId: String) = "album/$albumId"
    }
    data object ArtistDetail : Route("artist/{artistId}") {
        fun create(artistId: String) = "artist/$artistId"
    }
    data object PlaylistDetail : Route("playlist/{playlistId}") {
        fun create(playlistId: String) = "playlist/$playlistId"
    }
}
