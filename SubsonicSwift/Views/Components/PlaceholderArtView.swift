import SwiftUI

struct PlaceholderArtView: View {
    private static let placeholderImages = ["PlaceholderVinyl", "PlaceholderCassette", "PlaceholderCD", "PlaceholderBoombox"]

    private let imageName: String

    init() {
        // Use a stable random selection based on instance
        imageName = Self.placeholderImages.randomElement() ?? "PlaceholderVinyl"
    }

    var body: some View {
        Image(imageName)
            .resizable()
            .aspectRatio(contentMode: .fill)
    }
}

#Preview {
    PlaceholderArtView()
        .frame(width: 200, height: 200)
        .clipShape(RoundedRectangle(cornerRadius: 8))
}
