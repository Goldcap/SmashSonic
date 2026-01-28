import Foundation
import SwiftData

@Model
final class DownloadedSong {
    @Attribute(.unique) var id: String
    var title: String
    var album: String?
    var albumId: String?
    var artist: String?
    var artistId: String?
    var track: Int?
    var duration: Int?
    var coverArt: String?
    var suffix: String?
    var localPath: String
    var downloadedAt: Date
    var fileSize: Int?

    init(from song: Song, localPath: String) {
        self.id = song.id
        self.title = song.title
        self.album = song.album
        self.albumId = song.albumId
        self.artist = song.artist
        self.artistId = song.artistId
        self.track = song.track
        self.duration = song.duration
        self.coverArt = song.coverArt
        self.suffix = song.suffix
        self.localPath = localPath
        self.downloadedAt = Date()
        self.fileSize = song.size
    }

    var formattedDuration: String {
        guard let duration = duration else { return "--:--" }
        let minutes = duration / 60
        let seconds = duration % 60
        return String(format: "%d:%02d", minutes, seconds)
    }

    func toSong() -> Song {
        Song(
            id: id,
            title: title,
            album: album,
            albumId: albumId,
            artist: artist,
            artistId: artistId,
            track: track,
            duration: duration,
            coverArt: coverArt,
            suffix: suffix,
            bitRate: nil,
            size: fileSize,
            contentType: nil,
            path: nil
        )
    }
}
