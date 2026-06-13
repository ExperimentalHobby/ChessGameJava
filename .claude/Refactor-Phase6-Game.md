# コンポーネント別リファクタリング Phase 6: Game コンポーネント（内部構造化）

## 1. 目標

既存の game パッケージ内を詳細に組織化し、責任分離を明確にする。
コア（ChessGame）、プレイヤー（Player, AIPlayer）、オブザーバーに分割。

## 2. 対象ファイル

### 移動対象
```
Java ソース:
- src/main/java/com/chessgame/game/ChessGame.java
  → src/main/java/com/chessgame/game/core/ChessGame.java

- src/main/java/com/chessgame/game/Player.java
  → src/main/java/com/chessgame/game/player/Player.java

- src/main/java/com/chessgame/game/AIPlayer.java
  → src/main/java/com/chessgame/game/player/AIPlayer.java

- src/main/java/com/chessgame/game/GameObserver.java
  → src/main/java/com/chessgame/game/observer/GameObserver.java

テストコード:
- src/test/java/com/chessgame/game/ChessGameTest.java
  → src/test/java/com/chessgame/game/core/ChessGameTest.java

- src/test/java/com/chessgame/game/AIPlayerTest.java
  → src/test/java/com/chessgame/game/player/AIPlayerTest.java

- src/test/java/com/chessgame/game/AiEngineParityTest.java
  → src/test/java/com/chessgame/game/core/AiEngineParityTest.java
```

## 3. パッケージ構造

```
Before:
com.chessgame.game.*

After:
com.chessgame.game.core.ChessGame
com.chessgame.game.player.Player
com.chessgame.game.player.AIPlayer
com.chessgame.game.observer.GameObserver
```

## 4. 影響を受けるファイル

Game コンポーネント内のファイルに依存するファイルのインポート文を更新する必要があります：

### Java ソース（更新対象）
- `src/main/java/com/chessgame/InteractiveGame.java` — ChessGame, AIPlayer, GameObserver, Player
- `src/main/java/com/chessgame/javafx/ChessGameApp.java` — ChessGame, GameObserver
- `src/main/java/com/chessgame/swing/SwingChessGameFrame.java` — ChessGame, GameObserver

### テストコード（更新対象）
- `src/test/java/com/chessgame/TestGame.java` — ChessGame

## 5. 実装ステップ

### Step 1: ディレクトリ作成
```bash
mkdir -p src/main/java/com/chessgame/game/{core,player,observer}
mkdir -p src/test/java/com/chessgame/game/{core,player}
```

### Step 2: ファイル移動（git mv で履歴保持）
```bash
# core
git mv src/main/java/com/chessgame/game/ChessGame.java \
        src/main/java/com/chessgame/game/core/

# player
git mv src/main/java/com/chessgame/game/Player.java \
        src/main/java/com/chessgame/game/player/
git mv src/main/java/com/chessgame/game/AIPlayer.java \
        src/main/java/com/chessgame/game/player/

# observer
git mv src/main/java/com/chessgame/game/GameObserver.java \
        src/main/java/com/chessgame/game/observer/

# テストファイル
git mv src/test/java/com/chessgame/game/ChessGameTest.java \
        src/test/java/com/chessgame/game/core/
git mv src/test/java/com/chessgame/game/AiEngineParityTest.java \
        src/test/java/com/chessgame/game/core/
git mv src/test/java/com/chessgame/game/AIPlayerTest.java \
        src/test/java/com/chessgame/game/player/
```

### Step 3: パッケージ宣言を更新
```java
// ChessGame.java
package com.chessgame.game.core;

// Player.java, AIPlayer.java
package com.chessgame.game.player;

// GameObserver.java
package com.chessgame.game.observer;

// テストファイル
package com.chessgame.game.core;
package com.chessgame.game.player;
```

### Step 4: インポート文を更新（全ファイル）
```
com.chessgame.game.ChessGame → com.chessgame.game.core.ChessGame
com.chessgame.game.Player → com.chessgame.game.player.Player
com.chessgame.game.AIPlayer → com.chessgame.game.player.AIPlayer
com.chessgame.game.GameObserver → com.chessgame.game.observer.GameObserver
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

- [ ] Game コンポーネント内の構造が詳細に組織化
- [ ] すべてのインポート文が更新済み
- [ ] コンパイルエラーなし
- [ ] テスト全パス

## 8. 次のステップ（Phase 7）

Phase 6 が完了したら、UI コンポーネント（Swing）をリファクタリング予定。