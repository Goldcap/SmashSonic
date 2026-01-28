import SwiftUI

struct ArtistsView: View {
    @ObservedObject var viewModel: LibraryViewModel

    var body: some View {
        Group {
            if viewModel.isLoading && viewModel.artists.isEmpty {
                VStack {
                    ProgressView("Loading artists...")
                    Spacer()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
                .padding(.top, 50)
            } else if let error = viewModel.error, viewModel.artists.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)
                    Text(error)
                        .foregroundStyle(.secondary)
                    Button("Retry") {
                        Task { await viewModel.loadArtists() }
                    }
                    .buttonStyle(.bordered)
                    Spacer()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
                .padding(.top, 50)
            } else if viewModel.artists.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "music.mic")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)
                    Text("No Artists")
                        .font(.headline)
                    Text("Your library is empty")
                        .foregroundStyle(.secondary)
                    Spacer()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
                .padding(.top, 50)
            } else {
                List(viewModel.artists) { artist in
                    NavigationLink(destination: ArtistDetailView(artist: artist, viewModel: viewModel)) {
                        ArtistRow(artist: artist)
                    }
                }
                .listStyle(.plain)
                .refreshable {
                    await viewModel.loadArtists()
                }
            }
        }
        .task {
            if viewModel.artists.isEmpty {
                await viewModel.loadArtists()
            }
        }
    }
}

struct ArtistRow: View {
    let artist: Artist

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: artist.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 100) }) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Image(systemName: "person.circle.fill")
                    .font(.system(size: 24))
                    .foregroundStyle(.secondary)
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
        }
        .padding(.vertical, 4)
    }
}

struct ArtistDetailView: View {
    let artist: Artist
    @ObservedObject var viewModel: LibraryViewModel
    @State private var artistDetail: ArtistDetail?

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                if let albums = artistDetail?.albums {
                    ForEach(albums) { album in
                        NavigationLink(destination: AlbumDetailView(album: album)) {
                            AlbumRow(album: album)
                        }
                        .buttonStyle(.plain)
                        Divider()
                    }
                }
            }
        }
        .navigationTitle(artist.name)
        .task {
            artistDetail = await viewModel.loadArtist(id: artist.id)
        }
        .overlay {
            if artistDetail == nil {
                ProgressView()
            }
        }
    }
}
