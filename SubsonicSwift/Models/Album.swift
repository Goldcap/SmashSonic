import Foundation

struct Album: Codable, Identifiable, Hashable {
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

    enum CodingKeys: String, CodingKey {
        case id, name, artist, artistId, coverArt, songCount, year, genre, duration
        case songs = "song"
    }

    static func == (lhs: Album, rhs: Album) -> Bool {
        lhs.id == rhs.id
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}

struct AlbumResponse: Codable {
    let album: Album
}

struct AlbumListResponse: Codable {
    let albumList2: AlbumList

    struct AlbumList: Codable {
        let album: [Album]?
    }
}
