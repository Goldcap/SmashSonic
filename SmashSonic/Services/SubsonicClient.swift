import Foundation

final class SubsonicClient: ObservableObject {
    static let shared = SubsonicClient()

    @Published var serverConfig: ServerConfig
    @Published var isConnected = false

    private let session: URLSession

    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 300
        self.session = URLSession(configuration: config)
        self.serverConfig = KeychainService.shared.loadServerConfig() ?? .empty
    }

    func updateConfig(_ config: ServerConfig) {
        self.serverConfig = config
        try? KeychainService.shared.saveServerConfig(config)
    }

    private func buildURL(endpoint: String, additionalParams: [String: String] = [:]) -> URL? {
        guard let baseURL = serverConfig.baseURL else { return nil }
        let url = baseURL.appendingPathComponent("rest").appendingPathComponent(endpoint)
        var params = SubsonicAuth.authParams(username: serverConfig.username, password: serverConfig.password)
        params.merge(additionalParams) { _, new in new }
        return url.appendingQueryItems(params)
    }

    private func fetchData(endpoint: String, params: [String: String] = [:]) async throws -> [String: Any] {
        guard let url = buildURL(endpoint: endpoint, additionalParams: params) else {
            throw SubsonicError.invalidURL
        }

        let (data, response) = try await session.data(from: url)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw SubsonicError.invalidResponse
        }

        guard httpResponse.statusCode == 200 else {
            throw SubsonicError.httpError(httpResponse.statusCode)
        }

        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              let subsonicResponse = json["subsonic-response"] as? [String: Any] else {
            throw SubsonicError.invalidResponse
        }

        if let error = subsonicResponse["error"] as? [String: Any],
           let code = error["code"] as? Int,
           let message = error["message"] as? String {
            throw SubsonicError.apiError(code, message)
        }

        guard subsonicResponse["status"] as? String == "ok" else {
            throw SubsonicError.invalidResponse
        }

        return subsonicResponse
    }

    // MARK: - API Methods

    func ping() async throws -> Bool {
        _ = try await fetchData(endpoint: "ping")
        return true
    }

    func testConnection() async -> Bool {
        do {
            _ = try await ping()
            await MainActor.run { self.isConnected = true }
            return true
        } catch {
            await MainActor.run { self.isConnected = false }
            return false
        }
    }

    func getArtists() async throws -> [Artist] {
        let response = try await fetchData(endpoint: "getArtists")

        guard let artists = response["artists"] as? [String: Any],
              let index = artists["index"] as? [[String: Any]] else {
            return []
        }

        var result: [Artist] = []
        for section in index {
            if let artistList = section["artist"] as? [[String: Any]] {
                for artistData in artistList {
                    if let artist = Artist(from: artistData) {
                        result.append(artist)
                    }
                }
            }
        }
        return result
    }

    func getArtist(id: String) async throws -> ArtistDetail {
        let response = try await fetchData(endpoint: "getArtist", params: ["id": id])

        guard let artistData = response["artist"] as? [String: Any] else {
            throw SubsonicError.emptyResponse
        }

        return ArtistDetail(from: artistData)
    }

    func getAlbum(id: String) async throws -> Album {
        let response = try await fetchData(endpoint: "getAlbum", params: ["id": id])

        guard let albumData = response["album"] as? [String: Any],
              let album = Album(from: albumData) else {
            throw SubsonicError.emptyResponse
        }

        return album
    }

    func getAlbumList(type: AlbumListType = .alphabeticalByName, size: Int = 50, offset: Int = 0) async throws -> [Album] {
        let response = try await fetchData(endpoint: "getAlbumList2", params: [
            "type": type.rawValue,
            "size": String(size),
            "offset": String(offset)
        ])

        guard let albumList = response["albumList2"] as? [String: Any],
              let albums = albumList["album"] as? [[String: Any]] else {
            return []
        }

        return albums.compactMap { Album(from: $0) }
    }

    func search(query: String, artistCount: Int = 20, albumCount: Int = 20, songCount: Int = 50) async throws -> SearchResult {
        let response = try await fetchData(endpoint: "search3", params: [
            "query": query,
            "artistCount": String(artistCount),
            "albumCount": String(albumCount),
            "songCount": String(songCount)
        ])

        let searchResult = response["searchResult3"] as? [String: Any] ?? [:]

        let artists = (searchResult["artist"] as? [[String: Any]])?.compactMap { Artist(from: $0) } ?? []
        let albums = (searchResult["album"] as? [[String: Any]])?.compactMap { Album(from: $0) } ?? []
        let songs = (searchResult["song"] as? [[String: Any]])?.compactMap { Song(from: $0) } ?? []

        return SearchResult(artist: artists, album: albums, song: songs)
    }

    func getPlaylists() async throws -> [Playlist] {
        let response = try await fetchData(endpoint: "getPlaylists")

        guard let playlists = response["playlists"] as? [String: Any],
              let playlistList = playlists["playlist"] as? [[String: Any]] else {
            return []
        }

        return playlistList.compactMap { Playlist(from: $0) }
    }

    func getPlaylist(id: String) async throws -> Playlist {
        let response = try await fetchData(endpoint: "getPlaylist", params: ["id": id])

        guard let playlistData = response["playlist"] as? [String: Any],
              let playlist = Playlist(from: playlistData) else {
            throw SubsonicError.emptyResponse
        }

        return playlist
    }

    // MARK: - URL Builders

    func streamURL(for songId: String, maxBitRate: Int? = nil) -> URL? {
        var params: [String: String] = ["id": songId]
        if let bitRate = maxBitRate {
            params["maxBitRate"] = String(bitRate)
        }
        return buildURL(endpoint: "stream", additionalParams: params)
    }

    func coverArtURL(for id: String, size: Int = 300) -> URL? {
        return buildURL(endpoint: "getCoverArt", additionalParams: ["id": id, "size": String(size)])
    }

    func downloadURL(for songId: String) -> URL? {
        return buildURL(endpoint: "download", additionalParams: ["id": songId])
    }

    // MARK: - Starring

    func star(songId: String) async throws {
        _ = try await fetchData(endpoint: "star", params: ["id": songId])
    }

    func unstar(songId: String) async throws {
        _ = try await fetchData(endpoint: "unstar", params: ["id": songId])
    }

    func getStarred() async throws -> [Song] {
        let response = try await fetchData(endpoint: "getStarred2")

        guard let starred = response["starred2"] as? [String: Any],
              let songs = starred["song"] as? [[String: Any]] else {
            return []
        }

        return songs.compactMap { Song(from: $0) }
    }

    func getRandomSongs(size: Int = 50) async throws -> [Song] {
        let response = try await fetchData(endpoint: "getRandomSongs", params: ["size": String(size)])

        guard let randomSongs = response["randomSongs"] as? [String: Any],
              let songs = randomSongs["song"] as? [[String: Any]] else {
            return []
        }

        return songs.compactMap { Song(from: $0) }
    }
}

// MARK: - Response Types

struct ArtistDetail {
    let id: String
    let name: String
    let albumCount: Int?
    let coverArt: String?
    let albums: [Album]

    init(from data: [String: Any]) {
        self.id = data["id"] as? String ?? ""
        self.name = data["name"] as? String ?? ""
        self.albumCount = data["albumCount"] as? Int
        self.coverArt = data["coverArt"] as? String
        self.albums = (data["album"] as? [[String: Any]])?.compactMap { Album(from: $0) } ?? []
    }
}

struct SearchResult {
    let artist: [Artist]
    let album: [Album]
    let song: [Song]
}

enum AlbumListType: String {
    case random
    case newest
    case frequent
    case recent
    case starred
    case alphabeticalByName
    case alphabeticalByArtist
    case byYear
    case byGenre
}

// MARK: - Errors

enum SubsonicError: LocalizedError {
    case invalidURL
    case invalidResponse
    case httpError(Int)
    case apiError(Int, String)
    case emptyResponse

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid server URL"
        case .invalidResponse:
            return "Invalid response from server"
        case .httpError(let code):
            return "HTTP error: \(code)"
        case .apiError(_, let message):
            return message
        case .emptyResponse:
            return "Empty response from server"
        }
    }
}
