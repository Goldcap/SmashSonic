import Foundation

struct Artist: Codable, Identifiable, Hashable {
    let id: String
    let name: String
    let albumCount: Int?
    let coverArt: String?

    enum CodingKeys: String, CodingKey {
        case id, name, albumCount, coverArt
    }
}

struct ArtistsResponse: Codable {
    let artists: ArtistsIndex

    struct ArtistsIndex: Codable {
        let index: [ArtistIndex]?
        let ignoredArticles: String?
    }

    struct ArtistIndex: Codable {
        let name: String
        let artist: [Artist]?
    }
}

struct ArtistResponse: Codable {
    let artist: ArtistDetail

    struct ArtistDetail: Codable {
        let id: String
        let name: String
        let albumCount: Int?
        let coverArt: String?
        let album: [Album]?
    }
}
