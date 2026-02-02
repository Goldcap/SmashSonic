import Foundation
import SwiftData
import Combine

final class LikesManager: ObservableObject {
    static let shared = LikesManager()

    @Published var likedSongIds: Set<String> = []

    private var modelContext: ModelContext?

    private init() {}

    func setModelContext(_ context: ModelContext) {
        self.modelContext = context
        loadLikedSongs()
    }

    func isLiked(_ songId: String) -> Bool {
        likedSongIds.contains(songId)
    }

    func loadLikedSongs() {
        guard let context = modelContext else { return }

        let descriptor = FetchDescriptor<LikedSong>()
        if let songs = try? context.fetch(descriptor) {
            DispatchQueue.main.async { [weak self] in
                self?.likedSongIds = Set(songs.map { $0.id })
            }
        }
    }

    func toggleLike(_ song: Song) async {
        guard let context = modelContext else { return }

        if isLiked(song.id) {
            await unlike(song, context: context)
        } else {
            await like(song, context: context)
        }
    }

    private func like(_ song: Song, context: ModelContext) async {
        // Add to local database
        let likedSong = LikedSong(from: song)
        context.insert(likedSong)
        try? context.save()

        DispatchQueue.main.async { [weak self] in
            self?.likedSongIds.insert(song.id)
        }

        // Sync with server
        do {
            try await SubsonicClient.shared.star(songId: song.id)
        } catch {
            print("Failed to star song on server: \(error)")
        }
    }

    private func unlike(_ song: Song, context: ModelContext) async {
        // Remove from local database
        let descriptor = FetchDescriptor<LikedSong>(
            predicate: #Predicate { $0.id == song.id }
        )
        if let likedSong = try? context.fetch(descriptor).first {
            context.delete(likedSong)
            try? context.save()
        }

        DispatchQueue.main.async { [weak self] in
            self?.likedSongIds.remove(song.id)
        }

        // Sync with server
        do {
            try await SubsonicClient.shared.unstar(songId: song.id)
        } catch {
            print("Failed to unstar song on server: \(error)")
        }
    }

    func unlikeSong(id songId: String, context: ModelContext) async {
        let descriptor = FetchDescriptor<LikedSong>(
            predicate: #Predicate { $0.id == songId }
        )
        if let likedSong = try? context.fetch(descriptor).first {
            context.delete(likedSong)
            try? context.save()
        }

        DispatchQueue.main.async { [weak self] in
            self?.likedSongIds.remove(songId)
        }

        do {
            try await SubsonicClient.shared.unstar(songId: songId)
        } catch {
            print("Failed to unstar song on server: \(error)")
        }
    }
}
