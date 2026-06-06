# Java Chess Game

完全に機能するチェスゲーム（Swing GUI・JavaFX GUI・コンソール対応）

## ゲーム概要

Java で実装された完全なチェスゲームです。標準チェスルールに準拠し、Swing GUI / JavaFX GUI / コンソールでプレイできます。Human vs Human および Human vs AI（難易度3段階）モードをサポートします。

## 機能

- 完全なチェスルール実装（標準ルール準拠）
- 2人対戦モード
- AI 対戦モード（Easy / Medium / Hard の3段階）
- すべてのピース移動対応
- チェック / チェックメイト / ステールメイト検出
- 移動やり直し機能
- ゲーム履歴追跡
- ポーン昇格対応
- キャスリング対応
- アンパッサン対応

## 必要環境

- Java 25 以上（[Eclipse Temurin](https://adoptium.net/) OpenJDK 25+ 推奨）
- Maven Wrapper 同梱（`mvnw.cmd` / `mvnw`）— 別途インストール不要

## ビルド方法

```cmd
REM Swing 版（デフォルト・Maven 不要）
build\build.bat

REM JavaFX 版（Maven 使用）
build\build.bat --javafx
```

Unix の場合:

```bash
build/build.sh           # Swing のみ
build/build.sh --javafx  # JavaFX も含む
```

ビルドの自動処理（4ステップ）:

| ステップ | 処理 |
|--------|------|
| 1 | メインソース（Swing + モデル）をコンパイル → `target\classes` |
| 2 | デモランナーをコンパイル → `target\test-classes` |
| 3 | JUnit テストをコンパイル（Maven がない環境はスキップ） |
| 4 | JavaFX ソースをコンパイル（`--javafx` 指定時のみ） |

exe にパッケージングする場合:

```cmd
REM Swing exe（デフォルト）
build\package.bat

REM JavaFX exe
build\package.bat --javafx
```

生成物:

| ターゲット | 実行ファイル | 配布フォルダ |
|-----------|------------|------------|
| Swing | `dist\ChessGame\ChessGame.exe` | `dist\ChessGame\` |
| JavaFX | `dist\ChessGameFX\ChessGameFX.exe` | `dist\ChessGameFX\` |

各フォルダをそのまま配布すれば、Java がない PC でも動作します。

## ゲーム実行方法

### exe から起動（推奨）

```cmd
dist\ChessGame\ChessGame.exe
```

### ソースから直接起動

```cmd
REM GUI モード（Swing）
java -cp target\classes com.chessgame.Main

REM コンソール対話モード
java -cp target\classes com.chessgame.InteractiveGame

REM JavaFX GUI
mvnw.cmd javafx:run
```

### デモランナー

```cmd
REM Windows
java -cp "target\classes;target\test-classes" com.chessgame.TestGame
java -cp "target\classes;target\test-classes" com.chessgame.SpecialMovesTest

REM Unix
java -cp 'target/classes:target/test-classes' com.chessgame.TestGame
java -cp 'target/classes:target/test-classes' com.chessgame.SpecialMovesTest
```

### コンソールモードのコマンド

```
e2e4      移動入力（e2 から e4 へ）
board(b)  現在のボード表示
moves(m)  移動履歴表示
undo(u)   最後の移動を取り消し
resign(r) ゲーム降参
new(n)    新ゲーム開始
help(?)   ヘルプ表示
quit(q)   ゲーム終了
```

## Visual Studio Code での開発環境構築

### 必要なもの

| ツール | バージョン | 入手先 |
|--------|-----------|--------|
| VS Code | 最新版 | [code.visualstudio.com](https://code.visualstudio.com/) |
| JDK | 25 以上 | [Eclipse Temurin](https://adoptium.net/) |
| Extension Pack for Java | 最新版 | VS Code 拡張機能マーケットプレイス |

### 手順

**1. JDK 25 のインストール**

[Eclipse Temurin](https://adoptium.net/) から JDK 25 をダウンロードしてインストールし、`JAVA_HOME` を設定します。

```cmd
REM インストール確認
java -version
```

**2. 拡張機能のインストール**

VS Code の拡張機能ビュー（`Ctrl+Shift+X`）で **Extension Pack for Java** を検索してインストールします。

以下の拡張機能が一括で導入されます:

| 拡張機能 | 役割 |
|---------|------|
| Language Support for Java | コード補完・リファクタリング |
| Debugger for Java | ブレークポイント・ステップ実行 |
| Test Runner for Java | JUnit テストの実行・デバッグ |
| Maven for Java | Maven ライフサイクル操作 |
| Project Manager for Java | プロジェクト管理 |

**3. プロジェクトを開く**

```cmd
code d:\git\github\JavaTest\ChessGame
```

または VS Code の「フォルダーを開く」からリポジトリルートを選択します。  
初回起動時に Language Server が依存関係を解析するため、数十秒かかる場合があります。

### 既存の設定ファイル

このリポジトリには VS Code 用の設定が含まれています。

**`.vscode/settings.json`** — ソースパスと除外ディレクトリの設定:

```json
{
  "java.project.sourcePaths": ["src/main/java", "src/test/java"],
  "files.watcherExclude": { "**/target/**": true, ... }
}
```

**`.settings/org.eclipse.jdt.core.prefs`** — Eclipse JDT Language Server に Java 25 を認識させる設定（自動で読み込まれます）。

### ビルド・実行（VS Code UI から）

- **Maven サイドバー**（`Ctrl+Shift+P` → "Maven"）でフェーズを選択して実行
- **テスト**は `src/test/java` を開いてクラス左の `▶` アイコンをクリック
- **デバッグ**は `F5`、またはメインクラス（`Main.java` / `InteractiveGame.java`）を右クリック → "Debug"

### 注意事項

- Eclipse JDT Language Server は `instanceof` パターンマッチング（Java 16+）の一部でエラーを誤検知する場合があります。実際のビルド正否は `mvnw.cmd test` で確認してください。
- JavaFX モジュール関連のクラス（`com.chessgame.javafx`）は Maven ビルド（`--javafx`）でのみコンパイルされるため、IDE 上で赤波線が表示される場合があります。

## テスト

```cmd
REM Maven 経由で JUnit テストを実行（Windows）
mvnw.cmd test

REM Unix
./mvnw test
```

JUnit テスト一覧（計40件）:

| テストクラス | 対象 |
|------------|------|
| `ChessGameTest` | ゲームフロー全般・ポーン昇格 |
| `CheckDetectorTest` | 王手検出・ブロッカー動作 |
| `PositionTest` | 座標変換・DSL |
| `BoardTest` | 盤面操作 |
| `MoveTest` | 移動オブジェクト |
| `PieceTypeTest` | 駒種の素材値・記法文字 |

## プロジェクト構造

```
ChessGame/
├── src/
│   ├── main/java/com/chessgame/
│   │   ├── model/              # データモデル層
│   │   │   ├── board/          (Board, Position, Square)
│   │   │   ├── piece/          (Piece 抽象クラス、6 種の具象クラス、PieceType)
│   │   │   ├── move/           (Move, MoveHistory, MoveType)
│   │   │   ├── Color.java
│   │   │   └── GameState.java
│   │   ├── rules/              # ルール検証層（副作用なし）
│   │   │   ├── MoveValidator.java
│   │   │   ├── CheckDetector.java
│   │   │   └── CheckmateDetector.java
│   │   ├── game/               # ゲームコントローラー層
│   │   │   ├── ChessGame.java
│   │   │   ├── Player.java
│   │   │   ├── GameObserver.java
│   │   │   └── AIPlayer.java   (難易度 1〜3 実装済み)
│   │   ├── swing/              # Swing GUI 層（安定版）
│   │   │   ├── SwingChessGameFrame.java
│   │   │   ├── SwingChessBoardPanel.java
│   │   │   └── PieceImageGenerator.java
│   │   ├── javafx/             # JavaFX GUI 層（開発中）
│   │   │   ├── FXLauncher.java
│   │   │   ├── ChessGameApp.java
│   │   │   ├── ChessBoardView.java
│   │   │   ├── SquareView.java
│   │   │   ├── ControlPanel.java
│   │   │   ├── StatusBar.java
│   │   │   ├── PromotionDialog.java
│   │   │   ├── PieceRenderer.java
│   │   │   └── PieceImageLoader.java
│   │   ├── InteractiveGame.java
│   │   └── Main.java
│   └── test/java/com/chessgame/
│       ├── TestGame.java                       # デモランナー
│       ├── SpecialMovesTest.java               # デモランナー
│       ├── game/ChessGameTest.java
│       ├── rules/CheckDetectorTest.java
│       ├── model/board/PositionTest.java
│       ├── model/board/BoardTest.java
│       ├── model/move/MoveTest.java
│       └── model/piece/PieceTypeTest.java
├── target\classes\         # コンパイル済みクラス
├── target\test-classes\    # コンパイル済みテスト・デモクラス
├── target\ChessGame.jar    # 実行可能 JAR（package.bat 生成）
├── dist\ChessGame\         # Swing パッケージ済み exe（生成物）
├── dist\ChessGameFX\       # JavaFX パッケージ済み exe（生成物）
├── build\
│   ├── build.bat           # コンパイルのみ（Windows・開発用）
│   ├── build.sh            # コンパイルのみ（Unix・開発用）
│   └── package.bat         # ビルド → exe 生成（Windows）
├── LICENSE
└── README.md
```

## アーキテクチャ

MVC の4層構造。

| 層 | パッケージ | 役割 |
|----|-----------|------|
| Model | `com.chessgame.model` | イミュータブルな値オブジェクト群 |
| Rules | `com.chessgame.rules` | 副作用のない純粋なルールロジック |
| Controller | `com.chessgame.game` | ゲーム状態管理・API |
| View | `com.chessgame.swing` / `com.chessgame.javafx` | GUI 実装 |

### AI 難易度

| 難易度 | 選択肢 | 戦略 |
|--------|--------|------|
| 1 (Easy) | Human vs AI（Easy） | ランダムな合法手 |
| 2 (Medium) | Human vs AI（Medium） | 駒取りを優先、次いでランダム |
| 3 (Hard) | Human vs AI（Hard） | 最善手を素材評価で選択 |

### 設計パターン

- **Observer**: `GameObserver` でゲームロジックと UI を疎結合
- **Value Object**: `Position`・`Move` は不変オブジェクト
- **Strategy**: `MoveValidator` でピース種ごとの移動ロジックを分離

## 既知の制限

- PGN インポート/エクスポートは未実装
- ネットワークマルチプレイは未実装
- JavaFX UI は開発中のためビルド対象外（`--javafx` 指定時のみ）

## 今後の拡張

- AI の強化（minimax + alpha-beta pruning）
- PGN ファイルのインポート/エクスポート
- 時間管理（blitz / rapid / classical）
- ネットワークマルチプレイ

## ライセンス

MIT License — 詳細は [LICENSE](LICENSE) を参照してください。

---

**バージョン**: 1.1.0  
**更新日**: 2026年6月
