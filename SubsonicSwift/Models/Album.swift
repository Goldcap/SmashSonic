import Foundation

struct Album: Identifiable, Hashable {
    let id: String
    let name: String
    let artist: String?
    let artistId: String?
    let coverArt: String?
    let songCount: Int?
    let year: Int?
    let genre: String?
    let duration: Int?
    var songs: [Song]?

    init?(from data: [String: Any]) {
        guard let id = data["id"] as? String else {
            return nil
        }
        guard let name = data["name"] as? String ?? data["title"] as? String else {
            return nil
        }
        self.id = id
        self.name = name
        self.artist = data["artist"] as? String
        self.artistId = data["artistId"] as? String
        self.coverArt = data["coverArt"] as? String
        self.songCount = data["songCount"] as? Int
        self.year = data["year"] as? Int
        self.genre = data["genre"] as? String
        self.duration = data["duration"] as? Int
        self.songs = (data["song"] as? [[String: Any]])?.compactMap { Song(from: $0) }
    }

    static func == (lhs: Album, rhs: Album) -> Bool {
        lhs.id == rhs.id
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}
