import SwiftUI

struct SongsView: View {
    let songs: [Song]
    @EnvironmentObject var playerViewModel: PlayerViewModel
    @StateObject private var downloadsViewModel = DownloadsViewModel()
    @StateObject private var likesViewModel = LikesViewModel()

    var body: some View {
        List(songs) { song in
            SongRow(song: song, songs: songs, downloadsViewModel: downloadsViewModel, likesViewModel: likesViewModel)
                .listRowInsets(EdgeInsets())
        }
        .listStyle(.plain)
    }
}

struct SongListItem: View {
    let song: Song
    let showAlbumArt: Bool
    @EnvironmentObject var playerViewModel: PlayerViewModel

    init(song: Song, showAlbumArt: Bool = true) {
        self.song = song
        self.showAlbumArt = showAlbumArt
    }

    var body: some View {
        HStack(spacing: 12) {
            if showAlbumArt {
                AsyncImage(url: song.coverArt.flatMap { SubsonicClient.shared.coverArtURL(for: $0, size: 100) }) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    PlaceholderArtView()
                }
                .frame(width: 50, height: 50)
                .clipShape(RoundedRectangle(cornerRadius: 4))
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(song.title)
                    .font(.body)
                    .lineLimit(1)
                    .foregroundColor(playerViewModel.currentSong?.id == song.id ? .accentColor : .primary)

                HStack(spacing: 4) {
                    if let artist = song.artist {
                        Text(artist)
                    }
                    if song.artist != nil && song.album != nil {
                        Text("·")
                    }
                    if let album = song.album {
                        Text(album)
                    }
                }
                .font(.caption)
                .foregroundStyle(.secondary)
                .lineLimit(1)
            }

            Spacer()

            Text(song.formattedDuration)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }
}
