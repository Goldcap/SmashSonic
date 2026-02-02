import SwiftUI
import SwiftData

struct NowPlayingView: View {
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @StateObject private var likesViewModel = LikesViewModel()
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @State private var dragOffset: CGFloat = 0

    var body: some View {
        ZStack {
            // Background blur
            if let coverArt = playerViewModel.currentSong?.coverArt {
                AsyncImage(url: SubsonicClient.shared.coverArtURL(for: coverArt, size: 600)) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .blur(radius: 50)
                        .overlay(Color.black.opacity(0.5))
                } placeholder: {
                    Color.black
                }
                .ignoresSafeArea()
            } else {
                Color.black.ignoresSafeArea()
            }

            VStack(spacing: 24) {
                // Drag handle
                Capsule()
                    .fill(Color.white.opacity(0.3))
                    .frame(width: 40, height: 5)
                    .padding(.top, 8)

                Spacer()

                // Album Art
                AsyncImage(url: playerViewModel.currentSong?.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 600) }) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    PlaceholderArtView()
                }
                .frame(width: 300, height: 300)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .shadow(radius: 20)

                Spacer()

                // Track Info
                VStack(spacing: 8) {
                    Text(playerViewModel.currentSong?.title ?? "Not Playing")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundStyle(.white)
                        .lineLimit(1)

                    Text(playerViewModel.currentSong?.artist ?? "Unknown Artist")
                        .font(.title3)
                        .foregroundStyle(.white.opacity(0.7))
                        .lineLimit(1)

                    if let album = playerViewModel.currentSong?.album {
                        Text(album)
                            .font(.subheadline)
                            .foregroundStyle(.white.opacity(0.5))
                            .lineLimit(1)
                    }
                }
                .padding(.horizontal)

                // Like Button
                Button {
                    if let song = playerViewModel.currentSong {
                        likesViewModel.toggleLike(song, context: modelContext)
                    }
                } label: {
                    Image(systemName: likesViewModel.isLiked(playerViewModel.currentSong?.id ?? "") ? "heart.fill" : "heart")
                        .font(.title2)
                        .foregroundStyle(likesViewModel.isLiked(playerViewModel.currentSong?.id ?? "") ? .red : .white)
                }
                .padding(.top, 8)

                // Progress Bar
                VStack(spacing: 8) {
                    Slider(value: Binding(
                        get: { playerViewModel.progress },
                        set: { playerViewModel.seek(to: $0 * playerViewModel.duration) }
                    ))
                    .tint(.white)

                    HStack {
                        Text(playerViewModel.currentTimeFormatted)
                        Spacer()
                        Text("-\(playerViewModel.remainingTimeFormatted)")
                    }
                    .font(.caption)
                    .foregroundStyle(.white.opacity(0.5))
                }
                .padding(.horizontal, 24)

                // Playback Controls
                HStack(spacing: 40) {
                    Button {
                        playerViewModel.previous()
                    } label: {
                        Image(systemName: "backward.fill")
                            .font(.system(size: 32))
                    }

                    Button {
                        playerViewModel.togglePlayPause()
                    } label: {
                        ZStack {
                            Circle()
                                .fill(Color.white)
                                .frame(width: 70, height: 70)

                            if playerViewModel.isLoading {
                                ProgressView()
                                    .tint(.black)
                            } else {
                                Image(systemName: playerViewModel.isPlaying ? "pause.fill" : "play.fill")
                                    .font(.system(size: 30))
                                    .foregroundStyle(.black)
                            }
                        }
                    }

                    Button {
                        playerViewModel.next()
                    } label: {
                        Image(systemName: "forward.fill")
                            .font(.system(size: 32))
                    }
                }
                .foregroundStyle(.white)

                // Skip Controls
                HStack(spacing: 60) {
                    Button {
                        playerViewModel.skipBackward()
                    } label: {
                        Image(systemName: "gobackward.15")
                            .font(.system(size: 24))
                    }

                    Button {
                        playerViewModel.skipForward()
                    } label: {
                        Image(systemName: "goforward.15")
                            .font(.system(size: 24))
                    }
                }
                .foregroundStyle(.white.opacity(0.7))
                .padding(.top, 8)

                Spacer()
            }
            .padding()
        }
        .gesture(
            DragGesture()
                .onChanged { value in
                    if value.translation.height > 0 {
                        dragOffset = value.translation.height
                    }
                }
                .onEnded { value in
                    if value.translation.height > 100 {
                        dismiss()
                    }
                    dragOffset = 0
                }
        )
        .offset(y: dragOffset)
        .animation(.interactiveSpring(), value: dragOffset)
    }
}

#Preview {
    NowPlayingView()
        .environmentObject(PlayerViewModel())
}
