import SwiftUI
import SwiftData

struct DownloadsView: View {
    @ObservedObject var viewModel: DownloadsViewModel
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \DownloadedSong.downloadedAt, order: .reverse) private var downloadedSongs: [DownloadedSong]

    var body: some View {
        NavigationStack {
            Group {
                if downloadedSongs.isEmpty && viewModel.activeDownloads.isEmpty {
                    ContentUnavailableView(
                        "No Downloads",
                        systemImage: "arrow.down.circle",
                        description: Text("Downloaded songs will appear here for offline listening")
                    )
                } else {
                    List {
                        // Active Downloads
                        if !viewModel.activeDownloads.isEmpty {
                            Section("Downloading") {
                                ForEach(Array(viewModel.activeDownloads.values), id: \.song.id) { download in
                                    DownloadingRow(
                                        song: download.song,
                                        progress: viewModel.progress(for: download.song.id),
                                        onCancel: { viewModel.cancelDownload(download.song.id) }
                                    )
                                }
                            }
                        }

                        // Downloaded Songs
                        if !downloadedSongs.isEmpty {
                            Section {
                                ForEach(downloadedSongs) { downloaded in
                                    DownloadedSongRow(
                                        downloaded: downloaded,
                                        isPlaying: playerViewModel.currentSong?.id == downloaded.id
                                    )
                                    .onTapGesture {
                                        let songs = downloadedSongs.map { $0.toSong() }
                                        playerViewModel.play(downloaded.toSong(), queue: songs)
                                    }
                                }
                                .onDelete { indexSet in
                                    for index in indexSet {
                                        let song = downloadedSongs[index]
                                        viewModel.deleteDownload(song.id, context: modelContext)
                                    }
                                }
                            } header: {
                                HStack {
                                    Text("Downloaded")
                                    Spacer()
                                    Text(totalSizeFormatted)
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                    }
                    .listStyle(.insetGrouped)
                    .scrollContentBackground(.hidden)
                }
            }
            .background(Color.clear)
            .navigationTitle("Downloads")
            .toolbarBackground(.hidden, for: .navigationBar)
            .toolbar {
                if !downloadedSongs.isEmpty {
                    ToolbarItem(placement: .topBarTrailing) {
                        Menu {
                            Button {
                                let songs = downloadedSongs.map { $0.toSong() }
                                if let first = songs.first {
                                    playerViewModel.play(first, queue: songs)
                                }
                            } label: {
                                Label("Play All", systemImage: "play")
                            }

                            Button {
                                let songs = downloadedSongs.map { $0.toSong() }.shuffled()
                                if let first = songs.first {
                                    playerViewModel.play(first, queue: songs)
                                }
                            } label: {
                                Label("Shuffle All", systemImage: "shuffle")
                            }
                        } label: {
                            Image(systemName: "ellipsis.circle")
                        }
                    }
                }
            }
        }
        .onAppear {
            DownloadManager.shared.setModelContext(modelContext)
        }
    }

    var totalSizeFormatted: String {
        let total = downloadedSongs.compactMap { $0.fileSize }.reduce(0, +)
        return total.formattedFileSize
    }
}

struct DownloadingRow: View {
    let song: Song
    let progress: Double
    let onCancel: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: song.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 100) }) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                PlaceholderArtView()
            }
            .frame(width: 50, height: 50)
            .clipShape(RoundedRectangle(cornerRadius: 4))

            VStack(alignment: .leading, spacing: 4) {
                Text(song.title)
                    .font(.body)
                    .lineLimit(1)

                ProgressView(value: progress)
                    .tint(.accentColor)
            }

            Button(action: onCancel) {
                Image(systemName: "xmark.circle.fill")
                    .foregroundStyle(.secondary)
            }
            .buttonStyle(.plain)
        }
    }
}

struct DownloadedSongRow: View {
    let downloaded: DownloadedSong
    let isPlaying: Bool

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: downloaded.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 100) }) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                PlaceholderArtView()
            }
            .frame(width: 50, height: 50)
            .clipShape(RoundedRectangle(cornerRadius: 4))

            VStack(alignment: .leading, spacing: 2) {
                Text(downloaded.title)
                    .font(.body)
                    .lineLimit(1)
                    .foregroundColor(isPlaying ? .accentColor : .primary)

                HStack(spacing: 4) {
                    if let artist = downloaded.artist {
                        Text(artist)
                    }
                    if downloaded.artist != nil && downloaded.album != nil {
                        Text("·")
                    }
                    if let album = downloaded.album {
                        Text(album)
                    }
                }
                .font(.caption)
                .foregroundStyle(.secondary)
                .lineLimit(1)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text(downloaded.formattedDuration)
                    .font(.caption)
                    .foregroundStyle(.secondary)

                if let size = downloaded.fileSize {
                    Text(size.formattedFileSize)
                        .font(.caption2)
                        .foregroundStyle(.tertiary)
                }
            }
        }
        .contentShape(Rectangle())
    }
}

#Preview {
    DownloadsView(viewModel: DownloadsViewModel())
        .environmentObject(PlayerViewModel())
}
