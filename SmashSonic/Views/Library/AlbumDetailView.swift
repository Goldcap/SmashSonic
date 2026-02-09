import SwiftUI
import SwiftData

struct AlbumDetailView: View {
    let album: Album
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @ObservedObject private var settingsManager = SettingsManager.shared
    @StateObject private var viewModel = LibraryViewModel()
    @StateObject private var downloadsViewModel = DownloadsViewModel()
    @StateObject private var likesViewModel = LikesViewModel()
    @State private var loadedAlbum: Album?

    var displayAlbum: Album {
        loadedAlbum ?? album
    }

    var sortedSongs: [Song] {
        displayAlbum.songs?.sorted { song1, song2 in
            // Sort by track number first (nil values go last)
            switch (song1.track, song2.track) {
            case let (track1?, track2?):
                if track1 != track2 {
                    return track1 < track2
                }
            case (nil, _?):
                return false
            case (_?, nil):
                return true
            case (nil, nil):
                break
            }
            // Then sort by title
            return song1.title.localizedCaseInsensitiveCompare(song2.title) == .orderedAscending
        } ?? []
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
                        PlaceholderArtView()
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

                    // Play/Shuffle/Download Buttons
                    if !sortedSongs.isEmpty {
                        HStack(spacing: 12) {
                            Button {
                                playerViewModel.play(sortedSongs[0], queue: sortedSongs)
                            } label: {
                                Image("PixelPlayButton")
                                    .renderingMode(.original)
                                    .resizable()
                                    .interpolation(.none)
                                    .scaledToFit()
                            }
                            .buttonStyle(.plain)

                            Button {
                                let shuffled = sortedSongs.shuffled()
                                playerViewModel.play(shuffled[0], queue: shuffled)
                            } label: {
                                Image("PixelShuffleButton")
                                    .renderingMode(.original)
                                    .resizable()
                                    .interpolation(.none)
                                    .scaledToFit()
                            }
                            .buttonStyle(.plain)

                            Button {
                                for song in sortedSongs {
                                    if !downloadsViewModel.isDownloaded(song.id) && !downloadsViewModel.isDownloading(song.id) {
                                        downloadsViewModel.download(song)
                                    }
                                }
                            } label: {
                                Image("PixelDownloadButton")
                                    .renderingMode(.original)
                                    .resizable()
                                    .interpolation(.none)
                                    .scaledToFit()
                            }
                            .buttonStyle(.plain)
                        }
                        .frame(height: 44)
                        .padding(.horizontal)
                    }
                }
                .padding()

                // Track List
                if !sortedSongs.isEmpty {
                    Divider()
                    LazyVStack(spacing: 0) {
                        ForEach(sortedSongs) { song in
                            SongRow(song: song, songs: sortedSongs, showTrackNumber: true, downloadsViewModel: downloadsViewModel, likesViewModel: likesViewModel)
                            Divider()
                        }
                    }
                    .padding(.bottom, 80)
                }
            }
        }
        .scrollContentBackground(.hidden)
        .background(backgroundView.ignoresSafeArea())
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        .task {
            loadedAlbum = await viewModel.loadAlbum(id: album.id)
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

struct SongRow: View {
    let song: Song
    let songs: [Song]
    var showTrackNumber: Bool = false
    @ObservedObject var downloadsViewModel: DownloadsViewModel
    @ObservedObject var likesViewModel: LikesViewModel
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @Environment(\.modelContext) private var modelContext

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

                Divider()

                Button {
                    playerViewModel.playNow(song)
                } label: {
                    Label("Play Now", systemImage: "play.circle")
                }

                Button {
                    playerViewModel.playNext(song)
                } label: {
                    Label("Play Next", systemImage: "text.line.first.and.arrowtriangle.forward")
                }

                Button {
                    playerViewModel.playLast(song)
                } label: {
                    Label("Play Last", systemImage: "text.line.last.and.arrowtriangle.forward")
                }

                Divider()

                Button {
                    likesViewModel.toggleLike(song, context: modelContext)
                } label: {
                    Label(likesViewModel.isLiked(song.id) ? "Unlike" : "Like",
                          systemImage: likesViewModel.isLiked(song.id) ? "heart.fill" : "heart")
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
        .background(Color.black.opacity(0.5))
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .padding(.horizontal)
        .contentShape(Rectangle())
        .onTapGesture {
            playerViewModel.play(song, queue: songs)
        }
    }
}
