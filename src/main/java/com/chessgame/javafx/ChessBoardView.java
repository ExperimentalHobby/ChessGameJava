package com.chessgame.javafx;

import com.chessgame.game.ChessGame;
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JavaFX 版チェス盤ビュー。8×8 のマスを {@link SquareView} で構成し、
 * クリックによる駒の選択・移動・ハイライト表示を管理する。
 * ポーン昇格時は {@link PromotionDialog} を表示して駒種を選択させる。
 */
public class ChessBoardView extends StackPane {

    private static final int BOARD_SIZE = 8;

    private final GridPane boardGrid;
    private final SquareView[][] squares;
    private final PieceImageLoader imageLoader;
    private ChessGame game;
    private SquareView selectedSquare;
    private final Map<Position, SquareView> squareMap;
    private Runnable onMoveCallback;

    /**
     * 指定したゲームに紐づいた盤面ビューを生成する。
     *
     * @param game 表示対象の {@link ChessGame}
     */
    public ChessBoardView(ChessGame game) {
        this.game = game;
        this.imageLoader = new PieceImageLoader();
        this.boardGrid = new GridPane();
        this.squares = new SquareView[BOARD_SIZE][BOARD_SIZE];
        this.squareMap = new HashMap<>();

        initializeBoard();
        updateBoardDisplay();

        boardGrid.setAlignment(Pos.CENTER);
        getChildren().add(boardGrid);
    }

    /**
     * 8×8 の {@link SquareView} を生成して {@code GridPane} に配置し、クリックハンドラを設定する。
     */
    private void initializeBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Position position = Position.of(row, col);
                SquareView square = new SquareView(position);
                square.setOnClickHandler(() -> handleSquareClick(square));
                squares[row][col] = square;
                squareMap.put(position, square);
                boardGrid.add(square, col, row);
            }
        }
    }

    /**
     * 現在のゲーム状態に合わせて盤面表示を更新する。移動後やundo後に呼ぶ。
     */
    public void updateBoardDisplay() {
        Board board = game.getBoard();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Position position = Position.of(row, col);
                SquareView squareView = squares[row][col];
                Piece piece = board.getPieceAt(position);
                squareView.removePiece();
                if (piece != null) {
                    squareView.setPiece(piece, imageLoader.getPieceImageView(piece));
                }
            }
        }
        clearHighlights();
    }

    /**
     * マスのクリックを処理する。初回クリックで駒を選択し、2回目クリックで移動を試みる。
     * 同じマスをクリックした場合は選択解除、別の自駒をクリックした場合は再選択する。
     *
     * @param clicked クリックされた {@link SquareView}
     */
    /** 現在参照するゲームを差し替える。新ゲーム開始時に呼ぶ。 */
    public void setGame(ChessGame game) { this.game = game; }

    private void handleSquareClick(SquareView clicked) {
        if (game.isGameOver()) return;
        if (!game.getCurrentPlayer().isHuman()) return;

        Position clickedPos = clicked.getPosition();
        Piece clickedPiece = game.getBoard().getPieceAt(clickedPos);

        if (selectedSquare == null) {
            // 【未選択状態】 自駒をクリック → 選択してハイライト表示
            if (clickedPiece != null && clickedPiece.getColor() == game.getCurrentPlayer().getColor()) {
                select(clicked);
            }
        } else {
            // 【選択済み状態】 同じマスを再クリック → 選択解除
            if (clicked.equals(selectedSquare)) {
                clearSelection();
                return;
            }

            // 別の自駒をクリック → 選択を解除してから新しい駒を選択
            if (clickedPiece != null && clickedPiece.getColor() == game.getCurrentPlayer().getColor()) {
                clearSelection();
                select(clicked);
                return;
            }

            // 上記以外（空マスまたは相手駒）→ 移動を試みる
            Position from = selectedSquare.getPosition();
            Piece moving = game.getBoard().getPieceAt(from);
            boolean success;

            if (moving != null && isPromotionMove(moving, clickedPos)) {
                PieceType choice = askPromotion(moving.getColor());
                success = game.makeMove(from, clickedPos, choice);
            } else {
                success = game.makeMove(from, clickedPos);
            }

            clearSelection();

            if (success) {
                updateBoardDisplay();
                if (onMoveCallback != null) onMoveCallback.run();
            }
        }
    }

    /**
     * マスを選択状態にして合法手のハイライトを表示する。
     *
     * @param square 選択する {@link SquareView}
     */
    private void select(SquareView square) {
        selectedSquare = square;
        square.highlight(SquareView.HighlightType.SELECTED);
        highlightMoves(square.getPosition());
    }

    /**
     * 指定位置の駒が移動できる全マスをハイライト表示する。
     *
     * @param from 移動元の位置
     */
    private void highlightMoves(Position from) {
        List<Position> targets = game.getAvailableMoves(from)
                .stream().map(Move::getTo).collect(Collectors.toList());
        for (Position pos : targets) {
            SquareView sv = squareMap.get(pos);
            if (sv != null) sv.highlight(SquareView.HighlightType.AVAILABLE);
        }
    }

    /**
     * 全マスのハイライトを消去する。
     */
    private void clearHighlights() {
        for (SquareView[] row : squares)
            for (SquareView sq : row) sq.clearHighlight();
    }

    /**
     * ハイライトを消去して選択中のマスをリセットする。
     */
    private void clearSelection() {
        clearHighlights();
        selectedSquare = null;
    }

    /**
     * 指定した駒の指定マスへの移動がポーン昇格かどうかを返す。
     *
     * @param piece 動かす駒
     * @param to    移動先の位置
     * @return 昇格を伴う合法手であれば true
     */
    private boolean isPromotionMove(Piece piece, Position to) {
        if (piece.getType() != PieceType.PAWN) return false;
        return game.getAvailableMoves(piece.getPosition())
                .stream().anyMatch(m -> m.getTo().equals(to) && m.isPromotion());
    }

    /**
     * {@link PromotionDialog} を表示して昇格先の駒種を取得する。キャンセル時はクイーンを返す。
     *
     * @param color 昇格するポーンの色
     * @return 選択された駒種
     */
    private PieceType askPromotion(com.chessgame.model.Color color) {
        PromotionDialog dialog = new PromotionDialog(color, getScene().getWindow());
        Optional<PieceType> result = dialog.showAndWait();
        return result.orElse(PieceType.QUEEN);
    }

    /**
     * 手が確定したときに呼ばれるコールバックを設定する。ステータスバーの更新などに使う。
     *
     * @param callback 手確定時に実行する処理
     */
    public void setOnMoveCallback(Runnable callback) { this.onMoveCallback = callback; }

    /**
     * 選択状態とハイライトをクリアして盤面表示を再描画する。新ゲーム開始時などに使う。
     */
    public void resetView() {
        clearSelection();
        updateBoardDisplay();
    }
}
