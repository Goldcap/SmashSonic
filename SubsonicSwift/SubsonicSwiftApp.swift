import SwiftUI
import SwiftData

@main
struct SubsonicSwiftApp: App {
    @StateObject private var playerViewModel = PlayerViewModel()

    var sharedModelContainer: ModelContainer = {
        let schema = Schema([
            DownloadedSong.self,
        ])
        let modelConfiguration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)

        do {
            return try ModelContainer(for: schema, configurations: [modelConfiguration])
        } catch {
            fatalError("Could not create ModelContainer: \(error)")
        }
    }()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(playerViewModel)
                .modelContainer(sharedModelContainer)
        }
    }
}
