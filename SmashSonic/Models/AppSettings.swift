import Foundation
import SwiftUI

enum BackgroundType: String, CaseIterable, Codable {
    // No background
    case none = "none"

    // Solid colors
    case solidBlack = "solidBlack"
    case solidDarkGray = "solidDarkGray"
    case solidNavy = "solidNavy"
    case solidPurple = "solidPurple"
    case solidForest = "solidForest"
    case solidBurgundy = "solidBurgundy"

    // Pixel art backgrounds
    case stars = "stars"
    case notes = "notes"
    case grid = "grid"
    case space = "space"

    var displayName: String {
        switch self {
        case .none: return "None"
        case .solidBlack: return "Black"
        case .solidDarkGray: return "Dark Gray"
        case .solidNavy: return "Navy"
        case .solidPurple: return "Purple"
        case .solidForest: return "Forest"
        case .solidBurgundy: return "Burgundy"
        case .stars: return "8-Bit Stars"
        case .notes: return "8-Bit Notes"
        case .grid: return "Retro Grid"
        case .space: return "Deep Space"
        }
    }

    var imageName: String? {
        switch self {
        case .none, .solidBlack, .solidDarkGray, .solidNavy, .solidPurple, .solidForest, .solidBurgundy:
            return nil
        case .stars: return "Background8BitStars"
        case .notes: return "Background8BitNotes"
        case .grid: return "Background8BitGrid"
        case .space: return "Background8BitSpace"
        }
    }

    var solidColor: Color? {
        switch self {
        case .solidBlack: return Color.black
        case .solidDarkGray: return Color(white: 0.15)
        case .solidNavy: return Color(red: 0.1, green: 0.1, blue: 0.3)
        case .solidPurple: return Color(red: 0.2, green: 0.1, blue: 0.3)
        case .solidForest: return Color(red: 0.05, green: 0.2, blue: 0.1)
        case .solidBurgundy: return Color(red: 0.3, green: 0.1, blue: 0.1)
        default: return nil
        }
    }

    var isPixelArt: Bool {
        imageName != nil
    }

    var isSolidColor: Bool {
        solidColor != nil
    }

    static var solidColors: [BackgroundType] {
        [.solidBlack, .solidDarkGray, .solidNavy, .solidPurple, .solidForest, .solidBurgundy]
    }

    static var pixelArtBackgrounds: [BackgroundType] {
        [.stars, .notes, .grid, .space]
    }
}

struct AppSettings: Codable {
    var backgroundType: BackgroundType

    static let `default` = AppSettings(backgroundType: .none)
}
