import SwiftUI

struct SearchView: View {
    @ObservedObject var viewModel: SearchViewModel
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @StateObject private var downloadsViewModel = DownloadsViewModel()

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Search Bar
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundStyle(.secondary)

                    TextField("Artists, albums, or songs", text: $viewModel.query)
                        .textFieldStyle(.plain)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)

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
                                        SongRow(song: song, songs: viewModel.songs, downloadsViewModel: downloadsViewModel)
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
                } else if !viewModel.hasSearched {
                    Spacer()
                    VStack(spacing: 12) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 48))
                            .foregroundStyle(.secondary)
                        Text("Search your library")
                            .font(.headline)
                            .foregroundStyle(.secondary)
                    }
                    Spacer()
                }
            }
            .navigationTitle("Search")
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
        .padding(.vertical, 4)
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
                Rectangle()
                    .fill(Color.secondary.opacity(0.2))
                    .overlay {
                        Image(systemName: "music.note")
                            .font(.title)
                            .foregroundStyle(.secondary)
                    }
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
        }
        .frame(width: 140)
    }
}

#Preview {
    SearchView(viewModel: SearchViewModel())
        .environmentObject(PlayerViewModel())
}
