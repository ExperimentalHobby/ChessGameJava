# Java Chess Game

完全に機能するチェスゲーム（Swing GUI・JavaFX GUI・コンソール対応）

## ゲーム概要

Java で実装された完全なチェスゲームです。標準チェスルールに準拠し、Swing GUI / JavaFX GUI / コンソールでプレイできます。Human vs Human および Human vs AI（難易度4段階）モードをサポートします。

## 機能

- 完全なチェスルール実装（標準ルール準拠）
- 2人対戦モード
- AI 対戦モード（Easy / Medium / Hard / Expert の4段階。Expert は minimax + alpha-beta + 静止探索）
- すべてのピース移動対応
- チェック / チェックメイト / ステールメイト検出
- 引き分け判定（50手ルール・千日手・戦力不足）
- 移動やり直し機能
- ゲーム履歴追跡・直前の手のハイライト表示・棋譜パネル表示
- ポーン昇格対応
- キャスリング対応
- アンパッサン対応
- SAN記譜によるPGNのインポート/エクスポート（`ChessGame` API）
- FENによる局面の保存・読み込み（`ChessGame` API）

## 必要環境

- Java 25 以上（[Eclipse Temurin](https://adoptium.net/) OpenJDK 25+ 推奨）
- Maven Wrapper 同梱（`mvnw.cmd` / `mvnw`）— 別途インストール不要
- Python 3（任意）— AI の着手選択に使用。未導入でも Java フォールバックで動作するが、難易度4（minimax）を使うには必要（標準ライブラリのみ・pip 不要）

## ビルド方法

```cmd
REM Swing 版（デフォルト・Maven 不要）
build.bat

REM JavaFX 版（Maven 使用）
build.bat --javafx
```

Unix の場合:

```bash
./build.sh           # Swing のみ
./build.sh --javafx  # JavaFX も含む
```

ビルドの自動処理（3ステップ）:

| ステップ | 処理 |
|--------|------|
| 1 | メインソース（Swing + モデル）をコンパイル → `target\classes` |
| 2 | JUnit テストをコンパイル（Maven がない環境はスキップ） → `target\test-classes` |
| 3 | exe にパッケージング → `bin\ChessGame\`（Swing）/ `bin\ChessGameFX\`（`--javafx` 指定時） |

生成物:

| ターゲット | 実行ファイル | 配布フォルダ |
|-----------|------------|------------|
| Swing | `bin\ChessGame\ChessGame.exe` | `bin\ChessGame\` |
| JavaFX | `bin\ChessGameFX\ChessGameFX.exe` | `bin\ChessGameFX\` |

各フォルダをそのまま配布すれば、Java がない PC でも動作します。

> **注意**: `build.sh`（Unix）はコンパイルのみです。exe 生成は Windows の `build.bat` でのみサポートされています。

## ゲーム実行方法

### exe から起動（推奨）

```cmd
bin\ChessGame\ChessGame.exe
```

### ソースから直接起動

```cmd
REM GUI モード（Swing）
java -cp target\classes com.chessgame.Main

REM コンソール対話モード
java -cp target\classes com.chessgame.InteractiveGame

REM JavaFX GUI
mvnw.cmd javafx:run
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

## Visual Studio Code での開発環境構築

### 必要なもの

| ツール | バージョン | 入手先 |
|--------|-----------|--------|
| VS Code | 最新版 | [code.visualstudio.com](https://code.visualstudio.com/) |
| JDK | 25 以上 | [Eclipse Temurin](https://adoptium.net/) |
| Extension Pack for Java | 最新版 | VS Code 拡張機能マーケットプレイス |
| Python 3（任意） | 3.x | [python.org](https://www.python.org/) — AI 難易度4・Python テスト用 |

### 手順

**1. JDK 25 のインストール**

[Eclipse Temurin](https://adoptium.net/) から JDK 25 をダウンロードしてインストールし、`JAVA_HOME` を設定します。

```cmd
REM インストール確認
java -version
```

**2. 拡張機能のインストール**

VS Code の拡張機能ビュー（`Ctrl+Shift+X`）で **Extension Pack for Java** を検索してインストールします。

以下の拡張機能が一括で導入されます:

| 拡張機能 | 役割 |
|---------|------|
| Language Support for Java | コード補完・リファクタリング |
| Debugger for Java | ブレークポイント・ステップ実行 |
| Test Runner for Java | JUnit テストの実行・デバッグ |
| Maven for Java | Maven ライフサイクル操作 |
| Project Manager for Java | プロジェクト管理 |

**3. プロジェクトを開く**

```cmd
code d:\git\github\JavaTest\ChessGame
```

または VS Code の「フォルダーを開く」からリポジトリルートを選択します。  
初回起動時に Language Server が依存関係を解析するため、数十秒かかる場合があります。

### 既存の設定ファイル

以下は開発時にローカルで使用している VS Code 用の設定です。`.gitignore` 対象のためリポジトリには含まれません。開発時は同じ内容でローカルに作成してください。

**`.vscode/settings.json`** — ソースパスと除外ディレクトリの設定:

```json
{
  "java.project.sourcePaths": ["src/main/java", "src/test/java"],
  "files.watcherExclude": { "**/target/**": true, ... }
}
```

**`.settings/org.eclipse.jdt.core.prefs`** — Eclipse JDT Language Server に Java 25 を認識させる設定（自動で読み込まれます）。

**`.vscode/tasks.json`** — VS Code タスク設定。ビルド・テスト実行を Ctrl+Shift+B で実行可能。

### ビルド・実行（VS Code UI から）

**タスク実行** (`Ctrl+Shift+P` → "タスク: タスクを実行"）:

| タスク名 | 説明 |
|---------|------|
| Maven: compile | クイックコンパイル |
| Maven: clean compile | キャッシュクリア後コンパイル |
| Maven: test | JUnit テスト実行 |
| Maven: clean test | キャッシュクリア後テスト実行 |
| Python: run AI tests | Python AI テスト実行 |
| Build: Swing (bin) | Swing exe ビルド |
| Build: JavaFX (bin) | JavaFX exe ビルド |
| Build: Both (Swing + JavaFX to bin) | 両方同時ビルド |

その他の操作:

- **テスト**は `src/test/java` を開いてクラス左の `▶` アイコンをクリック
- **デバッグ**は `F5`、またはメインクラス（`Main.java` / `InteractiveGame.java`）を右クリック → "Debug"

### 注意事項

- Eclipse JDT Language Server は `instanceof` パターンマッチング（Java 16+）の一部でエラーを誤検知する場合があります。実際のビルド正否は `mvnw.cmd test` で確認してください。
- JavaFX モジュール関連のクラス（`com.chessgame.javafx`）は Maven ビルド（`--javafx`）でのみコンパイルされるため、IDE 上で赤波線が表示される場合があります。

### UI 統一（Swing と JavaFX）

Swing（安定版）と JavaFX（開発版）の UI を統一しました。

| 項目 | Swing | JavaFX |
|-----|-------|--------|
| ゲームモード選択 | 複数ボタンダイアログ | 複数ボタンダイアログ（カスタム） |
| ウィンドウサイズ | 自動（`pack()`、リサイズ不可） | 640 × 550（固定、リサイズ不可） |
| ボード色 | RGB(240,217,181) / RGB(181,136,99) | #F0D9B5 / #B58863 |
| ハイライト色 | RGB(255,215,0,160) | #FFD700（透明度63%） |
| コントロール | 右側に配置、固定サイズ | 右側に配置、自動サイズ |

どちらのバージョンでも同じゲーム体験が得られます。JavaFX は Maven ビルド（`build.bat --javafx`）で完全にサポートされています。

## テスト

```cmd
REM Maven 経由で JUnit テストを実行（Windows）
mvnw.cmd test

REM Unix
./mvnw test
```

JUnit テスト一覧（計196件）:

| テストクラス | 対象 | 件数 |
|------------|------|------|
| `ChessGameTest` | ゲームフロー全般・ポーン昇格・引き分け・FEN/PGN | 44 |
| `CheckDetectorTest` | 王手検出・ブロッカー動作 | 4 |
| `CheckmateDetectorTest` | チェックメイト・ステールメイト判定 | 8 |
| `DrawDetectorTest` | 50手ルール・千日手・戦力不足の判定 | 11 |
| `MoveValidatorTest` | 全駒種の擬似合法手生成・キャスリング・アンパッサン | 22 |
| `MoveHistoryTest` | 移動履歴の追加・Undo・棋譜フォーマット | 8 |
| `PositionTest` | 座標変換・DSL | 7 |
| `BoardTest` | 盤面操作・クローン | 12 |
| `MoveTest` | 移動オブジェクト・型判定 | 5 |
| `PieceTypeTest` | 駒種の素材値・記法文字 | 2 |
| `PlayerTest` | プレイヤーファクトリ・属性・同値性 | 5 |
| `AIPlayerTest` | AI 着手選択（難易度1〜4・Python フォールバック） | 14 |
| `AiEngineParityTest` | Java ルールと Python エンジンの合法手一致 | 1 |
| `FenCodecTest` | FEN文字列とBoard/局面情報の相互変換 | 5 |
| `SanCodecTest` | 手とSAN記譜の相互変換 | 12 |
| `StatusPanelTest` | Swing UI ステータス表示パネル | 4 |
| `ControlPanelTest` | Swing UI コントロールパネル | 4 |
| `MoveHistoryPanelTest`（Swing） | Swing UI 棋譜表示パネル | 2 |
| `swing/GameModeDialogTest` | Swing UI ゲームモード選択ダイアログ | 3 |
| `javafx/GameModeDialogTest` | JavaFX UI ゲームモード選択ダイアログ | 5 |
| `InteractiveGameTest` | コンソールUI（InteractiveGame）の結合テスト | 5 |
| `swing/SwingChessBoardPanelTest` | Swing 盤面パネルの結合テスト | 5 |
| `swing/SwingChessGameFrameTest` | Swing メインフレームの結合テスト | 4 |
| `javafx/ChessGameAppTest` | JavaFX UIとゲームロジックの結合テスト | 4 |

Python 側ロジック（難易度1〜3 の選択・難易度4 エンジンの perft / 評価 / 探索）のテストは
標準ライブラリ `unittest` で、pip 不要で実行できる:

```cmd
py -m unittest discover -s ai -p "test_*.py" -v
```

## CI（継続的インテグレーション）

GitHub Actions（[`.github/workflows/ci.yml`](.github/workflows/ci.yml)）で以下を自動実行しています。

| ジョブ | 内容 | トリガー |
|--------|------|---------|
| Linux | `./mvnw test`（Javaテスト）、Swing/JavaFX のコンパイル確認、Pythonテスト | push / PR（main） |
| Windows | `mvnw.cmd`（コンパイル・テスト）、exe パッケージング確認 | push / PR（main） |
| Static Analysis | Checkstyle・PMD・Ruff・Bandit・Gitleaks（各ステップ `continue-on-error: true` のため情報提供目的。マージのブロックはしない） | push / PR（main） |
| CodeQL | Java/Kotlin の静的セキュリティ解析（GitHub CodeQL） | push / PR（main） |
| Weekly Dependency & Secret Audit | Trivy による依存関係脆弱性スキャン、Gitleaks | 毎週月曜 JST 9:00 |

Weekly Dependency & Secret Audit 以外の全ジョブは手動実行（`workflow_dispatch`）でも起動できる。また毎週月曜 JST 9:00 の定期実行（`schedule`）では Weekly Dependency & Secret Audit に加え、他の全ジョブも併走する。

## プロジェクト構造

```
ChessGame/
├── src/
│   ├── main/java/com/chessgame/
│   │   ├── board/
│   │   │   ├── model/          (Board, Position, Square)
│   │   ├── piece/
│   │   │   ├── model/          (Piece 抽象クラス、6 種の具象クラス、PieceType)
│   │   │   └── rules/          (CheckDetector)
│   │   ├── move/
│   │   │   └── model/          (Move, MoveHistory, MoveType)
│   │   ├── gamestate/
│   │   │   └── model/          (GameState)
│   │   ├── detection/
│   │   │   └── rules/          (CheckmateDetector, DrawDetector)
│   │   ├── notation/
│   │   │   └── rules/          (FenCodec, SanCodec)
│   │   ├── rules/              # MoveValidator（コンポーネント非依存のルール検証）
│   │   │   └── MoveValidator.java
│   │   ├── model/
│   │   │   └── Color.java      # 広く依存されるため独立配置
│   │   ├── game/               # ゲームコントローラー層
│   │   │   ├── core/           (ChessGame)
│   │   │   ├── player/         (Player, AIPlayer ― 難易度1〜4／Python ブリッジ＋Java フォールバック)
│   │   │   └── observer/       (GameObserver)
│   │   ├── swing/              # Swing GUI 層（安定版・コンポーネント分割）
│   │   │   ├── ui/             (SwingChessGameFrame)
│   │   │   │   ├── dialog/     (GameModeDialog)
│   │   │   │   └── panel/      (StatusPanel, ControlPanel, MoveHistoryPanel)
│   │   │   ├── board/          (SwingChessBoardPanel)
│   │   │   └── asset/          (PieceImageGenerator)
│   │   ├── javafx/             # JavaFX GUI 層（開発版・コンポーネント分割）
│   │   │   ├── ui/             (FXLauncher, ChessGameApp, ControlPanel, StatusBar, MoveHistoryPanel)
│   │   │   │   └── dialog/     (GameModeDialog, PromotionDialog)
│   │   │   ├── board/          (ChessBoardView, SquareView)
│   │   │   └── asset/          (PieceRenderer, PieceImageLoader)
│   │   ├── InteractiveGame.java
│   │   └── Main.java
│   └── test/java/com/chessgame/
│       ├── InteractiveGameTest.java        # コンソールUIの結合テスト
│       ├── board/ (BoardTest, PositionTest)
│       ├── piece/ (PieceTypeTest, CheckDetectorTest)
│       ├── move/ (MoveTest, MoveHistoryTest)
│       ├── rules/MoveValidatorTest.java
│       ├── detection/ (CheckmateDetectorTest, DrawDetectorTest)
│       ├── notation/rules/ (FenCodecTest, SanCodecTest)
│       ├── game/core/ (ChessGameTest, AiEngineParityTest)
│       ├── game/player/ (PlayerTest, AIPlayerTest)
│       ├── swing/board/SwingChessBoardPanelTest.java   # Swing 盤面パネルの結合テスト
│       ├── swing/ui/SwingChessGameFrameTest.java       # Swing メインフレームの結合テスト
│       ├── swing/ui/{dialog,panel}/ (GameModeDialogTest, StatusPanelTest, ControlPanelTest, MoveHistoryPanelTest)
│       └── javafx/ui/ (ChessGameAppTest, dialog/GameModeDialogTest)  # ChessGameAppTest はゲームロジックとの結合テスト
├── ai/                     # AI 着手選択（Python サブプロセス連携）
│   ├── chess_ai.py         # 難易度別の着手選択ディスパッチ（難易度1〜3／4分岐）
│   ├── engine.py           # 難易度4: minimax + αβ エンジン（FEN・move-gen・評価）
│   ├── invalid_bestmove_stub.py # 不正なUCI応答を返すスタブ（Javaフォールバックの結合テスト用）
│   ├── test_chess_ai.py    # 難易度1〜3 の選択ロジックのテスト（pip 不要）
│   ├── test_engine.py      # 評価・探索（詰み/ただ取り/αβ一致）のテスト
│   └── test_engine_perft.py # move-gen の perft 検証テスト
├── target\classes\         # コンパイル済みクラス
├── target\test-classes\    # コンパイル済みテスト・デモクラス
├── target\ChessGame.jar    # 実行可能 JAR（build.bat 生成）
├── bin\ChessGame\          # Swing パッケージ済み exe（生成物）
├── bin\ChessGameFX\        # JavaFX パッケージ済み exe（生成物）
├── build.bat               # コンパイル → exe 生成（Windows）
├── build.sh                # コンパイルのみ（Unix）
├── .github\workflows\
│   └── ci.yml              # CI（ビルド・テスト・静的解析・週次依存監査）
├── LICENSE
└── README.md
```

## アーキテクチャ

MVC の4層構造。

| 層 | パッケージ | 役割 |
|----|-----------|------|
| Model | `com.chessgame.{board,piece,move,gamestate}.model` / `com.chessgame.model.Color` | 値オブジェクト群（`Piece` を除き不変） |
| Rules | `com.chessgame.rules` / `com.chessgame.piece.rules` / `com.chessgame.detection.rules` / `com.chessgame.notation.rules` | 副作用のない純粋なルールロジック（棋譜・FEN変換を含む） |
| Controller | `com.chessgame.game.{core,player,observer}` | ゲーム状態管理・API |
| View | `com.chessgame.swing` / `com.chessgame.javafx` | GUI 実装 |

### AI 難易度

| 難易度 | 選択肢 | 戦略 |
|--------|--------|------|
| 1 (Easy) | Human vs AI（Easy） | ランダムな合法手 |
| 2 (Medium) | Human vs AI（Medium） | 駒取りを優先、次いでランダム |
| 3 (Hard) | Human vs AI（Hard） | 最善手を素材評価で選択（1手読み） |
| 4 (Expert) | Human vs AI（Expert） | minimax + alpha-beta + 静止探索（既定深さ3、マテリアル + PST 評価。水平線効果を静止探索で緩和） |

#### AI の着手選択（Python ブリッジ）

着手選択ロジックは Python に分離されている。`AIPlayer` は Python プロセス（stdin/stdout）
へ委譲し、Python が利用できない／連携に失敗した場合は同等の Java 実装に**自動フォール
バック**するため、Python が無い環境でもそのまま動作する。

- **難易度1〜3** — [`ai/chess_ai.py`](ai/chess_ai.py): 合法手を JSON で渡し、選ばれた手の index を受け取る。フォールバックは同ロジックの Java 実装。
- **難易度4** — [`ai/engine.py`](ai/engine.py): 盤面を FEN で渡し、minimax + alpha-beta 探索の最善手を UCI（例 `e2e4`）で受け取る自己完結エンジン。フォールバックは難易度3相当（1手読み）。move-gen の正しさは perft、Java ルールとの整合性は `AiEngineParityTest` で担保する。

| 設定（システムプロパティ / 環境変数） | 既定値 | 用途 |
|---|---|---|
| `chess.ai.python` / `CHESS_AI_PYTHON` | `py` → `python3` → `python` を順に試行 | Python 実行コマンド |
| `chess.ai.script` | `ai/chess_ai.py` | AI スクリプトのパス |
| `chess.ai.depth` | `3` | 難易度4の探索深さ |
| `chess.ai.timeout` | `20` | 難易度4の実行タイムアウト（秒） |

Python 側ロジックのテスト（move-gen の perft・評価・探索を含む）:

```bat
py -m unittest discover -s ai -p "test_*.py" -v
```

## 既知の制限

- PGN/FEN の入出力は `ChessGame` の API（`toPgn()` / `fromPgn()` / `toFen()` / `fromFen()`）としては実装済みだが、GUI（Swing/JavaFX）・コンソールからファイル保存/読み込みを行うメニュー・コマンドは未実装
- 対局の持ち時間管理（blitz / rapid / classical）は未実装
- ネットワークマルチプレイは未実装

## アーキテクチャドキュメント

### パッケージ責任分離

各パッケージは単一責任の原則に従い、段階的なリファクタリングで整理されました。Phase 1-7 は `.claude/Refactor-Phase1〜7-*.md` にドキュメント化されています（Phase 8-10 の UI 細分化は個別ドキュメントを作成せず、下表と実装のみで管理）：

| フェーズ | パッケージ | 説明 |
|---------|-----------|------|
| Phase 1-2 | `board.model` / `piece.model` + `piece.rules` | 値オブジェクト（盤面：不変、駒：可変）、CheckDetector を piece に統合 |
| Phase 3-4 | `move.model` / `gamestate.model` | 移動履歴・ゲーム状態表現（`Color` は依存が広いため `model` 直下に据え置き） |
| Phase 5-6 | `detection.rules` / `game.{core,player,observer}` | チェックメイト検出の独立化、ゲームコントローラーの内部構造化 |
| Phase 7-8 | `swing.{ui,board,asset}` / `javafx.{ui,board,asset}` | Swing / JavaFX UI 責任分離 |
| Phase 9-10 | `swing.ui.{dialog,panel}` / `javafx.ui.dialog` | Swing・JavaFX UI さらなる細分化 |

なお `MoveValidator` は Phase 1 でコンポーネント統合を見送り、`com.chessgame.rules` に据え置かれている。

### Swing UI コンポーネント構造（Phase 9）

```
SwingChessGameFrame
├── GameModeDialog    # ゲームモード選択
│   └── ChessGame 生成
├── StatusPanel       # ステータス表示
│   ├── 手番・手数表示
│   └── ゲーム状態表示（王手・チェックメイト・引き分け等）
├── MoveHistoryPanel  # 棋譜（指し手履歴）表示
└── ControlPanel      # ボタン制御
    ├── NewGame ボタン
    ├── Undo ボタン
    ├── Resign ボタン
    └── Quit ボタン
```

### 設計パターン

- **Observer** — `GameObserver`: ゲーム状態変化を UI に通知
- **Strategy** — `MoveValidator`: 駒種ごとの移動ルール
- **Factory** — `ChessGame.createTwoPlayerGame()`: ゲーム生成
- **Callback** — `ControlPanel.setOnXXX()`: ボタン動作設定
- **Immutable** — `Position`, `Move`: 状態の一貫性担保（`Piece` は可変オブジェクトのため除く）

## 今後の拡張

- AI のさらなる強化（反復深化・置換表）
- 上記「既知の制限」の解消（PGN/FEN のGUI・コンソール統合、時間管理、ネットワークマルチプレイ）

## ライセンス

MIT License — 詳細は [LICENSE](LICENSE) を参照してください。

---

**更新日**: 2026年7月11日（Swing/JavaFX/コンソールの結合テスト拡充・CI（ビルド・静的解析の自動化）導入）
