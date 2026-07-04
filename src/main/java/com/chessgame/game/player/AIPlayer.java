package com.chessgame.game.player;

import com.chessgame.model.Color;
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;
import com.chessgame.game.core.ChessGame;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * AIプレイヤーの実装クラス。難易度に応じた手選択ロジックを提供する。
 * 難易度1: ランダム選択、難易度2: 駒取り優先、難易度3: 素材価値最大化、
 * 難易度4: minimax + alpha-beta（Python エンジン）。
 *
 * <p>着手選択は Python スクリプト（{@code ai/chess_ai.py}）へサブプロセス経由で
 * 委譲する。難易度1〜3は合法手リストから index を選ばせ、難易度4は盤面を FEN で
 * 渡して minimax 探索（{@code ai/engine.py}）に最善手を求めさせる。Python が利用
 * できない場合や連携に失敗した場合は Java 実装にフォールバックするため、Python
 * ランタイムが無い環境でも動作する（難易度4は難易度3相当に退避）。</p>
 *
 * <p>連携先・探索は次のシステムプロパティ／環境変数で上書きできる:</p>
 * <ul>
 *   <li>{@code chess.ai.python} / 環境変数 {@code CHESS_AI_PYTHON} — Python コマンド</li>
 *   <li>{@code chess.ai.script} — AI スクリプトのパス（既定 {@code ai/chess_ai.py}）</li>
 *   <li>{@code chess.ai.depth} — 難易度4の探索深さ（既定 3）</li>
 *   <li>{@code chess.ai.timeout} — 難易度4の実行タイムアウト秒（既定 20）</li>
 * </ul>
 */
public class AIPlayer extends Player {
    private static final Random random = new Random();

    /** AI スクリプトの既定パス（作業ディレクトリ基準）。 */
    private static final String DEFAULT_SCRIPT = "ai/chess_ai.py";
    /** Python コマンドの既定候補。先頭から順に試行する。 */
    private static final List<String> DEFAULT_PYTHON_COMMANDS = List.of("py", "python3", "python");
    /** 難易度1〜3の Python プロセス実行タイムアウト（秒）。 */
    private static final long SELECT_TIMEOUT_SECONDS = 5;

    private final int difficulty;

    /**
     * AIプレイヤーを生成する。
     *
     * @param name       プレイヤー名
     * @param color      担当する色
     * @param difficulty 難易度（1=ランダム、2=駒取り優先、3=最善手優先、4=minimax）
     */
    public AIPlayer(String name, com.chessgame.model.Color color, int difficulty) {
        super(color, name, false);
        this.difficulty = difficulty;
    }

    /**
     * 非同期（SwingWorker / JavaFX Task）で選択した手を、UI スレッド側で安全に
     * 適用できるかを判定する。キャンセル済み・手なし・選択時から {@link ChessGame}
     * インスタンスが変わった（New Game）・ゲームが既に終了しているのいずれかに
     * 該当すれば適用不可（false）とする。
     *
     * @param move                非同期に選択された手（null 可）
     * @param gameAtSelectionTime 選択を開始した時点の {@link ChessGame}
     * @param currentGame         適用しようとしている時点の現在の {@link ChessGame}
     * @param cancelled           非同期タスクがキャンセルされていたか
     * @return 適用してよい場合 true
     */
    public static boolean isMoveStillApplicable(Move move, ChessGame gameAtSelectionTime,
                                                 ChessGame currentGame, boolean cancelled) {
        return !cancelled && move != null && gameAtSelectionTime == currentGame
            && !currentGame.isGameOver();
    }

    /**
     * 現在のゲーム状態から難易度に応じた手を選んで返す。
     * 合法手が存在しない場合は null を返す。
     *
     * <p>まず Python スクリプトへ委譲し、失敗時は Java 実装にフォールバックする。</p>
     *
     * @param game 現在のゲーム
     * @return 選択した {@link Move}、または null
     */
    public Move selectMove(ChessGame game) {
        List<Move> availableMoves = collectAllAvailableMoves(game);

        if (availableMoves.isEmpty()) {
            return null;
        }

        if (difficulty == 4) {
            Move engineMove = trySelectWithEngine(game, availableMoves);
            if (engineMove != null) {
                return engineMove;
            }
            // Python エンジンが使えない場合は難易度3相当（1手読み最善）に退避する
            return selectBestMove(availableMoves);
        }

        Integer pythonIndex = trySelectWithPython(availableMoves);
        if (pythonIndex != null) {
            return availableMoves.get(pythonIndex);
        }

        return selectMoveWithJava(availableMoves);
    }

    /**
     * 現在の盤面で AI の色の全駒が指せる合法手をまとめて返す。
     *
     * @param game 現在のゲーム
     * @return 全合法手のリスト
     */
    private List<Move> collectAllAvailableMoves(ChessGame game) {
        List<Move> allMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = Position.of(row, col);
                Piece piece = game.getBoard().getPieceAt(pos);
                if (piece != null && piece.getColor() == getColor()) {
                    allMoves.addAll(game.getAvailableMoves(pos));
                }
            }
        }
        return allMoves;
    }

    // ----------------------------------------------------------------------
    // 難易度1〜3: Python 連携（手リスト → index）
    // ----------------------------------------------------------------------

    /**
     * Python スクリプトに着手選択を委譲し、選ばれた手の index を返す。
     * スクリプトが存在しない・起動に失敗した・不正な出力だった場合は null を返し、
     * 呼び出し側で Java フォールバックに切り替える。
     *
     * @param moves 合法手のリスト（空でないこと）
     * @return 選択された手の index、失敗時は null
     */
    private Integer trySelectWithPython(List<Move> moves) {
        String scriptPath = aiScriptPath();
        if (!new File(scriptPath).isFile()) {
            return null;
        }

        String requestJson = buildMovesRequestJson(moves);
        for (String pythonCommand : pythonCommands()) {
            String output = runPython(pythonCommand, scriptPath, requestJson, SELECT_TIMEOUT_SECONDS);
            if (output != null) {
                try {
                    int index = Integer.parseInt(output.trim());
                    if (index >= 0 && index < moves.size()) {
                        return index;
                    }
                } catch (NumberFormatException ignored) {
                    // 不正な出力：次の候補（あれば）へ
                }
            }
        }
        return null;
    }

    /**
     * Python に渡すリクエスト JSON（難易度1〜3用）を組み立てる。
     * 各手は capture フラグと取れる駒の素材価値のみを持ち、配列の添字が
     * {@code moves} の index に対応する。
     *
     * @param moves 合法手のリスト
     * @return 1 行の JSON 文字列
     */
    private String buildMovesRequestJson(List<Move> moves) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"difficulty\":").append(difficulty).append(",\"moves\":[");
        for (int i = 0; i < moves.size(); i++) {
            Piece captured = moves.get(i).getCapturedPiece();
            boolean isCapture = captured != null;
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"capture\":").append(isCapture)
              .append(",\"captureValue\":").append(getPieceValue(captured)).append('}');
        }
        sb.append("]}");
        return sb.toString();
    }

    // ----------------------------------------------------------------------
    // 難易度4: Python minimax エンジン（FEN → UCI 最善手）
    // ----------------------------------------------------------------------

    /**
     * 盤面を FEN で Python エンジンに渡し、返ってきた UCI 最善手を {@link Move} に解決する。
     * スクリプト不在・起動失敗・不正な出力・合法手に一致しない場合は null を返す。
     *
     * @param game  現在のゲーム
     * @param moves 合法手のリスト（UCI からの解決に使用）
     * @return 解決した {@link Move}、失敗時は null
     */
    private Move trySelectWithEngine(ChessGame game, List<Move> moves) {
        String scriptPath = aiScriptPath();
        if (!new File(scriptPath).isFile()) {
            return null;
        }

        String requestJson = "{\"difficulty\":4,\"depth\":" + engineDepth()
            + ",\"fen\":\"" + buildFen(game) + "\"}";
        long timeout = engineTimeoutSeconds();
        for (String pythonCommand : pythonCommands()) {
            String output = runPython(pythonCommand, scriptPath, requestJson, timeout);
            if (output != null && !output.isBlank()) {
                Move move = resolveUciMove(output.trim(), moves);
                if (move != null) {
                    return move;
                }
            }
        }
        return null;
    }

    /**
     * 現在の盤面状態を FEN 文字列に変換する。
     * 手番は AI の色、キャスリング権はキング・ルークの移動回数から導出する。
     *
     * <p>整合性テスト（{@code AiEngineParityTest}）から検証するためパッケージプライベート。</p>
     *
     * @param game 現在のゲーム
     * @return FEN 文字列
     */
    public String buildFen(ChessGame game) {
        Board board = game.getBoard();
        StringBuilder fen = new StringBuilder();

        // 1. 駒の配置（row 0 = ランク8 から row 7 = ランク1 へ）
        for (int row = 0; row < 8; row++) {
            int emptyRun = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(Position.of(row, col));
                if (piece == null) {
                    emptyRun++;
                } else {
                    if (emptyRun > 0) {
                        fen.append(emptyRun);
                        emptyRun = 0;
                    }
                    char notation = piece.getType().getNotation();
                    fen.append(piece.getColor() == Color.WHITE
                        ? Character.toUpperCase(notation) : Character.toLowerCase(notation));
                }
            }
            if (emptyRun > 0) {
                fen.append(emptyRun);
            }
            if (row < 7) {
                fen.append('/');
            }
        }

        // 2. 手番（AI の手番なので AI の色）
        fen.append(' ').append(getColor() == Color.WHITE ? 'w' : 'b');
        // 3. キャスリング権
        fen.append(' ').append(buildCastlingField(board));
        // 4. アンパッサン対象
        Position enPassant = game.getEnPassantTarget();
        fen.append(' ').append(enPassant != null ? enPassant.toAlgebraic() : "-");
        // 5. ハーフムーブ / 6. フルムーブ（探索に影響しないため固定）
        fen.append(" 0 1");

        return fen.toString();
    }

    /**
     * キャスリング権フィールド（例 "KQkq"）を構築する。
     * キング・ルークの移動回数が 0 で原位置にある場合に権利ありとみなす。
     *
     * @param board 現在の盤面
     * @return キャスリング権文字列（権利が無ければ "-"）
     */
    private String buildCastlingField(Board board) {
        StringBuilder sb = new StringBuilder();
        if (hasCastlingRight(board, Position.of(7, 4), Position.of(7, 7), Color.WHITE)) sb.append('K');
        if (hasCastlingRight(board, Position.of(7, 4), Position.of(7, 0), Color.WHITE)) sb.append('Q');
        if (hasCastlingRight(board, Position.of(0, 4), Position.of(0, 7), Color.BLACK)) sb.append('k');
        if (hasCastlingRight(board, Position.of(0, 4), Position.of(0, 0), Color.BLACK)) sb.append('q');
        return sb.length() == 0 ? "-" : sb.toString();
    }

    /**
     * 指定したキング・ルークが未移動で原位置にあるか（キャスリング権があるか）を返す。
     *
     * @param board    現在の盤面
     * @param kingSq   キングの原位置
     * @param rookSq   ルークの原位置
     * @param color    対象の色
     * @return キャスリング権があれば true
     */
    private boolean hasCastlingRight(Board board, Position kingSq, Position rookSq, Color color) {
        Piece king = board.getPieceAt(kingSq);
        Piece rook = board.getPieceAt(rookSq);
        return king != null && king.getType() == PieceType.KING
            && king.getColor() == color && king.getMoveCount() == 0
            && rook != null && rook.getType() == PieceType.ROOK
            && rook.getColor() == color && rook.getMoveCount() == 0;
    }

    /**
     * UCI 文字列（例 "e2e4" / "e7e8q"）を合法手リスト中の {@link Move} に解決する。
     * 一致する手が無ければ null（→ フォールバック）。
     *
     * @param uci   エンジンが返した UCI 文字列
     * @param moves 合法手のリスト
     * @return 一致する {@link Move}、または null
     */
    private Move resolveUciMove(String uci, List<Move> moves) {
        if (uci.length() < 4) {
            return null;
        }
        Position from;
        Position to;
        try {
            from = Position.of(uci.substring(0, 2));
            to = Position.of(uci.substring(2, 4));
        } catch (IllegalArgumentException e) {
            return null;
        }
        PieceType promotion = uci.length() >= 5 ? charToPromotion(uci.charAt(4)) : null;

        Move promotionFallback = null;
        for (Move move : moves) {
            if (!move.getFrom().equals(from) || !move.getTo().equals(to)) {
                continue;
            }
            if (!move.isPromotion()) {
                return move;
            }
            PieceType wanted = (promotion != null) ? promotion : PieceType.QUEEN;
            if (move.getPromotionPiece() == wanted) {
                return move;
            }
            if (promotionFallback == null) {
                promotionFallback = move;
            }
        }
        return promotionFallback;
    }

    /**
     * UCI の昇格文字（q/r/b/n）を {@link PieceType} に変換する。不明な場合は null。
     */
    private PieceType charToPromotion(char c) {
        switch (Character.toLowerCase(c)) {
            case 'q': return PieceType.QUEEN;
            case 'r': return PieceType.ROOK;
            case 'b': return PieceType.BISHOP;
            case 'n': return PieceType.KNIGHT;
            default:  return null;
        }
    }

    // ----------------------------------------------------------------------
    // Python プロセス実行・設定の共通処理
    // ----------------------------------------------------------------------

    /**
     * Python スクリプトを実行し、標準出力の1行目を返す。
     * 起動失敗・タイムアウト・非ゼロ終了の場合は null を返す。
     *
     * @param pythonCommand  Python 実行コマンド
     * @param scriptPath     スクリプトのパス
     * @param requestJson    stdin に渡す JSON
     * @param timeoutSeconds 実行タイムアウト（秒）
     * @return 標準出力の1行目、失敗時は null
     */
    private String runPython(String pythonCommand, String scriptPath, String requestJson, long timeoutSeconds) {
        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(pythonCommand, scriptPath);
            // stderr（トレースバック等）は読まずに破棄し、パイプ詰まりを防ぐ
            builder.redirectError(ProcessBuilder.Redirect.DISCARD);
            process = builder.start();

            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(requestJson.getBytes(StandardCharsets.UTF_8));
            }

            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                output = reader.readLine();
            }

            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return null;
            }
            if (process.exitValue() != 0) {
                return null;
            }
            return output;
        } catch (Exception e) {
            // Python 未インストール・スタブ・不正出力など：黙ってフォールバックする
            if (process != null) {
                process.destroyForcibly();
            }
            return null;
        }
    }

    /**
     * 試行する Python コマンドの候補リストを返す。
     * {@code chess.ai.python} / {@code CHESS_AI_PYTHON} が指定されていればそれを優先する。
     *
     * @return Python コマンド候補
     */
    private List<String> pythonCommands() {
        String override = System.getProperty("chess.ai.python");
        if (override == null || override.isBlank()) {
            override = System.getenv("CHESS_AI_PYTHON");
        }
        if (override != null && !override.isBlank()) {
            return List.of(override);
        }
        return DEFAULT_PYTHON_COMMANDS;
    }

    /**
     * AI スクリプトのパスを返す（{@code chess.ai.script} で上書き可能）。
     *
     * @return スクリプトのパス
     */
    private String aiScriptPath() {
        return System.getProperty("chess.ai.script", DEFAULT_SCRIPT);
    }

    /**
     * 難易度4の探索深さを返す（{@code chess.ai.depth}、既定3、範囲1〜10）。
     */
    private int engineDepth() {
        return parseBoundedIntProperty("chess.ai.depth", 3, 1, 10);
    }

    /**
     * 難易度4の実行タイムアウト秒を返す（{@code chess.ai.timeout}、既定20、範囲1〜600）。
     */
    private long engineTimeoutSeconds() {
        return parseBoundedIntProperty("chess.ai.timeout", 20, 1, 600);
    }

    /**
     * 整数システムプロパティを範囲制限付きで読み取る。未設定・不正値は既定値を返す。
     */
    private int parseBoundedIntProperty(String key, int defaultValue, int min, int max) {
        try {
            int value = Integer.parseInt(System.getProperty(key, Integer.toString(defaultValue)).trim());
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ----------------------------------------------------------------------
    // Java フォールバック（難易度1〜3、Python と同一ロジック）
    // ----------------------------------------------------------------------

    /**
     * Python 連携が使えない場合のフォールバック。難易度に応じて Java 側で手を選ぶ。
     *
     * @param availableMoves 選択候補の合法手リスト
     * @return 選択した手
     */
    private Move selectMoveWithJava(List<Move> availableMoves) {
        // difficulty 1（default）: ランダム選択
        // difficulty 2: 駒取り優先
        // difficulty 3: 素材価値が最大になる手を選択
        switch (difficulty) {
            case 2:
                return selectMoveWithPreference(availableMoves);
            case 3:
                return selectBestMove(availableMoves);
            default:
                return availableMoves.get(random.nextInt(availableMoves.size()));
        }
    }

    /**
     * 難易度2用。駒を取る手を優先して選択し、なければランダムに選ぶ。
     *
     * @param availableMoves 選択候補の合法手リスト
     * @return 選択した手
     */
    private Move selectMoveWithPreference(List<Move> availableMoves) {
        List<Move> captures = availableMoves.stream()
            .filter(m -> m.getCapturedPiece() != null)
            .toList();

        if (!captures.isEmpty()) {
            return captures.get(random.nextInt(captures.size()));
        }
        return availableMoves.get(random.nextInt(availableMoves.size()));
    }

    /**
     * 難易度3用。取れる駒の素材価値が最大になる手を選ぶ。
     *
     * @param availableMoves 選択候補の合法手リスト
     * @return 選択した手
     */
    private Move selectBestMove(List<Move> availableMoves) {
        Move bestMove = availableMoves.get(0);
        int bestScore = Integer.MIN_VALUE;

        for (Move move : availableMoves) {
            int score = getPieceValue(move.getCapturedPiece());
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    /**
     * 指定した駒の素材価値を返す。null の場合は 0。
     * 価値は {@link com.chessgame.piece.model.PieceType#getMaterialValue()} に集約されている。
     *
     * @param piece 価値を調べる駒（null 可）
     * @return 素材価値（駒がなければ 0）
     */
    private int getPieceValue(Piece piece) {
        if (piece == null) return 0;
        return piece.getType().getMaterialValue();
    }
}
