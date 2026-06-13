# コンポーネント別リファクタリング Phase 2: Piece コンポーネント

## 1. 目標

Piece コンポーネント（駒・駒種・チェック検出）を独立したパッケージに再構成する。

## 2. 対象ファイル

### 移動対象
```
Java ソース:
- src/main/java/com/chessgame/model/piece/Piece.java
  → src/main/java/com/chessgame/piece/model/Piece.java

- src/main/java/com/chessgame/model/piece/PieceType.java
  → src/main/java/com/chessgame/piece/model/PieceType.java

- src/main/java/com/chessgame/model/piece/King.java
  → src/main/java/com/chessgame/piece/model/King.java

- src/main/java/com/chessgame/model/piece/Queen.java
  → src/main/java/com/chessgame/piece/model/Queen.java

- src/main/java/com/chessgame/model/piece/Rook.java
  → src/main/java/com/chessgame/piece/model/Rook.java

- src/main/java/com/chessgame/model/piece/Bishop.java
  → src/main/java/com/chessgame/piece/model/Bishop.java

- src/main/java/com/chessgame/model/piece/Knight.java
  → src/main/java/com/chessgame/piece/model/Knight.java

- src/main/java/com/chessgame/model/piece/Pawn.java
  → src/main/java/com/chessgame/piece/model/Pawn.java

- src/main/java/com/chessgame/rules/CheckDetector.java
  → src/main/java/com/chessgame/piece/rules/CheckDetector.java

テストコード:
- src/test/java/com/chessgame/model/piece/PieceTypeTest.java
  → src/test/java/com/chessgame/piece/PieceTypeTest.java

- src/test/java/com/chessgame/rules/CheckDetectorTest.java
  → src/test/java/com/chessgame/piece/CheckDetectorTest.java
```

## 3. パッケージ構造

```
Before:
com.chessgame.model.piece.*
com.chessgame.rules.CheckDetector

After:
com.chessgame.piece.model.*
com.chessgame.piece.rules.CheckDetector
```

## 4. 影響を受けるファイル

Piece コンポーネントに依存するファイルのインポート文を更新する必要があります：

### Java ソース（更新対象）
- `src/main/java/com/chessgame/game/ChessGame.java` — Piece/PieceType/CheckDetector
- `src/main/java/com/chessgame/game/AIPlayer.java` — Piece/PieceType
- `src/main/java/com/chessgame/board/model/Board.java` — Piece
- `src/main/java/com/chessgame/move/model/Move.java` — Piece/PieceType
- `src/main/java/com/chessgame/swing/*.java` — Piece/PieceType
- `src/main/java/com/chessgame/javafx/*.java` — Piece/PieceType
- `src/main/java/com/chessgame/InteractiveGame.java` — Piece/PieceType
- `src/main/java/com/chessgame/model/GameState.java` — Piece

### テストコード（更新対象）
- `src/test/java/com/chessgame/game/ChessGameTest.java`
- `src/test/java/com/chessgame/board/BoardTest.java`

## 5. 実装ステップ

### Step 1: ディレクトリ作成
```bash
mkdir -p src/main/java/com/chessgame/piece/{model,rules}
mkdir -p src/test/java/com/chessgame/piece
```

### Step 2: ファイル移動（git mv で履歴保持）
```bash
# model ファイル（8ファイル）
git mv src/main/java/com/chessgame/model/piece/* \
        src/main/java/com/chessgame/piece/model/

# rules ファイル
git mv src/main/java/com/chessgame/rules/CheckDetector.java \
        src/main/java/com/chessgame/piece/rules/

# テストファイル（2ファイル）
git mv src/test/java/com/chessgame/model/piece/PieceTypeTest.java \
        src/test/java/com/chessgame/piece/
git mv src/test/java/com/chessgame/rules/CheckDetectorTest.java \
        src/test/java/com/chessgame/piece/
```

### Step 3: パッケージ宣言を更新
```java
// Piece.java, King.java, Queen.java, Rook.java, Bishop.java, Knight.java, Pawn.java, PieceType.java
package com.chessgame.piece.model;

// CheckDetector.java
package com.chessgame.piece.rules;

// テストファイル
package com.chessgame.piece;
```

### Step 4: インポート文を更新（全ファイル）
```
com.chessgame.model.piece.* → com.chessgame.piece.model.*
com.chessgame.rules.CheckDetector → com.chessgame.piece.rules.CheckDetector
```

### Step 5: 古いディレクトリを削除
```bash
git rm -r src/main/java/com/chessgame/model/piece
git rm -r src/test/java/com/chessgame/model/piece
git rm -r src/test/java/com/chessgame/rules（もし CheckmateDetector のみ残れば）
```

### Step 6: コンパイル・テスト
```bash
mvnw.cmd clean compile
mvnw.cmd test
```

## 6. テスト計画

- [ ] コンパイルエラーなし
- [ ] `mvnw.cmd test` で全テスト（52件）がパス
- [ ] PieceTypeTest が新しい package で実行成功
- [ ] CheckDetectorTest が新しい package で実行成功

## 7. 成功基準

- [ ] Piece コンポーネント関連ファイルが新パッケージに移動
- [ ] すべてのインポート文が更新済み
- [ ] コンパイルエラーなし
- [ ] テスト全パス
- [ ] 古いパッケージディレクトリが削除済み

## 8. 次のステップ（Phase 3）

Phase 2 が完了したら、同じアプローチで **Move コンポーネント** をリファクタリング：
- `src/main/java/com/chessgame/model/move/*`
- 対応するテストファイル
