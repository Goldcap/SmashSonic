import Foundation
import CryptoKit

struct SubsonicAuth {
    static let apiVersion = "1.16.1"
    static let clientName = "SubsonicSwift"

    static func generateSalt() -> String {
        let characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return String((0..<12).compactMap { _ in characters.randomElement() })
    }

    static func generateToken(password: String, salt: String) -> String {
        let data = Data((password + salt).utf8)
        let hash = Insecure.MD5.hash(data: data)
        return hash.map { String(format: "%02x", $0) }.joined()
    }

    static func authParams(username: String, password: String) -> [String: String] {
        let salt = generateSalt()
        let token = generateToken(password: password, salt: salt)

        return [
            "u": username,
            "t": token,
            "s": salt,
            "v": apiVersion,
            "c": clientName,
            "f": "json"
        ]
    }
}
