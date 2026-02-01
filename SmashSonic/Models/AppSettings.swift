import Foundation

enum BackgroundType: String, CaseIterable, Codable {
    case none = "none"
    case stars = "stars"
    case notes = "notes"
    case grid = "grid"
    case space = "space"

    var displayName: String {
        switch self {
        case .none: return "None"
        case .stars: return "8-Bit Stars"
        case .notes: return "8-Bit Notes"
        case .grid: return "Retro Grid"
        case .space: return "Deep Space"
        }
    }

    var imageName: String? {
        switch self {
        case .none: return nil
        case .stars: return "Background8BitStars"
        case .notes: return "Background8BitNotes"
        case .grid: return "Background8BitGrid"
        case .space: return "Background8BitSpace"
        }
    }
}

struct AppSettings: Codable {
    var backgroundType: BackgroundType

    static let `default` = AppSettings(backgroundType: .none)
}
