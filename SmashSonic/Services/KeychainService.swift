import Foundation
import Security

final class KeychainService {
    static let shared = KeychainService()
    private let serviceName = "SubsonicSwift"

    private init() {}

    func saveServerConfig(_ config: ServerConfig) throws {
        let data = try JSONEncoder().encode(config)
        try save(data, forKey: "serverConfig")
    }

    func loadServerConfig() -> ServerConfig? {
        guard let data = load(forKey: "serverConfig") else { return nil }
        return try? JSONDecoder().decode(ServerConfig.self, from: data)
    }

    func deleteServerConfig() {
        delete(forKey: "serverConfig")
    }

    private func save(_ data: Data, forKey key: String) throws {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: key,
            kSecValueData as String: data
        ]

        SecItemDelete(query as CFDictionary)

        let status = SecItemAdd(query as CFDictionary, nil)
        guard status == errSecSuccess else {
            throw KeychainError.unableToSave
        }
    }

    private func load(forKey key: String) -> Data? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)

        guard status == errSecSuccess else { return nil }
        return result as? Data
    }

    private func delete(forKey key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: serviceName,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
    }

    enum KeychainError: Error {
        case unableToSave
        case unableToLoad
    }
}
