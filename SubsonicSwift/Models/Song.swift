import Foundation

struct Song: Identifiable, Hashable {
    let id: String
    let title: String
    let album: String?
    let albumId: String?
    let artist: String?
    let artistId: String?
    let track: Int?
    let duration: Int?
    let coverArt: String?
    let suffix: String?
    let bitRate: Int?
    let size: Int?
    let contentType: String?
    let path: String?

    init?(from data: [String: Any]) {
        guard let id = data["id"] as? String,
              let title = data["title"] as? String else {
            return nil
        }
        self.id = id
        self.title = title
        self.album = data["album"] as? String
        self.albumId = data["albumId"] as? String
        self.artist = data["artist"] as? String
        self.artistId = data["artistId"] as? String
        self.track = data["track"] as? Int
        self.duration = data["duration"] as? Int
        self.coverArt = data["coverArt"] as? String
        self.suffix = data["suffix"] as? String
        self.bitRate = data["bitRate"] as? Int
        self.size = data["size"] as? Int
        self.contentType = data["contentType"] as? String
        self.path = data["path"] as? String
    }

    init(id: String, title: String, album: String?, albumId: String?, artist: String?, artistId: String?, track: Int?, duration: Int?, coverArt: String?, suffix: String?, bitRate: Int?, size: Int?, contentType: String?, path: String?) {
        self.id = id
        self.title = title
        self.album = album
        self.albumId = albumId
        self.artist = artist
        self.artistId = artistId
        self.track = track
        self.duration = duration
        self.coverArt = coverArt
        self.suffix = suffix
        self.bitRate = bitRate
        self.size = size
        self.contentType = contentType
        self.path = path
    }

    var formattedDuration: String {
        guard let duration = duration else { return "--:--" }
        let minutes = duration / 60
        let seconds = duration % 60
        return String(format: "%d:%02d", minutes, seconds)
    }

    static func == (lhs: Song, rhs: Song) -> Bool {
        lhs.id == rhs.id
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}
