import Foundation
import Combine

class SettingsManager: ObservableObject {
    static let shared = SettingsManager()

    private let userDefaults = UserDefaults.standard
    private let settingsKey = "appSettings"

    @Published var backgroundType: BackgroundType {
        didSet {
            saveSettings()
        }
    }

    private init() {
        let settings = Self.loadSettings()
        self.backgroundType = settings.backgroundType
    }

    private static func loadSettings() -> AppSettings {
        guard let data = UserDefaults.standard.data(forKey: "appSettings"),
              let settings = try? JSONDecoder().decode(AppSettings.self, from: data) else {
            return .default
        }
        return settings
    }

    private func saveSettings() {
        let settings = AppSettings(backgroundType: backgroundType)
        guard let data = try? JSONEncoder().encode(settings) else { return }
        userDefaults.set(data, forKey: settingsKey)
    }
}
