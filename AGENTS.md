# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

- **Name:** poche-app-android
- **Type:** Android Native (Kotlin マルチモジュール)
- **Origin:** Flutter アプリ `poche-app` の完全 Android ネイティブ化
- **Architecture:** app / core / feature 3-tier + Now in Android 準拠
- **Source Flutter Repo:** `../poche-app/` (機能仕様・ドメインモデルの参照先)

## Response Language

All responses MUST be in **Japanese**.

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Kotlin (latest stable) |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt (Dagger) |
| Async | Kotlin Coroutines + Flow |
| Database | Room |
| HTTP Client | Ktor Client |
| Serialization | Kotlinx Serialization |
| Navigation | Compose Navigation (type-safe) |
| Image Loading | Coil |
| Backend | Firebase (Auth, Analytics, Crashlytics, Messaging, Remote Config) |
| Testing | JUnit5 + Turbine + MockK |
| Build | Gradle (Kotlin DSL) + Version Catalog + Convention Plugins |

## Module Structure

Flutter 3-tier 構成を Kotlin マルチモジュールで再現し、Now in Android のパターンを適用。

```
poche-app-android/
├── app/                          # Application module (entry point)
├── build-logic/                  # Convention Plugins (共通ビルド設定)
│   └── convention/
├── core/
│   ├── common/                   # Result type, extensions, logger
│   ├── data/                     # Repository implementations
│   ├── database/                 # Room database, DAOs, entities
│   ├── datastore/                # DataStore (preferences, proto)
│   ├── designsystem/             # Theme, colors, typography, components
│   ├── domain/                   # Use cases, repository interfaces
│   ├── model/                    # Domain models (data classes)
│   ├── network/                  # Ktor client, API definitions
│   ├── testing/                  # Test utilities, fakes, rules
│   ├── analytics/                # Analytics abstraction + Firebase impl
│   ├── auth/                     # Auth abstraction + Firebase impl
│   ├── notifications/            # Notifications abstraction + Firebase impl
│   └── ui/                       # Shared composables, preview utilities
├── feature/
│   ├── capture/                  # Quick capture (memo/photo/voice)
│   ├── home/                     # Home screen
│   ├── memo/                     # Memo detail
│   ├── settings/                 # Settings
│   ├── onboarding/               # Onboarding flow
│   └── devtools/                 # Debug tools (debug build only)
├── gradle/
│   └── libs.versions.toml        # Version Catalog
├── settings.gradle.kts
└── build.gradle.kts
```

### Module Dependency Rules

- `feature/*` → `core/*` (features depend on core only, never on each other)
- `core/data` → `core/domain`, `core/database`, `core/network`, `core/datastore`
- `core/domain` → `core/model`, `core/common`
- `app` → `feature/*`, `core/*` (assembles all modules, DI wiring)
- [Forbidden] Circular dependencies between modules
- [Forbidden] `feature` → `feature` direct dependency (routing via app module)

## Build Commands

```bash
# Build
./gradlew assembleDevDebug                  # Dev flavor debug APK
./gradlew assembleProdRelease               # Prod flavor release APK

# Tests
./gradlew test                              # All unit tests
./gradlew :feature:home:testDebugUnitTest   # Single module test
./gradlew connectedAndroidTest              # Instrumented tests

# Lint & Analysis
./gradlew detekt                            # Static analysis
./gradlew lint                              # Android Lint
./gradlew spotlessCheck                     # Formatting check
./gradlew spotlessApply                     # Auto-format

# Dependency Graph
./gradlew :app:dependencies                 # Dependency tree

# Convention Plugin Development
./gradlew :build-logic:convention:test      # Build-logic tests
```

## Build Flavors

Flutter 版と同じ 3 環境構成。Firebase プロジェクトはフレーバーごとに分離。

| Flavor | Firebase Project | 用途 |
|--------|-----------------|------|
| `dev`  | poche-app-dev   | 開発用 |
| `stg`  | poche-app-stg   | ステージング (内部テスト) |
| `prod` | poche-app       | 本番 |

`google-services.json` は `app/src/{flavor}/` に配置。

## Convention Plugins (build-logic)

`build-logic/convention/` に共通ビルド設定を Convention Plugin として定義。

| Plugin ID | 用途 |
|-----------|------|
| `poche.android.application` | Application module 共通設定 |
| `poche.android.library` | Library module 共通設定 |
| `poche.android.compose` | Compose 有効化 + BOM バージョン管理 |
| `poche.android.hilt` | Hilt DI セットアップ |
| `poche.android.room` | Room セットアップ + KSP |
| `poche.android.feature` | Feature module 共通設定 (Compose + Hilt + Navigation) |
| `poche.kotlin.library` | Pure Kotlin module 設定 |
| `poche.android.testing` | テスト共通設定 (JUnit5 + MockK + Turbine) |

## Architecture Patterns

### Presentation Layer (feature modules)

- **UI State:** `StateFlow<UiState>` in ViewModel
- **Side Effects:** `SharedFlow<UiEvent>` (one-shot events)
- **Screen Pattern:** Stateless composable + ViewModel

```kotlin
// UiState: sealed interface
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val memos: List<Memo>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
```

### Domain Layer (core/domain)

- Use cases are single-method classes with `operator fun invoke()`
- Repository interfaces defined here, implemented in core/data

### Data Layer (core/data)

- Repository implementations combine multiple data sources
- `Result<T>` type for error handling (from core/common)

## Commit Convention

Format: `{type}({scope}): {description}`

- Type/scope: English
- Description/body: **Japanese**
- Scopes: module name in kebab-case (e.g., `home`, `core-data`, `build-logic`)
- Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`

Example: `feat(capture): メモのクイック入力機能を追加`

## Key Design Decisions

- **Firebase 統合:** Flutter 版の core/*_firebase パターンを踏襲。抽象化と実装を分離
- **Home Screen Widget:** Android AppWidgetProvider でネイティブ実装。URI スキーム `poche://capture?type=memo|photo|voice`
- **i18n:** Android string resources (`res/values/strings.xml`) を使用
- **Code Generation:** Hilt (DI), Room (DB), KSP ベース。手動コード生成コマンドは不要 (Gradle が自動実行)
- **Dependency Injection:** Hilt の `@Module` + `@Provides` / `@Binds` で DI グラフ構築。環境別設定は `@Qualifier` で切り替え
