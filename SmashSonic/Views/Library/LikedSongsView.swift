import SwiftUI
import SwiftData

struct LikedSongsView: View {
    @ObservedObject var viewModel: LikesViewModel
    @ObservedObject private var settingsManager = SettingsManager.shared
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \LikedSong.likedAt, order: .reverse) private var likedSongs: [LikedSong]
    @State private var isSyncing = false

    var body: some View {
        NavigationStack {
            ZStack {
                backgroundView
                    .ignoresSafeArea()

                if likedSongs.isEmpty {
                    VStack {
                        Spacer()
                            .frame(height: 100)
                        Image(systemName: "heart")
                            .font(.system(size: 50))
                            .foregroundStyle(.secondary)
                        Text("No Liked Songs")
                            .font(.title2)
                            .fontWeight(.semibold)
                            .padding(.top, 8)
                        Text("Songs you like will appear here.\nPull down to sync from server.")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.top, 2)
                        Spacer()
                    }
                    .frame(maxWidth: .infinity)
                } else {
                    List {
                        Section {
                            ForEach(likedSongs) { likedSong in
                                LikedSongRow(
                                    likedSong: likedSong,
                                    isPlaying: playerViewModel.currentSong?.id == likedSong.id
                                )
                                .listRowBackground(Color.black.opacity(0.5))
                                .onTapGesture {
                                    let songs = likedSongs.map { $0.toSong() }
                                    playerViewModel.play(likedSong.toSong(), queue: songs)
                                }
                            }
                            .onDelete { indexSet in
                                for index in indexSet {
                                    let song = likedSongs[index]
                                    viewModel.unlike(song.id, context: modelContext)
                                }
                            }
                        } header: {
                            HStack {
                                Text("\(likedSongs.count) songs")
                                Spacer()
                            }
                        }
                    }
                    .listStyle(.insetGrouped)
                    .scrollContentBackground(.hidden)
                }
            }
            .navigationTitle("Liked Songs")
            .toolbarBackground(.hidden, for: .navigationBar)
            .refreshable {
                await syncFromServer()
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Button {
                            Task { await syncFromServer() }
                        } label: {
                            Label("Sync from Server", systemImage: "arrow.triangle.2.circlepath")
                        }

                        if !likedSongs.isEmpty {
                            Divider()

                            Button {
                                let songs = likedSongs.map { $0.toSong() }
                                if let first = songs.first {
                                    playerViewModel.play(first, queue: songs)
                                }
                            } label: {
                                Label("Play All", systemImage: "play")
                            }

                            Button {
                                let songs = likedSongs.map { $0.toSong() }.shuffled()
                                if let first = songs.first {
                                    playerViewModel.play(first, queue: songs)
                                }
                            } label: {
                                Label("Shuffle All", systemImage: "shuffle")
                            }
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
        }
        .onAppear {
            LikesManager.shared.setModelContext(modelContext)
            viewModel.loadLikedSongs(context: modelContext)
        }
    }

    private func syncFromServer() async {
        isSyncing = true
        defer { isSyncing = false }

        do {
            let starredSongs = try await SubsonicClient.shared.getStarred()
            let existingIds = Set(likedSongs.map { $0.id })

            for song in starredSongs {
                if !existingIds.contains(song.id) {
                    let likedSong = LikedSong(from: song)
                    modelContext.insert(likedSong)
                }
            }

            try? modelContext.save()
            LikesManager.shared.loadLikedSongs()
            viewModel.loadLikedSongs(context: modelContext)
        } catch {
            print("Failed to sync starred songs: \(error)")
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

struct LikedSongRow: View {
    let likedSong: LikedSong
    let isPlaying: Bool

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: likedSong.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 100) }) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                PlaceholderArtView()
            }
            .frame(width: 50, height: 50)
            .clipShape(RoundedRectangle(cornerRadius: 4))

            VStack(alignment: .leading, spacing: 2) {
                Text(likedSong.title)
                    .font(.body)
                    .lineLimit(1)
                    .foregroundColor(isPlaying ? .accentColor : .primary)

                HStack(spacing: 4) {
                    if let artist = likedSong.artist {
                        Text(artist)
                    }
                    if likedSong.artist != nil && likedSong.album != nil {
                        Text("·")
                    }
                    if let album = likedSong.album {
                        Text(album)
                    }
                }
                .font(.caption)
                .foregroundStyle(.secondary)
                .lineLimit(1)
            }

            Spacer()

            Text(likedSong.formattedDuration)
                .font(.caption)
                .foregroundStyle(.secondary)

            Image(systemName: "heart.fill")
                .foregroundStyle(.red)
        }
        .contentShape(Rectangle())
    }
}

#Preview {
    LikedSongsView(viewModel: LikesViewModel())
        .environmentObject(PlayerViewModel())
}
