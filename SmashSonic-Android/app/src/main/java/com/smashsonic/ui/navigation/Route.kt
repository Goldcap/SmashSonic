package com.smashsonic.ui.navigation

sealed class Route(val route: String) {
    data object ServerSetup : Route("server_setup")
    data object Main : Route("main")

    // Transparent start destination of the inner content NavHost inside MainScreen.
    // While it is current, the persistent tab layer shows through; detail screens are
    // pushed on top of it so navigating in/out never disposes the tab content.
    data object ContentRoot : Route("content_root")
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
