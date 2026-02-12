import SwiftUI

struct MiniPlayerView: View {
    @EnvironmentObject var playerViewModel: PlayerViewModel

    var body: some View {
        VStack(spacing: 0) {
            // Progress bar with time display
            HStack(spacing: 8) {
                Text(playerViewModel.currentTimeFormatted)
                    .font(.caption2)
                    .monospacedDigit()

                ProgressView(value: playerViewModel.progress)
                    .progressViewStyle(.linear)
                    .tint(.cyan)
                    .frame(height: 6)

                Text(playerViewModel.durationFormatted)
                    .font(.caption2)
                    .monospacedDigit()

                // DEBUG: show raw values
                Text("d:\(Int(playerViewModel.duration))")
                    .font(.caption2)
                    .foregroundStyle(.red)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)

            HStack(spacing: 12) {
                // Album Art
                AsyncImage(url: playerViewModel.currentSong?.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 100) }) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    PlaceholderArtView()
                }
                .frame(width: 48, height: 48)
                .clipShape(RoundedRectangle(cornerRadius: 4))

                // Track Info
                VStack(alignment: .leading, spacing: 2) {
                    Text(playerViewModel.currentSong?.title ?? "Not Playing")
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .lineLimit(1)

                    Text(playerViewModel.currentSong?.artist ?? "Unknown Artist")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }

                Spacer()

                // Controls
                HStack(spacing: 16) {
                    Button {
                        playerViewModel.rewindToBeginning()
                    } label: {
                        Image(systemName: "backward.fill")
                            .font(.title3)
                    }

                    Button {
                        playerViewModel.togglePlayPause()
                    } label: {
                        if playerViewModel.isLoading {
                            ProgressView()
                        } else {
                            Image(systemName: playerViewModel.isPlaying ? "pause.fill" : "play.fill")
                                .font(.title2)
                        }
                    }

                    Button {
                        playerViewModel.next()
                    } label: {
                        Image(systemName: "forward.fill")
                            .font(.title3)
                    }

                    Button {
                        playerViewModel.cyclePlayMode()
                    } label: {
                        Image(systemName: playerViewModel.playMode.icon)
                            .font(.body)
                    }
                    .foregroundStyle(playerViewModel.playMode == .playOnce ? .secondary : .primary)
                }
                .foregroundStyle(.primary)
            }
            .padding(.horizontal)
            .padding(.vertical, 8)

            // Cyan bottom border
            Rectangle()
                .fill(Color.cyan)
                .frame(height: 2)
        }
        .background(.ultraThinMaterial)
        .onTapGesture {
            playerViewModel.showFullPlayer = true
        }
    }
}

#Preview {
    VStack {
        Spacer()
        MiniPlayerView()
    }
    .environmentObject(PlayerViewModel())
}
