/*
 * MIT License
 *
 * Copyright (c) 2026 ChessGame Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.chessgame.javafx.board;

import com.chessgame.game.core.ChessGame;
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;
import com.chessgame.javafx.ui.dialog.PromotionDialog;
import com.chessgame.javafx.asset.PieceImageLoader;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private final Map<Position, SquareView> squareMap;
    private final BoardSelectionController controller;
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
        this.controller = new BoardSelectionController(game, this::askPromotion);

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
        updateLastMoveHighlight();
    }

    /**
     * 直前の手のマスに半透明ハイライトを付ける。着手後・undo後・New Game後の
     * いずれでも {@link #updateBoardDisplay()} 経由で呼ばれるため、その時点の
     * 履歴（New Game 後は空）に自動的に追従する。
     */
    private void updateLastMoveHighlight() {
        for (SquareView[] row : squares) {
            for (SquareView sq : row) {
                sq.setLastMoveHighlight(false);
            }
        }
        Move lastMove = game.getMoveHistory().getLastMove();
        if (lastMove == null) {
            return;
        }
        SquareView from = squareMap.get(lastMove.getFrom());
        SquareView to = squareMap.get(lastMove.getTo());
        if (from != null) from.setLastMoveHighlight(true);
        if (to != null) to.setLastMoveHighlight(true);
    }

    /** 現在参照するゲームを差し替える。新ゲーム開始時に呼ぶ。 */
    public void setGame(ChessGame game) {
        this.game = game;
        controller.setGame(game);
    }

    /**
     * マスのクリックを処理する。選択・移動判定は {@link BoardSelectionController} に委譲し、
     * ここでは結果に応じた描画のみを行う。
     *
     * @param clicked クリックされた {@link SquareView}
     */
    private void handleSquareClick(SquareView clicked) {
        ClickOutcome outcome = controller.handleClick(clicked.getPosition());

        switch (outcome.getType()) {
            case SELECTED:
                clearHighlights();
                SquareView selected = squareMap.get(outcome.getPosition());
                if (selected != null) selected.highlight(SquareView.HighlightType.SELECTED);
                for (Position pos : outcome.getHighlightTargets()) {
                    SquareView sv = squareMap.get(pos);
                    if (sv != null) sv.highlight(SquareView.HighlightType.AVAILABLE);
                }
                break;
            case DESELECTED:
                clearHighlights();
                break;
            case MOVE_ATTEMPTED:
                clearHighlights();
                if (outcome.isMoveSucceeded()) {
                    updateBoardDisplay();
                    if (onMoveCallback != null) onMoveCallback.run();
                }
                break;
            case NONE:
            default:
                break;
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
        controller.clearSelection();
        updateBoardDisplay();
    }
}
