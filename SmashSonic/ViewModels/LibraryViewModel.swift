import Foundation

@MainActor
final class LibraryViewModel: ObservableObject {
    @Published var artists: [Artist] = []
    @Published var albums: [Album] = []
    @Published var randomAlbums: [Album] = []
    @Published var recentAlbums: [Album] = []
    @Published var starredAlbums: [Album] = []
    @Published var playlists: [Playlist] = []
    @Published var isLoading = false
    @Published var error: String?

    private let client = SubsonicClient.shared

    func loadHomeData() async {
        isLoading = true
        error = nil

        async let random = client.getAlbumList(type: .random, size: 10)
        async let recent = client.getAlbumList(type: .newest, size: 10)
        async let starred = client.getAlbumList(type: .starred, size: 10)
        async let playlistsData = client.getPlaylists()

        do {
            randomAlbums = try await random
            recentAlbums = try await recent
            starredAlbums = try await starred
            playlists = try await playlistsData
        } catch {
            self.error = error.localizedDescription
        }

        isLoading = false
    }

    func loadArtists() async {
        isLoading = true
        error = nil

        do {
            artists = try await client.getArtists()
        } catch {
            self.error = error.localizedDescription
        }

        isLoading = false
    }

    func loadAlbums(type: AlbumListType = .alphabeticalByName) async {
        isLoading = true
        error = nil

        do {
            albums = try await client.getAlbumList(type: type, size: 500)
        } catch {
            self.error = error.localizedDescription
        }

        isLoading = false
    }

    func loadArtist(id: String) async -> ArtistDetail? {
        do {
            return try await client.getArtist(id: id)
        } catch {
            self.error = error.localizedDescription
            return nil
        }
    }

    func loadAlbum(id: String) async -> Album? {
        do {
            return try await client.getAlbum(id: id)
        } catch {
            self.error = error.localizedDescription
            return nil
        }
    }

    func loadPlaylist(id: String) async -> Playlist? {
        do {
            return try await client.getPlaylist(id: id)
        } catch {
            self.error = error.localizedDescription
            return nil
        }
    }

    func refreshRandom() async {
        do {
            randomAlbums = try await client.getAlbumList(type: .random, size: 10)
        } catch {
            self.error = error.localizedDescription
        }
    }
}
