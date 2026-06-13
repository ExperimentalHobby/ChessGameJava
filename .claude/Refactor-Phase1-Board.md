# コンポーネント別リファクタリング Phase 1: Board コンポーネント

## 1. 目標

Board コンポーネント（盤面・座標・マス・移動ルール検証）を独立したパッケージに再構成し、他のコンポーネントとの依存関係を明確化する。

## 2. 対象ファイル

### 移動対象
```
現在の場所 → 新しい場所

Java ソース:
- src/main/java/com/chessgame/model/board/Board.java
  → src/main/java/com/chessgame/board/model/Board.java

- src/main/java/com/chessgame/model/board/Position.java
  → src/main/java/com/chessgame/board/model/Position.java

- src/main/java/com/chessgame/model/board/Square.java
  → src/main/java/com/chessgame/board/model/Square.java

- src/main/java/com/chessgame/rules/MoveValidator.java
  → src/main/java/com/chessgame/board/rules/MoveValidator.java

テストコード:
- src/test/java/com/chessgame/model/board/BoardTest.java
  → src/test/java/com/chessgame/board/BoardTest.java

- src/test/java/com/chessgame/model/board/PositionTest.java
  → src/test/java/com/chessgame/board/PositionTest.java
```

## 3. パッケージ構造

```
Before:
com.chessgame.model.board.*
com.chessgame.rules.MoveValidator

After:
com.chessgame.board.model.*
com.chessgame.board.rules.MoveValidator
```

## 4. 影響を受けるファイル

Board コンポーネントに依存するファイルのインポート文を更新する必要があります：

### Java ソース（更新対象）
- `src/main/java/com/chessgame/model/piece/*.java` — Board/Position をインポート
- `src/main/java/com/chessgame/game/ChessGame.java` — Board/Position/MoveValidator をインポート
- `src/main/java/com/chessgame/game/Player.java` — Position をインポート
- `src/main/java/com/chessgame/swing/*.java` — Board/Position をインポート
- `src/main/java/com/chessgame/javafx/*.java` — Board/Position をインポート
- `src/main/java/com/chessgame/InteractiveGame.java` — Board/Position をインポート

### テストコード（更新対象）
- `src/test/java/com/chessgame/game/ChessGameTest.java`
- `src/test/java/com/chessgame/model/piece/PieceTypeTest.java`

## 5. 実装ステップ

### Step 1: ディレクトリ作成
```bash
mkdir -p src/main/java/com/chessgame/board/{model,rules}
mkdir -p src/test/java/com/chessgame/board
```

### Step 2: ファイル移動（git mv で履歴保持）
```bash
# model ファイル
git mv src/main/java/com/chessgame/model/board/Board.java \
        src/main/java/com/chessgame/board/model/
git mv src/main/java/com/chessgame/model/board/Position.java \
        src/main/java/com/chessgame/board/model/
git mv src/main/java/com/chessgame/model/board/Square.java \
        src/main/java/com/chessgame/board/model/

# rules ファイル
git mv src/main/java/com/chessgame/rules/MoveValidator.java \
        src/main/java/com/chessgame/board/rules/

# テストファイル
git mv src/test/java/com/chessgame/model/board/BoardTest.java \
        src/test/java/com/chessgame/board/
git mv src/test/java/com/chessgame/model/board/PositionTest.java \
        src/test/java/com/chessgame/board/
```

### Step 3: パッケージ宣言を更新
各ファイルの `package` 宣言を新パッケージに変更：

```java
// Board.java, Position.java, Square.java
package com.chessgame.board.model;

// MoveValidator.java
package com.chessgame.board.rules;

// テストファイル
package com.chessgame.board;
```

### Step 4: インポート文を更新
影響を受けるファイルのインポート文を更新：

```java
// Before
import com.chessgame.model.board.Board;
import com.chessgame.model.board.Position;
import com.chessgame.rules.MoveValidator;

// After
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.board.rules.MoveValidator;
```

### Step 5: 古いディレクトリを削除
```bash
# model/board ディレクトリの削除
git rm -r src/main/java/com/chessgame/model/board
git rm -r src/test/java/com/chessgame/model/board

# model ディレクトリが空なら削除（Color など他のファイルがあれば保持）
# rules ディレクトリが空なら削除（他のルール検証クラスがあれば保持）
```

### Step 6: コンパイル・テスト
```bash
mvnw.cmd clean compile
mvnw.cmd test
```

## 6. テスト計画

- [ ] コンパイルエラーなし
- [ ] `mvnw.cmd test` で全テスト（52件）がパス
- [ ] BoardTest, PositionTest が新しい package で実行成功
- [ ] 他の依存クラスのテストも引き続きパス

## 7. 成功基準

- [ ] Board コンポーネント関連ファイルが新パッケージに移動
- [ ] すべてのインポート文が更新済み
- [ ] コンパイルエラーなし
- [ ] テスト全パス
- [ ] 古いパッケージディレクトリが削除済み

## 8. 次のステップ（Phase 2）

Phase 1 が完了したら、同じアプローチで **piece コンポーネント** をリファクタリング：
- `src/main/java/com/chessgame/model/piece/*`
- `src/main/java/com/chessgame/rules/CheckDetector.java`
- 対応するテストファイル
