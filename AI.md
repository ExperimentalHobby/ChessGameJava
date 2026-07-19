# AI 仕様

ChessGame の AI 対戦相手（着手選択）の仕様をまとめたドキュメント。

- 対象コード（Java）: [`AIPlayer.java`](src/main/java/com/chessgame/game/player/AIPlayer.java)
- 対象コード（Python）: [`ai/chess_ai.py`](ai/chess_ai.py) / [`ai/engine.py`](ai/engine.py)

---

## 1. 概要

AI の着手選択ロジックは **Python に分離**されている。Java 側の `AIPlayer` は
サブプロセスとして Python を起動し、stdin/stdout 経由で着手を委譲する。

- **難易度1〜3** … 軽量な選択ロジック（[`ai/chess_ai.py`](ai/chess_ai.py)）
- **難易度4** … minimax + alpha-beta + 静止探索 + 反復深化 + 置換表の自己完結エンジン（[`ai/engine.py`](ai/engine.py)）

Python が利用できない・連携に失敗した場合は、**同等の Java 実装に自動フォールバック**
するため、Python 非導入環境でもそのまま動作する（難易度4は難易度3相当に退避）。
依存ライブラリは追加していない（JSON は手組み、応答は単一行テキスト）。

---

## 2. アーキテクチャ / データフロー

```
                         ┌──────────────────────────────┐
   [難易度1〜3]          │  AIPlayer.selectMove(game)   │
   合法手リスト + 価値    │  - 合法手を収集               │
        │ JSON           │  - Python へ委譲              │
        ▼                │  - 失敗時は Java フォールバック │
  ai/chess_ai.py ──index─┤                              │
                         │                              │
   [難易度4]             │                              │
   盤面(FEN)+timeout      │                              │
        │ JSON           │                              │
        ▼                │                              │
  ai/chess_ai.py         │                              │
        │ 委譲           │                              │
        ▼                │                              │
  ai/engine.py ──UCI 手──┘  最善手を Java 合法手に照合 → 採用
    (反復深化+置換表          （照合できなければフォールバック）
     +αβ+静止探索)
```

- Python の起動は 1 手につき 1 回（短命プロセス）。
- 難易度4で Python が返した手は、必ず Java の合法手リストに照合してから採用する。
  万一エンジンが不正手を返しても**盤面には反映されない**（不一致ならフォールバック）。
- AI の着手選択自体は Swing（`SwingWorker`）・JavaFX（`Task<Move>`）で**バックグラウンド
  スレッド実行**する（UIをフリーズさせないため）。選択中に New Game 等でゲームが
  差し替わっていないかは `AIPlayer.isMoveStillApplicable()` で確認してから適用する。

---

## 3. 難易度仕様

| 難易度 | 選択肢ラベル | 実装 | 戦略 |
|:---:|---|---|---|
| 1 | Easy | chess_ai.py | 全合法手から一様ランダム |
| 2 | Medium | chess_ai.py | 駒を取る手を優先（無ければランダム） |
| 3 | Hard | chess_ai.py | 取れる駒の素材価値が最大の手（同値は最初の手） |
| 4 | Expert | engine.py | minimax(negamax) + alpha-beta + 静止探索 + 反復深化 + 置換表（既定深さ3、マテリアル + PST 評価） |

### 詳細な挙動

- **難易度1**: `random` で index を一様選択。
- **難易度2**: capture フラグが立つ手の中からランダム。capture が無ければ全手からランダム。
- **難易度3**: `captureValue` が最大の手を選ぶ。比較は strict `>`（Java 実装と一致）のため、
  最大値が複数あるときは**最初に出現した手**を選ぶ。capture が無い場合は全手の価値が 0 となり
  先頭の手を返す。
- **難易度4**: 後述のエンジンで探索した最善手。

> **駒価値の使い分けに注意**
> - 難易度2・3 の `captureValue` は Java の `PieceType.getMaterialValue()`（P=1, N=3, B=3, R=5, Q=9, K=0）。
> - 難易度4 のエンジン評価はセンチポーン値（P=100, N=320, B=330, R=500, Q=900, K=20000）。

---

## 4. Java ↔ Python プロトコル

Python は stdin から 1 件の JSON を読み、結果を stdout に 1 行で返す。
標準エラー出力は Java 側で破棄する。終了コードが 0 以外・想定外の出力は失敗とみなす。

### 4.1 着手選択（難易度1〜3）

**リクエスト**（`moves` 配列の添字が Java 側の合法手 index に対応）:

```json
{"difficulty": 2,
 "moves": [{"capture": true, "captureValue": 5},
           {"capture": false, "captureValue": 0}]}
```

**レスポンス**: 選択した手の index（合法手が無い場合は `-1`）

```
0
```

### 4.2 着手選択（難易度4・エンジン）

**リクエスト**（`timeout` は省略可。秒単位で、反復深化がその時間予算内で自発的に打ち切る）:

```json
{"difficulty": 4, "depth": 3, "timeout": 20, "fen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"}
```

**レスポンス**: 最善手の UCI 文字列（合法手が無い場合は空行）

```
e7e5
```

### 4.3 合法手列挙（整合性テスト用）

**リクエスト**:

```json
{"command": "movegen", "fen": "<FEN>"}
```

**レスポンス**: 合法手を UCI で空白区切り・ソート済みで列挙

```
a2a3 a2a4 b1a3 b1c3 ...
```

---

## 5. 盤面表現（FEN）

難易度4では `AIPlayer.buildFen()` が現在の盤面を **FEN** に変換して渡す。

### インデックス規約（Java / Python 共通）

```
sq = row * 8 + col
row 0 = ランク8（最上段）, row 7 = ランク1
col 0 = ファイル a,       col 7 = ファイル h
白駒 = 大文字 (KQRBNP), 黒駒 = 小文字 (kqrbnp)
```

この規約は Java の [`Position`](src/main/java/com/chessgame/board/model/Position.java)
（`row 0 = 8 段目`）と完全に一致する。

### FEN 各フィールドの生成元

| FEN フィールド | 生成元 |
|---|---|
| 駒の配置 | `Board.getPieceAt()` を row 0→7 / col 0→7 で走査 |
| 手番 | AI の手番なので `AIPlayer.getColor()`（`w` / `b`） |
| キャスリング権 | `ChessGame.hasCastlingRight()`（キング・ルークの `getMoveCount() == 0` かつ原位置にあるかで導出。専用フラグは持たない） |
| アンパッサン対象 | `ChessGame.getEnPassantTarget()`（無ければ `-`） |
| ハーフムーブ / フルムーブ | 探索に影響しないため `0 1` 固定（後述「引き分け判定」の限界の一因） |

---

## 6. UCI 形式（手の表現）

エンジンが返す手・`movegen` の出力は **UCI 風**の表記:

```
<from><to>[promotion]
例) e2e4      … e2 から e4 へ
    e7e8q     … e7 から e8 へ昇格（クイーン）
```

- 昇格文字: `q`（クイーン）/ `r`（ルーク）/ `b`（ビショップ）/ `n`（ナイト）。
- Java 側は `from`/`to` を `Position.of(...)`、昇格文字を `PieceType` に変換し、
  合法手リストから一致する `Move` を解決する。昇格文字が無い場合はクイーンを既定とする。

---

## 7. Python エンジン（engine.py）

難易度4の中核。FEN を受け取り最善手を返す自己完結のチェスエンジン。

### 7.1 合法手生成

- 駒ごとに擬似合法手を生成（ポーン前進/2マス/斜め取り/昇格、ナイト、スライダー、キング）。
- **キャスリング**: キング・ルークが原位置、間が空、キングの通過マス・着地マスが
  相手に攻撃されていないことを `is_attacked` で確認。
- **アンパッサン**: ep ターゲットへの斜め移動として生成し、取られるポーンを除去。
- **昇格**: 到達時に Q/R/B/N の 4 手を生成（取り・非取りとも）。
- 擬似合法手のうち、**着手後に自王が王手に晒される手を除外**して合法手とする。

### 7.2 評価関数（手番側視点）

```
評価 = (白のマテリアル + 白の PST) - (黒のマテリアル + 黒の PST)
       ※ 手番が黒なら符号反転して返す（negamax 用）
```

- マテリアル: P=100, N=320, B=330, R=500, Q=900, K=20000。
- PST（ピーススクエアテーブル）: Tomasz Michniewski の "Simplified Evaluation Function" に準拠。
  白視点で記述し、黒駒は上下反転（mirror）して参照する。
- 初期局面は左右・上下対称のため評価は `0`。

### 7.3 静止探索（quiescence search）

- `negamax` は探索深さが 0 になると即座に static eval で打ち切らず、`quiescence()` に委譲する。
- 深さ 0 で static eval に打ち切ると、駒の取り合いの途中（水平線）で評価してしまい
  「損な駒交換」を「得」と誤認識する（**水平線効果**）。
- `quiescence()` は stand-pat（現局面の static eval を下限候補にする）を起点に、
  **取り・アンパッサン・昇格（tactical な手）のみ**を延長探索し、取り合いが尽きる
  （静止した）局面まで評価を遅らせる。
- 各再帰は実際に駒を取る手のみを辿るため、盤上の駒数が単調減少し有限回で必ず終端する。

### 7.4 置換表（transposition table）

- 局面のキーは **Zobrist ハッシュ**（`zobrist_hash()`）。盤面配置に加え、手番・キャスリング権・
  アンパッサン対象が異なれば別局面として扱う。
- エントリは `(depth, score, flag, best_move)`。`flag` は alpha-beta の打ち切りにより
  格納時点の値が真の値か上下限かが変わるため、`TT_EXACT` / `TT_LOWERBOUND` / `TT_UPPERBOUND`
  の3種で区別する（標準的な手法）。
- 手順を入れ替えただけの同一局面（トランスポジション）の再探索を避けることで、
  実質的な探索効率を高める。
- 置換表は 1 回の `best_move()` 呼び出し（＝反復深化の全イテレーション）を通じて共有し、
  イテレーションが進むほど再利用が効いて高速化する。

### 7.5 反復深化（iterative deepening）

- `best_move()` は深さ 1 から指定深さまで **1 手ずつ深さを伸ばしながら**探索する。
- 各イテレーションで得た最善手を次イテレーションの手順序付けの先頭に回すことで、
  alpha-beta の枝刈り効率を高める。
- `timeout_seconds` 指定時は時間予算の 90% を内部の締切とし（Java 側の外部 kill より
  先に自発的に打ち切るための安全マージン）、締切を超えたら**直前に完了した深さの
  最善手**を返す。`timeout_seconds` 省略時は無制限に指定深さまで探索する
  （置換表・手順序付けの再利用による高速化のみが目的で、結果は変わらない）。

### 7.6 探索全体（negamax + alpha-beta）

- **詰み**: 合法手が無く王手 → `-MATE + ply`（ply が小さい＝早い詰みほど評価が悪い／良いを区別）。
- **ステールメイト**: 合法手が無く王手でない → `0`（引き分け）。
- **ムーブオーダリング**: 取り（MVV-LVA 風）と昇格、置換表に記録された前回の最善手を優先し、
  alpha-beta の枝刈り効率を上げる。
- ルートで各手を評価し、最大スコアの手を UCI で返す。

---

## 8. フォールバック仕様

Python 連携が次のいずれかで失敗した場合、Java 実装に切り替える:

- スクリプト（`ai/chess_ai.py`）が存在しない
- Python コマンドが起動できない（未インストール、Microsoft Store スタブ等）
- タイムアウト超過、終了コードが 0 以外
- 出力が解釈できない、または難易度4で返り手が合法手リストに無い

| 難易度 | フォールバック先（Java） |
|:---:|---|
| 1 | 全合法手からランダム |
| 2 | `selectMoveWithPreference`（駒取り優先） |
| 3 | `selectBestMove`（1手読み最善） |
| 4 | `selectBestMove`（難易度3相当に退避） |

---

## 9. 設定（システムプロパティ / 環境変数）

| キー | 既定値 | 用途 |
|---|---|---|
| `chess.ai.python` / 環境変数 `CHESS_AI_PYTHON` | `py` → `python3` → `python` を順に試行 | Python 実行コマンド |
| `chess.ai.script` | `ai/chess_ai.py` | AI スクリプトのパス |
| `chess.ai.depth` | `3` | 難易度4の探索深さ（1〜10 に丸め） |
| `chess.ai.timeout` | `20` | 難易度4の実行タイムアウト秒（1〜600 に丸め、反復深化の内部締切にも使われる） |

> この環境では `python` / `python3` が Microsoft Store スタブのため、Python ランチャ
> `py` を優先的に使用する。

設定例（深さ4・タイムアウト60秒で起動）:

```bat
java -Dchess.ai.depth=4 -Dchess.ai.timeout=60 -cp target\classes com.chessgame.Main
```

---

## 10. 正しさの担保

ルールを Python で再実装しているため、**Java ルール層との乖離が最大のリスク**。
次の三重で正しさを担保している。

1. **perft**（[`ai/test_engine_perft.py`](ai/test_engine_perft.py)）
   標準局面の既知のノード数と一致するかを検証し、キャスリング・アンパッサン・昇格・
   ピン・王手回避を含む move-gen の正しさを単独で確認する。
2. **整合性テスト**（[`AiEngineParityTest`](src/test/java/com/chessgame/game/core/AiEngineParityTest.java)）
   ランダム対局を進めながら各局面を FEN 化し、Java の合法手集合と Python の `movegen`
   出力（UCI）が完全一致することを確認する。FEN 生成の正しさも併せて担保する。
3. **Java 側の安全網**
   `AIPlayer` はエンジンが返した UCI 手を Java の合法手リストに照合してから使うため、
   不正な手が指されることはない（一致しなければフォールバック）。

---

## 11. 引き分け判定について

- **エンジン内部（Python）**: ステールメイトのみ `0`（引き分け）として評価する。
  50手ルール・3回同形は FEN 単体からは判定できず、エンジンは局面履歴を持たないため、
  探索・評価には反映されない（FEN のハーフムーブも探索に影響しないため固定値 `0` を渡している）。
- **対局全体（Java）**: 50手ルール・3回同形・戦力不足による引き分けは
  [`DrawDetector`](src/main/java/com/chessgame/detection/rules/DrawDetector.java)
  が対局進行全体を通じて判定しており、AI の手番かどうかに関わらず正しく機能する。
  つまり「AI がその場の一手を選ぶ際の評価」には反映されないが、「ゲームとして
  引き分けが成立したかどうか」は別レイヤーで正しく判定されている。

---

## 12. ファイル構成

```
ai/
├── chess_ai.py          # stdin の JSON を読み難易度別に処理（1〜3 は index、4 は engine 委譲、movegen）
├── engine.py            # 難易度4: FEN パース・合法手生成・評価(PST)・反復深化+置換表+αβ+静止探索
├── test_chess_ai.py     # 難易度1〜3 の選択ロジックのテスト
├── test_engine.py       # 評価・探索（詰み/ただ取り/αβ一致）のテスト
└── test_engine_perft.py # move-gen の perft 検証テスト

src/main/java/com/chessgame/game/
├── core/ChessGame.java          # getEnPassantTarget() / hasCastlingRight()（FEN 生成用）
└── player/AIPlayer.java         # Java 側エントリ。FEN 生成・UCI 解決・フォールバック

src/test/java/com/chessgame/game/
├── core/AiEngineParityTest.java  # Java↔Python 合法手一致の検証
└── player/AIPlayerTest.java      # 難易度1〜4・フォールバックの検証
```

---

## 13. テスト実行

```bat
REM Java（JUnit）
mvnw.cmd test

REM Python（標準ライブラリ unittest、pip 不要）
py -m unittest discover -s ai -p "test_*.py" -v
```

---

## 14. 既知の制限・今後の拡張

- 評価はマテリアル + PST のみ（モビリティ・キング安全度・ポーン構造などは未考慮）。
- 50手ルール・3回同形の引き分け判定はエンジン評価に未反映（詳細は「11. 引き分け判定について」）。
- 置換表はプロセス内メモリのみで永続化しない（1手ごとに短命プロセスとして起動するため、
  手をまたいだ再利用はできない）。
