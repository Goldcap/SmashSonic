import Foundation
import Combine

@MainActor
final class SearchViewModel: ObservableObject {
    @Published var query = ""
    @Published var artists: [Artist] = []
    @Published var albums: [Album] = []
    @Published var songs: [Song] = []
    @Published var isSearching = false
    @Published var error: String?
    @Published var hasSearched = false

    private var searchTask: Task<Void, Never>?
    private var cancellables = Set<AnyCancellable>()

    init() {
        $query
            .debounce(for: .milliseconds(300), scheduler: DispatchQueue.main)
            .removeDuplicates()
            .sink { [weak self] query in
                self?.performSearch(query: query)
            }
            .store(in: &cancellables)
    }

    func performSearch(query: String) {
        searchTask?.cancel()

        guard !query.trimmingCharacters(in: .whitespaces).isEmpty else {
            clearResults()
            return
        }

        searchTask = Task {
            await search(query: query)
        }
    }

    func search(query: String) async {
        isSearching = true
        error = nil

        do {
            let result = try await SubsonicClient.shared.search(query: query)
            artists = result.artist ?? []
            albums = result.album ?? []
            songs = result.song ?? []
            hasSearched = true
        } catch {
            if !Task.isCancelled {
                self.error = error.localizedDescription
            }
        }

        isSearching = false
    }

    func clearResults() {
        artists = []
        albums = []
        songs = []
        hasSearched = false
    }

    var hasResults: Bool {
        !artists.isEmpty || !albums.isEmpty || !songs.isEmpty
    }

    var isEmpty: Bool {
        hasSearched && !hasResults
    }
}
