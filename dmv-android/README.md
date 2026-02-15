# Texas DMV Practice - Android App

An offline-first Android application for Texas driver's license practice tests, built with Kotlin and Jetpack Compose. Features 660 original questions across 8 topics, SVG sign illustrations rendered via Coil, Room-backed stats tracking, and four quiz modes (Practice, Exam, Topic Drill, Mistakes). No internet connection required -- all content is bundled in-app.

## Prerequisites

- **JDK 17** (OpenJDK recommended)
- **Android SDK** with platform 35 and build-tools 35.0.0
  - Install via Android Studio or command-line: `sdkmanager "platforms;android-35" "build-tools;35.0.0"`
- **Gradle 8.11.1** (bundled via wrapper -- no manual install needed)

No Android Studio is required for building; the project builds from the command line.

## Build & Run

```bash
cd dmv-android

# Build debug APK
./gradlew assembleDebug

# Install on connected device or emulator
./gradlew installDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk` (~11 MB).

To build in Android Studio: open `dmv-android/` as the project root, let Gradle sync, and press Run.

## Project Structure

```
dmv-android/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/dmv/texas/
│   │   │   ├── DMVApp.kt                     # Application class (DB singleton, Coil ImageLoader)
│   │   │   ├── MainActivity.kt               # Single-Activity entry point
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── dao/                   # Room DAOs (5 files)
│   │   │   │   │   ├── db/                    # DMVDatabase, TypeConverters
│   │   │   │   │   └── entity/                # Room entities (5 files)
│   │   │   │   ├── model/                     # Data classes (PackJson, QuizConfig, QuizMode, AssetManifestEntry)
│   │   │   │   └── repository/                # QuestionRepository, StatsRepository
│   │   │   ├── import_/                       # PackImporter (asset scanning + Room import)
│   │   │   ├── ui/
│   │   │   │   ├── component/                 # Reusable composables (AnswerButton, QuestionImage, TimerDisplay, etc.)
│   │   │   │   ├── navigation/                # NavGraph, Screen sealed class
│   │   │   │   ├── screen/
│   │   │   │   │   ├── debug/                 # Debug dashboard
│   │   │   │   │   ├── home/                  # Quiz configuration screen
│   │   │   │   │   ├── import_/               # Pack import with progress
│   │   │   │   │   ├── quiz/                  # Quiz experience
│   │   │   │   │   ├── results/               # Score + missed questions review
│   │   │   │   │   └── stats/                 # Accuracy tracking + attempt history
│   │   │   │   └── theme/                     # Material 3 theming (Color, Type, Theme)
│   │   │   └── util/                          # AssetResolver, Constants
│   │   ├── res/values/                        # XML resources (themes, colors, strings)
│   │   ├── assets/
│   │   │   ├── packs/TX/tx_v1.json            # Question bank (660 questions)
│   │   │   ├── assets_manifest.json           # SVG asset registry
│   │   │   └── svg/                           # 109+ SVG sign illustrations
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts                       # App-level Gradle config
│   └── proguard-rules.pro                     # ProGuard/R8 rules for release builds
├── build.gradle.kts                           # Root Gradle config (plugin versions)
├── settings.gradle.kts                        # Project settings
├── gradle/wrapper/                            # Gradle wrapper (8.11.1)
└── README.md
```

## Tech Stack

| Component | Version | Purpose |
|---|---|---|
| Kotlin | 2.1.0 | Language |
| Jetpack Compose | BOM 2025.01.01 | UI framework (Material 3) |
| Room | 2.7.0 | Local SQLite database |
| Navigation Compose | 2.8.5 | Screen navigation |
| Coil 3 | 3.1.0 | SVG image loading (`coil-compose` + `coil-svg`) |
| kotlinx-serialization | 1.8.0 | JSON parsing |
| KSP | 2.1.0-1.0.29 | Annotation processing (Room compiler) |
| AGP | 8.7.3 | Android build tooling |

**No networking libraries.** The app is fully offline -- no Retrofit, OkHttp, or internet permission.

## Architecture

The app follows a 3-layer architecture with a single Activity:

- **UI layer** (`ui/`): Compose screens + ViewModels exposing `StateFlow`
- **Data layer** (`data/`): Room entities, DAOs, repositories, and JSON models
- **Import layer** (`import_/`): Pack scanner that reads bundled JSON and populates Room

Key patterns:
- **Navigation Compose** with a nested `quiz_flow` graph scoping the shared `QuizViewModel` across Quiz and Results screens
- **Manual DI** via `Application`-level database singleton (no Hilt/Dagger)
- **Quiz config** passed through `DMVApp.pendingQuizConfig` (not serialized into routes)
- **Coil 3** with global `SingletonImageLoader.Factory` for SVG decoding

## Quiz Modes

| Mode | Behavior |
|---|---|
| Practice | Untimed, immediate feedback + explanation after each answer |
| Exam | 30-minute timer, feedback only at end, auto-submits on timeout |
| Topic Drill | Filter by one or more topics |
| Mistakes | Re-quiz on previously missed questions, ordered by error rate |

## How to Add a New State Pack

Adding support for a new state (e.g., California) requires **no code changes**. The import pipeline auto-discovers packs.

1. **Create the question JSON** following the pack format:
   ```
   app/src/main/assets/packs/CA/ca_v1.json
   ```
   The JSON must include `stateCode`, `version`, `totalQuestions`, `topics`, `generatedDate`, and a `questions` array. See `packs/TX/tx_v1.json` for reference.

2. **Add SVG assets** (if any questions reference images):
   ```
   app/src/main/assets/svg/<assetId>.svg
   ```

3. **Update the asset manifest** (`assets/assets_manifest.json`) with entries for any new SVGs:
   ```json
   { "assetId": "ca-stop-sign", "description": "Stop sign", "file": "assets/svg/ca-stop-sign.svg", "sourceUrl": "", "license": "" }
   ```

4. **Build and run.** On first launch, `PackImporter` will scan `assets/packs/CA/`, parse the JSON, and import all questions into Room. The import is idempotent -- re-running with the same version produces no duplicates.

5. **Version upgrades:** To update a pack, increment `version` in the JSON. The importer only re-imports when the bundled version exceeds the installed version.

## Tests

Instrumented tests (run on device/emulator via `./gradlew connectedDebugAndroidTest`):

- **`PackImporterTest`** (7 tests): insert + count, upsert idempotency, upsert replaces fields, soft-delete reimport, deactivate isolation, version store/retrieve/upsert/null
- **`QuizGeneratorTest`** (12 tests): topic filter (single + multi), difficulty range, limit, no duplicates, isActive filtering, empty results, combined filters

Tests use `Room.inMemoryDatabaseBuilder` with `allowMainThreadQueries()`.

## Known Limitations / Future Work

- **Single state hardcoded in UI.** While the data layer supports multiple states, the UI currently uses `stateCode = "TX"` throughout. A state selection screen is needed for multi-state support.
- **No process death recovery for in-progress quizzes.** `pendingQuizConfig` lives on the `Application` object, which does not survive process death. A graceful fallback navigates the user back to Home.
- **No question bookmarking.** Users cannot save individual questions for later review.
- **No search or filter within stats.** The stats screen shows all-time data without date filtering.
- **Release builds** require ProGuard/R8 testing -- only debug builds have been verified.

## Content

- **660 questions** across 8 topics: Signs, Traffic Signals, Pavement Markings, Right of Way, Speed & Distance, Parking, Safe Driving, Special Situations
- **109 SVG illustrations** of traffic signs, road scenarios, and driving situations
- All content is original, based on Texas Transportation Code and MUTCD standards

## Legal

This application is for educational purposes only and is not affiliated with or endorsed by the Texas Department of Public Safety (DPS).
