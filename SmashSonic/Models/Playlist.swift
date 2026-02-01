import Foundation

struct Playlist: Identifiable, Hashable {
    let id: String
    let name: String
    let comment: String?
    let owner: String?
    let songCount: Int?
    let duration: Int?
    let created: String?
    let changed: String?
    let coverArt: String?
    var songs: [Song]?

    init?(from data: [String: Any]) {
        guard let id = data["id"] as? String,
              let name = data["name"] as? String else {
            return nil
        }
        self.id = id
        self.name = name
        self.comment = data["comment"] as? String
        self.owner = data["owner"] as? String
        self.songCount = data["songCount"] as? Int
        self.duration = data["duration"] as? Int
        self.created = data["created"] as? String
        self.changed = data["changed"] as? String
        self.coverArt = data["coverArt"] as? String
        self.songs = (data["entry"] as? [[String: Any]])?.compactMap { Song(from: $0) }
    }
}
