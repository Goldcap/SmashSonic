import Foundation
import AVFoundation
import MediaPlayer
import Combine

final class AudioPlayer: ObservableObject {
    static let shared = AudioPlayer()

    @Published var currentSong: Song?
    @Published var queue: [Song] = []
    @Published var currentIndex: Int = 0
    @Published var isPlaying = false
    @Published var currentTime: TimeInterval = 0
    @Published var duration: TimeInterval = 0
    @Published var isLoading = false

    private var player: AVPlayer?
    private var timeObserver: Any?
    private var cancellables = Set<AnyCancellable>()

    private init() {
        setupAudioSession()
        setupRemoteCommands()
    }

    private func setupAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Failed to setup audio session: \(error)")
        }
    }

    private func setupRemoteCommands() {
        let commandCenter = MPRemoteCommandCenter.shared()

        commandCenter.playCommand.addTarget { [weak self] _ in
            self?.play()
            return .success
        }

        commandCenter.pauseCommand.addTarget { [weak self] _ in
            self?.pause()
            return .success
        }

        commandCenter.nextTrackCommand.addTarget { [weak self] _ in
            self?.next()
            return .success
        }

        commandCenter.previousTrackCommand.addTarget { [weak self] _ in
            self?.previous()
            return .success
        }

        commandCenter.changePlaybackPositionCommand.addTarget { [weak self] event in
            guard let event = event as? MPChangePlaybackPositionCommandEvent else { return .commandFailed }
            self?.seek(to: event.positionTime)
            return .success
        }
    }

    func playSong(_ song: Song, queue: [Song]? = nil) {
        if let queue = queue, let index = queue.firstIndex(where: { $0.id == song.id }) {
            self.queue = queue
            self.currentIndex = index
        } else {
            self.queue = [song]
            self.currentIndex = 0
        }

        loadAndPlay(song)
    }

    private func loadAndPlay(_ song: Song) {
        isLoading = true
        currentSong = song

        // Check for local file first
        if let localURL = DownloadManager.shared.localURL(for: song.id) {
            setupPlayer(with: localURL)
        } else if let streamURL = SubsonicClient.shared.streamURL(for: song.id) {
            setupPlayer(with: streamURL)
        } else {
            isLoading = false
            return
        }
    }

    private func setupPlayer(with url: URL) {
        if let observer = timeObserver {
            player?.removeTimeObserver(observer)
        }

        let playerItem = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: playerItem)

        NotificationCenter.default.publisher(for: .AVPlayerItemDidPlayToEndTime, object: playerItem)
            .sink { [weak self] _ in
                self?.handlePlaybackEnded()
            }
            .store(in: &cancellables)

        playerItem.publisher(for: \.status)
            .receive(on: DispatchQueue.main)
            .sink { [weak self] status in
                if status == .readyToPlay {
                    self?.isLoading = false
                    self?.duration = playerItem.duration.seconds.isFinite ? playerItem.duration.seconds : 0
                    self?.player?.play()
                    self?.isPlaying = true
                    self?.updateNowPlayingInfo()
                }
            }
            .store(in: &cancellables)

        timeObserver = player?.addPeriodicTimeObserver(
            forInterval: CMTime(seconds: 0.5, preferredTimescale: 600),
            queue: .main
        ) { [weak self] time in
            self?.currentTime = time.seconds.isFinite ? time.seconds : 0
            self?.updateNowPlayingProgress()
        }
    }

    private func handlePlaybackEnded() {
        if currentIndex < queue.count - 1 {
            next()
        } else {
            isPlaying = false
            currentTime = 0
        }
    }

    func play() {
        player?.play()
        isPlaying = true
        updateNowPlayingInfo()
    }

    func pause() {
        player?.pause()
        isPlaying = false
        updateNowPlayingInfo()
    }

    func togglePlayPause() {
        if isPlaying {
            pause()
        } else {
            play()
        }
    }

    func next() {
        guard currentIndex < queue.count - 1 else { return }
        currentIndex += 1
        loadAndPlay(queue[currentIndex])
    }

    func previous() {
        if currentTime > 3 {
            seek(to: 0)
        } else if currentIndex > 0 {
            currentIndex -= 1
            loadAndPlay(queue[currentIndex])
        } else {
            seek(to: 0)
        }
    }

    func seek(to time: TimeInterval) {
        player?.seek(to: CMTime(seconds: time, preferredTimescale: 600))
        currentTime = time
        updateNowPlayingProgress()
    }

    func skipForward(_ seconds: TimeInterval = 15) {
        let newTime = min(currentTime + seconds, duration)
        seek(to: newTime)
    }

    func skipBackward(_ seconds: TimeInterval = 15) {
        let newTime = max(currentTime - seconds, 0)
        seek(to: newTime)
    }

    private func updateNowPlayingInfo() {
        guard let song = currentSong else {
            MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
            return
        }

        var info: [String: Any] = [
            MPMediaItemPropertyTitle: song.title,
            MPMediaItemPropertyPlaybackDuration: duration,
            MPNowPlayingInfoPropertyElapsedPlaybackTime: currentTime,
            MPNowPlayingInfoPropertyPlaybackRate: isPlaying ? 1.0 : 0.0
        ]

        if let artist = song.artist {
            info[MPMediaItemPropertyArtist] = artist
        }

        if let album = song.album {
            info[MPMediaItemPropertyAlbumTitle] = album
        }

        if let coverArt = song.coverArt,
           let coverURL = SubsonicClient.shared.coverArtURL(for: coverArt, size: 600) {
            Task {
                if let (data, _) = try? await URLSession.shared.data(from: coverURL),
                   let image = UIImage(data: data) {
                    await MainActor.run {
                        var updatedInfo = MPNowPlayingInfoCenter.default().nowPlayingInfo ?? [:]
                        updatedInfo[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: image.size) { _ in image }
                        MPNowPlayingInfoCenter.default().nowPlayingInfo = updatedInfo
                    }
                }
            }
        }

        MPNowPlayingInfoCenter.default().nowPlayingInfo = info
    }

    private func updateNowPlayingProgress() {
        guard var info = MPNowPlayingInfoCenter.default().nowPlayingInfo else { return }
        info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentTime
        info[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        MPNowPlayingInfoCenter.default().nowPlayingInfo = info
    }
}
