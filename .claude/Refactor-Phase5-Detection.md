# コンポーネント別リファクタリング Phase 5: Detection コンポーネント

## 1. 目標

Detection コンポーネント（ゲーム状態検出）を独立したパッケージに再構成する。
CheckmateDetector を rules に集約し、CheckDetector との関係を整理。

## 2. 対象ファイル

### 移動対象
```
Java ソース:
- src/main/java/com/chessgame/rules/CheckmateDetector.java
  → src/main/java/com/chessgame/detection/rules/CheckmateDetector.java

注記：
- CheckDetector は Phase 2 で既に com.chessgame.piece.rules に移動済み
  （Piece コンポーネント内に保持）
- 本 Phase では CheckmateDetector のみを detection コンポーネントに移動
```

## 3. パッケージ構造

```
Before:
com.chessgame.rules.CheckmateDetector

After:
com.chessgame.detection.rules.CheckmateDetector

Remained in original locations:
com.chessgame.piece.rules.CheckDetector （Phase 2 で既に移動）
com.chessgame.rules.MoveValidator （board/rules に移動済み）
```

## 4. 影響を受けるファイル

CheckmateDetector に依存するファイルのインポート文を更新する必要があります：

### Java ソース（更新対象）
- `src/main/java/com/chessgame/game/ChessGame.java` — CheckmateDetector

### テストコード（更新対象）
- `src/test/java/com/chessgame/game/ChessGameTest.java`

## 5. 実装ステップ

### Step 1: ディレクトリ作成
```bash
mkdir -p src/main/java/com/chessgame/detection/rules
```

### Step 2: ファイル移動（git mv で履歴保持）
```bash
git mv src/main/java/com/chessgame/rules/CheckmateDetector.java \
        src/main/java/com/chessgame/detection/rules/
```

### Step 3: パッケージ宣言を更新
```java
// CheckmateDetector.java
package com.chessgame.detection.rules;
```

### Step 4: インポート文を更新（全ファイル）
```
com.chessgame.rules.CheckmateDetector → com.chessgame.detection.rules.CheckmateDetector
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

- [ ] CheckmateDetector ファイルが新パッケージに移動
- [ ] すべてのインポート文が更新済み
- [ ] コンパイルエラーなし
- [ ] テスト全パス

## 8. 次のステップ（Phase 6）

Phase 5 が完了したら、Game コンポーネント（ChessGame, Player）をリファクタリング予定。