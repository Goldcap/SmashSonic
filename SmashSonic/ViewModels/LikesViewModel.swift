import Foundation
import SwiftData
import Combine

@MainActor
final class LikesViewModel: ObservableObject {
    @Published var likedSongs: [LikedSong] = []
    @Published var likedSongIds: Set<String> = []

    private var cancellables = Set<AnyCancellable>()
    private let likesManager = LikesManager.shared

    init() {
        likesManager.$likedSongIds
            .receive(on: DispatchQueue.main)
            .assign(to: &$likedSongIds)
    }

    func loadLikedSongs(context: ModelContext) {
        let descriptor = FetchDescriptor<LikedSong>(
            sortBy: [SortDescriptor(\.likedAt, order: .reverse)]
        )
        likedSongs = (try? context.fetch(descriptor)) ?? []
    }

    func toggleLike(_ song: Song, context: ModelContext) {
        Task {
            await likesManager.toggleLike(song)
            loadLikedSongs(context: context)
        }
    }

    func isLiked(_ songId: String) -> Bool {
        likesManager.isLiked(songId)
    }

    func unlike(_ songId: String, context: ModelContext) {
        Task {
            await likesManager.unlikeSong(id: songId, context: context)
            loadLikedSongs(context: context)
        }
    }
}
