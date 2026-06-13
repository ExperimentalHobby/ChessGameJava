# コンポーネント別パッケージ構造リファクタリング計画

## 1. 現状分析

### 現在のパッケージ構造
```
src/main/java/com/chessgame/
├── model/              ボード・駒・移動（混合）
│   ├── Color.java
│   ├── GameState.java
│   ├── board/
│   │   ├── Board.java
│   │   ├── Position.java
│   │   └── Square.java
│   ├── piece/
│   │   ├── Piece.java
│   │   ├── PieceType.java
│   │   ├── King.java
│   │   ├── Queen.java
│   │   ├── Rook.java
│   │   ├── Bishop.java
│   │   ├── Knight.java
│   │   └── Pawn.java
│   └── move/
│       ├── Move.java
│       ├── MoveHistory.java
│       └── MoveType.java
├── rules/              ルール検証（独立）
│   ├── MoveValidator.java
│   ├── CheckDetector.java
│   └── CheckmateDetector.java
├── game/               ゲームロジック（独立）
│   ├── ChessGame.java
│   ├── GameObserver.java
│   ├── Player.java
│   └── AIPlayer.java
├── swing/              Swing GUI
│   ├── SwingChessGameFrame.java
│   ├── SwingChessBoardPanel.java
│   └── PieceImageGenerator.java
├── javafx/             JavaFX GUI
│   ├── FXLauncher.java
│   ├── ChessGameApp.java
│   ├── ChessBoardView.java
│   ├── SquareView.java
│   ├── ControlPanel.java
│   ├── StatusBar.java
│   ├── PromotionDialog.java
│   ├── PieceRenderer.java
│   └── PieceImageLoader.java
├── InteractiveGame.java
└── Main.java
```

### 問題点
- モデル層が盤面・駒・移動にまたがる
- rules パッケージが独立しすぎている
- UI 層がそれぞれ独立している
- 機能単位でのグループ化がない

---

## 2. 目標構造

### 提案するコンポーネント別パッケージ構造
```
src/main/java/com/chessgame/
├── board/              チェスボード・座標関連
│   ├── model/
│   │   ├── Board.java
│   │   ├── Position.java
│   │   └── Square.java
│   └── rules/
│       └── MoveValidator.java
├── piece/              駒・駒種関連
│   ├── model/
│   │   ├── Piece.java
│   │   ├── PieceType.java
│   │   ├── King.java
│   │   ├── Queen.java
│   │   ├── Rook.java
│   │   ├── Bishop.java
│   │   ├── Knight.java
│   │   └── Pawn.java
│   └── rules/
│       └── CheckDetector.java
├── move/               移動・履歴関連
│   └── model/
│       ├── Move.java
│       ├── MoveHistory.java
│       └── MoveType.java
├── game/               ゲームロジック・状態管理
│   ├── ChessGame.java
│   ├── GameObserver.java
│   ├── Player.java
│   ├── AIPlayer.java
│   └── GameState.java
├── ai/                 AI 関連（Python ブリッジ）
│   └── AIPlayer.java   （game/ から移動）
├── detection/          チェック・チェックメイト検出
│   └── CheckmateDetector.java
├── ui/                 UI 層（共通）
│   ├── Color.java      （model/ から移動）
│   ├── swing/
│   │   ├── SwingChessGameFrame.java
│   │   ├── SwingChessBoardPanel.java
│   │   └── PieceImageGenerator.java
│   └── javafx/
│       ├── FXLauncher.java
│       ├── ChessGameApp.java
│       ├── ChessBoardView.java
│       ├── SquareView.java
│       ├── ControlPanel.java
│       ├── StatusBar.java
│       ├── PromotionDialog.java
│       ├── PieceRenderer.java
│       └── PieceImageLoader.java
├── InteractiveGame.java
└── Main.java
```

### 新パッケージの役割

| パッケージ | 責務 | 含まれるクラス |
|-----------|------|-----------------|
| `board` | ボード状態・座標・マス管理、移動ルール検証 | Board, Position, Square, MoveValidator |
| `piece` | 駒の種類・属性・検出ロジック | Piece, PieceType, 6種の駒クラス, CheckDetector |
| `move` | 移動の表現・履歴管理 | Move, MoveHistory, MoveType |
| `game` | ゲーム全体の状態・進行管理 | ChessGame, GameObserver, Player, GameState |
| `ai` | AI 着手選択（Python ブリッジ） | AIPlayer |
| `detection` | チェック・チェックメイト・ステールメイト判定 | CheckmateDetector |
| `ui` | ユーザーインターフェース | Swing, JavaFX, Color（共通） |

---

## 3. リファクタリング計画

### Phase 1: パッケージ構造の作成（影響なし）
- 新しいパッケージディレクトリを作成
- 既存クラスを新パッケージにコピー
- **この段階では既存パッケージは削除しない**

### Phase 2: インポート文の更新（段階的）
- 新パッケージへの参照にインポート文を更新
- テストコードも同時に更新
- コンパイルテストで確認

### Phase 3: 既存パッケージの削除
- 既存パッケージのクラスを削除
- `target/` をクリアして再コンパイル

### Phase 4: テスト・ドキュメント更新
- JUnit テスト実行（全テストパス）
- README.md のプロジェクト構造を更新
- CLAUDE.md のアーキテクチャセクションを更新

---

## 4. 実装ステップ（詳細）

### Step 1-1: 新パッケージの作成
```
mkdir -p src/main/java/com/chessgame/board/model
mkdir -p src/main/java/com/chessgame/board/rules
mkdir -p src/main/java/com/chessgame/piece/model
mkdir -p src/main/java/com/chessgame/piece/rules
mkdir -p src/main/java/com/chessgame/move/model
mkdir -p src/main/java/com/chessgame/game
mkdir -p src/main/java/com/chessgame/ai
mkdir -p src/main/java/com/chessgame/detection
mkdir -p src/main/java/com/chessgame/ui/swing
mkdir -p src/main/java/com/chessgame/ui/javafx
```

### Step 1-2: ファイルの移動（以下のファイルを新パッケージにコピー）

**board/model/**
- `src/main/java/com/chessgame/model/board/Board.java`
- `src/main/java/com/chessgame/model/board/Position.java`
- `src/main/java/com/chessgame/model/board/Square.java`

**board/rules/**
- `src/main/java/com/chessgame/rules/MoveValidator.java`

**piece/model/**
- `src/main/java/com/chessgame/model/piece/Piece.java`
- `src/main/java/com/chessgame/model/piece/PieceType.java`
- `src/main/java/com/chessgame/model/piece/King.java`
- `src/main/java/com/chessgame/model/piece/Queen.java`
- `src/main/java/com/chessgame/model/piece/Rook.java`
- `src/main/java/com/chessgame/model/piece/Bishop.java`
- `src/main/java/com/chessgame/model/piece/Knight.java`
- `src/main/java/com/chessgame/model/piece/Pawn.java`

**piece/rules/**
- `src/main/java/com/chessgame/rules/CheckDetector.java`

**move/model/**
- `src/main/java/com/chessgame/model/move/Move.java`
- `src/main/java/com/chessgame/model/move/MoveHistory.java`
- `src/main/java/com/chessgame/model/move/MoveType.java`

**game/**
- `src/main/java/com/chessgame/game/ChessGame.java`
- `src/main/java/com/chessgame/game/GameObserver.java`
- `src/main/java/com/chessgame/game/Player.java`
- `src/main/java/com/chessgame/model/GameState.java` → `game/GameState.java`

**ai/**
- `src/main/java/com/chessgame/game/AIPlayer.java` → `ai/AIPlayer.java`

**detection/**
- `src/main/java/com/chessgame/rules/CheckmateDetector.java`

**ui/**
- `src/main/java/com/chessgame/model/Color.java` → `ui/Color.java`

**ui/swing/**
- `src/main/java/com/chessgame/swing/*`

**ui/javafx/**
- `src/main/java/com/chessgame/javafx/*`

**ルート**
- `src/main/java/com/chessgame/InteractiveGame.java`
- `src/main/java/com/chessgame/Main.java`

### Step 2: パッケージ宣言の更新
各ファイルの先頭の `package` 宣言を新パッケージ名に変更。

例：
```java
// Before
package com.chessgame.model.board;

// After
package com.chessgame.board.model;
```

### Step 3: インポート文の更新
全ファイルのインポート文を新パッケージ構造に対応。

例：
```java
// Before
import com.chessgame.model.board.Position;
import com.chessgame.rules.MoveValidator;

// After
import com.chessgame.board.model.Position;
import com.chessgame.board.rules.MoveValidator;
```

### Step 4: テスト実行
```bash
mvnw.cmd clean compile
mvnw.cmd test
```

### Step 5: 既存パッケージの削除
```
rm -r src/main/java/com/chessgame/model
rm -r src/main/java/com/chessgame/rules
rm -r src/main/java/com/chessgame/swing
rm -r src/main/java/com/chessgame/javafx
rm -r src/test/java/com/chessgame/model
rm -r src/test/java/com/chessgame/rules
```

### Step 6: ドキュメント更新
- README.md の「プロジェクト構造」を更新
- CLAUDE.md の「アーキテクチャ」を更新

---

## 5. 影響範囲

### 修正が必要なファイル
- Java ソースコード: **40+ ファイル**（パッケージ名とインポート文）
- テストコード: **8+ ファイル**
- ドキュメント: README.md, CLAUDE.md

### コンパイル・テストの検証
- Java コンパイル ✓
- JUnit テスト全実行 ✓
- Python AI テスト ✓（影響なし）

---

## 6. リスク・注意点

1. **大規模なファイル移動**
   - git のファイル履歴が追跡困難になる可能性
   - 対策: `git mv` コマンドを使用して移動

2. **インポート文の重複編集**
   - IDE の自動インポート補完に頼らず、手動で確認
   - 対策: 段階的に検証

3. **テストの実行確認**
   - すべてのテストがパスすることを確認
   - 対策: 各 Phase ごとにテスト実行

4. **ドキュメントの乖離**
   - 古いドキュメントが残る可能性
   - 対策: README.md / CLAUDE.md を最新に更新

---

## 7. スケジュール目安

| Phase | 作業内容 | 見積時間 |
|-------|---------|---------|
| 1 | パッケージ・ファイル構成 | 30分 |
| 2 | インポート文・パッケージ名更新 | 1〜2時間 |
| 3 | コンパイル・テスト | 30分 |
| 4 | ドキュメント更新 | 30分 |

**合計**: 2.5〜3.5時間

---

## 8. 成功基準

- [ ] 新パッケージ構造が機能別に整理されている
- [ ] すべての Java ファイルが新パッケージに移動している
- [ ] `mvnw.cmd compile` がエラーなく完了
- [ ] `mvnw.cmd test` で全テスト（52件）がパス
- [ ] `py -m unittest discover -s ai -p "test_*.py"` が実行成功
- [ ] README.md がプロジェクト構造の変更を反映している
- [ ] CLAUDE.md のアーキテクチャセクションが更新されている
