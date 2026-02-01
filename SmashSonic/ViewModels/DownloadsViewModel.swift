import Foundation
import SwiftData
import Combine

@MainActor
final class DownloadsViewModel: ObservableObject {
    @Published var downloadedSongs: [DownloadedSong] = []
    @Published var activeDownloads: [String: DownloadManager.DownloadTask] = [:]
    @Published var downloadProgress: [String: Double] = [:]

    private var cancellables = Set<AnyCancellable>()
    private let downloadManager = DownloadManager.shared

    init() {
        downloadManager.$activeDownloads
            .receive(on: DispatchQueue.main)
            .assign(to: &$activeDownloads)

        downloadManager.$downloadProgress
            .receive(on: DispatchQueue.main)
            .assign(to: &$downloadProgress)
    }

    func loadDownloads(context: ModelContext) {
        let descriptor = FetchDescriptor<DownloadedSong>(
            sortBy: [SortDescriptor(\.downloadedAt, order: .reverse)]
        )
        downloadedSongs = (try? context.fetch(descriptor)) ?? []
    }

    func download(_ song: Song) {
        downloadManager.download(song)
    }

    func cancelDownload(_ songId: String) {
        downloadManager.cancelDownload(songId)
    }

    func deleteDownload(_ songId: String, context: ModelContext) {
        downloadManager.deleteDownload(songId)
        loadDownloads(context: context)
    }

    func deleteDownload(_ songId: String) {
        downloadManager.deleteDownload(songId)
    }

    func isDownloaded(_ songId: String) -> Bool {
        downloadManager.isDownloaded(songId)
    }

    func isDownloading(_ songId: String) -> Bool {
        activeDownloads[songId] != nil
    }

    func progress(for songId: String) -> Double {
        downloadProgress[songId] ?? 0
    }

    var totalDownloadSize: Int {
        downloadedSongs.compactMap { $0.fileSize }.reduce(0, +)
    }

    var totalDownloadSizeFormatted: String {
        totalDownloadSize.formattedFileSize
    }
}
