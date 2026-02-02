import SwiftUI
import SwiftData

struct LikedSongsView: View {
    @ObservedObject var viewModel: LikesViewModel
    @ObservedObject private var settingsManager = SettingsManager.shared
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \LikedSong.likedAt, order: .reverse) private var likedSongs: [LikedSong]

    var body: some View {
        NavigationStack {
            ZStack {
                backgroundView
                    .ignoresSafeArea()

                Group {
                    if likedSongs.isEmpty {
                        ContentUnavailableView(
                            "No Liked Songs",
                            systemImage: "heart",
                            description: Text("Songs you like will appear here")
                        )
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
            }
            .navigationTitle("Liked Songs")
            .toolbarBackground(.hidden, for: .navigationBar)
            .toolbar {
                if !likedSongs.isEmpty {
                    ToolbarItem(placement: .topBarTrailing) {
                        Menu {
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
                        } label: {
                            Image(systemName: "ellipsis.circle")
                        }
                    }
                }
            }
        }
        .onAppear {
            LikesManager.shared.setModelContext(modelContext)
            viewModel.loadLikedSongs(context: modelContext)
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
