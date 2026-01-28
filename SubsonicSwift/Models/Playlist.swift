import Foundation

struct Playlist: Codable, Identifiable, Hashable {
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

    enum CodingKeys: String, CodingKey {
        case id, name, comment, owner, songCount, duration, created, changed, coverArt
        case songs = "entry"
    }
}

struct PlaylistsResponse: Codable {
    let playlists: PlaylistsContainer

    struct PlaylistsContainer: Codable {
        let playlist: [Playlist]?
    }
}

struct PlaylistResponse: Codable {
    let playlist: Playlist
}
