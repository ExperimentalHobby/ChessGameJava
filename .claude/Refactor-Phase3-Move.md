# コンポーネント別リファクタリング Phase 3: Move コンポーネント

## 1. 目標

Move コンポーネント（移動・移動種別・移動履歴）を独立したパッケージに再構成する。

## 2. 対象ファイル

### 移動対象
```
Java ソース:
- src/main/java/com/chessgame/model/move/Move.java
  → src/main/java/com/chessgame/move/model/Move.java

- src/main/java/com/chessgame/model/move/MoveType.java
  → src/main/java/com/chessgame/move/model/MoveType.java

- src/main/java/com/chessgame/model/move/MoveHistory.java
  → src/main/java/com/chessgame/move/model/MoveHistory.java

テストコード:
- src/test/java/com/chessgame/model/move/MoveTest.java
  → src/test/java/com/chessgame/move/MoveTest.java
```

## 3. パッケージ構造

```
Before:
com.chessgame.model.move.*

After:
com.chessgame.move.model.*
```

## 4. 影響を受けるファイル

Move コンポーネントに依存するファイルのインポート文を更新する必要があります：

### Java ソース（更新対象）
- `src/main/java/com/chessgame/game/ChessGame.java` — Move/MoveType
- `src/main/java/com/chessgame/game/AIPlayer.java` — Move/MoveType
- `src/main/java/com/chessgame/game/GameObserver.java` — Move
- `src/main/java/com/chessgame/model/GameState.java` — Move/MoveHistory
- `src/main/java/com/chessgame/rules/MoveValidator.java` — Move/MoveType
- `src/main/java/com/chessgame/rules/CheckmateDetector.java` — Move
- `src/main/java/com/chessgame/InteractiveGame.java` — Move/MoveType
- `src/main/java/com/chessgame/javafx/ChessBoardView.java` — Move
- `src/main/java/com/chessgame/javafx/ChessGameApp.java` — Move
- `src/main/java/com/chessgame/swing/SwingChessBoardPanel.java` — Move
- `src/main/java/com/chessgame/swing/SwingChessGameFrame.java` — Move

### テストコード（更新対象）
- `src/test/java/com/chessgame/game/ChessGameTest.java`
- `src/test/java/com/chessgame/game/AIPlayerTest.java`
- `src/test/java/com/chessgame/game/AiEngineParityTest.java`
- `src/test/java/com/chessgame/TestGame.java`

## 5. 実装ステップ

### Step 1: ディレクトリ作成
```bash
mkdir -p src/main/java/com/chessgame/move/model
mkdir -p src/test/java/com/chessgame/move
```

### Step 2: ファイル移動（git mv で履歴保持）
```bash
# model ファイル（3ファイル）
git mv src/main/java/com/chessgame/model/move/* \
        src/main/java/com/chessgame/move/model/

# テストファイル（1ファイル）
git mv src/test/java/com/chessgame/model/move/MoveTest.java \
        src/test/java/com/chessgame/move/
```

### Step 3: パッケージ宣言を更新
```java
// Move.java, MoveType.java, MoveHistory.java
package com.chessgame.move.model;

// テストファイル
package com.chessgame.move;
```

### Step 4: インポート文を更新（全ファイル）
```
com.chessgame.model.move.* → com.chessgame.move.model.*
```

### Step 5: 古いディレクトリを削除
```bash
git rm -r src/main/java/com/chessgame/model/move
git rm -r src/test/java/com/chessgame/model/move
```

### Step 6: コンパイル・テスト
```bash
mvnw.cmd clean compile
mvnw.cmd test
```

## 6. テスト計画

- [ ] コンパイルエラーなし
- [ ] `mvnw.cmd test` で全テスト（52件）がパス
- [ ] MoveTest が新しい package で実行成功

## 7. 成功基準

- [ ] Move コンポーネント関連ファイルが新パッケージに移動
- [ ] すべてのインポート文が更新済み
- [ ] コンパイルエラーなし
- [ ] テスト全パス
- [ ] 古いパッケージディレクトリが削除済み

## 8. 次のステップ（Phase 4 以降）

Phase 3 が完了したら、同様のアプローチで残りのコンポーネントをリファクタリング予定：
- Phase 4: GameState コンポーネント
- Phase 5: Detection コンポーネント（CheckDetector, CheckmateDetector）
- Phase 6: Game コンポーネント（ChessGame, Player など）
- Phase 7: UI コンポーネント（Swing, JavaFX）