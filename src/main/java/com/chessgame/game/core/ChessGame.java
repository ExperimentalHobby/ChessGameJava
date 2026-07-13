package com.chessgame.game.core;

import com.chessgame.model.Color;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;
import com.chessgame.piece.rules.CheckDetector;
import com.chessgame.detection.rules.CheckmateDetector;
import com.chessgame.detection.rules.DrawDetector;
import com.chessgame.rules.MoveValidator;
import com.chessgame.game.player.Player;
import com.chessgame.game.observer.GameObserver;
import com.chessgame.notation.rules.FenCodec;
import com.chessgame.notation.rules.SanCodec;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * チェスゲームの主要コントローラークラス。
 * 移動の実行・合法手の取得・手戻り・投了・ゲーム状態管理を担う。
 * {@code ChessGame.createTwoPlayerGame(name1, name2)} で生成し、
 * {@link GameObserver} を登録することでUIと疎結合に連携できる。
 */
public class ChessGame {
    private final GameState gameState;
    private final Player whitePlayer;
    private final Player blackPlayer;
    private final MoveValidator moveValidator;
    private final CheckDetector checkDetector;
    private final CheckmateDetector checkmateDetector;
    private final DrawDetector drawDetector;
    private final List<GameObserver> observers;
    /** {@link #fromFen} で開始した場合の元 FEN。標準開始局面なら null（PGN 出力で使用）。 */
    private String startingFen;

    /**
     * 指定したプレイヤーでゲームを生成する。
     *
     * @param whitePlayer 白プレイヤー
     * @param blackPlayer 黒プレイヤー
     */
    public ChessGame(Player whitePlayer, Player blackPlayer) {
        this.gameState = new GameState();
        this.whitePlayer = Objects.requireNonNull(whitePlayer);
        this.blackPlayer = Objects.requireNonNull(blackPlayer);
        this.moveValidator = new MoveValidator();
        this.checkDetector = new CheckDetector();
        this.checkmateDetector = new CheckmateDetector();
        this.drawDetector = new DrawDetector();
        this.observers = new ArrayList<>();
    }

    /**
     * 2人対戦用ゲームを生成するファクトリメソッド。
     *
     * @param whiteName 白プレイヤーの名前
     * @param blackName 黒プレイヤーの名前
     * @return 新しい {@link ChessGame}
     */
    public static ChessGame createTwoPlayerGame(String whiteName, String blackName) {
        return new ChessGame(
            Player.human(Color.WHITE, whiteName),
            Player.human(Color.BLACK, blackName)
        );
    }

    /**
     * 現在の対局状態を FEN 文字列として返す。
     *
     * @return FEN 文字列
     */
    public String toFen() {
        return FenCodec.encode(
            gameState.getBoard(),
            gameState.getCurrentPlayerColor(),
            hasCastlingRight(Color.WHITE, true),
            hasCastlingRight(Color.WHITE, false),
            hasCastlingRight(Color.BLACK, true),
            hasCastlingRight(Color.BLACK, false),
            gameState.getEnPassantTarget(),
            gameState.getHalfmoveClock(),
            gameState.getFullmoveNumber()
        );
    }

    /**
     * FEN 文字列から対局を開始する。手の履歴は空（読み込み時点より前の手は存在しない）。
     * <p><b>注意:</b> 生成したゲームに対して {@link #startNewGame()} を呼ぶと標準初期配置に
     * 上書きされてしまうため呼ばないこと。</p>
     *
     * @param fen         読み込む FEN 文字列
     * @param whitePlayer 白プレイヤー
     * @param blackPlayer 黒プレイヤー
     * @return FEN の局面から開始する新しい {@link ChessGame}
     */
    public static ChessGame fromFen(String fen, Player whitePlayer, Player blackPlayer) {
        ChessGame game = new ChessGame(whitePlayer, blackPlayer);
        game.startingFen = fen;
        FenCodec.ParsedFen parsed = game.resetToStartingPosition();

        int halfmoveOffset = 2 * (parsed.fullmove() - 1) + (parsed.sideToMove() == Color.BLACK ? 1 : 0);
        game.gameState.setHalfmoveOffsetAtLoad(halfmoveOffset);
        game.gameState.recordPosition(game.computePositionKey(parsed.sideToMove()));

        return game;
    }

    /**
     * 対局の開始局面（盤面・手番・アンパッサン対象・ハーフムーブクロック・キャスリング権）を
     * {@link #gameState} に復元する。{@link #startingFen} があればその FEN から、
     * 無ければ標準初期配置から復元する。{@link #fromFen} と {@link #undo()} の両方で使う
     * ことで、undo が FEN 由来の局面を無視して標準初期配置に戻ってしまう不整合を防ぐ。
     *
     * @return startingFen をパースした結果（標準初期配置の場合は null）
     */
    private FenCodec.ParsedFen resetToStartingPosition() {
        if (startingFen == null) {
            gameState.setBoard(new Board());
            gameState.setCurrentPlayerColor(Color.WHITE);
            gameState.setEnPassantTarget(null);
            gameState.setHalfmoveClock(0);
            return null;
        }

        FenCodec.ParsedFen parsed = FenCodec.parse(startingFen);
        gameState.setBoard(parsed.board());
        gameState.setCurrentPlayerColor(parsed.sideToMove());
        gameState.setEnPassantTarget(parsed.enPassant());
        gameState.setHalfmoveClock(parsed.halfmove());

        // キャスリング権は移動回数で判定するため、FEN が否定している側のルークの
        // moveCount を進めて権利を抑制する（同一原位置に居ても指せなくする）。
        suppressCastlingRightIfDenied(parsed.board(), Color.WHITE, true, parsed.whiteKingside());
        suppressCastlingRightIfDenied(parsed.board(), Color.WHITE, false, parsed.whiteQueenside());
        suppressCastlingRightIfDenied(parsed.board(), Color.BLACK, true, parsed.blackKingside());
        suppressCastlingRightIfDenied(parsed.board(), Color.BLACK, false, parsed.blackQueenside());

        return parsed;
    }

    /**
     * これまでの対局を PGN（コメント・変化手・NAG 等の拡張構文は非対応）として返す。
     * 標準開始局面でない場合（{@link #fromFen} 由来）は {@code [FEN]}/{@code [SetUp]} タグを付ける。
     *
     * @return PGN 文字列
     */
    public String toPgn() {
        ChessGame replay = (startingFen != null)
            ? ChessGame.fromFen(startingFen, whitePlayer, blackPlayer)
            : new ChessGame(whitePlayer, blackPlayer);

        StringBuilder movetext = new StringBuilder();
        List<Move> history = gameState.getMoveHistory().getAll();
        for (int i = 0; i < history.size(); i++) {
            Move recorded = history.get(i);
            Color mover = replay.gameState.getCurrentPlayerColor();
            int fullmoveNumber = replay.getFullmoveNumber();
            List<Move> legalMoves = replay.getAllAvailableMoves();
            // makeMove は盤面を直接書き換えるため、SAN 生成のためにスナップショットを取っておく
            Board boardBeforeMove = replay.getBoard().clone();

            boolean applied = replay.makeMove(recorded.getFrom(), recorded.getTo(), recorded.getPromotionPiece());
            if (!applied) {
                throw new IllegalStateException("記録済みの手を再生できません: " + recorded);
            }

            GameState.GameStatus status = replay.getGameStatus();
            boolean isCheck = status == GameState.GameStatus.CHECK;
            boolean isCheckmate = status == GameState.GameStatus.CHECKMATE;
            String san = SanCodec.encode(boardBeforeMove, recorded, legalMoves, isCheck, isCheckmate);

            if (mover == Color.WHITE) {
                movetext.append(fullmoveNumber).append(". ").append(san).append(' ');
            } else if (i == 0) {
                // 黒番から始まる対局（FEN 読み込みで黒番スタート）
                movetext.append(fullmoveNumber).append("... ").append(san).append(' ');
            } else {
                movetext.append(san).append(' ');
            }
        }

        String result = resultTag();
        StringBuilder pgn = new StringBuilder();
        pgn.append("[Event \"Casual Game\"]\n");
        pgn.append("[Site \"?\"]\n");
        pgn.append("[Date \"????.??.??\"]\n");
        pgn.append("[Round \"?\"]\n");
        pgn.append("[White \"").append(whitePlayer.getName()).append("\"]\n");
        pgn.append("[Black \"").append(blackPlayer.getName()).append("\"]\n");
        pgn.append("[Result \"").append(result).append("\"]\n");
        if (startingFen != null) {
            pgn.append("[FEN \"").append(startingFen).append("\"]\n");
            pgn.append("[SetUp \"1\"]\n");
        }
        pgn.append('\n');
        pgn.append(movetext);
        pgn.append(result);

        return pgn.toString();
    }

    /**
     * 対局の勝敗を表す PGN の結果タグ（{@code 1-0}/{@code 0-1}/{@code 1/2-1/2}/{@code *}）を返す。
     */
    private String resultTag() {
        if (!gameState.isGameOver()) {
            return "*";
        }
        GameState.GameStatus status = gameState.getGameStatus();
        if (status == GameState.GameStatus.CHECKMATE) {
            // 詰みは「王手された側（現在の手番）」の負け
            return gameState.getCurrentPlayerColor() == Color.WHITE ? "0-1" : "1-0";
        }
        if (status == GameState.GameStatus.WHITE_RESIGNED) {
            return "0-1";
        }
        if (status == GameState.GameStatus.BLACK_RESIGNED) {
            return "1-0";
        }
        // ステールメイト・50手ルール・千日手・戦力不足はいずれも引き分け
        return "1/2-1/2";
    }

    private static final Pattern PGN_TAG_PATTERN = Pattern.compile("\\[(\\w+)\\s+\"([^\"]*)\"\\]");
    private static final Pattern MOVE_NUMBER_TOKEN_PATTERN = Pattern.compile("^\\d+\\.+$");
    private static final Pattern NAG_TOKEN_PATTERN = Pattern.compile("^\\$\\d+$");
    private static final Set<String> PGN_RESULT_TOKENS = Set.of("1-0", "0-1", "1/2-1/2", "*");

    /**
     * PGN 文字列から対局を再生する。ヘッダタグに {@code FEN} があればその局面から、
     * 無ければ標準開始局面から再生する。
     * <p>対応範囲: 標準的な手番号・SAN のみ。コメント {@code {...}}・変化手 {@code (...)}・
     * NAG（{@code $n}）は非対応（本メソッドは無視して読み飛ばす）。</p>
     *
     * @param pgn         読み込む PGN 文字列
     * @param whitePlayer 白プレイヤー
     * @param blackPlayer 黒プレイヤー
     * @return PGN の対局を再生した新しい {@link ChessGame}
     */
    public static ChessGame fromPgn(String pgn, Player whitePlayer, Player blackPlayer) {
        String fenTag = extractPgnTag(pgn, "FEN");
        ChessGame game = (fenTag != null)
            ? ChessGame.fromFen(fenTag, whitePlayer, blackPlayer)
            : new ChessGame(whitePlayer, blackPlayer);

        String movetext = stripPgnCommentsAndVariations(PGN_TAG_PATTERN.matcher(pgn).replaceAll("").trim());
        for (String rawToken : movetext.split("\\s+")) {
            if (rawToken.isEmpty() || PGN_RESULT_TOKENS.contains(rawToken)
                    || NAG_TOKEN_PATTERN.matcher(rawToken).matches()) {
                continue;
            }
            // "1." や "5..." のような手番号トークン、"1.e4" のように SAN に手番号が
            // 直結しているトークンの両方に対応する
            String sanToken = rawToken.replaceFirst("^\\d+\\.+", "");
            if (sanToken.isEmpty() || MOVE_NUMBER_TOKEN_PATTERN.matcher(sanToken).matches()) {
                continue;
            }

            List<Move> legalMoves = game.getAllAvailableMoves();
            Move move = SanCodec.decode(sanToken, game.getBoard(), legalMoves);
            if (move == null) {
                throw new IllegalArgumentException("PGN内の手を解決できません: " + sanToken);
            }
            game.makeMove(move);
        }

        return game;
    }

    /**
     * movetext からコメント {@code {...}}（ネストなし）と変化手 {@code (...)}（ネスト対応）を取り除く。
     */
    private static String stripPgnCommentsAndVariations(String movetext) {
        StringBuilder result = new StringBuilder();
        int variationDepth = 0;
        boolean inComment = false;
        for (int i = 0; i < movetext.length(); i++) {
            char c = movetext.charAt(i);
            if (inComment) {
                if (c == '}') {
                    inComment = false;
                }
                continue;
            }
            if (c == '{') {
                inComment = true;
                continue;
            }
            if (c == '(') {
                variationDepth++;
                continue;
            }
            if (c == ')') {
                if (variationDepth > 0) {
                    variationDepth--;
                }
                continue;
            }
            if (variationDepth > 0) {
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }

    /**
     * PGN のヘッダタグ（例 {@code [FEN "..."]})）から指定した名前の値を取り出す。
     * 見つからなければ null。
     */
    private static String extractPgnTag(String pgn, String tagName) {
        Matcher matcher = PGN_TAG_PATTERN.matcher(pgn);
        while (matcher.find()) {
            if (matcher.group(1).equals(tagName)) {
                return matcher.group(2);
            }
        }
        return null;
    }

    /**
     * FEN がキャスリング権を否定している場合、該当ルークの移動回数を進めて権利を抑制する。
     * 新規生成した駒は moveCount=0 のため、何もしなければ原位置にあるだけで権利ありと
     * 判定されてしまうことへの対処。
     */
    private static void suppressCastlingRightIfDenied(Board board, Color color, boolean kingside, boolean granted) {
        if (granted) {
            return;
        }
        int row = (color == Color.WHITE) ? 7 : 0;
        Position rookSquare = Position.of(row, kingside ? 7 : 0);
        Piece rook = board.getPieceAt(rookSquare);
        if (rook != null && rook.getType() == PieceType.ROOK && rook.getMoveCount() == 0) {
            rook.incrementMoveCount();
        }
    }

    /**
     * ゲームを初期状態から開始する。盤面・履歴をリセットしてオブザーバーに通知する。
     */
    public void startNewGame() {
        gameState.resetGame();
        gameState.recordPosition(computePositionKey(Color.WHITE));
        notifyBoardChanged();
        notifyGameStateChanged(GameState.GameStatus.IN_PROGRESS);
    }

    /**
     * 現在の手番のプレイヤーを返す。
     *
     * @return 現在のプレイヤー
     */
    public Player getCurrentPlayer() {
        Color currentColor = gameState.getCurrentPlayerColor();
        return currentColor == Color.WHITE ? whitePlayer : blackPlayer;
    }

    /**
     * 人間対AI戦において、人間側プレイヤーの色を返す。
     * 2人対戦（両者人間）など人間が一意に定まらない場合は null を返す。
     *
     * @return 人間側プレイヤーの色。一意に定まらない場合は null
     */
    public Color getHumanColor() {
        boolean whiteHuman = whitePlayer.isHuman();
        boolean blackHuman = blackPlayer.isHuman();
        if (whiteHuman == blackHuman) {
            return null;
        }
        return whiteHuman ? Color.WHITE : Color.BLACK;
    }

    /**
     * Resign 操作で投了させるべき色を返す。Human vs AI では手番に関わらず人間側の色を、
     * 2人対戦など人間が一意に定まらない場合は現在の手番の色を返す。
     *
     * @return 投了させるべきプレイヤーの色
     */
    public Color getResigningColor() {
        Color humanColor = getHumanColor();
        return humanColor != null ? humanColor : getCurrentPlayer().getColor();
    }

    /**
     * 現在の盤面を返す。
     *
     * @return {@link Board}
     */
    public Board getBoard() {
        return gameState.getBoard();
    }

    /**
     * 指定した位置にある現在のプレイヤーの駒が指せる合法手のリストを返す。
     * 王手になる手は除外済み。
     *
     * @param from 駒の現在位置
     * @return 合法手のリスト（空の場合あり）
     */
    public List<Move> getAvailableMoves(Position from) {
        Piece piece = gameState.getBoard().getPieceAt(from);

        if (piece == null || piece.getColor() != gameState.getCurrentPlayerColor()) {
            return Collections.emptyList();
        }

        List<Move> pseudoLegalMoves = moveValidator.getValidMoves(
            piece, gameState.getBoard(), gameState.getEnPassantTarget());
        List<Move> legalMoves = new ArrayList<>();

        for (Move move : pseudoLegalMoves) {
            if (checkmateDetector.isLegalMove(move, piece, gameState.getBoard(),
                                               gameState.getCurrentPlayerColor())) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    /**
     * 現在の手番の全駒が指せる合法手をまとめて返す。
     * SAN 生成時の曖昧回避判定や AI の着手選択で使用する。
     *
     * @return 現在の手番の全合法手のリスト
     */
    public List<Move> getAllAvailableMoves() {
        List<Move> allMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = Position.of(row, col);
                Piece piece = gameState.getBoard().getPieceAt(pos);
                if (piece != null && piece.getColor() == gameState.getCurrentPlayerColor()) {
                    allMoves.addAll(getAvailableMoves(pos));
                }
            }
        }
        return allMoves;
    }

    /**
     * 指定した位置から位置へ駒を動かす。昇格が必要な場合はクイーンに自動昇格する。
     *
     * @param from 移動元
     * @param to   移動先
     * @return 合法手として実行できた場合 true
     */
    public boolean makeMove(Position from, Position to) {
        return makeMoveInternal(from, to, null);
    }

    /**
     * 昇格先を指定して駒を動かす。ポーン昇格時に使用する。
     *
     * @param from          移動元
     * @param to            移動先
     * @param promotionType 昇格先の駒種
     * @return 合法手として実行できた場合 true
     */
    public boolean makeMove(Position from, Position to, PieceType promotionType) {
        return makeMoveInternal(from, to, promotionType);
    }

    /**
     * {@link Move} オブジェクトが保持する昇格先をそのまま適用して駒を動かす。
     * {@code getAvailableMoves()} などで得た手を、呼び出し側で from/to/昇格先に
     * 分解せずそのまま実行する用途に使う。
     *
     * @param move 実行する手（{@link #getAvailableMoves(Position)} 等で取得したもの）
     * @return 合法手として実行できた場合 true
     */
    public boolean makeMove(Move move) {
        return makeMoveInternal(move.getFrom(), move.getTo(), move.getPromotionPiece());
    }

    /**
     * 移動実行の内部実装。合法手チェック・盤面更新・状態計算・通知を一括処理する。
     *
     * @param from          移動元
     * @param to            移動先
     * @param promotionType 昇格先の駒種（昇格でない場合は null）
     * @return 実行に成功した場合 true
     */
    private boolean makeMoveInternal(Position from, Position to, PieceType promotionType) {
        // 引き分け成立後（合法手が残っていても）は着手を受け付けない。
        // CHECKMATE/STALEMATE は合法手0件により実質ブロックされるが、50手ルール・千日手・
        // 戦力不足は合法手が残ったまま終局するためこのガードが必要。
        if (gameState.isGameOver()) {
            return false;
        }

        Color currentColor = gameState.getCurrentPlayerColor();
        Piece piece = gameState.getBoard().getPieceAt(from);

        if (piece == null || piece.getColor() != currentColor) {
            return false;
        }

        List<Move> legalMoves = getAvailableMoves(from);
        // 昇格先が未指定の場合はクイーンを既定とする（手の生成順序に依存させない）
        PieceType effectivePromotion = (promotionType != null) ? promotionType : PieceType.QUEEN;
        Move selectedMove = findMove(legalMoves, from, to, effectivePromotion);

        if (selectedMove == null) {
            return false;
        }

        executeMoveOnBoard(selectedMove, piece);
        gameState.recordMove(selectedMove);
        updateHalfmoveClock(selectedMove, piece);

        // Compute state for the opponent (player about to move) before switching
        Color nextPlayer = gameState.getOpponentColor();
        int positionOccurrences = gameState.recordPosition(computePositionKey(nextPlayer));
        computeGameState(nextPlayer, positionOccurrences);
        gameState.switchPlayer();

        // All notifications fire after the switch so getCurrentPlayer() is correct
        notifyMoveMade(selectedMove);
        notifyBoardChanged();

        GameState.GameStatus status = gameState.getGameStatus();
        notifyGameStateChanged(status);

        if (status == GameState.GameStatus.CHECK) {
            notifyCheckDetected(gameState.getCurrentPlayerColor());
        } else if (status == GameState.GameStatus.CHECKMATE) {
            notifyGameOver(gameState.getCurrentPlayerColor().opposite());
        } else if (status == GameState.GameStatus.STALEMATE
                || status == GameState.GameStatus.FIFTY_MOVE_RULE
                || status == GameState.GameStatus.THREEFOLD_REPETITION
                || status == GameState.GameStatus.INSUFFICIENT_MATERIAL) {
            notifyGameOver(null);
        }

        return true;
    }

    /**
     * リストから移動元・移動先が一致する手を探して返す。
     * 昇格手の場合は {@code promotionType} に一致するものを優先し、
     * 一致する昇格手がなければ最初に見つかった昇格手にフォールバックする。
     *
     * @param moves         候補手のリスト
     * @param from          移動元
     * @param to            移動先
     * @param promotionType 昇格時に選ぶ駒種（昇格手でない場合は無視される）
     * @return 一致する {@link Move}、見つからなければ null
     */
    private Move findMove(List<Move> moves, Position from, Position to, PieceType promotionType) {
        Move promotionFallback = null;
        for (Move move : moves) {
            if (!move.getFrom().equals(from) || !move.getTo().equals(to)) {
                continue;
            }
            if (!move.isPromotion()) {
                return move;
            }
            // 昇格手: 指定された駒種に一致するものを優先する
            if (move.getPromotionPiece() == promotionType) {
                return move;
            }
            if (promotionFallback == null) {
                promotionFallback = move;
            }
        }
        return promotionFallback;
    }

    /**
     * 手を実際の盤面に適用する。移動カウントの更新・特殊手の処理・アンパッサン記録を含む。
     *
     * @param move  実行する手
     * @param piece 動かす駒
     */
    private void executeMoveOnBoard(Move move, Piece piece) {
        Board board = gameState.getBoard();
        Position from = move.getFrom();
        Position to = move.getTo();

        // 直前の手で設定されたアンパッサン対象は、次の手が実行された時点で必ず失効する
        // （2マス進んだ直後の1手のみ有効というルールのため、ここで一旦クリアする）
        gameState.setEnPassantTarget(null);

        // アンパッサンは to のマスに駒がいないため、先に removePiece すると無害だが明示的に除外する
        if (move.isCapture() && move.getMoveType() != com.chessgame.move.model.MoveType.EN_PASSANT) {
            board.removePiece(to);
        }

        piece.incrementMoveCount();
        board.removePiece(from);
        board.placePiece(piece, to);

        if (move.isCastling()) {
            executeCastling(move, board);
        } else if (move.isEnPassant()) {
            executeEnPassant(move, board);
        } else if (move.isPromotion()) {
            executePromotion(move, piece, board, to);
        }

        // ポーンが2マス進んだ場合のみ、通過マスをアンパッサンターゲットとして記録する
        if (piece.getType() == PieceType.PAWN) {
            int moveDistance = Math.abs(to.getRow() - from.getRow());
            if (moveDistance == 2) {
                // 通過マスは from と to の中間行
                int enPassantRow = (from.getRow() + to.getRow()) / 2;
                gameState.setEnPassantTarget(Position.of(enPassantRow, to.getCol()));
            }
        }
    }

    /**
     * キャスリングのルーク移動を実際の盤面に適用する。キングサイド・クイーンサイドを自動判定する。
     *
     * @param move  キャスリングの手
     * @param board 現在の盤面
     */
    private void executeCastling(Move move, Board board) {
        int kingRow = move.getFrom().getRow();
        int kingToCol = move.getTo().getCol();

        // キングの移動先列が元の列より大きければキングサイド（右方向）
        if (kingToCol > move.getFrom().getCol()) {
            // Kingside: ルーク h1/h8(col=7) → f1/f8(col=5)
            Piece rook = board.getPieceAt(Position.of(kingRow, 7));
            if (rook != null) {
                rook.incrementMoveCount();
                board.removePiece(Position.of(kingRow, 7));
                board.placePiece(rook, Position.of(kingRow, 5));
            }
        } else {
            // Queenside: ルーク a1/a8(col=0) → d1/d8(col=3)
            Piece rook = board.getPieceAt(Position.of(kingRow, 0));
            if (rook != null) {
                rook.incrementMoveCount();
                board.removePiece(Position.of(kingRow, 0));
                board.placePiece(rook, Position.of(kingRow, 3));
            }
        }
    }

    /**
     * アンパッサンで取られるポーンを盤面から取り除く。
     *
     * @param move  アンパッサンの手
     * @param board 現在の盤面
     */
    private void executeEnPassant(Move move, Board board) {
        // アンパッサンで取られるポーンは「移動元の行 × 移動先の列」に存在する
        Position capturePos = Position.of(move.getFrom().getRow(), move.getTo().getCol());
        board.removePiece(capturePos);
    }

    /**
     * ポーンを昇格先の駒に置き換える。
     *
     * @param move  昇格の手
     * @param piece 昇格するポーン
     * @param board 現在の盤面
     * @param to    昇格先の位置
     */
    private void executePromotion(Move move, Piece piece, Board board, Position to) {
        if (move.getPromotionPiece() != null) {
            Piece promotedPiece = createPromotionPiece(move.getPromotionPiece(), piece.getColor(), to);
            board.removePiece(to);
            board.placePiece(promotedPiece, to);
        }
    }

    /**
     * 昇格先の駒種に対応する新しい駒インスタンスを生成する。
     *
     * @param type  昇格先の駒種
     * @param color 駒の色
     * @param pos   配置位置
     * @return 生成した駒（不明な場合はクイーン）
     */
    private Piece createPromotionPiece(PieceType type, Color color, Position pos) {
        switch (type) {
            case QUEEN:  return new com.chessgame.piece.model.Queen(color, pos);
            case ROOK:   return new com.chessgame.piece.model.Rook(color, pos);
            case BISHOP: return new com.chessgame.piece.model.Bishop(color, pos);
            case KNIGHT: return new com.chessgame.piece.model.Knight(color, pos);
            default:     return new com.chessgame.piece.model.Queen(color, pos);
        }
    }

    /**
     * Computes and sets the game status for the given player (who is about to move).
     * Does NOT send any notifications — callers handle that.
     *
     * @param positionOccurrences playerAboutToMove 視点の局面が、これまでに出現した回数
     *                            （千日手判定に使用。呼び出し側で計算・記録済みの値を渡す）
     */
    private void computeGameState(Color playerAboutToMove, int positionOccurrences) {
        Position enPassantTarget = gameState.getEnPassantTarget();
        boolean isInCheck = checkDetector.isInCheck(playerAboutToMove, gameState.getBoard());

        if (isInCheck) {
            if (checkmateDetector.isCheckmate(playerAboutToMove, gameState.getBoard(), enPassantTarget)) {
                gameState.setGameStatus(GameState.GameStatus.CHECKMATE);
            } else {
                gameState.setGameStatus(GameState.GameStatus.CHECK);
            }
        } else if (checkmateDetector.isStalemate(playerAboutToMove, gameState.getBoard(), enPassantTarget)) {
            gameState.setGameStatus(GameState.GameStatus.STALEMATE);
        } else if (drawDetector.isFiftyMoveRule(gameState.getHalfmoveClock())) {
            gameState.setGameStatus(GameState.GameStatus.FIFTY_MOVE_RULE);
        } else if (drawDetector.isThreefoldRepetition(positionOccurrences)) {
            gameState.setGameStatus(GameState.GameStatus.THREEFOLD_REPETITION);
        } else if (drawDetector.isInsufficientMaterial(gameState.getBoard())) {
            gameState.setGameStatus(GameState.GameStatus.INSUFFICIENT_MATERIAL);
        } else {
            gameState.setGameStatus(GameState.GameStatus.IN_PROGRESS);
        }
    }

    /**
     * 現在の盤面・手番・キャスリング権・アンパッサン対象を一意に表す局面キーを生成する。
     * 千日手（同一局面3回出現）の判定に使用する。
     *
     * @param sideToMove この局面で次に指す側の色
     * @return 局面を一意に表す文字列
     */
    private String computePositionKey(Color sideToMove) {
        Board board = gameState.getBoard();
        StringBuilder key = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(Position.of(row, col));
                if (piece == null) {
                    key.append('.');
                } else {
                    char notation = piece.getType().getNotation();
                    key.append(piece.getColor() == Color.WHITE
                        ? Character.toUpperCase(notation) : Character.toLowerCase(notation));
                }
            }
        }
        key.append(sideToMove == Color.WHITE ? 'w' : 'b');
        key.append(hasCastlingRight(Color.WHITE, true) ? 'K' : '-');
        key.append(hasCastlingRight(Color.WHITE, false) ? 'Q' : '-');
        key.append(hasCastlingRight(Color.BLACK, true) ? 'k' : '-');
        key.append(hasCastlingRight(Color.BLACK, false) ? 'q' : '-');
        Position enPassant = gameState.getEnPassantTarget();
        key.append(enPassant != null ? enPassant.toAlgebraic() : "-");
        return key.toString();
    }

    /**
     * ハーフムーブクロックを更新する。ポーン移動または駒取りの場合は0にリセットし、
     * それ以外の手では1増やす（50手ルールの判定に使用）。
     *
     * @param move  実行した手
     * @param piece 動かした駒（昇格前でも {@code getType()} は元の駒種を返すため問題ない）
     */
    private void updateHalfmoveClock(Move move, Piece piece) {
        if (move.isCapture() || piece.getType() == PieceType.PAWN) {
            gameState.resetHalfmoveClock();
        } else {
            gameState.incrementHalfmoveClock();
        }
    }

    /**
     * 直前の手を取り消す。履歴を全リプレイして盤面を復元する。
     *
     * @return 取り消しに成功した場合 true（履歴が空の場合は false）
     */
    public boolean undo() {
        if (gameState.getMoveHistory().size() < 1) {
            return false;
        }

        // undo はインクリメンタルではなく「最終手を除いた全手をリプレイ」で実現する
        // 各駒の moveCount やアンパッサン状態を正確に復元するために全再実行が最もシンプル
        List<Move> moves = new ArrayList<>(gameState.getMoveHistory().getAll());
        moves.remove(moves.size() - 1);

        gameState.getMoveHistory().undoLastMove();
        // リプレイは対局の開始局面（fromFen 由来なら startingFen、無ければ標準初期配置）から行う。
        // 標準初期配置決め打ちだと fromFen で開始した対局の局面・手番・キャスリング権が破壊される。
        resetToStartingPosition();
        // ハーフムーブクロック・局面出現回数もリプレイで再構築するため、一旦クリアして開始局面を記録する
        gameState.clearPositionCounts();
        int positionOccurrences = gameState.recordPosition(computePositionKey(gameState.getCurrentPlayerColor()));

        // 残りの手をリプレイする。アンパッサン対象は executeMoveOnBoard が
        // 各手の冒頭でクリアしてから必要なら再設定するため、ここで個別に保持する必要はない。
        for (Move move : moves) {
            Piece piece = gameState.getBoard().getPieceAt(move.getFrom());
            if (piece != null) {
                executeMoveOnBoard(move, piece);
                updateHalfmoveClock(move, piece);
                gameState.switchPlayer();
                positionOccurrences = gameState.recordPosition(computePositionKey(gameState.getCurrentPlayerColor()));
            }
        }

        // リプレイ後は currentPlayerColor が「次に指す側」になっているため、そのまま状態を計算する
        gameState.setGameStatus(GameState.GameStatus.IN_PROGRESS);
        computeGameState(gameState.getCurrentPlayerColor(), positionOccurrences);

        notifyBoardChanged();
        notifyGameStateChanged(gameState.getGameStatus());

        return true;
    }

    /**
     * 指定した色のプレイヤーを投了させる。
     *
     * @param color 投了するプレイヤーの色
     * @return 投了処理が成功した場合 true（ゲーム終了済みの場合は false）
     */
    public boolean resign(Color color) {
        if (gameState.isGameOver()) {
            return false;
        }

        Color winner = color.opposite();
        if (color == Color.WHITE) {
            gameState.setGameStatus(GameState.GameStatus.WHITE_RESIGNED);
        } else {
            gameState.setGameStatus(GameState.GameStatus.BLACK_RESIGNED);
        }

        notifyGameOver(winner);
        notifyGameStateChanged(gameState.getGameStatus());
        return true;
    }

    public boolean isGameOver() {
        return gameState.isGameOver();
    }

    public GameState.GameStatus getGameStatus() {
        return gameState.getGameStatus();
    }

    public com.chessgame.move.model.MoveHistory getMoveHistory() {
        return gameState.getMoveHistory();
    }

    /**
     * 現在のアンパッサン対象マスを返す。アンパッサンが有効でない場合は null。
     * AI の FEN 生成など、現在の盤面状態を外部へシリアライズする用途で使用する。
     *
     * @return アンパッサン対象の {@link Position}、または null
     */
    public Position getEnPassantTarget() {
        return gameState.getEnPassantTarget();
    }

    /**
     * 現在のハーフムーブクロック（直近のポーン移動・駒取りからの半手数）を返す。
     * 50手ルールの状態確認などに使用する。
     *
     * @return ハーフムーブクロック
     */
    public int getHalfmoveClock() {
        return gameState.getHalfmoveClock();
    }

    /**
     * 現在のフルムーブ番号を返す（PGN/FEN 出力用）。
     *
     * @return フルムーブ番号
     */
    public int getFullmoveNumber() {
        return gameState.getFullmoveNumber();
    }

    /**
     * 指定した色がキャスリング権を持つかを返す（キング・対象ルークが未移動かつ原位置にあるか）。
     * AI の FEN 生成（{@link com.chessgame.game.player.AIPlayer#buildFen}）などで使用する。
     *
     * @param color    対象の色
     * @param kingside true でキングサイド、false でクイーンサイド
     * @return キャスリング権があれば true
     */
    public boolean hasCastlingRight(Color color, boolean kingside) {
        int row = (color == Color.WHITE) ? 7 : 0;
        Position kingSquare = Position.of(row, 4);
        Position rookSquare = Position.of(row, kingside ? 7 : 0);
        Board board = gameState.getBoard();
        Piece king = board.getPieceAt(kingSquare);
        Piece rook = board.getPieceAt(rookSquare);
        return king != null && king.getType() == PieceType.KING
            && king.getColor() == color && king.getMoveCount() == 0
            && rook != null && rook.getType() == PieceType.ROOK
            && rook.getColor() == color && rook.getMoveCount() == 0;
    }

    /**
     * オブザーバーを登録する。すでに登録済みの場合は無視する。
     *
     * @param observer 登録する {@link GameObserver}
     */
    public void addObserver(GameObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * オブザーバーの登録を解除する。
     *
     * @param observer 解除する {@link GameObserver}
     */
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    /**
     * 登録済みの全オブザーバーに盤面変化を通知する。
     */
    private void notifyBoardChanged() {
        for (GameObserver observer : new ArrayList<>(observers)) {
            observer.onBoardChanged();
        }
    }

    /**
     * 登録済みの全オブザーバーに手の確定を通知する。
     *
     * @param move 実行された手
     */
    private void notifyMoveMade(Move move) {
        for (GameObserver observer : new ArrayList<>(observers)) {
            observer.onMoveMade(move);
        }
    }

    /**
     * 登録済みの全オブザーバーにゲーム状態の変化を通知する。
     *
     * @param status 新しいゲーム状態
     */
    private void notifyGameStateChanged(GameState.GameStatus status) {
        for (GameObserver observer : new ArrayList<>(observers)) {
            observer.onGameStateChanged(status);
        }
    }

    /**
     * 登録済みの全オブザーバーに王手検出を通知する。
     *
     * @param kingColor 王手されているキングの色
     */
    private void notifyCheckDetected(Color kingColor) {
        for (GameObserver observer : new ArrayList<>(observers)) {
            observer.onCheckDetected(kingColor);
        }
    }

    /**
     * 登録済みの全オブザーバーにゲーム終了を通知する。
     *
     * @param winner 勝者の色。引き分けの場合は null
     */
    private void notifyGameOver(Color winner) {
        for (GameObserver observer : new ArrayList<>(observers)) {
            observer.onGameOver(winner);
        }
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "status=" + gameState.getGameStatus() +
                ", currentPlayer=" + getCurrentPlayer() +
                ", moveCount=" + gameState.getMoveCount() +
                '}';
    }
}
