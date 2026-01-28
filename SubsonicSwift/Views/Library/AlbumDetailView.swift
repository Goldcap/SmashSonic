import SwiftUI

struct AlbumDetailView: View {
    let album: Album
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @StateObject private var viewModel = LibraryViewModel()
    @StateObject private var downloadsViewModel = DownloadsViewModel()
    @State private var loadedAlbum: Album?

    var displayAlbum: Album {
        loadedAlbum ?? album
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Album Header
                VStack(spacing: 16) {
                    AsyncImage(url: displayAlbum.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 600) }) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Rectangle()
                            .fill(Color.secondary.opacity(0.2))
                            .overlay {
                                Image(systemName: "music.note")
                                    .font(.system(size: 60))
                                    .foregroundStyle(.secondary)
                            }
                    }
                    .frame(width: 250, height: 250)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .shadow(radius: 10)

                    VStack(spacing: 4) {
                        Text(displayAlbum.name)
                            .font(.title2)
                            .fontWeight(.bold)
                            .multilineTextAlignment(.center)

                        if let artist = displayAlbum.artist {
                            Text(artist)
                                .font(.title3)
                                .foregroundStyle(.secondary)
                        }

                        HStack(spacing: 8) {
                            if let year = displayAlbum.year {
                                Text(String(year))
                            }
                            if let genre = displayAlbum.genre {
                                Text("·")
                                Text(genre)
                            }
                            if let songCount = displayAlbum.songCount {
                                Text("·")
                                Text("\(songCount) tracks")
                            }
                        }
                        .font(.caption)
                        .foregroundStyle(.tertiary)
                    }

                    // Play Button
                    if let songs = displayAlbum.songs, !songs.isEmpty {
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
                if let songs = displayAlbum.songs {
                    Divider()
                    LazyVStack(spacing: 0) {
                        ForEach(songs) { song in
                            SongRow(song: song, songs: songs, showTrackNumber: true, downloadsViewModel: downloadsViewModel)
                            Divider()
                        }
                    }
                }
            }
        }
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            loadedAlbum = await viewModel.loadAlbum(id: album.id)
        }
    }
}

struct SongRow: View {
    let song: Song
    let songs: [Song]
    var showTrackNumber: Bool = false
    @ObservedObject var downloadsViewModel: DownloadsViewModel
    @EnvironmentObject var playerViewModel: PlayerViewModel

    var body: some View {
        HStack(spacing: 12) {
            if showTrackNumber, let track = song.track {
                Text("\(track)")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .frame(width: 24)
            }

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

            if downloadsViewModel.isDownloading(song.id) {
                ProgressView(value: downloadsViewModel.progress(for: song.id))
                    .frame(width: 24)
            } else if downloadsViewModel.isDownloaded(song.id) {
                Image(systemName: "arrow.down.circle.fill")
                    .foregroundStyle(.green)
            }

            Text(song.formattedDuration)
                .font(.caption)
                .foregroundStyle(.secondary)

            Menu {
                Button {
                    playerViewModel.play(song, queue: songs)
                } label: {
                    Label("Play", systemImage: "play")
                }

                if downloadsViewModel.isDownloaded(song.id) {
                    Button(role: .destructive) {
                        downloadsViewModel.deleteDownload(song.id)
                    } label: {
                        Label("Remove Download", systemImage: "trash")
                    }
                } else if !downloadsViewModel.isDownloading(song.id) {
                    Button {
                        downloadsViewModel.download(song)
                    } label: {
                        Label("Download", systemImage: "arrow.down.circle")
                    }
                }
            } label: {
                Image(systemName: "ellipsis")
                    .frame(width: 24, height: 24)
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 10)
        .contentShape(Rectangle())
        .onTapGesture {
            playerViewModel.play(song, queue: songs)
        }
    }
}
