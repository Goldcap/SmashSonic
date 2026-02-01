import SwiftUI

struct LaunchScreenView: View {
    private static let placeholderImages = ["PlaceholderVinyl", "PlaceholderCassette", "PlaceholderCD", "PlaceholderBoombox"]

    @State private var currentImageIndex = 0
    @State private var loadingDots = ""
    @State private var opacity: Double = 1.0
    @Binding var isFinished: Bool

    let timer = Timer.publish(every: 0.4, on: .main, in: .common).autoconnect()
    let dotTimer = Timer.publish(every: 0.3, on: .main, in: .common).autoconnect()

    var body: some View {
        ZStack {
            Color.black
                .ignoresSafeArea()

            VStack(spacing: 32) {
                Spacer()

                Image(Self.placeholderImages[currentImageIndex])
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 180, height: 180)
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                Text("SmashSonic")
                    .font(.system(size: 36, weight: .bold, design: .monospaced))
                    .foregroundStyle(.white)

                HStack(spacing: 8) {
                    ForEach(0..<4) { index in
                        Rectangle()
                            .fill(index <= currentImageIndex ? Color.accentColor : Color.gray.opacity(0.3))
                            .frame(width: 12, height: 12)
                    }
                }

                Text("Loading\(loadingDots)")
                    .font(.system(size: 16, weight: .medium, design: .monospaced))
                    .foregroundStyle(.gray)
                    .frame(width: 120, alignment: .leading)

                Spacer()
            }
        }
        .opacity(opacity)
        .onReceive(timer) { _ in
            withAnimation(.easeInOut(duration: 0.2)) {
                currentImageIndex = (currentImageIndex + 1) % Self.placeholderImages.count
            }
        }
        .onReceive(dotTimer) { _ in
            if loadingDots.count >= 3 {
                loadingDots = ""
            } else {
                loadingDots += "."
            }
        }
        .task {
            try? await Task.sleep(nanoseconds: 2_000_000_000)
            withAnimation(.easeOut(duration: 0.5)) {
                opacity = 0
            }
            try? await Task.sleep(nanoseconds: 500_000_000)
            isFinished = true
        }
    }
}

#Preview {
    LaunchScreenView(isFinished: .constant(false))
}
