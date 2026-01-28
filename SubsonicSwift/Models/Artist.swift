import Foundation

struct Artist: Identifiable, Hashable {
    let id: String
    let name: String
    let albumCount: Int?
    let coverArt: String?

    init?(from data: [String: Any]) {
        guard let id = data["id"] as? String,
              let name = data["name"] as? String else {
            return nil
        }
        self.id = id
        self.name = name
        self.albumCount = data["albumCount"] as? Int
        self.coverArt = data["coverArt"] as? String
    }
}
