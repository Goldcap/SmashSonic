import Foundation

struct ServerConfig: Codable, Equatable {
    var serverURL: String
    var username: String
    var password: String

    var isConfigured: Bool {
        !serverURL.isEmpty && !username.isEmpty && !password.isEmpty
    }

    var baseURL: URL? {
        var urlString = serverURL.trimmingCharacters(in: .whitespacesAndNewlines)
        if !urlString.hasPrefix("http://") && !urlString.hasPrefix("https://") {
            urlString = "https://" + urlString
        }
        if urlString.hasSuffix("/") {
            urlString = String(urlString.dropLast())
        }
        return URL(string: urlString)
    }

    static let empty = ServerConfig(serverURL: "", username: "", password: "")
}
