# SmashSonic

A native music streaming app for Subsonic-compatible servers (Navidrome, Airsonic, etc.), with clients for both iOS (SwiftUI) and Android (Jetpack Compose).

## Features

- **Library Browsing**: Browse artists and albums with cover art
- **Music Streaming**: Stream songs directly from your Subsonic server
- **Search**: Search for artists, albums, and songs
- **Offline Downloads**: Download songs for offline playback
- **Now Playing**: Full-screen player with album art and controls
- **Mini Player**: Persistent mini player with quick controls
- **Background Playback**: Continue listening with screen locked
- **Lock Screen Controls**: Control playback from the lock screen
- **Secure Storage**: Credentials stored in iOS Keychain

## Requirements

### iOS
- iOS 17.0+
- Xcode 15.0+

### Android
- Android 10+ (API 29)
- JDK 21 (with `javac` — install the `-devel` package, not just the JRE)
- Android SDK with platform 35, build-tools 35, and platform-tools
- A Subsonic-compatible server (Subsonic, Navidrome, Airsonic, etc.)

## Installation

### iOS

1. Open `SmashSonic.xcodeproj` in Xcode
2. Select your development team in Signing & Capabilities
3. Connect your iPhone
4. Select your device as the build target
5. Build and run (⌘R)

#### Sideloading Notes

- With a free Apple ID, apps expire after 7 days and must be re-installed
- For longer validity, consider using AltStore
- Max 3 apps can be sideloaded with a free Apple ID

### Android

1. Ensure `ANDROID_HOME` is set and the SDK is installed (see [Android SDK Setup](#android-sdk-setup))
2. Ensure `local.properties` exists in `SmashSonic-Android/` with `sdk.dir=/path/to/your/AndroidSDK`
3. Build the debug APK:
   ```bash
   cd SmashSonic-Android
   ./gradlew assembleDebug
   ```
4. Build the signed release APK:
   ```bash
   ./gradlew assembleRelease
   ```
5. APKs are output to `app/build/outputs/apk/{debug,release}/`

#### Android SDK Setup

Install the Android command-line tools, then:
```bash
sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"
```

Set these environment variables (e.g. in `/etc/profile.d/` or your shell rc):
```bash
export ANDROID_HOME=$HOME/AndroidSDK
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
```

On Fedora/RHEL, make sure you have the full JDK (not just JRE):
```bash
dnf install java-21-openjdk-devel
```

## Configuration

1. Launch the app
2. Enter your Subsonic server URL (e.g., `https://music.example.com`)
3. Enter your username and password
4. Tap "Test Connection" to verify
5. Tap "Save" to store credentials

## Project Structure

### iOS (`SmashSonic/`)

```
SmashSonic/
├── SmashSonicApp.swift         # App entry point
├── Models/                     # Data models
│   ├── Artist.swift
│   ├── Album.swift
│   ├── Song.swift
│   ├── Playlist.swift
│   ├── ServerConfig.swift
│   └── DownloadedSong.swift    # SwiftData model for offline songs
├── Services/
│   ├── SubsonicClient.swift    # API client (async/await)
│   ├── AudioPlayer.swift       # AVPlayer wrapper with lock screen controls
│   ├── DownloadManager.swift   # Background downloads
│   └── KeychainService.swift   # Secure credential storage
├── ViewModels/
│   ├── LibraryViewModel.swift
│   ├── PlayerViewModel.swift
│   ├── SearchViewModel.swift
│   └── DownloadsViewModel.swift
├── Views/
│   ├── ContentView.swift       # Tab navigation
│   ├── Library/
│   │   ├── ArtistsView.swift
│   │   ├── AlbumsView.swift
│   │   ├── AlbumDetailView.swift
│   │   └── SongsView.swift
│   ├── Player/
│   │   ├── NowPlayingView.swift
│   │   └── MiniPlayerView.swift
│   ├── Search/
│   │   └── SearchView.swift
│   ├── Downloads/
│   │   └── DownloadsView.swift
│   └── Settings/
│       └── ServerSetupView.swift
├── Utilities/
│   ├── SubsonicAuth.swift      # Token generation (MD5)
│   └── Extensions.swift
└── Assets.xcassets
```

### Android (`SmashSonic-Android/`)

```
SmashSonic-Android/
├── app/src/main/java/com/smashsonic/
│   ├── SmashSonicApp.kt              # Application + Coil image loader
│   ├── MainActivity.kt               # Single-activity entry point
│   ├── credential/CredentialManager.kt
│   ├── data/
│   │   ├── model/                    # Domain models (Song, Album, Artist, etc.)
│   │   ├── remote/                   # Subsonic API client (Retrofit + OkHttp)
│   │   ├── local/                    # Room database (downloads, liked songs)
│   │   └── repository/              # Repositories
│   ├── di/                           # Hilt dependency injection modules
│   ├── player/SmashSonicPlayer.kt    # Media3 ExoPlayer wrapper
│   ├── service/
│   │   ├── PlaybackService.kt        # Media3 session service
│   │   └── DownloadService.kt        # Background download service
│   └── ui/
│       ├── theme/                    # Material 3 theming
│       ├── navigation/               # Compose Navigation routes
│       ├── home/                     # Home + main tab scaffold
│       ├── browse/                   # Artist/album browsing
│       ├── search/                   # Search screen
│       ├── player/                   # Now playing, mini player, queue
│       ├── likes/                    # Liked songs
│       ├── downloads/                # Offline downloads
│       ├── settings/                 # Server setup + appearance
│       └── components/               # Shared composables
├── gradle/libs.versions.toml         # Version catalog
└── smashsonic-release.jks            # Release signing keystore (do NOT commit)
```

## API Compatibility

Uses the Subsonic API v1.16.1 with token-based authentication:
- `getArtists` - Browse artist index
- `getArtist` - Get artist albums
- `getAlbum` - Get album tracks
- `search3` - Search artists/albums/songs
- `stream` - Stream audio
- `getCoverArt` - Get album/artist artwork
- `download` - Download original files

## License

Personal use only.
