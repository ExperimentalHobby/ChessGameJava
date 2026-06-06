# CLAUDE.md

このファイルは、このリポジトリで作業する Claude Code (claude.ai/code) へのガイダンスを提供します。

## 作業ルール

- **実装前にプランを提示すること。** 実装に入る前に必ず日本語でプランを提示し、ユーザーの承認を得てから実装を開始する。
- **テスト駆動開発（TDD）を意識すること。** 新機能・バグ修正を実装する際は、先にテストを書いてから実装を行う。`src/test/java` 配下に JUnit 5 テストを追加し、`./mvnw.cmd test`（Unix: `./mvnw test`）で全テストがパスすることを確認してから作業を完了とする。

## 開発環境

- **Java**: 25（ビルド・実行ともに Java 25 を使用。`pom.xml` の `source`/`target` も 25）
- **ビルドツール**: Maven Wrapper（`mvnw.cmd` / `mvnw`）

## ビルド・実行コマンド

**ビルド（Maven がない環境では build\build.bat / build/build.sh を使う）**

```bat
# Windows — Swing のみ（Maven 不要）
build\build.bat

# Windows — JavaFX も含めてコンパイル（Maven 使用）
build\build.bat --javafx

# GUI 起動（Swing）
java -cp target\classes com.chessgame.Main

# JavaFX GUI 起動
mvnw.cmd javafx:run

# コンソール対話ゲーム
java -cp target\classes com.chessgame.InteractiveGame
```

Unix の場合:

```bash
build/build.sh           # Swing のみ
build/build.sh --javafx  # JavaFX も含む
```

注意: `src/main/java/com/chessgame/javafx/` は JavaFX WIP のため、`build/build.bat` / `build/build.sh` のコンパイル対象から除外している。

## アーキテクチャ

MVC の4層構造で設計されている。

**モデル層** (`com.chessgame.model`): イミュータブルな値オブジェクト群。
- `board/Board.java` — 8×8のグリッド。`Position.java` — イミュータブルな座標（`Position.of("e2")` DSL 形式を使うこと）。`Square.java` — 個々のマスの状態。
- `piece/Piece.java`（抽象クラス）— 色・位置・移動回数（キャスリング判定に使用）を保持。具象サブクラスが6種類。
- `move/Move.java` — 移動元・移動先の座標と `MoveType` 列挙型（`NORMAL`、`CASTLING`、`EN_PASSANT`、`PROMOTION`）。`MoveHistory.java` が手戻し機能を実現。
- `GameState.java` — 現在の手番・チェック状態などのゲーム状態を管理。

**ルール層** (`com.chessgame.rules`): 副作用のない純粋なロジック。
- `MoveValidator.java` — 駒種ごとの合法手を生成。
- `CheckDetector.java` — キングが攻撃されているかを検出。
- `CheckmateDetector.java` — チェックメイトとステールメイトを判別。

**ゲームコントローラ層** (`com.chessgame.game`):
- `ChessGame.java` — 主要 API。`ChessGame.createTwoPlayerGame(name1, name2)` で生成する。主なメソッド: `makeMove()`、`getAvailableMoves()`、`undo()`。
- `GameObserver.java` — オブザーバーインターフェース。盤面更新イベントを受け取るために実装する。
- `AIPlayer.java` — AI 対戦相手。難易度 1（ランダム）・2（駒取り優先）・3（最善手優先）の3段階を実装済み。

**UI層** (2種類の実装):
- `com.chessgame.javafx` — JavaFX（開発中）: `FXLauncher`（jpackage エントリーポイント）→ `ChessGameApp` → `ChessBoardView` → `SquareView`。`PieceImageLoader` がアセットを管理。`FXLauncher` は `Application` を継承しないことで "JavaFX runtime components missing" チェックを回避する。
- `com.chessgame.swing` — Swing（安定版）: `SwingChessGameFrame` + `SwingChessBoardPanel`。`PieceImageGenerator` が SVG で駒を描画。
- `InteractiveGame.java` — コンソール UI（最も安定したエントリーポイント）。

## 重要な設計上の決定事項

- **駒の移動回数トラッキング**によってキャスリング可否を判定している（専用フラグは持たない）。
- コード全体で `Position` が主要なアドレッシング機構。テストや新しいコードでは生の行・列整数より `Position.of("e2")` 形式を優先すること。
- JavaFX UI (`com.chessgame.javafx`) は開発中のため、安定した動作確認には Swing UI またはコンソールを使用すること。
- オブザーバーパターン (`GameObserver`) によってゲームロジックとすべての UI 層が疎結合になっている。新しい UI 実装はこのインターフェースを実装すること。
