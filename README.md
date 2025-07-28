# Supernova IPTV

**Provider-agnostic IPTV client for Android TV platforms**

## Overview 

Supernova is a modern IPTV client designed specifically for Android TV and related platforms. It provides fast, resilient catalog/EPG ingest, on-device search, and personalized recommendations while maintaining compatibility with any XC API-compatible provider.
Supernova is designed by ChatGPT and built by OpenAI Codex. I have no association with these companies.

### Key Features

- 🔄 **Fast Sync**: Memory-efficient streaming JSON ingest for large catalogs
- 🔍 **Full-Text Search**: On-device FTS4 search across all content with voice support
- 🎤 **Voice Control**: Android TV speech recognition with fallback to manual input
- 🔍 **Search-First UI**: Prominent search functionality across all screens
- 📺 **Multi-Platform**: Android TV, Chromecast with Google TV, Fire TV
- 🎯 **Personalized**: TMDB-powered recommendations and watch history
- 🎮 **TV-Optimized**: D-pad navigation with Jetpack Compose UI
- 🛡️ **Resilient**: Handles partial/malformed provider data gracefully

## Target Platforms

- Android TV (Primary)
- Chromecast with Google TV
- Amazon Fire TV (Leanback UI compatible)
- Modern Android TV boxes


## Key Features in Detail

### Search-First Experience

Supernova prioritizes search functionality throughout the application:

- **Global Voice Search**: Android TV speech recognition accessible from any screen
- **Smart Search Integration**: Unified search across all content types (movies, series, live TV, EPG)
- **Prominent Search UI**: Search bars with voice buttons featured prominently on every screen
- **Fuzzy Search Logic**: Handles typos, diacritics, and partial matches for better user experience
- **Search State Management**: Proper loading, success, and error states with graceful fallbacks

### Voice Control Integration

Built specifically for Android TV remote voice capabilities:

- **Native Speech Recognition**: Leverages Android TV's built-in speech recognition
- **Voice Search Service**: Dedicated service for handling voice input and transcription
- **Fallback Strategy**: Seamless transition to manual text input when voice fails
- **Visual Feedback**: Clear indication of voice listening state and transcription results
- **Search Result Integration**: Voice queries use the same pipeline as text search

### TV-Optimized Navigation

- **D-pad First**: All UI elements are focusable and navigable with TV remote
- **Voice Shortcuts**: Quick voice search access throughout the application  
- **Search-Centric Design**: Search functionality is never more than one button press away
- **Mini EPG Overlay**: 5-channel EPG with fuzzy search during live playback

## Architecture

### Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Database**: Room with FTS4 search
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil for Compose, Glide for legacy views
- **Background Work**: WorkManager
- **Video Playback**: ExoPlayer/Media3

### Core Components

#### Search & Voice Architecture
- **Voice Integration**: Android TV speech recognition with `VoiceSearchService`
- **Search-First Design**: Prominent search bars and voice controls on every screen
- **Multi-Source Search**: Parallel search across streams, movies, series, channels, EPG
- **Smart Normalization**: Diacritics, punctuation removal, and fuzzy matching
- **Voice Fallback**: Manual text input when speech recognition fails
- **Search State Management**: Loading, success, and error states with proper UX
- **Debounced Input**: 300ms debounce to prevent excessive search queries

#### Data Layer
- **Entities**: Streams, Categories, Episodes, EPG, User Profiles, Watch History
- **DAOs**: Type-safe database access with coroutines support
- **Repositories**: Data source abstraction with sync coordination
- **FTS Integration**: Full-text search for streams and EPG programmes

#### Search & Voice Architecture
- **Voice Integration**: Android TV speech recognition with `VoiceSearchService`
- **Search-First Design**: Prominent search bars and voice controls on every screen
- **Multi-Source Search**: Parallel search across streams, movies, series, channels, EPG
- **Smart Normalization**: Diacritics, punctuation removal, and fuzzy matching
- **Voice Fallback**: Manual text input when speech recognition fails
- **Search State Management**: Loading, success, and error states with proper UX
- **Debounced Input**: 300ms debounce to prevent excessive search queries

#### Sync Architecture
- **Parallel Workers**: Concurrent category syncing with error resilience
- **Streaming Parser**: Constant memory JSON processing for large catalogs
- **Conflict Resolution**: Atomic data swaps with rollback capability
- **Provider Agnostic**: Works with any XC API-compatible IPTV service

#### UI Architecture
- **Compose First**: Modern declarative UI for new screens
- **Search-Centric Navigation**: Search bars prominent on every screen with voice buttons
- **TV Navigation**: Focus-aware D-pad navigation patterns with voice shortcuts
- **Voice Integration**: Speech-to-text with visual feedback and error handling
- **Responsive Design**: Adaptive layouts for different screen sizes
- **Material Design**: TV-optimized Material 3 components with voice affordances
