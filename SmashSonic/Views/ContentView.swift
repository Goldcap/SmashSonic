import SwiftUI
import SwiftData

struct ContentView: View {
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @StateObject private var libraryViewModel = LibraryViewModel()
    @StateObject private var searchViewModel = SearchViewModel()
    @StateObject private var downloadsViewModel = DownloadsViewModel()
    @StateObject private var likesViewModel = LikesViewModel()
    @ObservedObject private var client = SubsonicClient.shared

    @State private var selectedTab = 0

    var body: some View {
        ZStack(alignment: .bottom) {
            // Content area
            Group {
                switch selectedTab {
                case 0:
                    HomeView(viewModel: libraryViewModel, likesViewModel: likesViewModel)
                case 1:
                    BrowseView(viewModel: libraryViewModel)
                case 2:
                    LikedSongsView(viewModel: likesViewModel)
                case 3:
                    SearchView(viewModel: searchViewModel)
                case 4:
                    DownloadsView(viewModel: downloadsViewModel)
                case 5:
                    NavigationStack {
                        ServerSetupView()
                    }
                default:
                    HomeView(viewModel: libraryViewModel, likesViewModel: likesViewModel)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            // Mini player (if playing)
            if playerViewModel.currentSong != nil {
                VStack(spacing: 0) {
                    MiniPlayerView()
                        .padding(.bottom, 100)
                }
            }

            // Custom tab bar
            CustomTabBar(selectedTab: $selectedTab, showMiniPlayer: playerViewModel.currentSong != nil)
        }
        .sheet(isPresented: $playerViewModel.showFullPlayer) {
            NowPlayingView()
        }
        .onAppear {
            DownloadManager.shared.setModelContext(modelContext)
            LikesManager.shared.setModelContext(modelContext)
        }
        .fullScreenCover(isPresented: .constant(!client.serverConfig.isConfigured)) {
            NavigationStack {
                ServerSetupView(isInitialSetup: true)
            }
        }
    }

    @Environment(\.modelContext) private var modelContext
}

// MARK: - Custom Tab Bar

struct CustomTabBar: View {
    @Binding var selectedTab: Int
    var showMiniPlayer: Bool

    private let tabs: [(icon: String, systemIcon: String?, label: String)] = [
        ("PixelHome", nil, "Home"),
        ("PixelBrowse", nil, "Browse"),
        ("PixelHeart", "heart.fill", "Liked"),
        ("PixelSearch", nil, "Search"),
        ("PixelDownloads", nil, "Downloads"),
        ("PixelSettings", nil, "Settings")
    ]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(0..<tabs.count, id: \.self) { index in
                Button {
                    selectedTab = index
                } label: {
                    VStack(spacing: 4) {
                        if UIImage(named: tabs[index].icon) != nil {
                            Image(tabs[index].icon)
                                .renderingMode(.original)
                                .resizable()
                                .interpolation(.none)
                                .scaledToFit()
                                .frame(width: 36, height: 36)
                                .opacity(selectedTab == index ? 1.0 : 0.5)
                        } else if let systemIcon = tabs[index].systemIcon {
                            Image(systemName: systemIcon)
                                .font(.system(size: 24))
                                .frame(width: 36, height: 36)
                                .foregroundStyle(selectedTab == index ? .red : .secondary)
                        } else {
                            Image(systemName: "questionmark.circle")
                                .font(.system(size: 24))
                                .frame(width: 36, height: 36)
                                .foregroundStyle(selectedTab == index ? .primary : .secondary)
                        }

                        Text(tabs[index].label)
                            .font(.system(size: 10))
                            .foregroundColor(selectedTab == index ? .primary : .secondary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 8)
        .padding(.bottom, 20)
        .background(
            Rectangle()
                .fill(.ultraThinMaterial)
                .ignoresSafeArea()
        )
    }
}

// MARK: - Home View

struct HomeView: View {
    @ObservedObject var viewModel: LibraryViewModel
    @ObservedObject var likesViewModel: LikesViewModel
    @ObservedObject private var settingsManager = SettingsManager.shared
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \LikedSong.likedAt, order: .reverse) private var likedSongs: [LikedSong]
    @State private var isLoadingRandom = false

    var body: some View {
        NavigationStack {
            ZStack {
                // Background
                backgroundView
                    .ignoresSafeArea()

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
                            // Quick Actions
                            HStack(spacing: 12) {
                                Button {
                                    Task { await playRandomSongs() }
                                } label: {
                                    HStack {
                                        if isLoadingRandom {
                                            ProgressView()
                                                .tint(.primary)
                                        } else {
                                            Image(systemName: "shuffle")
                                        }
                                        Text("Play Random")
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding()
                                    .background(Color.accentColor.opacity(0.2))
                                    .clipShape(RoundedRectangle(cornerRadius: 8))
                                }
                                .disabled(isLoadingRandom)
                            }
                            .padding(.horizontal)

                            // Liked Songs Section
                            if !likedSongs.isEmpty {
                                NavigationLink(destination: LikedSongsView(viewModel: likesViewModel)) {
                                    HStack(spacing: 12) {
                                        Image(systemName: "heart.fill")
                                            .foregroundStyle(.red)
                                            .font(.title2)
                                        VStack(alignment: .leading) {
                                            Text("Liked Songs")
                                                .font(.headline)
                                            Text("\(likedSongs.count) tracks")
                                                .font(.caption)
                                                .foregroundStyle(.secondary)
                                        }
                                        Spacer()
                                        Image(systemName: "chevron.right")
                                            .foregroundStyle(.tertiary)
                                    }
                                    .padding()
                                    .background(Color.black.opacity(0.5))
                                    .clipShape(RoundedRectangle(cornerRadius: 8))
                                }
                                .buttonStyle(.plain)
                                .padding(.horizontal)
                            }

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
            }
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

    @ViewBuilder
    private var backgroundView: some View {
        GeometryReader { geo in
            if let color = settingsManager.backgroundType.solidColor {
                color
            } else if let imageName = settingsManager.backgroundType.imageName {
                Image(imageName)
                    .resizable()
                    .scaledToFill()
                    .frame(width: geo.size.width, height: geo.size.height)
                    .clipped()
                    .opacity(0.3)
            } else {
                Color(.systemBackground)
            }
        }
    }

    private func playRandomSongs() async {
        isLoadingRandom = true
        playerViewModel.startRandomPlayback()
        // Small delay to let the loading start
        try? await Task.sleep(nanoseconds: 500_000_000)
        isLoadingRandom = false
    }
}

// MARK: - Browse View

struct BrowseView: View {
    @ObservedObject var viewModel: LibraryViewModel
    @ObservedObject private var settingsManager = SettingsManager.shared
    @State private var selectedSection = 0

    var body: some View {
        NavigationStack {
            ZStack {
                backgroundView
                    .ignoresSafeArea()

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
            }
            .navigationTitle("Browse")
            .toolbarBackground(.hidden, for: .navigationBar)
        }
    }

    @ViewBuilder
    private var backgroundView: some View {
        GeometryReader { geo in
            if let color = settingsManager.backgroundType.solidColor {
                color
            } else if let imageName = settingsManager.backgroundType.imageName {
                Image(imageName)
                    .resizable()
                    .scaledToFill()
                    .frame(width: geo.size.width, height: geo.size.height)
                    .clipped()
                    .opacity(0.3)
            } else {
                Color(.systemBackground)
            }
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
            .padding(.horizontal, 8)
            .padding(.vertical, 6)
            .frame(width: 150, alignment: .leading)
            .background(Color.black.opacity(0.5))
            .clipShape(RoundedRectangle(cornerRadius: 6))
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

            LazyVStack(spacing: 8) {
                ForEach(playlists) { playlist in
                    NavigationLink(destination: PlaylistDetailView(playlist: playlist, viewModel: viewModel)) {
                        PlaylistRow(playlist: playlist)
                    }
                    .buttonStyle(.plain)
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
        .background(Color.black.opacity(0.5))
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .padding(.horizontal)
    }
}

// MARK: - Playlist Detail View

struct PlaylistDetailView: View {
    let playlist: Playlist
    @ObservedObject var viewModel: LibraryViewModel
    @ObservedObject private var settingsManager = SettingsManager.shared
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @StateObject private var downloadsViewModel = DownloadsViewModel()
    @State private var loadedPlaylist: Playlist?

    var displayPlaylist: Playlist {
        loadedPlaylist ?? playlist
    }

    var body: some View {
        ZStack {
            backgroundView
                .ignoresSafeArea()

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
                                HStack(spacing: 8) {
                                    Image("PixelPlay")
                                        .renderingMode(.original)
                                        .resizable()
                                        .interpolation(.none)
                                        .scaledToFit()
                                        .frame(width: 24, height: 24)
                                    Text("Play")
                                        .font(.headline)
                                }
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                            }
                            .buttonStyle(.borderedProminent)

                            Button {
                                let shuffled = songs.shuffled()
                                playerViewModel.play(shuffled[0], queue: shuffled)
                            } label: {
                                HStack(spacing: 8) {
                                    Image("PixelShuffle")
                                        .renderingMode(.original)
                                        .resizable()
                                        .interpolation(.none)
                                        .scaledToFit()
                                        .frame(width: 24, height: 24)
                                    Text("Shuffle")
                                        .font(.headline)
                                }
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
        }
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

    @ViewBuilder
    private var backgroundView: some View {
        GeometryReader { geo in
            if let color = settingsManager.backgroundType.solidColor {
                color
            } else if let imageName = settingsManager.backgroundType.imageName {
                Image(imageName)
                    .resizable()
                    .scaledToFill()
                    .frame(width: geo.size.width, height: geo.size.height)
                    .clipped()
                    .opacity(0.3)
            } else {
                Color(.systemBackground)
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
        .background(Color.black.opacity(0.5))
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .padding(.horizontal)
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
