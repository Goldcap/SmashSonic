import SwiftUI

struct AppearanceSettingsView: View {
    @ObservedObject private var settingsManager = SettingsManager.shared

    var body: some View {
        Form {
            Section {
                backgroundOptionButton(for: .none)
                backgroundOptionButton(for: .stars)
                backgroundOptionButton(for: .notes)
                backgroundOptionButton(for: .grid)
                backgroundOptionButton(for: .space)
            } header: {
                Text("Background Theme")
            } footer: {
                Text("Choose an 8-bit themed background for the app.")
            }
        }
        .navigationTitle("Appearance")
    }

    @ViewBuilder
    private func backgroundOptionButton(for backgroundType: BackgroundType) -> some View {
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
                } else {
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color.secondary.opacity(0.2))
                        .frame(width: 60, height: 60)
                        .overlay {
                            Image(systemName: "nosign")
                                .foregroundStyle(.secondary)
                        }
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
