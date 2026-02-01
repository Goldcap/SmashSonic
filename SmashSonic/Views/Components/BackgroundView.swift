import SwiftUI

struct BackgroundView: View {
    @ObservedObject private var settingsManager = SettingsManager.shared

    var body: some View {
        Group {
            if let color = settingsManager.backgroundType.solidColor {
                color
            } else if let imageName = settingsManager.backgroundType.imageName {
                Image(imageName)
                    .resizable()
                    .scaledToFill()
                    .opacity(0.3)
            } else {
                Color.clear
            }
        }
    }
}

struct AppBackgroundModifier: ViewModifier {
    @ObservedObject private var settingsManager = SettingsManager.shared

    @ViewBuilder
    private var backgroundContent: some View {
        if let color = settingsManager.backgroundType.solidColor {
            color
        } else if let imageName = settingsManager.backgroundType.imageName {
            Image(imageName)
                .resizable()
                .scaledToFill()
                .opacity(0.3)
        } else {
            Color(.systemBackground)
        }
    }

    func body(content: Content) -> some View {
        content
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(
                backgroundContent
                    .ignoresSafeArea()
            )
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
