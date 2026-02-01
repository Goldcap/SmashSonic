import SwiftUI
import SwiftData

struct ContentView: View {
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @StateObject private var libraryViewModel = LibraryViewModel()
    @StateObject private var searchViewModel = SearchViewModel()
    @StateObject private var downloadsViewModel = DownloadsViewModel()
    @ObservedObject private var client = SubsonicClient.shared

    @State private var selectedTab = 0

    var body: some View {
        ZStack(alignment: .bottom) {
            // Background layer - uses Color.clear to establish size, then overlays the actual background
            Color.clear
                .overlay(BackgroundView())
                .ignoresSafeArea()

            TabView(selection: $selectedTab) {
                HomeView(viewModel: libraryViewModel)
                    .tabItem {
                        Label("Home", systemImage: "house")
                    }
                    .tag(0)

                BrowseView(viewModel: libraryViewModel)
                    .tabItem {
                        Label("Browse", systemImage: "square.grid.2x2")
                    }
                    .tag(1)

                SearchView(viewModel: searchViewModel)
                    .tabItem {
                        Label("Search", systemImage: "magnifyingglass")
                    }
                    .tag(2)

                DownloadsView(viewModel: downloadsViewModel)
                    .tabItem {
                        Label("Downloads", systemImage: "arrow.down.circle")
                    }
                    .tag(3)

                NavigationStack {
                    ServerSetupView()
                }
                    .tabItem {
                        Label("Settings", systemImage: "gear")
                    }
                    .tag(4)
            }
            .scrollContentBackground(.hidden)
            .toolbarBackground(.hidden, for: .tabBar)

            if playerViewModel.currentSong != nil {
                VStack(spacing: 0) {
                    MiniPlayerView()
                        .padding(.bottom, 49)
                }
            }
        }
        .sheet(isPresented: $playerViewModel.showFullPlayer) {
            NowPlayingView()
        }
        .onAppear {
            DownloadManager.shared.setModelContext(modelContext)
        }
        .fullScreenCover(isPresented: .constant(!client.serverConfig.isConfigured)) {
            NavigationStack {
                ServerSetupView(isInitialSetup: true)
            }
        }
    }

    @Environment(\.modelContext) private var modelContext
}

// MARK: - Home View

struct HomeView: View {
    @ObservedObject var viewModel: LibraryViewModel

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    if viewModel.isLoading && viewModel.randomAlbums.isEmpty {
                        HStack {
                            Spacer()
                            ProgressView()
                            Spacer()
                        }
                        .padding(.top, 50)
                    } else if let error = viewModel.error, viewModel.randomAlbums.isEmpty {
                        VStack(spacing: 12) {
                            Image(systemName: "exclamationmark.triangle")
                                .font(.largeTitle)
                                .foregroundStyle(.secondary)
                            Text(error)
                                .foregroundStyle(.secondary)
                            Button("Retry") {
                                Task { await viewModel.loadHomeData() }
                            }
                            .buttonStyle(.bordered)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.top, 50)
                    } else {
                        // Random Albums
                        if !viewModel.randomAlbums.isEmpty {
                            AlbumSection(
                                title: "Random",
                                albums: viewModel.randomAlbums,
                                showRefresh: true,
                                onRefresh: { Task { await viewModel.refreshRandom() } }
                            )
                        }

                        // Recently Added
                        if !viewModel.recentAlbums.isEmpty {
                            AlbumSection(title: "Recently Added", albums: viewModel.recentAlbums)
                        }

                        // Starred
                        if !viewModel.starredAlbums.isEmpty {
                            AlbumSection(title: "Starred", albums: viewModel.starredAlbums)
                        }

                        // Playlists
                        if !viewModel.playlists.isEmpty {
                            PlaylistSection(title: "Playlists", playlists: viewModel.playlists, viewModel: viewModel)
                        }
                    }
                }
                .padding(.vertical)
            }
            .scrollContentBackground(.hidden)
            .background(Color.clear)
            .navigationTitle("Home")
            .toolbarBackground(.hidden, for: .navigationBar)
            .refreshable {
                await viewModel.loadHomeData()
            }
            .task {
                if viewModel.randomAlbums.isEmpty {
                    await viewModel.loadHomeData()
                }
            }
        }
    }
}

// MARK: - Browse View

struct BrowseView: View {
    @ObservedObject var viewModel: LibraryViewModel
    @State private var selectedSection = 0

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Picker("Section", selection: $selectedSection) {
                    Text("Artists").tag(0)
                    Text("Albums").tag(1)
                }
                .pickerStyle(.segmented)
                .padding()

                Group {
                    if selectedSection == 0 {
                        ArtistsView(viewModel: viewModel)
                    } else {
                        AlbumsView(viewModel: viewModel)
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            }
            .background(Color.clear)
            .navigationTitle("Browse")
            .toolbarBackground(.hidden, for: .navigationBar)
        }
    }
}

// MARK: - Album Section

struct AlbumSection: View {
    let title: String
    let albums: [Album]
    var showRefresh: Bool = false
    var onRefresh: (() -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(title)
                    .font(.title2)
                    .fontWeight(.bold)

                Spacer()

                if showRefresh {
                    Button {
                        onRefresh?()
                    } label: {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .padding(.horizontal)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 16) {
                    ForEach(albums) { album in
                        NavigationLink(destination: AlbumDetailView(album: album)) {
                            HomeAlbumCard(album: album)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal)
            }
        }
    }
}

struct HomeAlbumCard: View {
    let album: Album

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            AsyncImage(url: album.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 300) }) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                PlaceholderArtView()
            }
            .frame(width: 150, height: 150)
            .clipShape(RoundedRectangle(cornerRadius: 8))

            VStack(alignment: .leading, spacing: 2) {
                Text(album.name)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .lineLimit(1)

                if let artist = album.artist {
                    Text(artist)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }
        }
        .frame(width: 150)
    }
}

// MARK: - Playlist Section

struct PlaylistSection: View {
    let title: String
    let playlists: [Playlist]
    @ObservedObject var viewModel: LibraryViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.title2)
                .fontWeight(.bold)
                .padding(.horizontal)

            LazyVStack(spacing: 0) {
                ForEach(playlists) { playlist in
                    NavigationLink(destination: PlaylistDetailView(playlist: playlist, viewModel: viewModel)) {
                        PlaylistRow(playlist: playlist)
                    }
                    .buttonStyle(.plain)
                    Divider()
                        .padding(.leading, 76)
                }
            }
        }
    }
}

struct PlaylistRow: View {
    let playlist: Playlist

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: playlist.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 100) }) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                PlaceholderArtView()
            }
            .frame(width: 56, height: 56)
            .clipShape(RoundedRectangle(cornerRadius: 6))

            VStack(alignment: .leading, spacing: 2) {
                Text(playlist.name)
                    .font(.body)
                    .lineLimit(1)

                if let songCount = playlist.songCount {
                    Text("\(songCount) tracks")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundStyle(.tertiary)
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
    }
}

// MARK: - Playlist Detail View

struct PlaylistDetailView: View {
    let playlist: Playlist
    @ObservedObject var viewModel: LibraryViewModel
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @StateObject private var downloadsViewModel = DownloadsViewModel()
    @State private var loadedPlaylist: Playlist?

    var displayPlaylist: Playlist {
        loadedPlaylist ?? playlist
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Header
                VStack(spacing: 16) {
                    AsyncImage(url: displayPlaylist.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 600) }) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        PlaceholderArtView()
                    }
                    .frame(width: 200, height: 200)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .shadow(radius: 10)

                    VStack(spacing: 4) {
                        Text(displayPlaylist.name)
                            .font(.title2)
                            .fontWeight(.bold)
                            .multilineTextAlignment(.center)

                        if let songCount = displayPlaylist.songCount {
                            Text("\(songCount) tracks")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                    }

                    // Play Button
                    if let songs = displayPlaylist.songs, !songs.isEmpty {
                        HStack(spacing: 12) {
                            Button {
                                playerViewModel.play(songs[0], queue: songs)
                            } label: {
                                Label("Play", systemImage: "play.fill")
                                    .font(.headline)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 12)
                            }
                            .buttonStyle(.borderedProminent)

                            Button {
                                let shuffled = songs.shuffled()
                                playerViewModel.play(shuffled[0], queue: shuffled)
                            } label: {
                                Label("Shuffle", systemImage: "shuffle")
                                    .font(.headline)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 12)
                            }
                            .buttonStyle(.bordered)
                        }
                        .padding(.horizontal)
                    }
                }
                .padding()

                // Track List
                if let songs = displayPlaylist.songs {
                    Divider()
                    LazyVStack(spacing: 0) {
                        ForEach(Array(songs.enumerated()), id: \.element.id) { index, song in
                            PlaylistSongRow(song: song, index: index + 1, songs: songs, downloadsViewModel: downloadsViewModel)
                            Divider()
                        }
                    }
                    .padding(.bottom, 80)
                }
            }
        }
        .scrollContentBackground(.hidden)
        .background(Color.clear)
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(.hidden, for: .navigationBar)
        .task {
            loadedPlaylist = await viewModel.loadPlaylist(id: playlist.id)
        }
        .overlay {
            if loadedPlaylist == nil {
                ProgressView()
            }
        }
    }
}

struct PlaylistSongRow: View {
    let song: Song
    let index: Int
    let songs: [Song]
    @ObservedObject var downloadsViewModel: DownloadsViewModel
    @EnvironmentObject var playerViewModel: PlayerViewModel

    var body: some View {
        HStack(spacing: 12) {
            Text("\(index)")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .frame(width: 28)

            AsyncImage(url: song.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 100) }) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                PlaceholderArtView()
            }
            .frame(width: 44, height: 44)
            .clipShape(RoundedRectangle(cornerRadius: 4))

            VStack(alignment: .leading, spacing: 2) {
                Text(song.title)
                    .font(.body)
                    .lineLimit(1)
                    .foregroundColor(playerViewModel.currentSong?.id == song.id ? .accentColor : .primary)

                if let artist = song.artist {
                    Text(artist)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }

            Spacer()

            Text(song.formattedDuration)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
        .contentShape(Rectangle())
        .onTapGesture {
            playerViewModel.play(song, queue: songs)
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(PlayerViewModel())
}
