import SwiftUI
import SwiftData

@main
struct SubsonicSwiftApp: App {
    @StateObject private var playerViewModel = PlayerViewModel()
    @State private var launchScreenFinished = false

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
            ZStack {
                ContentView()
                    .environmentObject(playerViewModel)
                    .modelContainer(sharedModelContainer)

                if !launchScreenFinished {
                    LaunchScreenView(isFinished: $launchScreenFinished)
                }
            }
        }
    }
}
