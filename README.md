# Java Chess Game

完全に機能するチェスゲーム（Swing GUI・コンソール対応）

## ゲーム概要

Java で実装された完全なチェスゲームです。標準チェスルールに準拠し、Swing GUI またはコンソールでプレイできます。

## 機能

- 完全なチェスルール実装（標準ルール準拠）
- 2人対戦モード
- すべてのピース移動対応
- チェック / チェックメイト / スタールメイト検出
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

exe にパッケージングする場合:

```cmd
REM Swing exe（デフォルト）
build\package.bat

REM JavaFX exe
build\package.bat --javafx
```

Swing ビルドの自動処理:

| ステップ | 処理 |
|--------|------|
| 1 | Java ソースをコンパイル |
| 2 | 実行可能 JAR (`ChessGame.jar`) を生成 |
| 3 | `jpackage` で JRE 同梱の `.exe` を生成 |

生成物:

```
dist\ChessGame\
  ChessGame.exe       ← 起動ファイル
  app\                ← JAR・設定
  runtime\            ← 同梱 JRE（Java インストール不要）
```

`dist\ChessGame` フォルダをそのまま配布すれば、Java がない PC でも動作します。

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

## プロジェクト構造

```
ChessGame/
├── src/main/java/com/chessgame/
│   ├── model/              # データモデル層
│   │   ├── board/          (Board, Position, Square)
│   │   ├── piece/          (Piece 階層、6 ピースタイプ)
│   │   └── move/           (Move, MoveHistory)
│   ├── rules/              # ルール検証層
│   │   ├── MoveValidator.java
│   │   ├── CheckDetector.java
│   │   └── CheckmateDetector.java
│   ├── game/               # ゲームコントローラー層
│   │   ├── ChessGame.java
│   │   ├── Player.java
│   │   └── GameObserver.java
│   ├── swing/              # Swing GUI 層（安定版）
│   │   ├── SwingChessGameFrame.java
│   │   └── SwingChessBoardPanel.java
│   ├── javafx/             # JavaFX GUI 層（開発中）
│   │   ├── ChessGameApp.java
│   │   ├── ChessBoardView.java
│   │   ├── SquareView.java
│   │   ├── ControlPanel.java
│   │   ├── StatusBar.java
│   │   ├── PromotionDialog.java
│   │   ├── PieceRenderer.java
│   │   └── PieceImageLoader.java
│   ├── util/               # ユーティリティ
│   │   └── MoveNotation.java
│   ├── InteractiveGame.java
│   └── Main.java
├── target\classes\         # コンパイル済みクラス
├── target\ChessGame.jar    # 実行可能 JAR
├── dist\ChessGame\         # パッケージ済み exe（生成物）
├── build\                  # ビルドスクリプト
│   ├── build.bat           # コンパイルのみ（Windows・開発用）
│   ├── build.sh            # コンパイルのみ（Unix・開発用）
│   └── package.bat         # ビルド → exe 生成
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
| View | `com.chessgame.swing` | Swing GUI 実装 |

### 設計パターン

- **Observer**: `GameObserver` でゲームロジックと UI を疎結合
- **Value Object**: `Position`・`Move` は不変オブジェクト
- **Strategy**: `MoveValidator` でピース種ごとの移動ロジックを分離

## 既知の制限

- AI 対戦相手は未実装
- PGN インポート/エクスポートは未実装
- ネットワークマルチプレイは未実装
- JavaFX UI は開発中のためビルド対象外

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
