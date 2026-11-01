# Brainstorm: Android ↔ iOS Feature Parity

**Date:** 2026-10-31
**Status:** Ready for planning
**Author:** Andy Madsen (with Claude)

## What We're Building

Bring the Android app (Jetpack Compose) up to feature parity with the iOS app
(SwiftUI), which has moved ahead with recent work. A full screen-by-screen audit
found the apps are at **near-complete parity** already — the Android app landed in
a single large commit with the full Subsonic client, playback, downloads, search,
browse, playlists, appearance themes, and song favorites.

**The one substantive gap: favorited albums & artists + a real "Favorites" screen.**

Today Android stars **songs only**. The repository even discards the album and
artist arrays that `getStarred2` already returns, and only stars by song `id`. iOS
recently added album/artist favorites (server-backed) and a three-section Favorites
screen (Artists · Albums · Songs).

## Parity Audit Summary

Everything below is already at parity on Android:
- Subsonic API surface (20 endpoints, 1:1 with iOS)
- Server setup / auth (ServerSetupScreen + CredentialManager)
- Browse (artists/albums/songs) + Album/Artist detail
- Playlists + PlaylistDetailScreen
- Search
- Now Playing with a seekable `Slider` (uses an `isSeeking`/`seekPosition` guard —
  functionally equivalent to iOS's recent scrubber fixes)
- Mini player, Queue
- Play modes, track radio, auto-add random songs
- Downloads / offline (DownloadService + Room)
- Appearance: identical `BackgroundType` set (none, 6 solids, 4 pixel-art images)
- Favorited **songs** (local: Room on Android, SwiftData on iOS)

**Gaps:**
1. **Favorited albums & artists** — not implemented on Android.
2. **Favorites screen** — Android's "Liked" tab shows songs only; iOS shows a
   three-section Favorites screen with an empty state and pull-to-sync.

**Cosmetic deltas (minor):**
- Tab/screen is labeled "Liked"; iOS says "Favorites".
- Android's scrub **time label** doesn't follow the thumb while dragging (iOS does).

## Why This Approach

**Decision: server-backed album/artist favorites (match iOS exactly).**

Album/artist favorites live server-side via `star`/`unstar` + `getStarred2`,
synced on launch/refresh and held in memory via a StateFlow in the likes
repository/ViewModel. Songs stay in Room as they are today. **No new DB tables.**

Why this over the alternatives:
- **Local-first (new Room tables):** full offline favorites, but adds a DB
  migration, write-through, and sync-conflict handling — more code, and it diverges
  from iOS. YAGNI for now.
- **Hybrid (server truth + Room cache):** instant/offline render, but extra caching
  code for marginal benefit.
- Matching iOS keeps the two apps' mental models identical and is the smallest
  change that closes the gap. The Subsonic endpoints are already wired on Android.

## Key Decisions

- **Source of truth:** server for album/artist favorites; local Room for song
  favorites (unchanged). Mirrors iOS.
- **No new persistence:** album/artist favorites are not stored in Room.
- **Favorites screen:** expand the current "Liked" screen into a three-section
  Favorites screen — Artists (horizontal chips/cards), Albums (cards), Songs (rows)
  — with an empty state and pull-to-refresh that syncs starred state from the
  server. Mirrors iOS's `LikedSongsView` (now the Favorites screen).
- **Star toggles:** add star/unstar controls on Album detail and Artist detail
  screens (matching iOS), backed by the likes repository.
- **Subsonic star semantics:** album stars use `id` (or `albumId`); artist stars
  require the `artistId` param. The current Android `star(id)` signature must grow
  to cover album and artist starring. `getStarred2` parsing must read `starred2`'s
  `album` and `artist` arrays, not just `song`.

## Resolved Questions

- **Data model?** → Server-backed, match iOS. No new Room tables. (User decision.)
- **Scope?** → Full parity audit first (done); the audit shows favorites is the only
  substantive gap. (User decision.)
- **Rename "Liked" → "Favorites"?** → Yes. Tab and screen title become "Favorites"
  (heart icon stays), matching iOS. (User decision.)
- **Scrub-time-label polish?** → Include now. The Now Playing current-time label
  will follow the thumb while dragging, matching iOS. (User decision.)

## Scope of the Plan

1. **Data layer:** extend `getStarred2` parsing to read `album` + `artist` arrays;
   add album/artist star/unstar (artists via `artistId`); extend the likes
   repository with `starredAlbums`/`starredArtists` StateFlows synced from the
   server.
2. **Favorites screen:** rebuild the "Liked" screen as a three-section "Favorites"
   screen (Artists · Albums · Songs) with empty state and pull-to-refresh; rename
   the tab to "Favorites".
3. **Star toggles:** add star controls to Album detail and Artist detail screens.
4. **Scrubber polish:** current-time label follows the thumb while dragging.
