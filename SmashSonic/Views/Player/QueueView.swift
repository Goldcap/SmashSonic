import SwiftUI

struct QueueView: View {
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var isLoadingRandom = false

    var body: some View {
        NavigationStack {
            ZStack {
                Color.black.opacity(0.95).ignoresSafeArea()

                if playerViewModel.queue.isEmpty {
                    VStack(spacing: 16) {
                        Image(systemName: "music.note.list")
                            .font(.system(size: 50))
                            .foregroundStyle(.secondary)
                        Text("Queue is Empty")
                            .font(.title2)
                            .fontWeight(.semibold)
                        Text("Start playing music or add random songs")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)

                        Button {
                            startRandomPlayback()
                        } label: {
                            HStack {
                                Image(systemName: "shuffle")
                                Text("Play Random")
                            }
                            .padding(.horizontal, 24)
                            .padding(.vertical, 12)
                            .background(Color.accentColor)
                            .foregroundStyle(.white)
                            .clipShape(Capsule())
                        }
                        .padding(.top, 8)
                    }
                } else {
                    List {
                        // Now Playing
                        if let currentSong = playerViewModel.currentSong {
                            Section {
                                QueueSongRow(
                                    song: currentSong,
                                    isCurrentlyPlaying: true,
                                    isPlaying: playerViewModel.isPlaying
                                )
                                .listRowBackground(Color.accentColor.opacity(0.2))
                            } header: {
                                HStack {
                                    Text("Now Playing")
                                    Spacer()
                                    if playerViewModel.autoAddRandomSongs {
                                        Label("Auto-Add On", systemImage: "infinity")
                                            .font(.caption)
                                            .foregroundStyle(.green)
                                    }
                                }
                            }
                        }

                        // Up Next
                        if !playerViewModel.upcomingSongs.isEmpty {
                            Section("Up Next (\(playerViewModel.upcomingSongs.count))") {
                                ForEach(Array(playerViewModel.upcomingSongs.enumerated()), id: \.element.id) { offset, song in
                                    QueueSongRow(
                                        song: song,
                                        isCurrentlyPlaying: false,
                                        isPlaying: false
                                    )
                                    .listRowBackground(Color.black.opacity(0.5))
                                    .onTapGesture {
                                        playerViewModel.playFromQueue(at: playerViewModel.currentIndex + 1 + offset)
                                    }
                                }
                                .onDelete { indexSet in
                                    for index in indexSet {
                                        let actualIndex = playerViewModel.currentIndex + 1 + index
                                        playerViewModel.removeFromQueue(at: actualIndex)
                                    }
                                }
                            }
                        }

                        // Previously Played
                        if !playerViewModel.playedSongs.isEmpty {
                            Section("Previously Played") {
                                ForEach(Array(playerViewModel.playedSongs.enumerated()), id: \.element.id) { offset, song in
                                    QueueSongRow(
                                        song: song,
                                        isCurrentlyPlaying: false,
                                        isPlaying: false
                                    )
                                    .listRowBackground(Color.black.opacity(0.3))
                                    .opacity(0.6)
                                    .onTapGesture {
                                        playerViewModel.playFromQueue(at: offset)
                                    }
                                }
                            }
                        }
                    }
                    .listStyle(.insetGrouped)
                    .scrollContentBackground(.hidden)
                }
            }
            .navigationTitle("Play Queue")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Done") {
                        dismiss()
                    }
                }

                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Button {
                            startRandomPlayback()
                        } label: {
                            Label("Play Random", systemImage: "shuffle")
                        }

                        Button {
                            playerViewModel.addRandomSongsToQueue(count: 10)
                        } label: {
                            Label("Add 10 Random Songs", systemImage: "plus.circle")
                        }

                        Divider()

                        if playerViewModel.autoAddRandomSongs {
                            Button {
                                AudioPlayer.shared.autoAddRandomSongs = false
                            } label: {
                                Label("Disable Auto-Add", systemImage: "infinity.circle")
                            }
                        } else {
                            Button {
                                AudioPlayer.shared.autoAddRandomSongs = true
                            } label: {
                                Label("Enable Auto-Add Random", systemImage: "infinity.circle")
                            }
                        }

                        if !playerViewModel.queue.isEmpty {
                            Divider()

                            Button(role: .destructive) {
                                playerViewModel.clearQueue()
                            } label: {
                                Label("Clear Queue", systemImage: "trash")
                            }
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
        }
    }

    private func startRandomPlayback() {
        isLoadingRandom = true
        playerViewModel.startRandomPlayback()
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            isLoadingRandom = false
        }
    }
}

struct QueueSongRow: View {
    let song: Song
    let isCurrentlyPlaying: Bool
    let isPlaying: Bool

    var body: some View {
        HStack(spacing: 12) {
            if isCurrentlyPlaying {
                Image(systemName: isPlaying ? "speaker.wave.2.fill" : "speaker.fill")
                    .foregroundStyle(.accentColor)
                    .frame(width: 24)
            }

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
                    .foregroundColor(isCurrentlyPlaying ? .accentColor : .primary)

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
        .contentShape(Rectangle())
    }
}

#Preview {
    QueueView()
        .environmentObject(PlayerViewModel())
}
