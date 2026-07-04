# CLAUDE.md

このファイルは、このリポジトリで作業する Claude Code (claude.ai/code) へのガイダンスを提供します。

## 作業ルール

### 実装前プラン提示
- **実装に入る前に必ず日本語でプランを提示し、ユーザーの承認を得てから実装を開始する。** 方針・変更箇所・影響範囲をプランで明記する。
- **承認されたプランは `docs/{機能名}-Plan.md` として書き出しておく**（`docs/` は `.gitignore` 対象のローカル資料であり PR には含まれない。実装の経緯を後から追えるようにする）。
- **複数項目をまとめて依頼された場合は、1項目ずつ「プラン提示→承認→Issue作成→TDD実装→ビルド/テスト確認→コミットメッセージ提示→承認→コミット・PR作成」のサイクルを完了させてから次の項目に進む。** TodoWrite で全項目の進捗を管理し、項目間で状態を見失わないようにする。

### テスト駆動開発（TDD）
新機能・バグ修正を実装する際は、Red → Green → Refactor のサイクルを厳守する。

**Red → Green → Refactor サイクル**

1. **TODO リスト作成（実装前）** — 実装に入る前に、何をテストするかをリストアップする。コーディング中は視野が狭くなるため、冷静な状態で考える。

2. **テスト記述** — 1 サイクルで倒すテストは必ず 1 つ。複数テストを同時に通そうとしない。

3. **Red 確認（必須）** — テストを実行して失敗することを確認する。**期待したエラーメッセージであることまで検証**してから次に進む。この確認を怠るとバグ原因の特定が困難になる。

4. **Green（最短経路で合格）** — まず動くコードを書く。一時的な汚さは許容し、リファクタリングは次のフェーズに回す。

5. **Refactor（外部ふるまいを変えず内部品質を上げる）** — テストが Green の状態を保ちながら実施。こまめにテストを実行し、Red になったら直前の変更を見直す。**時間制限を設けて終わりを決める。**

6. **繰り返し** — TODO リストから完了項目を消し、次のテストに進む。

**テスト実行コマンド**
```bash
./mvnw.cmd test              # Java テスト全実行
py -m unittest discover -s ai -p "test_*.py"  # Python テスト全実行
```

（Claude Code の Bash ツール環境では `py` ランチャが見つからない場合がある。その場合は `python3 -m unittest discover -s ai -p "test_*.py"` を**リポジトリルートから**実行する。`ai/` ディレクトリ内から実行すると相対 import が解決できずエラーになる）

**単体テスト vs 結合テスト**
- **単体テスト**: 変更した関数・クラスの正常系・異常系・境界値を網羅する
- **結合テスト**: 変更が影響する他機能についても結合テストを追加し、機能間の連携が壊れていないことを確認する

例）MoveValidator の変更なら、その変更が GameObserver の呼び出しや UI の表示に影響しないか確認する

**アンチパターン（やってはいけないこと）**
- 複数テストを同時に倒そうとする
- Red 確認をスキップして実装に入る
- Refactor 中にテスト実行を怠る
- Refactor に時間制限を設けず終わりのないリファクタリングを続ける
- テストなしで実装を始める

### GitHub Flow に従うこと
コード変更は必ず feature ブランチで行う。

**ブランチ運用手順**
1. **Issue作成** — プラン承認後、作業内容を GitHub Issue として作成する。
2. **ブランチ作成** — `main` から feature ブランチを切る。**分岐前に必ず `git pull` 等でローカルの `main` を最新化すること**（複数の feature ブランチ/PR を連続して扱う作業では、先行する PR が途中でマージされていることが多く、古い `main` から分岐すると後続の実装が既存の変更と衝突し、ブランチの作り直しが必要になる）。ブランチ名は作業内容を端的に示す（例: `feature/ui-unification`、`fix/promotion-bug`）
3. **コミット** — 小さい単位でこまめにコミット。

   **コミットメッセージの書き方**
   - 型プレフィックス（英語）: `feat`/`fix`/`docs`/`refactor`/`test`
   - 本文は日本語で記述
   - **メッセージは「何をした」よりも「なぜそうしたか」を説明する** — 変更の意図・理由・背景を含める
   - 例: 
     - ✓ `feat: Phase 9 swing/ui/ をさらに責任別に細分化（ゲームモード選択をダイアログに分離して保守性向上）`
     - ✗ `feat: swing/ui/ を分割`（何をしたかだけで理由がない）

4. **プルリクエスト** — 実装完了後に `main` への PR を作成（`Closes #N` 等で Issue と関連付ける）
   - PR タイトル: 70 文字以内
   - PR 本文: 変更内容・テスト方法を記載

5. **マージ後の後始末** — マージ後はローカル・リモートともにブランチを削除

**禁止事項**
- `main` ブランチへの直接コミット
- **ユーザーの明示的な指示なしにコミットを実行すること** — 実装完了後はコミットメッセージ案を提示してユーザーの承認を得てからコミットする

### 軽微な修正は直接編集可
以下の軽微な修正はプラン提示なしで直接編集可とする。ただし、変更内容が明らかな場合のみ:
- スペルミス・タイポ修正（`Reciever` → `Receiver` など）
- 単純な値の誤り（符号違い `+`/`-`、定数値の訂正など）
- コメント・ドキュメントの文言修正（既存ドキュメントの改善）
- IDE 警告削除（未使用インポート、未使用フィールドなど）

ただし、以下のような変更は軽微ではないため、プラン提示後に実装すること:
- ロジック変更が伴う修正
- ファイル追加・削除
- テスト追加
- 依存関係の変更

### 作業完了時の要件
作業を終了する際は、以下の状態で完了すること。**これらの要件を満たさずに作業を終了しないこと:**

1. **ビルドエラーがないこと** — `mvnw.cmd clean compile` と `build.bat` の両方が成功すること
   ```bash
   mvnw.cmd clean compile
   build.bat
   build.bat --javafx
   ```

2. **警告がないこと** — IDE での未使用インポートやコンパイル警告がないこと
   - 未使用のインポート（IDE の警告で検出される）
   - Java コンパイルエラーまたは警告
   - ビルドスクリプト（build.bat / build.sh）の実行失敗

3. **テストが全てパスしていること** — `mvnw.cmd test` で全テストが成功すること
   ```bash
   mvnw.cmd test              # Java テスト
   py -m unittest discover -s ai -p "test_*.py"  # Python テスト
   ```

4. **コミット・プッシュが完了していること** — PR を作成する直前まで全ての変更がコミット済みであること
   - **ユーザーの明示的な指示がない限り、コミットを実行しない**
   - コミット完了後は `git push` も実施すること

## よくある落とし穴

1. **イミュータブルオブジェクトへの誤認識**: `Position`, `Move`, `Piece` などは不変オブジェクト設計。履歴や他オブジェクトへ渡す前に必ず状態を確認し、参照をそのまま保存しないこと。

2. **チェック状態の判定順序**: チェックメイト・ステールメイト判定の前に、まず手番プレイヤーがチェック状態にあるかを確認すること。順序が逆になるとロジックが破綻する。

3. **テスト失敗の確認漏れ**: Red フェーズで「期待したエラーメッセージ」であることを検証してから実装に進むこと。確認なしで進むとバグ原因特定が難しくなる。

4. **複数テストの同時実装**: TDD では 1 サイクルで必ず 1 つのテストのみを倒すこと。複数テストを同時に通そうとするとテストの品質が低下し、バグを見落とす。

5. **Refactor のゴール不設定**: Refactor フェーズに時間制限を設けないと終わりが来ない。「10 分で終わらせる」など区切りを決めてから始めること。

6. **コメント規約の逆転**: 「何をしているか」ではなく「なぜそうしているか」をコメントする。前者は自明なコードであればコメント不要。

7. **ビルドスクリプトの更新漏れ**: `build.bat`/`build.sh` は Maven を使わず明示的なファイルリストでコンパイルするため、`src/main/java/com/chessgame/` 配下に新しいパッケージ（例 `notation/rules`）を追加した際は両スクリプトのコンパイル対象にも追加すること。`mvnw` はソースを自動検出するため、Maven でのビルド成功だけでは更新漏れに気づけない。

8. **改行コードの混入確認漏れ**: 既存ファイル（特に `.gitattributes` で改行コードが固定されていない `.py` 等）を編集した後は `git diff --stat` で差分行数を確認すること。編集ツールが意図せず LF を CRLF に変換すると、実際の変更が数行でもファイル全体が差分になってしまう。混入していたら LF に戻してから再度確認する。

9. **無関係な既存バグを独断で処理してしまう**: 作業中に今のタスクと無関係な既存バグを見つけた場合、黙って直す・黙って無視するのではなく、見つけた時点でユーザーに扱い（今の PR に含めるか、別 PR に分けるか）を確認してから進めること。

## 開発環境

- **Java**: 25（ビルド・実行ともに Java 25 を使用。`pom.xml` の `source`/`target` も 25）
- **ビルドツール**: Maven Wrapper（`mvnw.cmd` / `mvnw`）

## ビルド・実行コマンド

**ビルド（Maven がない環境では build.bat / build.sh を使う）**

```bat
# Windows — Swing のみ（Maven 不要）
build.bat

# Windows — JavaFX も含めてコンパイル（Maven 使用）
build.bat --javafx

# GUI 起動（Swing）
java -cp target\classes com.chessgame.Main

# JavaFX GUI 起動
mvnw.cmd javafx:run

# コンソール対話ゲーム
java -cp target\classes com.chessgame.InteractiveGame

# テスト（Java / Python）
mvnw.cmd test
py -m unittest discover -s ai -p "test_*.py"
```

Unix の場合:

```bash
./build.sh           # Swing のみ
./build.sh --javafx  # JavaFX も含む
```

注意: `src/main/java/com/chessgame/javafx/` は JavaFX WIP のため、`build.bat` / `build.sh` のコンパイル対象から除外している。

## アーキテクチャ

MVC の4層構造で設計されている。パッケージはコンポーネント（機能単位）別に分割され、各コンポーネント内をさらに `model` / `rules` などの役割別サブパッケージに分けている（詳細な移行経緯は `.claude/Refactor-Phase1〜7-*.md` を参照）。

**モデル層**: イミュータブルな値オブジェクト群。
- `com.chessgame.board.model` — `Board.java`（8×8のグリッド）、`Position.java`（イミュータブルな座標。`Position.of("e2")` DSL 形式を使うこと）、`Square.java`（個々のマスの状態）。
- `com.chessgame.piece.model` — `Piece.java`（抽象クラス。色・位置・移動回数＝キャスリング判定に使用、を保持）と具象サブクラス6種類、`PieceType.java`。
- `com.chessgame.move.model` — `Move.java`（移動元・移動先の座標と `MoveType` 列挙型：`NORMAL`、`CASTLING`、`EN_PASSANT`、`PROMOTION`）、`MoveHistory.java`（手戻し機能）。
- `com.chessgame.gamestate.model.GameState` — 現在の手番・チェック状態などのゲーム状態を管理。
- `com.chessgame.model.Color` — 全コンポーネントから広く参照されるため、独立して `model` 直下に配置。

**ルール層**: 副作用のない純粋なロジック。対応するモデル・コンポーネントごとに配置されている。
- `com.chessgame.rules.MoveValidator` — 駒種ごとの合法手を生成。
- `com.chessgame.piece.rules.CheckDetector` — キングが攻撃されているかを検出。
- `com.chessgame.detection.rules.CheckmateDetector` — チェックメイトとステールメイトを判別。

**ゲームコントローラ層** (`com.chessgame.game`):
- `com.chessgame.game.core.ChessGame` — 主要 API。`ChessGame.createTwoPlayerGame(name1, name2)` で生成する。主なメソッド: `makeMove()`、`getAvailableMoves()`、`undo()`。
- `com.chessgame.game.observer.GameObserver` — オブザーバーインターフェース。盤面更新イベントを受け取るために実装する。
- `com.chessgame.game.player.Player` / `com.chessgame.game.player.AIPlayer` — `AIPlayer` は AI 対戦相手。難易度 1（ランダム）・2（駒取り優先）・3（最善手優先＝1手読み）・4（minimax + alpha-beta）の4段階。着手選択は Python へサブプロセス連携で委譲し、Python が使えない場合は Java 実装に自動フォールバックする（難易度4は難易度3相当に退避）。難易度4では `buildFen()` で盤面を FEN 化して渡す。

**AI 着手選択層** (`ai/`): Java 非依存の Python ロジック。
- `ai/chess_ai.py` — stdin の JSON を読み、難易度1〜3は手リストから index を、難易度4は engine に委譲して最善手を UCI で stdout に返す。`command:"movegen"` で FEN の合法手列挙も担う（整合性テスト用）。
- `ai/engine.py` — 難易度4の自己完結エンジン。FEN パース・合法手生成・評価（マテリアル + PST）・minimax + alpha-beta を実装。盤面インデックス規約は Java と同一（row 0 = ランク8, col 0 = a ファイル、白＝大文字）。
- `ai/test_*.py` — 標準ライブラリ `unittest` のテスト（pip 不要）。`py -m unittest discover -s ai -p "test_*.py"` で実行。`test_engine_perft.py` が move-gen を perft で検証する。
- 設定は `chess.ai.python`（環境変数 `CHESS_AI_PYTHON`）・`chess.ai.script`・`chess.ai.depth`（難易度4の探索深さ、既定3）・`chess.ai.timeout`（秒、既定20）で上書き可能。この環境では `python`/`python3` が Microsoft Store スタブのため、Python ランチャ `py` を優先的に使用する。

**UI層** (2種類の実装。それぞれ `ui` / `board` / `asset` にさらに分割):
- `com.chessgame.javafx` — JavaFX（開発中）: `ui.FXLauncher`（jpackage エントリーポイント）→ `ui.ChessGameApp` → `board.ChessBoardView` → `board.SquareView`。`ui.dialog` に `GameModeDialog` / `PromotionDialog`。`asset.PieceImageLoader` がアセットを管理。`FXLauncher` は `Application` を継承しないことで "JavaFX runtime components missing" チェックを回避する。
- `com.chessgame.swing` — Swing（安定版）: `ui.SwingChessGameFrame` + `board.SwingChessBoardPanel`。`ui.dialog.GameModeDialog`、`ui.panel` に `StatusPanel` / `ControlPanel`。`asset.PieceImageGenerator` が SVG で駒を描画。
- `InteractiveGame.java` — コンソール UI（最も安定したエントリーポイント）。

## コメント規約

### コメントを書かない条件（共通）
過剰なコメントは読み手の負担になるため、以下の場合はコメントを書かない:
- **コードを読めば自明な内容**（変数名・メソッド名・型で意図が分かるもの）
- **処理の「何を」を繰り返すだけのコメント**（`// カウンター変数をインクリメント` など）

### コメントを書くべき場面
- **なぜ（Why）その実装になったか** — 制約・トレードオフ・特定バグの回避策など
- **非自明な前提・不変条件** — 読み手が驚くような挙動の根拠
- **パフォーマンス最適化** — 意図的に複雑にした理由

### Java コメント規約（JavaDoc）

**public / 対外インターフェース（必須）**
```java
/**
 * キングが指定の位置に移動可能かを判定する。
 * @param targetPosition 移動先の座標
 * @return 移動可能な場合 true
 */
public boolean canMoveKing(Position targetPosition) { ... }

/**
 * 指定位置から目標位置への移動が合法手かを検証する。
 * キャスリング・アンパッサン・昇格を含む全ルールをチェック。
 */
public boolean isMoveValid(Position from, Position to) { ... }
```

**private メソッド・フィールド（任意）**
- 処理が自明な private メソッドにはコメント不要
- 非自明なアルゴリズム・副作用がある場合のみ `//` またはインラインコメントを付ける

### Python コメント規約（docstring）

**モジュール・関数・クラス（必須）**
```python
def get_flips(board, row, col, player):
    """指定位置に石を置いた際に反転される相手石の座標リストを返す。"""
    
def evaluate_position(board, player, depth=0):
    """
    盤面を評価し、手番側視点のスコアを返す。
    材料価（マテリアル）と位置の価値（PST）を合算して評価。
    """
```

**インラインコメント（任意）**
- 条件の意味が名前から分からない場合のみ `#` で補足
- アルゴリズムの意図・前提条件・制約を記述

## 主要な設計パターン

- **Observer** — `GameObserver`: ゲーム状態変化を UI（Swing・JavaFX）に通知。ロジックと UI の疎結合を実現。
- **Strategy** — `MoveValidator`: 駒種ごとの移動ルールを分離。拡張性・テスト容易性向上。
- **Factory** — `ChessGame.createTwoPlayerGame()`: ゲーム生成。複雑な初期化を隠蔽。
- **Callback** — UI コンポーネント（StatusPanel, ControlPanel）の動作設定。責任分離を強化。
- **Immutable** — `Position`, `Move`, `Piece`: 状態の一貫性を保証。並行処理・履歴管理を簡潔化。

## 重要な設計上の決定事項

- **駒の移動回数トラッキング**によってキャスリング可否を判定している（専用フラグは持たない）。
- コード全体で `Position` が主要なアドレッシング機構。テストや新しいコードでは生の行・列整数より `Position.of("e2")` 形式を優先すること。
- JavaFX UI (`com.chessgame.javafx`) は開発中のため、安定した動作確認には Swing UI またはコンソールを使用すること。
- オブザーバーパターン (`GameObserver`) によってゲームロジックとすべての UI 層が疎結合になっている。新しい UI 実装はこのインターフェースを実装すること。
- **AI 着手選択は Python サブプロセスに委譲**している。難易度1〜3は「合法手リスト（capture フラグ＋素材価値）⇄ 選択 index」、難易度4は「FEN ⇄ UCI 最善手」という最小限のやり取りで、依存ライブラリは追加していない（JSON は手組み、応答は単一行）。Python 連携が失敗しても Java フォールバックで必ず合法手を返すため、`build.bat`（Maven 不要・Swing のみ）や Python 非導入環境を壊さない。
- **難易度4のエンジンはルールを Python で再実装**しているため、Java ルール層との乖離が最大リスク。正しさは二重で担保する: ① `ai/test_engine_perft.py` の perft（move-gen 単独検証）、② `AiEngineParityTest`（Java の合法手集合と Python の `movegen` 出力の一致）。さらに `AIPlayer` は Python が返した UCI 手を Java の合法手リストに照合してから使うため、不正な手が指されることはない（一致しなければフォールバック）。エンジンの盤面表現を変更したら必ず perft を再実行すること。
- AI ロジックを変更する場合は、難易度1〜3は `ai/chess_ai.py` と `AIPlayer` のフォールバック双方を、難易度4は `ai/engine.py` と `AIPlayer.selectBestMove`（退避先）を意識して一致させること。
