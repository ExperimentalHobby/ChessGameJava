# Java Chess Game

完全に機能するチェスゲーム（Swing GUI・コンソール対応）

## ゲーム概要

Java で実装された完全なチェスゲームです。標準チェスルールに準拠し、Swing GUI またはコンソールでプレイできます。

## 機能

- 完全なチェスルール実装（標準ルール準拠）
- 2人対戦モード
- すべてのピース移動対応
- チェック / チェックメイト / ステールメイト検出
- 移動やり直し機能
- ゲーム履歴追跡
- ポーン昇格対応
- キャスリング対応
- アンパッサン対応

## 必要環境（ビルド時のみ）

- Java 14 以上（`jpackage` を含む JDK）
- 推奨: [Eclipse Temurin](https://adoptium.net/) OpenJDK 17+ LTS

> **配布後の実行には Java 不要。** `package.bat` が JRE を同梱した `.exe` を生成します。

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

## テスト

```cmd
REM Maven 経由で JUnit テストを実行（Windows）
mvnw.cmd test

REM Unix
./mvnw test
```

JUnit テスト一覧:

| テストクラス | 対象 |
|------------|------|
| `ChessGameTest` | ゲームフロー全般 |
| `PositionTest` | 座標変換・DSL |
| `BoardTest` | 盤面操作 |
| `MoveTest` | 移動オブジェクト |

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
│   │   │   └── AIPlayer.java   (スタブ)
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
│   │   ├── util/               # ユーティリティ
│   │   │   └── MoveNotation.java
│   │   ├── InteractiveGame.java
│   │   └── Main.java
│   └── test/java/com/chessgame/
│       ├── TestGame.java           # デモランナー
│       ├── SpecialMovesTest.java   # デモランナー
│       ├── game/ChessGameTest.java
│       ├── model/board/PositionTest.java
│       ├── model/board/BoardTest.java
│       └── model/move/MoveTest.java
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

### 設計パターン

- **Observer**: `GameObserver` でゲームロジックと UI を疎結合
- **Value Object**: `Position`・`Move` は不変オブジェクト
- **Strategy**: `MoveValidator` でピース種ごとの移動ロジックを分離

## 既知の制限

- AI 対戦相手は未実装
- PGN インポート/エクスポートは未実装
- ネットワークマルチプレイは未実装
- JavaFX UI は開発中のためビルド対象外（`--javafx` 指定時のみ）

## 今後の拡張

- AI 対戦相手（minimax アルゴリズム）
- PGN ファイルのインポート/エクスポート
- 時間管理（blitz / rapid / classical）
- ネットワークマルチプレイ

## ライセンス

MIT License — 詳細は [LICENSE](LICENSE) を参照してください。

---

**バージョン**: 1.0.0  
**制作日**: 2026年5月
