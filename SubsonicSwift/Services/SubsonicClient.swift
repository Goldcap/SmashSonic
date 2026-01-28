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

    private func request<T: Codable>(_ endpoint: String, params: [String: String] = [:]) async throws -> T {
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

        let wrapper = try JSONDecoder().decode(SubsonicResponse<T>.self, from: data)

        if let error = wrapper.subsonicResponse.error {
            throw SubsonicError.apiError(error.code, error.message)
        }

        guard let result = wrapper.subsonicResponse.result else {
            if T.self == EmptyResponse.self {
                return EmptyResponse() as! T
            }
            throw SubsonicError.emptyResponse
        }

        return result
    }

    // MARK: - API Methods

    func ping() async throws -> Bool {
        let _: EmptyResponse = try await request("ping")
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
        let response: ArtistsResponse = try await request("getArtists")
        return response.artists.index?.flatMap { $0.artist ?? [] } ?? []
    }

    func getArtist(id: String) async throws -> ArtistResponse.ArtistDetail {
        let response: ArtistResponse = try await request("getArtist", params: ["id": id])
        return response.artist
    }

    func getAlbum(id: String) async throws -> Album {
        let response: AlbumResponse = try await request("getAlbum", params: ["id": id])
        return response.album
    }

    func getAlbumList(type: AlbumListType = .alphabeticalByName, size: Int = 50, offset: Int = 0) async throws -> [Album] {
        let response: AlbumListResponse = try await request("getAlbumList2", params: [
            "type": type.rawValue,
            "size": String(size),
            "offset": String(offset)
        ])
        return response.albumList2.album ?? []
    }

    func search(query: String, artistCount: Int = 20, albumCount: Int = 20, songCount: Int = 50) async throws -> SearchResult {
        let response: SearchResponse = try await request("search3", params: [
            "query": query,
            "artistCount": String(artistCount),
            "albumCount": String(albumCount),
            "songCount": String(songCount)
        ])
        return response.searchResult3
    }

    func getPlaylists() async throws -> [Playlist] {
        let response: PlaylistsResponse = try await request("getPlaylists")
        return response.playlists.playlist ?? []
    }

    func getPlaylist(id: String) async throws -> Playlist {
        let response: PlaylistResponse = try await request("getPlaylist", params: ["id": id])
        return response.playlist
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
}

// MARK: - Response Types

struct SubsonicResponse<T: Codable>: Codable {
    let subsonicResponse: SubsonicResponseContent<T>

    enum CodingKeys: String, CodingKey {
        case subsonicResponse = "subsonic-response"
    }

    struct SubsonicResponseContent<R: Codable>: Codable {
        let status: String
        let version: String
        let error: SubsonicError.APIError?

        var result: R? {
            try? container.decode(R.self)
        }

        private let container: SingleValueDecodingContainer

        init(from decoder: Decoder) throws {
            let keyedContainer = try decoder.container(keyedBy: CodingKeys.self)
            self.status = try keyedContainer.decode(String.self, forKey: .status)
            self.version = try keyedContainer.decode(String.self, forKey: .version)
            self.error = try keyedContainer.decodeIfPresent(SubsonicError.APIError.self, forKey: .error)
            self.container = try decoder.singleValueContainer()
        }

        func encode(to encoder: Encoder) throws {
            var container = encoder.container(keyedBy: CodingKeys.self)
            try container.encode(status, forKey: .status)
            try container.encode(version, forKey: .version)
            try container.encodeIfPresent(error, forKey: .error)
        }

        enum CodingKeys: String, CodingKey {
            case status, version, error
        }
    }
}

struct EmptyResponse: Codable {}

struct SearchResponse: Codable {
    let searchResult3: SearchResult
}

struct SearchResult: Codable {
    let artist: [Artist]?
    let album: [Album]?
    let song: [Song]?
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

    struct APIError: Codable {
        let code: Int
        let message: String
    }

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
