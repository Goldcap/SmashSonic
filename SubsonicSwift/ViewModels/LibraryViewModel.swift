import Foundation

@MainActor
final class LibraryViewModel: ObservableObject {
    @Published var artists: [Artist] = []
    @Published var albums: [Album] = []
    @Published var isLoading = false
    @Published var error: String?

    private let client = SubsonicClient.shared

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
            albums = try await client.getAlbumList(type: type)
        } catch {
            self.error = error.localizedDescription
        }

        isLoading = false
    }

    func loadArtist(id: String) async -> ArtistResponse.ArtistDetail? {
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
}
