import SwiftUI

struct AppearanceSettingsView: View {
    @ObservedObject private var settingsManager = SettingsManager.shared

    var body: some View {
        Form {
            // None option
            Section {
                backgroundOptionButton(for: .none)
            } header: {
                Text("Default")
            }

            // Solid colors section
            Section {
                ForEach(BackgroundType.solidColors, id: \.self) { backgroundType in
                    solidColorButton(for: backgroundType)
                }
            } header: {
                Text("Solid Colors")
            } footer: {
                Text("Simple solid color backgrounds.")
            }

            // Pixel art section
            Section {
                ForEach(BackgroundType.pixelArtBackgrounds, id: \.self) { backgroundType in
                    pixelArtButton(for: backgroundType)
                }
            } header: {
                Text("Pixel Art")
            } footer: {
                Text("8-bit themed background images.")
            }
        }
        .scrollContentBackground(.hidden)
        .background(Color.clear)
        .navigationTitle("Appearance")
        .toolbarBackground(.hidden, for: .navigationBar)
    }

    @ViewBuilder
    private func backgroundOptionButton(for backgroundType: BackgroundType) -> some View {
        Button {
            settingsManager.backgroundType = backgroundType
        } label: {
            HStack {
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.secondary.opacity(0.2))
                    .frame(width: 60, height: 60)
                    .overlay {
                        Image(systemName: "nosign")
                            .foregroundStyle(.secondary)
                    }

                Text(backgroundType.displayName)
                    .foregroundStyle(.primary)

                Spacer()

                if settingsManager.backgroundType == backgroundType {
                    Image(systemName: "checkmark")
                        .foregroundStyle(Color.accentColor)
                }
            }
        }
    }

    @ViewBuilder
    private func solidColorButton(for backgroundType: BackgroundType) -> some View {
        Button {
            settingsManager.backgroundType = backgroundType
        } label: {
            HStack {
                if let color = backgroundType.solidColor {
                    RoundedRectangle(cornerRadius: 8)
                        .fill(color)
                        .frame(width: 60, height: 60)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .strokeBorder(Color.white.opacity(0.3), lineWidth: 1)
                        )
                }

                Text(backgroundType.displayName)
                    .foregroundStyle(.primary)

                Spacer()

                if settingsManager.backgroundType == backgroundType {
                    Image(systemName: "checkmark")
                        .foregroundStyle(Color.accentColor)
                }
            }
        }
    }

    @ViewBuilder
    private func pixelArtButton(for backgroundType: BackgroundType) -> some View {
        Button {
            settingsManager.backgroundType = backgroundType
        } label: {
            HStack {
                if let imageName = backgroundType.imageName {
                    Image(imageName)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(width: 60, height: 60)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }

                Text(backgroundType.displayName)
                    .foregroundStyle(.primary)

                Spacer()

                if settingsManager.backgroundType == backgroundType {
                    Image(systemName: "checkmark")
                        .foregroundStyle(Color.accentColor)
                }
            }
        }
    }
}

#Preview {
    NavigationStack {
        AppearanceSettingsView()
    }
}
