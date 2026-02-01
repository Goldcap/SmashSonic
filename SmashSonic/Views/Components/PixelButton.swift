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

enum PixelActionButtonStyle {
    case primary
    case secondary

    var backgroundImage: String {
        switch self {
        case .primary: return "PixelButtonPrimary"
        case .secondary: return "PixelButtonSecondary"
        }
    }
}

struct PixelActionButton: View {
    let title: String?
    let icon: String?
    let style: PixelActionButtonStyle
    let action: () -> Void

    init(title: String? = nil, icon: String? = nil, style: PixelActionButtonStyle = .primary, action: @escaping () -> Void) {
        self.title = title
        self.icon = icon
        self.style = style
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            ZStack {
                Image(style.backgroundImage)
                    .renderingMode(.original)
                    .resizable()
                    .interpolation(.none)
                    .scaledToFill()

                HStack(spacing: 6) {
                    if let icon = icon {
                        Image(icon)
                            .renderingMode(.original)
                            .resizable()
                            .interpolation(.none)
                            .scaledToFit()
                            .frame(width: 20, height: 20)
                    }

                    if let title = title {
                        Text(title)
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                            .shadow(color: .black, radius: 1, x: 1, y: 1)
                    }
                }
            }
            .frame(height: 44)
            .frame(maxWidth: title != nil ? .infinity : 60)
            .clipShape(RoundedRectangle(cornerRadius: 6))
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    VStack(spacing: 20) {
        HStack(spacing: 12) {
            PixelActionButton(title: "Play", icon: "PixelPlay", style: .primary) {}
            PixelActionButton(title: "Shuffle", icon: "PixelShuffle", style: .secondary) {}
            PixelActionButton(icon: "PixelDownload", style: .secondary) {}
        }
        .padding(.horizontal)

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
