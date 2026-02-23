# Poche App Android

Flutter アプリ `poche-app` を Android ネイティブ (Kotlin) で完全に再構築したプロジェクト。
Now in Android アーキテクチャに準拠したマルチモジュール構成を採用している。

## 目次

- [技術スタック](#技術スタック)
- [前提条件](#前提条件)
- [セットアップ](#セットアップ)
- [モジュール構成](#モジュール構成)
- [ビルドとラン](#ビルドとラン)
- [ビルドフレーバー](#ビルドフレーバー)
- [テスト](#テスト)
- [コード品質](#コード品質)
- [アーキテクチャ](#アーキテクチャ)
- [Convention Plugins](#convention-plugins)
- [コミット規約](#コミット規約)
- [コントリビューション](#コントリビューション)

## 技術スタック

| カテゴリ | 技術 |
|----------|------|
| 言語 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt (Dagger) |
| 非同期処理 | Kotlin Coroutines + Flow |
| データベース | Room |
| HTTP クライアント | Ktor Client |
| シリアライゼーション | Kotlinx Serialization |
| ナビゲーション | Compose Navigation (type-safe) |
| 画像読み込み | Coil |
| バックエンド | Firebase (Auth, Analytics, Crashlytics, Messaging, Remote Config) |
| テスト | JUnit5 + Turbine + MockK |
| ビルド | Gradle (Kotlin DSL) + Version Catalog + Convention Plugins |

各ライブラリの具体的なバージョンは [`gradle/libs.versions.toml`](gradle/libs.versions.toml) を参照すること。

## 前提条件

以下のツールをインストールする。

| ツール | バージョン | 備考 |
|--------|-----------|------|
| Android Studio | 最新安定版 | |
| JDK | 21 | JVM ターゲットが JDK 21 に設定されている |
| Android SDK | API 36 (compileSdk / targetSdk) | SDK Manager でインストール |

minSdk は **33** (Android 13) に設定されている。API 33 未満のデバイスはサポート対象外。

## セットアップ

### 1. リポジトリのクローン

```bash
git clone <repository-url>
cd poche-app-android
```

### 2. Firebase 設定ファイルの配置

各フレーバーに対応する `google-services.json` を以下のパスに配置する。
このファイルは `.gitignore` で除外されているため、Firebase コンソールからダウンロードする。

```
app/src/dev/google-services.json     # dev フレーバー用
app/src/stg/google-services.json     # stg フレーバー用
app/src/prod/google-services.json    # prod フレーバー用
```

### 3. ビルド確認

```bash
./gradlew assembleDevDebug
```

**注意:** `google-services.json` が未配置の場合、ビルドは失敗する。

## モジュール構成

app / core / feature の3層構成で、合計22モジュールから構成される。

```
poche-app-android/
├── app/                          # Application module (エントリポイント、DI 結合)
├── build-logic/                  # Convention Plugins (共通ビルド設定)
│   └── convention/
├── core/
│   ├── common/                   # Result type, 拡張関数, Logger
│   ├── data/                     # Repository 実装
│   ├── database/                 # Room データベース, DAO, Entity
│   ├── datastore/                # DataStore (Preferences, Proto)
│   ├── designsystem/             # テーマ, カラー, タイポグラフィ, 共通コンポーネント
│   ├── domain/                   # Use Case, Repository インターフェース
│   ├── model/                    # ドメインモデル (data class)
│   ├── network/                  # Ktor クライアント, API 定義 (scaffold)
│   ├── testing/                  # テストユーティリティ, Fake, Rule
│   ├── analytics/                # Analytics 抽象化 + Firebase 実装
│   ├── auth/                     # Auth 抽象化 + Firebase 実装
│   ├── notifications/            # Firebase Cloud Messaging (FCM) 受信
│   └── ui/                       # 共有 Composable, Preview ユーティリティ
├── feature/
│   ├── capture/                  # クイックキャプチャ (メモ / 写真 / 音声)
│   ├── home/                     # ホーム画面
│   ├── memo/                     # メモ詳細
│   ├── settings/                 # 設定
│   ├── onboarding/               # オンボーディング (stub)
│   └── devtools/                 # デバッグツール
├── gradle/
│   └── libs.versions.toml        # Version Catalog
├── settings.gradle.kts
└── build.gradle.kts
```

### モジュール依存ルール

```
feature/* ──→ core/*        feature は core のみに依存する
core/data ──→ core/domain, core/database, core/network, core/datastore
core/domain ──→ core/model, core/common
app ──→ feature/*, core/*   app が全モジュールを結合する
```

以下は禁止事項:
- モジュール間の循環依存
- `feature` モジュール同士の直接依存 (ルーティングは `app` モジュール経由)

## ビルドとラン

### ビルドコマンド

```bash
# Dev フレーバー debug APK
./gradlew assembleDevDebug

# Prod フレーバー release APK
./gradlew assembleProdRelease
```

### 依存関係ツリーの確認

```bash
./gradlew :app:dependencies
```

### Convention Plugin の開発

```bash
./gradlew :build-logic:convention:test
```

## ビルドフレーバー

3環境構成。Firebase プロジェクトはフレーバーごとに分離されている。

| フレーバー | Application ID サフィックス | Firebase プロジェクト | 用途 |
|-----------|--------------------------|---------------------|------|
| `dev` | `.dev` | poche-app-dev | 開発 |
| `stg` | `.stg` | poche-app-stg | ステージング (内部テスト) |
| `prod` | (なし) | poche-app | 本番 |

ビルドタイプとフレーバーの組み合わせ例:

| バリアント | Application ID | 用途 |
|-----------|---------------|------|
| `devDebug` | `cloud.poche.app.dev.debug` | 日常開発 |
| `stgDebug` | `cloud.poche.app.stg.debug` | ステージング検証 |
| `prodRelease` | `cloud.poche.app` | 本番リリース |

## テスト

### ユニットテスト

```bash
# 全モジュールのユニットテストを実行
./gradlew test

# 特定モジュールのユニットテストを実行
./gradlew :feature:home:testDebugUnitTest
```

### インストルメンテーションテスト

```bash
# 接続されたデバイス/エミュレータでテストを実行
./gradlew connectedAndroidTest
```

### テスト技術スタック

| ライブラリ | 用途 |
|-----------|------|
| JUnit5 | テストフレームワーク |
| Turbine | `Flow` テストユーティリティ |
| MockK | Kotlin モックライブラリ |
| Espresso | UI テスト (インストルメンテーション) |

**現状:** テストユーティリティ (`core/testing`) は整備済みだが、テストケースは未作成。

## コード品質

### 静的解析

```bash
# Detekt (静的解析)
./gradlew detekt

# Android Lint
./gradlew lint
```

### コードフォーマット

```bash
# フォーマットチェック
./gradlew spotlessCheck

# 自動フォーマット
./gradlew spotlessApply
```

コミット前に `spotlessCheck` と `detekt` がパスすることを確認する。現在 Git フックによる自動実行は未設定のため、手動で確認する。

## アーキテクチャ

Now in Android アーキテクチャパターンに準拠した3層構造を採用する。

### Presentation Layer (feature モジュール)

各 feature モジュールは以下のパターンで実装する。

- **UI State:** `StateFlow<UiState>` を ViewModel で管理
- **Side Effects:** `SharedFlow<UiEvent>` でワンショットイベントを通知
- **Screen Pattern:** ステートレスな Composable + ViewModel

```kotlin
// UiState の定義例
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val memos: List<Memo>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
```

### Domain Layer (core/domain)

- Use Case は `operator fun invoke()` を持つ単一メソッドクラスとして実装する
- Repository インターフェースをこの層で定義し、`core/data` で実装する

### Data Layer (core/data)

- Repository 実装はローカルデータソース (Database, DataStore) を使用する (Network 同期は未実装)
- エラーハンドリングには `core/common` の `Result<T>` 型を使用する

### 主要な設計判断

| 項目 | 判断内容 |
|------|---------|
| Firebase 統合 | 抽象化と実装を分離 (`core/analytics`, `core/auth`)。`core/notifications` は FCM 受信のみ実装 |
| Home Screen Widget | `AppWidgetProvider` でネイティブ実装予定。URI スキーム `poche://capture?type=memo\|photo\|voice` (未実装) |
| i18n | Android String Resources (`res/values/strings.xml`) を使用 |
| コード生成 | Hilt (DI), Room (DB) は KSP ベース。Gradle が自動実行するため手動コマンド不要 |
| DI | Hilt の `@Module` + `@Provides` / `@Binds` で DI グラフ構築 |

## Convention Plugins

`build-logic/convention/` に共通ビルド設定を Convention Plugin として集約している。
新規モジュール作成時は、用途に応じた Plugin を適用する。

| Plugin ID | 用途 | 適用先 |
|-----------|------|--------|
| `poche.android.application` | Application module 共通設定 | `app` |
| `poche.android.library` | Library module 共通設定 | `core/*` |
| `poche.android.compose` | Compose 有効化 + BOM バージョン管理 | Compose 使用モジュール |
| `poche.android.hilt` | Hilt DI セットアップ | DI が必要なモジュール |
| `poche.android.room` | Room セットアップ + KSP | `core/database` |
| `poche.android.feature` | Feature module 共通設定 (Compose + Hilt + Navigation) | `feature/*` |
| `poche.kotlin.library` | Pure Kotlin module 設定 | `core/model`, `core/common` 等 |
| `poche.android.testing` | テスト共通設定 (JUnit5 + MockK + Turbine) | `core/testing` |

### SDK バージョン設定

Convention Plugin で一元管理されている値:

| 設定項目 | 値 |
|---------|-----|
| compileSdk | 36 |
| targetSdk | 36 |
| minSdk | 33 |
| JVM ターゲット | 21 |
| Java 互換性 | 21 |

## コミット規約

Conventional Commits 形式を採用する。

```
{type}({scope}): {description}
```

- **type / scope**: 英語
- **description / body**: 日本語
- **scope**: モジュール名の kebab-case (例: `home`, `core-data`, `build-logic`)

### type 一覧

| type | 用途 |
|------|------|
| `feat` | 新機能 |
| `fix` | バグ修正 |
| `docs` | ドキュメント |
| `style` | コードスタイル (フォーマット等) |
| `refactor` | リファクタリング |
| `perf` | パフォーマンス改善 |
| `test` | テスト |
| `build` | ビルド設定 |
| `ci` | CI/CD |
| `chore` | その他雑務 |
| `revert` | コミット取り消し |

コミットメッセージ例:

```
feat(capture): メモのクイック入力機能を追加
fix(core-data): オフライン時のデータ同期エラーを修正
refactor(home): ViewModel の状態管理を StateFlow に統一
```

## コントリビューション

### 開発の流れ

1. GitHub Issue を確認または作成する
2. Issue に対応するブランチを作成する (命名規則: `{prefix}/GH-{issue-number}`)
3. 実装とテストを行う
4. `spotlessApply` でフォーマットを適用する
5. `detekt` と `test` がパスすることを確認する
6. Pull Request を作成する

### ブランチ命名規則

| 目的 | プレフィックス | 例 |
|------|-------------|-----|
| 新機能 | `feature/` | `feature/GH-42` |
| バグ修正 | `fix/` | `fix/GH-15` |
| ドキュメント | `docs/` | `docs/GH-8` |
| リファクタリング | `refactor/` | `refactor/GH-23` |
| CI/CD | `ci/` | `ci/GH-11` |
| その他 | `chore/` | `chore/GH-5` |

ブランチプレフィックスは `feature/` (フルワード) を使用する。`feat/` は使用しない。

### コードレビュー観点

- モジュール依存ルールが守られているか
- Convention Plugin が適切に適用されているか
- テストが追加されているか
- コミットが1つの論理的変更に対応しているか
