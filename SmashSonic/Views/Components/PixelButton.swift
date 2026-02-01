import SwiftUI

enum PixelButtonType: String {
    case play = "PixelPlay"
    case pause = "PixelPause"
    case next = "PixelNext"
    case previous = "PixelPrevious"
    case shuffle = "PixelShuffle"
    case download = "PixelDownload"

    var fallbackSystemImage: String {
        switch self {
        case .play: return "play.fill"
        case .pause: return "pause.fill"
        case .next: return "forward.fill"
        case .previous: return "backward.fill"
        case .shuffle: return "shuffle"
        case .download: return "arrow.down.circle"
        }
    }
}

enum PixelIconType: String {
    case home = "PixelHome"
    case browse = "PixelBrowse"
    case search = "PixelSearch"
    case downloads = "PixelDownloads"
    case settings = "PixelSettings"

    var fallbackSystemImage: String {
        switch self {
        case .home: return "house"
        case .browse: return "square.grid.2x2"
        case .search: return "magnifyingglass"
        case .downloads: return "arrow.down.circle"
        case .settings: return "gear"
        }
    }
}

struct PixelButtonImage: View {
    let type: PixelButtonType
    var size: CGFloat = 32

    var body: some View {
        if let uiImage = UIImage(named: type.rawValue) {
            Image(uiImage: uiImage)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: size, height: size)
        } else {
            Image(systemName: type.fallbackSystemImage)
                .font(.system(size: size * 0.7))
        }
    }
}

struct PixelIcon: View {
    let type: PixelIconType
    var size: CGFloat = 24

    var body: some View {
        if let uiImage = UIImage(named: type.rawValue) {
            Image(uiImage: uiImage)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: size, height: size)
        } else {
            Image(systemName: type.fallbackSystemImage)
                .font(.system(size: size * 0.7))
        }
    }
}

struct PixelButton: View {
    let type: PixelButtonType
    var size: CGFloat = 32
    var action: () -> Void

    var body: some View {
        Button(action: action) {
            PixelButtonImage(type: type, size: size)
        }
    }
}

struct PixelPlayPauseButton: View {
    let isPlaying: Bool
    let isLoading: Bool
    var size: CGFloat = 32
    var action: () -> Void

    var body: some View {
        Button(action: action) {
            if isLoading {
                ProgressView()
                    .frame(width: size, height: size)
            } else {
                PixelButtonImage(type: isPlaying ? .pause : .play, size: size)
            }
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        HStack(spacing: 20) {
            PixelButton(type: .previous) {}
            PixelButton(type: .play, size: 48) {}
            PixelButton(type: .pause, size: 48) {}
            PixelButton(type: .next) {}
        }

        HStack(spacing: 20) {
            PixelButton(type: .shuffle) {}
            PixelButton(type: .download) {}
        }

        HStack(spacing: 20) {
            PixelIcon(type: .home)
            PixelIcon(type: .browse)
            PixelIcon(type: .search)
            PixelIcon(type: .downloads)
            PixelIcon(type: .settings)
        }
    }
    .padding()
}
