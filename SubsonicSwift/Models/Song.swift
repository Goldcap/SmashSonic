import Foundation

struct Song: Codable, Identifiable, Hashable {
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

    enum CodingKeys: String, CodingKey {
        case id, title, album, albumId, artist, artistId, track, duration
        case coverArt, suffix, bitRate, size, contentType, path
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
