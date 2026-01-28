import Foundation
import SwiftUI

extension URL {
    func appendingQueryItems(_ items: [String: String]) -> URL {
        var components = URLComponents(url: self, resolvingAgainstBaseURL: false)
        var queryItems = components?.queryItems ?? []
        for (key, value) in items {
            queryItems.append(URLQueryItem(name: key, value: value))
        }
        components?.queryItems = queryItems
        return components?.url ?? self
    }
}

extension Int {
    var formattedDuration: String {
        let minutes = self / 60
        let seconds = self % 60
        return String(format: "%d:%02d", minutes, seconds)
    }

    var formattedFileSize: String {
        let kb = Double(self) / 1024
        if kb < 1024 {
            return String(format: "%.1f KB", kb)
        }
        let mb = kb / 1024
        if mb < 1024 {
            return String(format: "%.1f MB", mb)
        }
        let gb = mb / 1024
        return String(format: "%.2f GB", gb)
    }
}

extension View {
    func hideKeyboard() {
        UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    }
}

extension Color {
    static let accentRed = Color(red: 150/255, green: 21/255, blue: 21/255)
    static let accentYellow = Color(red: 228/255, green: 242/255, blue: 61/255)
}
