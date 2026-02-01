import Foundation
import SwiftData
import Combine

final class DownloadManager: NSObject, ObservableObject {
    static let shared = DownloadManager()

    @Published var activeDownloads: [String: DownloadTask] = [:]
    @Published var downloadProgress: [String: Double] = [:]

    private var backgroundSession: URLSession!
    private var modelContext: ModelContext?

    struct DownloadTask {
        let song: Song
        let task: URLSessionDownloadTask
        var progress: Double = 0
    }

    override private init() {
        super.init()

        let config = URLSessionConfiguration.background(withIdentifier: "com.subsonicswift.downloads")
        config.isDiscretionary = false
        config.sessionSendsLaunchEvents = true
        backgroundSession = URLSession(configuration: config, delegate: self, delegateQueue: nil)
    }

    func setModelContext(_ context: ModelContext) {
        self.modelContext = context
    }

    var documentsDirectory: URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("Downloads", isDirectory: true)
    }

    func localURL(for songId: String) -> URL? {
        let directory = documentsDirectory
        let files = (try? FileManager.default.contentsOfDirectory(at: directory, includingPropertiesForKeys: nil)) ?? []
        return files.first { $0.lastPathComponent.hasPrefix(songId) }
    }

    func isDownloaded(_ songId: String) -> Bool {
        localURL(for: songId) != nil
    }

    func download(_ song: Song) {
        guard let url = SubsonicClient.shared.downloadURL(for: song.id) else { return }
        guard activeDownloads[song.id] == nil else { return }

        createDownloadsDirectory()

        let task = backgroundSession.downloadTask(with: url)
        task.taskDescription = song.id
        activeDownloads[song.id] = DownloadTask(song: song, task: task)
        downloadProgress[song.id] = 0
        task.resume()
    }

    func cancelDownload(_ songId: String) {
        activeDownloads[songId]?.task.cancel()
        activeDownloads.removeValue(forKey: songId)
        downloadProgress.removeValue(forKey: songId)
    }

    func deleteDownload(_ songId: String) {
        if let url = localURL(for: songId) {
            try? FileManager.default.removeItem(at: url)
        }

        if let context = modelContext {
            let descriptor = FetchDescriptor<DownloadedSong>(
                predicate: #Predicate { $0.id == songId }
            )
            if let downloaded = try? context.fetch(descriptor).first {
                context.delete(downloaded)
                try? context.save()
            }
        }
    }

    private func createDownloadsDirectory() {
        let directory = documentsDirectory
        if !FileManager.default.fileExists(atPath: directory.path) {
            try? FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        }
    }

    private func saveDownloadedSong(_ song: Song, localPath: String) {
        guard let context = modelContext else { return }

        let downloaded = DownloadedSong(from: song, localPath: localPath)
        context.insert(downloaded)
        try? context.save()
    }
}

extension DownloadManager: URLSessionDownloadDelegate {
    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didFinishDownloadingTo location: URL) {
        guard let songId = downloadTask.taskDescription,
              let download = activeDownloads[songId] else { return }

        let suffix = download.song.suffix ?? "mp3"
        let destinationURL = documentsDirectory.appendingPathComponent("\(songId).\(suffix)")

        do {
            if FileManager.default.fileExists(atPath: destinationURL.path) {
                try FileManager.default.removeItem(at: destinationURL)
            }
            try FileManager.default.moveItem(at: location, to: destinationURL)

            DispatchQueue.main.async { [weak self] in
                self?.saveDownloadedSong(download.song, localPath: destinationURL.path)
                self?.activeDownloads.removeValue(forKey: songId)
                self?.downloadProgress.removeValue(forKey: songId)
            }
        } catch {
            print("Failed to move downloaded file: \(error)")
        }
    }

    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didWriteData bytesWritten: Int64, totalBytesWritten: Int64, totalBytesExpectedToWrite: Int64) {
        guard let songId = downloadTask.taskDescription else { return }

        let progress = Double(totalBytesWritten) / Double(totalBytesExpectedToWrite)

        DispatchQueue.main.async { [weak self] in
            self?.downloadProgress[songId] = progress
            if var download = self?.activeDownloads[songId] {
                download.progress = progress
                self?.activeDownloads[songId] = download
            }
        }
    }

    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        guard let songId = task.taskDescription else { return }

        if let error = error {
            print("Download failed for \(songId): \(error)")
        }

        DispatchQueue.main.async { [weak self] in
            self?.activeDownloads.removeValue(forKey: songId)
            self?.downloadProgress.removeValue(forKey: songId)
        }
    }
}
