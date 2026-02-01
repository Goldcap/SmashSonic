# SmashSonic

A native SwiftUI iOS app for streaming music from a Subsonic server.

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

- iOS 17.0+
- Xcode 15.0+
- A Subsonic-compatible server (Subsonic, Navidrome, Airsonic, etc.)

## Installation

1. Open `SmashSonic.xcodeproj` in Xcode
2. Select your development team in Signing & Capabilities
3. Connect your iPhone
4. Select your device as the build target
5. Build and run (⌘R)

### Sideloading Notes

- With a free Apple ID, apps expire after 7 days and must be re-installed
- For longer validity, consider using AltStore
- Max 3 apps can be sideloaded with a free Apple ID

## Configuration

1. Launch the app
2. Enter your Subsonic server URL (e.g., `https://music.example.com`)
3. Enter your username and password
4. Tap "Test Connection" to verify
5. Tap "Save" to store credentials

## Project Structure

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
