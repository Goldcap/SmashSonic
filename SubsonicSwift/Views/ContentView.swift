import SwiftUI
import SwiftData

struct ContentView: View {
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @StateObject private var libraryViewModel = LibraryViewModel()
    @StateObject private var searchViewModel = SearchViewModel()
    @StateObject private var downloadsViewModel = DownloadsViewModel()
    @ObservedObject private var client = SubsonicClient.shared

    @State private var selectedTab = 0

    var body: some View {
        ZStack(alignment: .bottom) {
            TabView(selection: $selectedTab) {
                LibraryView(viewModel: libraryViewModel)
                    .tabItem {
                        Label("Library", systemImage: "music.note.house")
                    }
                    .tag(0)

                SearchView(viewModel: searchViewModel)
                    .tabItem {
                        Label("Search", systemImage: "magnifyingglass")
                    }
                    .tag(1)

                DownloadsView(viewModel: downloadsViewModel)
                    .tabItem {
                        Label("Downloads", systemImage: "arrow.down.circle")
                    }
                    .tag(2)

                ServerSetupView()
                    .tabItem {
                        Label("Settings", systemImage: "gear")
                    }
                    .tag(3)
            }

            if playerViewModel.currentSong != nil {
                VStack(spacing: 0) {
                    MiniPlayerView()
                        .padding(.bottom, 49)
                }
            }
        }
        .sheet(isPresented: $playerViewModel.showFullPlayer) {
            NowPlayingView()
        }
        .onAppear {
            DownloadManager.shared.setModelContext(modelContext)
        }
        .fullScreenCover(isPresented: .constant(!client.serverConfig.isConfigured)) {
            NavigationStack {
                ServerSetupView(isInitialSetup: true)
            }
        }
    }

    @Environment(\.modelContext) private var modelContext
}

struct LibraryView: View {
    @ObservedObject var viewModel: LibraryViewModel
    @State private var selectedSection = 0

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Picker("Section", selection: $selectedSection) {
                    Text("Artists").tag(0)
                    Text("Albums").tag(1)
                }
                .pickerStyle(.segmented)
                .padding()

                if selectedSection == 0 {
                    ArtistsView(viewModel: viewModel)
                } else {
                    AlbumsView(viewModel: viewModel)
                }
            }
            .navigationTitle("Library")
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(PlayerViewModel())
}
