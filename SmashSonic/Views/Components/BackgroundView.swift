import SwiftUI

struct BackgroundView: View {
    @ObservedObject private var settingsManager = SettingsManager.shared

    var body: some View {
        Group {
            if let imageName = settingsManager.backgroundType.imageName {
                Image(imageName)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .ignoresSafeArea()
            } else if let color = settingsManager.backgroundType.solidColor {
                color
                    .ignoresSafeArea()
            } else {
                Color.clear
            }
        }
    }
}

struct AppBackgroundModifier: ViewModifier {
    @ObservedObject private var settingsManager = SettingsManager.shared

    func body(content: Content) -> some View {
        ZStack {
            // Solid color backgrounds (full opacity)
            if let color = settingsManager.backgroundType.solidColor {
                color
                    .ignoresSafeArea()
            }
            // Pixel art backgrounds (with opacity)
            else if let imageName = settingsManager.backgroundType.imageName {
                Image(imageName)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .ignoresSafeArea()
                    .opacity(0.3)
            }
            content
        }
    }
}

extension View {
    func appBackground() -> some View {
        modifier(AppBackgroundModifier())
    }
}

#Preview {
    BackgroundView()
}
