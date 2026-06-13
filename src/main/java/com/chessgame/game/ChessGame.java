package com.chessgame.game;

import com.chessgame.model.Color;
import com.chessgame.model.GameState;
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.move.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;
import com.chessgame.piece.rules.CheckDetector;
import com.chessgame.rules.CheckmateDetector;
import com.chessgame.rules.MoveValidator;
import java.util.*;

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
    private final List<GameObserver> observers;

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
     * ゲームを初期状態から開始する。盤面・履歴をリセットしてオブザーバーに通知する。
     */
    public void startNewGame() {
        gameState.resetGame();
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
     * 移動実行の内部実装。合法手チェック・盤面更新・状態計算・通知を一括処理する。
     *
     * @param from          移動元
     * @param to            移動先
     * @param promotionType 昇格先の駒種（昇格でない場合は null）
     * @return 実行に成功した場合 true
     */
    private boolean makeMoveInternal(Position from, Position to, PieceType promotionType) {
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

        // Compute state for the opponent (player about to move) before switching
        computeGameState(gameState.getOpponentColor());
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
        } else if (status == GameState.GameStatus.STALEMATE) {
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

        // アンパッサンは to のマスに駒がいないため、先に removePiece すると無害だが明示的に除外する
        if (move.isCapture() && move.getMoveType() != com.chessgame.model.move.MoveType.EN_PASSANT) {
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
        // switchPlayer() が呼ばれると enPassantTarget はリセットされるため、ここで設定しておく
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
     */
    private void computeGameState(Color playerAboutToMove) {
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
        } else {
            gameState.setGameStatus(GameState.GameStatus.IN_PROGRESS);
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

        Board board = gameState.getBoard();
        board.resetBoard();

        // undo はインクリメンタルではなく「最終手を除いた全手をリプレイ」で実現する
        // 各駒の moveCount やアンパッサン状態を正確に復元するために全再実行が最もシンプル
        List<Move> moves = new ArrayList<>(gameState.getMoveHistory().getAll());
        moves.remove(moves.size() - 1);

        gameState.getMoveHistory().undoLastMove();
        // リプレイは初期配置（手番 WHITE）から始まるため currentPlayerColor を WHITE に直接リセットする。
        // 従来の switchPlayer() による補正はリプレイ後の手番が奇数手の場合に誤った結果をもたらしていた。
        gameState.setCurrentPlayerColor(Color.WHITE);
        gameState.setEnPassantTarget(null);

        // 残りの手をリプレイしながら、最後に設定された en passant を保持する
        Position lastEnPassant = null;
        for (Move move : moves) {
            Piece piece = board.getPieceAt(move.getFrom());
            if (piece != null) {
                executeMoveOnBoard(move, piece);
                // executeMoveOnBoard 内で enPassantTarget が設定される場合がある
                lastEnPassant = gameState.getEnPassantTarget();
                gameState.switchPlayer(); // switchPlayer は enPassantTarget をリセットするため先に保存
            }
        }
        // switchPlayer で消去されたアンパッサンターゲットを復元する
        if (lastEnPassant != null) {
            gameState.setEnPassantTarget(lastEnPassant);
        }

        // リプレイ後は currentPlayerColor が「次に指す側」になっているため、そのまま状態を計算する
        gameState.setGameStatus(GameState.GameStatus.IN_PROGRESS);
        computeGameState(gameState.getCurrentPlayerColor());

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

    public com.chessgame.model.move.MoveHistory getMoveHistory() {
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
