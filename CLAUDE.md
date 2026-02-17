# CLAUDE.md

## Project Overview

SmashSonic is a music streaming app for Subsonic-compatible servers (Navidrome, Airsonic, etc.) with native clients for iOS (SwiftUI) and Android (Jetpack Compose).

## Repository Layout

- `SmashSonic/` — iOS app (SwiftUI, iOS 17+)
- `SmashSonic.xcodeproj/` — Xcode project
- `SmashSonic-Android/` — Android app (Jetpack Compose, API 29+)

## Android Development Environment

### Java

- **JDK 21** is required — must be the full JDK with `javac`, not just JRE
- On Fedora: `dnf install java-21-openjdk-devel`
- Gradle needs `org.gradle.java.home=/usr/lib/jvm/java-21-openjdk` in `gradle.properties` for toolchain detection on Fedora

### Android SDK

- SDK location: `/root/AndroidSDK`
- Environment configured in `/etc/profile.d/AndroidSDK`:
  ```
  export ANDROID_HOME=$HOME/AndroidSDK
  export ANDROID_SDK_ROOT=$ANDROID_HOME
  PATH includes: cmdline-tools/latest/bin, platform-tools
  ```
- Required packages: `platforms;android-35`, `build-tools;35.0.0`, `platform-tools`
- `local.properties` must point to the SDK: `sdk.dir=/root/AndroidSDK`

### Build Commands

```bash
source /etc/profile.d/AndroidSDK
cd SmashSonic-Android

# Debug build
./gradlew assembleDebug

# Signed release build
./gradlew assembleRelease
```

APKs output to `app/build/outputs/apk/{debug,release}/`.

### Signing

- Keystore: `SmashSonic-Android/smashsonic-release.jks`
- Key alias: `smashsonic`
- Signing config is in `app/build.gradle.kts`
- The `.jks` file is in `.gitignore` — do NOT commit it

### Key Libraries & Versions

- **Gradle**: 8.10.2 (wrapper)
- **AGP**: 8.8.2
- **Kotlin**: 2.1.10
- **Compose BOM**: 2025.02.00
- **Hilt**: 2.55 (DI)
- **Media3**: 1.6.0 (ExoPlayer for audio)
- **Retrofit + OkHttp**: Networking
- **Moshi**: JSON parsing
- **Room**: Local database (downloads, liked songs)
- **Coil 3**: Image loading (uses `SubcomposeAsyncImage` for composable error fallbacks, `OkHttpNetworkFetcherFactory` for auth)
- **DataStore**: Preferences storage

### Known Quirks

- `Image(painter=...)` in Compose does NOT accept `filterQuality` — that parameter only exists on `Image(bitmap=...)`
- Coil 3 `ImageLoader.Builder` does not have `.okHttpClient()` — use `.components { add(OkHttpNetworkFetcherFactory(...)) }`
- Coil 3 `AsyncImage` `error` parameter expects `Painter?`, not a composable — use `SubcomposeAsyncImage` for composable error content
- Hilt version 2.54.1 does not exist; use 2.54 or 2.55+
- Fedora's `java-21-openjdk` package (without `-devel`) lacks `javac`, causing Gradle toolchain detection to fail with "does not provide the required capabilities: [JAVA_COMPILER]"
