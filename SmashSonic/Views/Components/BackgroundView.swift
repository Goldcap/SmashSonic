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

    func body(content: Content) -> some View {
        content
            .background {
                GeometryReader { geometry in
                    if let imageName = settingsManager.backgroundType.imageName {
                        Image(imageName)
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(width: geometry.size.width, height: geometry.size.height)
                            .clipped()
                            .opacity(0.3)
                    } else if let color = settingsManager.backgroundType.solidColor {
                        color
                    }
                }
                .ignoresSafeArea()
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
