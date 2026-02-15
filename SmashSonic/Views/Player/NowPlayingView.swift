import SwiftUI
import SwiftData

struct NowPlayingView: View {
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @StateObject private var likesViewModel = LikesViewModel()
    @Environment(\.modelContext) private var modelContext

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

            VStack(spacing: 16) {
                // Drag handle
                Capsule()
                    .fill(Color.white.opacity(0.3))
                    .frame(width: 40, height: 5)
                    .padding(.top, 8)

                // Album Art
                AsyncImage(url: playerViewModel.currentSong?.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 600) }) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    PlaceholderArtView()
                }
                .frame(width: 280, height: 280)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .shadow(radius: 20)

                // Track Info
                VStack(spacing: 4) {
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
                    Image(likesViewModel.isLiked(playerViewModel.currentSong?.id ?? "") ? "PixelHeart" : "PixelHeartEmpty")
                        .renderingMode(.original)
                        .resizable()
                        .interpolation(.none)
                        .scaledToFit()
                        .frame(width: 32, height: 32)
                }

                // Progress Bar
                HStack(spacing: 10) {
                    Text(playerViewModel.currentTimeFormatted)
                        .font(.caption)
                        .monospacedDigit()
                        .foregroundColor(.white)

                    // Custom progress bar using GeometryReader
                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            // Background track
                            Capsule()
                                .fill(Color.white.opacity(0.3))

                            // Progress fill
                            Capsule()
                                .fill(Color.cyan)
                                .frame(width: geo.size.width * playerViewModel.progress)
                        }
                    }
                    .frame(height: 6)

                    Text(playerViewModel.durationFormatted)
                        .font(.caption)
                        .monospacedDigit()
                        .foregroundColor(.white)
                }
                .padding(.horizontal, 48)
                .padding(.vertical, 10)

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

                // Secondary Controls
                HStack(spacing: 24) {
                    // Play Mode Button
                    Button {
                        playerViewModel.cyclePlayMode()
                    } label: {
                        VStack(spacing: 2) {
                            Image(systemName: playerViewModel.playMode.icon)
                                .font(.system(size: 20))
                            Text(playerViewModel.playMode.rawValue)
                                .font(.system(size: 9))
                        }
                        .frame(width: 50)
                    }
                    .foregroundStyle(playerViewModel.playMode == .playOnce ? .white.opacity(0.7) : .white)

                    Button {
                        playerViewModel.skipBackward()
                    } label: {
                        Image(systemName: "gobackward.15")
                            .font(.system(size: 22))
                    }

                    Button {
                        playerViewModel.showQueue = true
                    } label: {
                        VStack(spacing: 2) {
                            Image(systemName: "list.bullet")
                                .font(.system(size: 22))
                            if playerViewModel.upcomingSongs.count > 0 {
                                Text("\(playerViewModel.upcomingSongs.count)")
                                    .font(.caption2)
                            }
                        }
                    }

                    Button {
                        playerViewModel.skipForward()
                    } label: {
                        Image(systemName: "goforward.15")
                            .font(.system(size: 22))
                    }

                    // Rewind to Beginning Button
                    Button {
                        playerViewModel.rewindToBeginning()
                    } label: {
                        VStack(spacing: 2) {
                            Image(systemName: "backward.fill")
                                .font(.system(size: 20))
                            Text("Rewind")
                                .font(.system(size: 9))
                        }
                        .frame(width: 50)
                    }
                }
                .foregroundStyle(.white.opacity(0.7))

                Spacer()
            }
            .padding()
        }
        .presentationDragIndicator(.hidden)
    }
}

#Preview {
    NowPlayingView()
        .environmentObject(PlayerViewModel())
}
