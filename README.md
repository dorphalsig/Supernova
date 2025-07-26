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
- Mobile/tablet companion app (roadmap)

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
- **UI Framework**: Jetpack Compose (XML migration in progress)
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

## Development Setup

### Prerequisites

- Android Studio Hedgehog+ (2023.1.1+)
- JDK 11+
- Android SDK 26+ (minimum), target SDK 34
- Git

### Build Instructions

```bash
# Clone the repository
git clone https://github.com/username/supernova.git
cd supernova

# Make build script executable
chmod +x ./build.py

# Clean build and run tests
./build.py clean :app:assembleDebug :app:testDebugUnitTest
```

### Development Workflow

The project uses an orchestrated development approach with atomic, conflict-free tasks:

1. **Assessment Phase**: Analyze project state, errors, and risks
2. **Task Planning**: Generate parallel, atomic tasks (max 7 per wave)
3. **Implementation**: Each task = 1 PR with comprehensive tests
4. **Validation**: Automatic build verification and coverage checks

### Testing Strategy

- **Unit Tests**: JUnit5 + MockK + kotlinx-coroutines-test
- **Coverage Requirement**: ≥70% lines & branches for changed logic
- **Integration Tests**: Room in-memory database testing
- **UI Tests**: Compose test framework (Espresso being phased out)
- **No Robolectric**: Gradual removal in favor of pure unit tests

### Code Quality

- **Kotlin Code Style**: Follow official Kotlin conventions
- **Documentation**: KDoc for all public APIs
- **Lint**: Baseline configuration with CI enforcement
- **Coverage**: Jacoco reporting with threshold enforcement

## Project Structure

```
app/
├── src/main/java/com/supernova/
│   ├── data/           # Data layer (entities, DAOs, repositories)
│   ├── search/         # Search integration and voice services
│   ├── ui/             # Compose screens and components
│   │   ├── components/ # Reusable UI components (SearchBar, voice controls)
│   │   └── screens/    # Main application screens
│   ├── domain/         # Business logic and use cases
│   ├── network/        # API clients and DTOs
│   ├── sync/           # Background sync workers
│   └── utils/          # Shared utilities
├── src/test/           # Unit tests
└── src/androidTest/    # Integration tests (minimal)
```

## API Integration

### XC IPTV API Support

Supernova supports standard XC API endpoints:

- `get_live_categories` - Live TV categories
- `get_vod_streams` - Video on demand content
- `get_series` - TV series metadata
- `get_series_info` - Detailed series information
- `get_vod_info` - VOD details with TMDB integration
- EPG endpoints for electronic program guide

### TMDB Integration

- Automatic metadata enrichment for movies and series
- Star ratings and movie information
- Personalized recommendations based on viewing history
- Trending content discovery

## Configuration

### Build Variants

- **Debug**: Development build with logging and debug tools
- **Release**: Production build with ProGuard optimization

### Environment Setup

1. Add your TMDB API key to `local.properties`:
   ```properties
   tmdb.api.key=your_api_key_here
   ```

2. Configure provider settings in the app or via intent extras

## Contributing

### Development Policies

- **No XML for new screens** - Use Jetpack Compose
- **No new Robolectric tests** - Prefer pure unit tests
- **Spec-first development** - Architecture changes require documentation updates
- **Atomic commits** - Each PR should be self-contained and conflict-free

### Testing Requirements

- All new logic must have unit tests with ≥70% coverage
- Use MockK for mocking dependencies
- Test naming: `<Subject><Behavior>Test`
- No Thread.sleep in tests - use TestDispatcher

### Code Review Checklist

- ✅ Compiles successfully
- ✅ All tests pass
- ✅ Coverage threshold met
- ✅ KDoc documentation added
- ✅ No unrelated changes
- ✅ Symbol scope declared
- ✅ No flakiness patterns

## Architecture Decisions

### Migration Strategy

- **From XML to Compose**: Gradual migration of UI components
- **From Robolectric**: Moving to pure unit tests and Compose testing
- **Room FTS4**: Using FTS4 for broader compatibility over FTS5

### Performance Considerations

- **Streaming JSON parsing**: Constant memory usage during large syncs
- **Image preloading**: LRU cache with 2x viewport preloading
- **Parallel sync**: Configurable parallelism (default: 5 concurrent)
- **Memory optimization**: Kotlin/JVM flags optimized for containers

### Data Persistence

- **Local-first**: All data stored in Room database
- **Sync coordination**: Atomic updates with rollback capability
- **UTF-8 encoding**: Consistent character encoding throughout
- **Minimal server dependency**: Self-contained operation

## Troubleshooting

### Common Issues

**Build failures**:
```bash
# Clean and rebuild
./build.py clean :app:assembleDebug
```

**Test failures**:
```bash
# Run specific test suite
./build.py :app:testDebugUnitTest --tests "*.StreamDaoTest"
```

**Sync issues**:
- Check provider API endpoints
- Verify network connectivity
- Review WorkManager logs

### Debug Tools

- **Database Inspector**: Use Android Studio's database inspector
- **Network Profiler**: Monitor API calls and responses
- **Compose Inspector**: Debug UI hierarchy and state

## License

[License information to be added]

## Support

For technical issues and feature requests, please use the GitHub issue tracker.

---

**Note**: This project follows a strict orchestrated development model with AI-assisted task generation and conflict-free parallel development workflows.

Ref #38
