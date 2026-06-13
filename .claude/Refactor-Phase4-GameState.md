# コンポーネント別リファクタリング Phase 4: GameState コンポーネント

## 1. 目標

GameState（ゲーム状態管理）を独立したパッケージに再構成する。Color.java は広く依存されるため、コア共通モデルとして model/ に保持。

## 2. 対象ファイル

### 移動対象
```
Java ソース:
- src/main/java/com/chessgame/model/GameState.java
  → src/main/java/com/chessgame/gamestate/model/GameState.java

保持対象（Phase 4 の対象外）:
- src/main/java/com/chessgame/model/Color.java
  （31個のファイルから依存されているため、core/shared モデルとして保持）
```

## 3. パッケージ構造

```
Before:
com.chessgame.model.GameState

After:
com.chessgame.gamestate.model.GameState

Kept:
com.chessgame.model.Color （広い依存性のため保持）
```

## 4. 影響を受けるファイル

GameState に依存するファイルのインポート文を更新する必要があります：

### Java ソース（更新対象）
- `src/main/java/com/chessgame/game/ChessGame.java` — GameState
- `src/main/java/com/chessgame/game/GameObserver.java` — GameState
- `src/main/java/com/chessgame/InteractiveGame.java` — GameState
- `src/main/java/com/chessgame/javafx/ChessGameApp.java` — GameState
- `src/main/java/com/chessgame/swing/SwingChessGameFrame.java` — GameState

### テストコード（更新対象）
- `src/test/java/com/chessgame/game/ChessGameTest.java`
- `src/test/java/com/chessgame/TestGame.java`

## 5. 実装ステップ

### Step 1: ディレクトリ作成
```bash
mkdir -p src/main/java/com/chessgame/gamestate/model
```

### Step 2: ファイル移動（git mv で履歴保持）
```bash
git mv src/main/java/com/chessgame/model/GameState.java \
        src/main/java/com/chessgame/gamestate/model/
```

### Step 3: パッケージ宣言を更新
```java
// GameState.java
package com.chessgame.gamestate.model;
```

### Step 4: インポート文を更新（全ファイル）
```
com.chessgame.model.GameState → com.chessgame.gamestate.model.GameState
```

### Step 5: コンパイル・テスト
```bash
mvnw.cmd clean compile
mvnw.cmd test
```

## 6. テスト計画

- [ ] コンパイルエラーなし
- [ ] `mvnw.cmd test` で全テスト（52件）がパス

## 7. 成功基準

- [ ] GameState ファイルが新パッケージに移動
- [ ] すべてのインポート文が更新済み
- [ ] コンパイルエラーなし
- [ ] テスト全パス

## 8. 次のステップ（Phase 5）

Phase 4 が完了したら、Detection コンポーネント（CheckDetector, CheckmateDetector）をリファクタリング。

注記: CheckDetector は既に Phase 2 で com.chessgame.piece.rules に移動済みだが、CheckmateDetector はまだ com.chessgame.rules に残っているため、Phase 5 では CheckmateDetector および関連する detection ロジックを com.chessgame.detection コンポーネントに統合予定。