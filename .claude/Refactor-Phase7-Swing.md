# コンポーネント別リファクタリング Phase 7: Swing UI コンポーネント

## 1. 目標

Swing UI コンポーネント内の責任分離を明確にする。
UI フレーム、ボード表示、画像生成に分割。

## 2. 対象ファイル

### 移動対象
```
Java ソース:
- src/main/java/com/chessgame/swing/SwingChessGameFrame.java
  → src/main/java/com/chessgame/swing/ui/SwingChessGameFrame.java

- src/main/java/com/chessgame/swing/SwingChessBoardPanel.java
  → src/main/java/com/chessgame/swing/board/SwingChessBoardPanel.java

- src/main/java/com/chessgame/swing/PieceImageGenerator.java
  → src/main/java/com/chessgame/swing/asset/PieceImageGenerator.java
```

## 3. パッケージ構造

```
Before:
com.chessgame.swing.*

After:
com.chessgame.swing.ui.SwingChessGameFrame
com.chessgame.swing.board.SwingChessBoardPanel
com.chessgame.swing.asset.PieceImageGenerator
```

## 4. 影響を受けるファイル

Swing コンポーネントに依存するファイルのインポート文を更新する必要があります：

### Java ソース（更新対象）
- `src/main/java/com/chessgame/InteractiveGame.java` — SwingChessGameFrame
- `src/main/java/com/chessgame/Main.java` — SwingChessGameFrame

## 5. 実装ステップ

### Step 1: ディレクトリ作成
```bash
mkdir -p src/main/java/com/chessgame/swing/{ui,board,asset}
```

### Step 2: ファイル移動（git mv で履歴保持）
```bash
# ui
git mv src/main/java/com/chessgame/swing/SwingChessGameFrame.java \
        src/main/java/com/chessgame/swing/ui/

# board
git mv src/main/java/com/chessgame/swing/SwingChessBoardPanel.java \
        src/main/java/com/chessgame/swing/board/

# asset
git mv src/main/java/com/chessgame/swing/PieceImageGenerator.java \
        src/main/java/com/chessgame/swing/asset/
```

### Step 3: パッケージ宣言を更新
```java
// SwingChessGameFrame.java
package com.chessgame.swing.ui;

// SwingChessBoardPanel.java
package com.chessgame.swing.board;

// PieceImageGenerator.java
package com.chessgame.swing.asset;
```

### Step 4: インポート文を更新（全ファイル）
```
com.chessgame.swing.SwingChessGameFrame → com.chessgame.swing.ui.SwingChessGameFrame
com.chessgame.swing.SwingChessBoardPanel → com.chessgame.swing.board.SwingChessBoardPanel
com.chessgame.swing.PieceImageGenerator → com.chessgame.swing.asset.PieceImageGenerator
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

- [ ] Swing UI コンポーネント内の構造が整理
- [ ] すべてのインポート文が更新済み
- [ ] コンパイルエラーなし
- [ ] テスト全パス

## 8. 次のステップ（Phase 8 - オプション）

JavaFX UI コンポーネント（`com.chessgame.javafx`）の整理も可能。
同様の構造（ui/, board/, asset/）に分割予定。