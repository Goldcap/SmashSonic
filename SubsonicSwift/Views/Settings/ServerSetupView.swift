import SwiftUI

struct ServerSetupView: View {
    @ObservedObject private var client = SubsonicClient.shared
    @Environment(\.dismiss) private var dismiss

    @State private var serverURL: String = ""
    @State private var username: String = ""
    @State private var password: String = ""
    @State private var isTesting = false
    @State private var testResult: TestResult?

    var isInitialSetup: Bool = false

    enum TestResult {
        case success
        case failure(String)
    }

    var body: some View {
        Form {
            Section {
                TextField("Server URL", text: $serverURL)
                    .textContentType(.URL)
                    .keyboardType(.URL)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)

                TextField("Username", text: $username)
                    .textContentType(.username)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)

                SecureField("Password", text: $password)
                    .textContentType(.password)
            } header: {
                Text("Server Connection")
            } footer: {
                Text("Enter your Subsonic server URL (e.g., https://music.example.com)")
            }

            Section {
                Button {
                    testConnection()
                } label: {
                    HStack {
                        Text("Test Connection")
                        Spacer()
                        if isTesting {
                            ProgressView()
                        } else if let result = testResult {
                            switch result {
                            case .success:
                                Image(systemName: "checkmark.circle.fill")
                                    .foregroundStyle(.green)
                            case .failure:
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundStyle(.red)
                            }
                        }
                    }
                }
                .disabled(isTesting || !isFormValid)

                if case .failure(let message) = testResult {
                    Text(message)
                        .font(.caption)
                        .foregroundStyle(.red)
                }
            }

            Section {
                Button("Save") {
                    saveConfiguration()
                }
                .disabled(!isFormValid)
                .frame(maxWidth: .infinity)
            }

            if !isInitialSetup && client.serverConfig.isConfigured {
                Section {
                    Button("Sign Out", role: .destructive) {
                        signOut()
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
        .navigationTitle(isInitialSetup ? "Connect to Server" : "Settings")
        .onAppear {
            loadCurrentConfig()
        }
    }

    private var isFormValid: Bool {
        !serverURL.trimmingCharacters(in: .whitespaces).isEmpty &&
        !username.trimmingCharacters(in: .whitespaces).isEmpty &&
        !password.isEmpty
    }

    private func loadCurrentConfig() {
        serverURL = client.serverConfig.serverURL
        username = client.serverConfig.username
        password = client.serverConfig.password
    }

    private func testConnection() {
        isTesting = true
        testResult = nil

        let config = ServerConfig(
            serverURL: serverURL.trimmingCharacters(in: .whitespaces),
            username: username.trimmingCharacters(in: .whitespaces),
            password: password
        )

        client.updateConfig(config)

        Task {
            let success = await client.testConnection()

            await MainActor.run {
                isTesting = false
                testResult = success ? .success : .failure("Could not connect to server. Check your URL and credentials.")
            }
        }
    }

    private func saveConfiguration() {
        let config = ServerConfig(
            serverURL: serverURL.trimmingCharacters(in: .whitespaces),
            username: username.trimmingCharacters(in: .whitespaces),
            password: password
        )

        client.updateConfig(config)

        if isInitialSetup {
            dismiss()
        }
    }

    private func signOut() {
        KeychainService.shared.deleteServerConfig()
        client.updateConfig(.empty)
        client.isConnected = false
        serverURL = ""
        username = ""
        password = ""
        testResult = nil
    }
}

#Preview {
    NavigationStack {
        ServerSetupView()
    }
}
