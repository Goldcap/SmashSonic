import SwiftUI

struct SearchView: View {
    @ObservedObject var viewModel: SearchViewModel
    @ObservedObject private var settingsManager = SettingsManager.shared
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @StateObject private var downloadsViewModel = DownloadsViewModel()
    @StateObject private var likesViewModel = LikesViewModel()
    @FocusState private var isSearchFocused: Bool

    var body: some View {
        NavigationStack {
            ZStack {
                backgroundView
                    .ignoresSafeArea()

                VStack(spacing: 0) {
                    // Search Bar
                    HStack {
                        HStack {
                            Image(systemName: "magnifyingglass")
                                .foregroundStyle(.secondary)

                            TextField("Artists, albums, or songs", text: $viewModel.query)
                                .textFieldStyle(.plain)
                                .autocorrectionDisabled()
                                .textInputAutocapitalization(.never)
                                .focused($isSearchFocused)

                            if !viewModel.query.isEmpty {
                                Button {
                                    viewModel.query = ""
                                } label: {
                                    Image(systemName: "xmark.circle.fill")
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                        .padding(12)
                        .background(Color.secondary.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 10))

                        if isSearchFocused {
                            Button("Cancel") {
                                isSearchFocused = false
                            }
                            .transition(.move(edge: .trailing).combined(with: .opacity))
                        }
                    }
                    .animation(.easeInOut(duration: 0.2), value: isSearchFocused)
                    .padding()

                    // Results
                    if viewModel.isSearching {
                        Spacer()
                        ProgressView("Searching...")
                        Spacer()
                    } else if viewModel.isEmpty {
                        Spacer()
                        ContentUnavailableView.search(text: viewModel.query)
                        Spacer()
                    } else if viewModel.hasResults {
                        ScrollView {
                            LazyVStack(alignment: .leading, spacing: 24) {
                                // Artists
                                if !viewModel.artists.isEmpty {
                                    Section {
                                        ForEach(viewModel.artists) { artist in
                                            NavigationLink(destination: ArtistDetailView(artist: artist, viewModel: LibraryViewModel())) {
                                                SearchArtistRow(artist: artist)
                                            }
                                            .buttonStyle(.plain)
                                        }
                                    } header: {
                                        Text("Artists")
                                            .font(.headline)
                                            .padding(.horizontal)
                                    }
                                }

                                // Albums
                                if !viewModel.albums.isEmpty {
                                    Section {
                                        ScrollView(.horizontal, showsIndicators: false) {
                                            HStack(spacing: 16) {
                                                ForEach(viewModel.albums) { album in
                                                    NavigationLink(destination: AlbumDetailView(album: album)) {
                                                        SearchAlbumCard(album: album)
                                                    }
                                                    .buttonStyle(.plain)
                                                }
                                            }
                                            .padding(.horizontal)
                                        }
                                    } header: {
                                        Text("Albums")
                                            .font(.headline)
                                            .padding(.horizontal)
                                    }
                                }

                                // Songs
                                if !viewModel.songs.isEmpty {
                                    Section {
                                        ForEach(viewModel.songs) { song in
                                            SongRow(song: song, songs: viewModel.songs, downloadsViewModel: downloadsViewModel, likesViewModel: likesViewModel)
                                        }
                                    } header: {
                                        Text("Songs")
                                            .font(.headline)
                                            .padding(.horizontal)
                                    }
                                }
                            }
                            .padding(.vertical)
                        }
                        .scrollDismissesKeyboard(.interactively)
                    } else if !viewModel.hasSearched {
                        VStack(spacing: 12) {
                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 48))
                                .foregroundStyle(.secondary)
                            Text("Search your library")
                                .font(.headline)
                                .foregroundStyle(.secondary)
                        }
                        .padding(.top, 40)
                        Spacer()
                    }
                }
            }
            .navigationTitle("Search")
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

struct SearchArtistRow: View {
    let artist: Artist

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: artist.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 100) }) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Circle()
                    .fill(Color.secondary.opacity(0.2))
                    .overlay {
                        Image(systemName: "person.circle.fill")
                            .font(.title2)
                            .foregroundStyle(.secondary)
                    }
            }
            .frame(width: 50, height: 50)
            .clipShape(Circle())

            VStack(alignment: .leading, spacing: 2) {
                Text(artist.name)
                    .font(.body)

                if let albumCount = artist.albumCount {
                    Text("\(albumCount) album\(albumCount == 1 ? "" : "s")")
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

struct SearchAlbumCard: View {
    let album: Album

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            AsyncImage(url: album.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 200) }) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                PlaceholderArtView()
            }
            .frame(width: 140, height: 140)
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
            .frame(width: 140, alignment: .leading)
            .background(Color.black.opacity(0.5))
            .clipShape(RoundedRectangle(cornerRadius: 6))
        }
        .frame(width: 140)
    }
}

#Preview {
    SearchView(viewModel: SearchViewModel())
        .environmentObject(PlayerViewModel())
}
