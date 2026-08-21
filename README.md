# 🎵 Odette — Modern Local Music Player for Android

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-purple.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-9.3.1-green.svg?style=flat-square&logo=android)](https://developer.android.com/studio/releases/gradle-plugin)
[![Jetpack Compose](https://img.shields.io/badge/Compose-Material%203-4285F4.svg?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Media3](https://img.shields.io/badge/AndroidX-Media3%201.11.0-orange.svg?style=flat-square)](https://developer.android.com/media/media3)
[![Room Database](https://img.shields.io/badge/Room-2.8.4-blue.svg?style=flat-square)](https://developer.android.com/training/data-storage/room)
[![Hilt](https://img.shields.io/badge/Dagger-Hilt%202.60.1-red.svg?style=flat-square)](https://dagger.dev/hilt/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](LICENSE)

**Odette** is an elegant, privacy-first, local-only Android music player engineered with modern Android architecture. Built from the ground up using **Jetpack Compose (Material 3)**, **AndroidX Media3 (ExoPlayer + MediaSessionService)**, and **Clean Architecture**, Odette provides a seamless, robust, and beautiful playback experience for your offline audio library.

---

## ✨ Features

### 🎧 Core Playback Engine (Media3)
- **Foreground Playback Service**: Uninterrupted audio playback that survives Activity lifecycle destruction, screen-off, and app switching via AndroidX `MediaSessionService`.
- **System Media Controls**: Full integration with the Android Notification Shade (with rich artwork, squiggly seekbar, and media action buttons) and Lock Screen controls.
- **Audio Becoming Noisy**: Automatically pauses playback when headphones or Bluetooth audio devices are disconnected.
- **Queue, Shuffle & Repeat**: Support for dynamic playback queues, full shuffle modes, repeat-all, repeat-one, and repeat-off.
- **Customizable Fast Seek**: Quick jump controls with user-configurable skip intervals (5s, 10s, 15s, 30s).
- **Session State Restoration**: Automatically persists and restores the active queue, last playing track, position, and shuffle/repeat state across app restarts.

### 📚 Library & Discovery
- **MediaStore Discovery**: Fast indexing of on-device music, albums, artists, and genres with high-resolution album artwork loading via Coil.
- **Instant Search**: Real-time debounce search across songs, artists, albums, and genres.
- **Direct Filtering**: Filter and play songs directly by Album, Artist, or Genre.

### 🗄️ Local Data & Organization (Room)
- **Playlists Management**: Create, rename, delete, and reorder playlists with drag-and-drop song ordering.
- **Favorites**: One-tap favoriting with dedicated Favorites screen.
- **Recently Played & History**: Automated playback history tracking with play count metrics and a "Jump Back In" home feed.
- **100% Offline & Private**: Zero network requests, zero telemetry, and zero tracking. All user metadata is securely stored in local Room SQLite databases.

### 🎨 Material You UI & Theming
- **Dynamic Color**: Seamlessly adapts to your device wallpaper color palette on Android 12+ (Monet).
- **Theme Modes**: Full support for System Default, Dark Mode (OLED-friendly), and Light Mode.
- **MiniPlayer & Full Player Sheet**: Interactive swipeable bottom player sheet with live seekbar scrub, duration timers, and queue viewer.

---

## 🏗️ Architecture

Odette follows the official **Android Architecture Guidelines** and **Clean Architecture** principles:

```
                      ┌──────────────────────────────┐
                      │    Presentation (Compose)    │
                      │  Screens, ViewModels, Theme  │
                      └──────────────┬───────────────┘
                                     │
                                     ▼
                      ┌──────────────────────────────┐
                      │         Domain Layer         │
                      │   Models, Use Cases, Repos   │
                      └──────────────┬───────────────┘
                                     │
                    ┌────────────────┴────────────────┐
                    ▼                                 ▼
    ┌──────────────────────────────┐  ┌──────────────────────────────┐
    │     Data / Local Storage     │  │   Player Engine (Media3)     │
    │  MediaStore, Room Database   │  │ PlaybackService, Controller  │
    └──────────────────────────────┘  └──────────────────────────────┘
```

- **Presentation Layer**: 100% Declarative UI with Jetpack Compose and Material 3 design tokens. Unidirectional Data Flow (UDF) powered by `StateFlow` and Coroutines.
- **Domain Layer**: Clean business logic encapsulated into focused Use Cases (`PlaySongUseCase`, `ToggleFavoriteUseCase`, `CreatePlaylistUseCase`, etc.).
- **Data Layer**: Room Database for user-specific metadata and `MediaStoreDataSource` for device audio scanning.
- **Player Layer**: Media3 `MediaSessionService` running in an isolated foreground service bound via IPC to `MusicPlayerController`.
- **Dependency Injection**: Dagger Hilt for modular, testable, and maintainable architecture.

---

## 📂 Project Structure

```text
com.kunvarpreet.odette/
├── data/
│   ├── datasource/        # MediaStore audio scanner & local file queries
│   ├── local/             # Room Database, DAOs (Favorite, Playlist, History), and Entities
│   └── repository/        # Repository implementations & DataStore user preferences
├── di/                    # Dagger Hilt modules (App, Database, Player, Repository)
├── domain/
│   ├── model/             # Core immutable domain models (Song, Album, Artist, Playlist)
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Domain business use cases
├── player/                # AndroidX Media3 PlaybackService, MediaItemMapper, and Controller
└── ui/
    ├── components/        # Reusable Compose widgets (Artwork, SongCard, MiniPlayer)
    ├── favorites/         # Favorites screen
    ├── history/           # Recently Played history screen
    ├── home/              # Home dashboard with Quick Picks and Jump Back In
    ├── library/           # Library tabs (Songs, Albums, Artists, Genres)
    ├── main/              # Root container, Navigation bars, and MainViewModel
    ├── navigation/        # Compose Navigation routes & NavGraph
    ├── player/            # FullPlayerSheet & playback controls
    ├── playlists/         # Playlist management & playlist details
    ├── search/            # Instant search screen
    ├── settings/          # Theme, dynamic colors, skip intervals, and about
    └── theme/             # Material 3 Color Schemes, Typography, and Shapes
```

---

## 🔒 Permissions & Security

Odette strictly adheres to the principle of least privilege:

| Permission | Purpose | API Level |
| :--- | :--- | :--- |
| `android.permission.READ_MEDIA_AUDIO` | Required to query and read local audio files from storage | Android 13+ (API 33+) |
| `android.permission.READ_EXTERNAL_STORAGE` | Legacy permission for reading audio files from shared storage | Android 12 & below |
| `android.permission.POST_NOTIFICATIONS` | Displays the system media notification and lock screen controls | Android 13+ (API 33+) |
| `android.permission.FOREGROUND_SERVICE` | Enables persistent background audio playback when the app is closed | Android 9+ (API 28+) |
| `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Declares foreground service type for media playback compliance | Android 14+ (API 34+) |
