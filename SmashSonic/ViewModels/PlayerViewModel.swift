import Foundation
import Combine

@MainActor
final class PlayerViewModel: ObservableObject {
    @Published var currentSong: Song?
    @Published var queue: [Song] = []
    @Published var isPlaying = false
    @Published var currentTime: TimeInterval = 0
    @Published var duration: TimeInterval = 0
    @Published var isLoading = false
    @Published var showFullPlayer = false

    private var cancellables = Set<AnyCancellable>()
    private let audioPlayer = AudioPlayer.shared

    init() {
        audioPlayer.$currentSong
            .receive(on: DispatchQueue.main)
            .assign(to: &$currentSong)

        audioPlayer.$queue
            .receive(on: DispatchQueue.main)
            .assign(to: &$queue)

        audioPlayer.$isPlaying
            .receive(on: DispatchQueue.main)
            .assign(to: &$isPlaying)

        audioPlayer.$currentTime
            .receive(on: DispatchQueue.main)
            .assign(to: &$currentTime)

        audioPlayer.$duration
            .receive(on: DispatchQueue.main)
            .assign(to: &$duration)

        audioPlayer.$isLoading
            .receive(on: DispatchQueue.main)
            .assign(to: &$isLoading)
    }

    func play(_ song: Song, queue: [Song]? = nil) {
        audioPlayer.playSong(song, queue: queue)
    }

    func togglePlayPause() {
        audioPlayer.togglePlayPause()
    }

    func next() {
        audioPlayer.next()
    }

    func previous() {
        audioPlayer.previous()
    }

    func seek(to time: TimeInterval) {
        audioPlayer.seek(to: time)
    }

    func skipForward() {
        audioPlayer.skipForward()
    }

    func skipBackward() {
        audioPlayer.skipBackward()
    }

    var progress: Double {
        guard duration > 0 else { return 0 }
        return currentTime / duration
    }

    var currentTimeFormatted: String {
        formatTime(currentTime)
    }

    var durationFormatted: String {
        formatTime(duration)
    }

    var remainingTimeFormatted: String {
        formatTime(duration - currentTime)
    }

    private func formatTime(_ time: TimeInterval) -> String {
        let minutes = Int(time) / 60
        let seconds = Int(time) % 60
        return String(format: "%d:%02d", minutes, seconds)
    }
}
