import Foundation
import SwiftData
import Combine

final class LikesManager: ObservableObject {
    static let shared = LikesManager()

    @Published var likedSongIds: Set<String> = []
    @Published var starredAlbumIds: Set<String> = []
    @Published var starredArtistIds: Set<String> = []

    private var modelContext: ModelContext?

    private init() {}

    func setModelContext(_ context: ModelContext) {
        self.modelContext = context
        loadLikedSongs()
        loadStarredFromServer()
    }

    func isLiked(_ songId: String) -> Bool {
        likedSongIds.contains(songId)
    }

    func isAlbumStarred(_ albumId: String) -> Bool {
        starredAlbumIds.contains(albumId)
    }

    func isArtistStarred(_ artistId: String) -> Bool {
        starredArtistIds.contains(artistId)
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

    // MARK: - Album & Artist Favorites

    /// Loads the set of starred albums and artists from the server so favorite
    /// state survives across launches. Songs continue to be backed locally by
    /// SwiftData; albums/artists rely on the server as the source of truth.
    func loadStarredFromServer() {
        Task {
            guard let result = try? await SubsonicClient.shared.getStarredIDs() else { return }
            DispatchQueue.main.async { [weak self] in
                self?.starredArtistIds = result.artists
                self?.starredAlbumIds = result.albums
            }
        }
    }

    func toggleAlbumStar(_ album: Album) async {
        let id = album.id
        let wasStarred = starredAlbumIds.contains(id)

        DispatchQueue.main.async { [weak self] in
            if wasStarred {
                self?.starredAlbumIds.remove(id)
            } else {
                self?.starredAlbumIds.insert(id)
            }
        }

        do {
            if wasStarred {
                try await SubsonicClient.shared.unstar(albumId: id)
            } else {
                try await SubsonicClient.shared.star(albumId: id)
            }
        } catch {
            print("Failed to update album star on server: \(error)")
        }
    }

    func toggleArtistStar(_ artist: Artist) async {
        let id = artist.id
        let wasStarred = starredArtistIds.contains(id)

        DispatchQueue.main.async { [weak self] in
            if wasStarred {
                self?.starredArtistIds.remove(id)
            } else {
                self?.starredArtistIds.insert(id)
            }
        }

        do {
            if wasStarred {
                try await SubsonicClient.shared.unstar(artistId: id)
            } else {
                try await SubsonicClient.shared.star(artistId: id)
            }
        } catch {
            print("Failed to update artist star on server: \(error)")
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
        let songId = song.id
        let descriptor = FetchDescriptor<LikedSong>(
            predicate: #Predicate { $0.id == songId }
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
