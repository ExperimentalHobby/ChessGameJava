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

import com.chessgame.board.model.Position;
import com.chessgame.game.core.ChessGame;
import com.chessgame.model.Color;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 盤面クリックによる駒の選択・移動・昇格判定を担う（JavaFX非依存）。
 * {@link ChessBoardView} はこのクラスに判定を委譲し、結果（{@link ClickOutcome}）に
 * 応じた描画のみを担当する。JavaFX Toolkitに依存しないため単体テストから直接検証できる。
 */
class BoardSelectionController {
    private final Function<Color, PieceType> promotionResolver;
    private ChessGame game;
    private Position selectedPosition;

    /**
     * @param game              対象のゲーム
     * @param promotionResolver 昇格を伴う移動の際に呼ばれ、選択された駒種を返す関数
     */
    BoardSelectionController(ChessGame game, Function<Color, PieceType> promotionResolver) {
        this.game = game;
        this.promotionResolver = promotionResolver;
    }

    /** 表示対象のゲームを差し替える。New Game 開始時に呼ぶ。 */
    void setGame(ChessGame game) {
        this.game = game;
        clearSelection();
    }

    /** 現在選択中のマスの位置を返す。未選択なら null。 */
    Position getSelectedPosition() {
        return selectedPosition;
    }

    /** 選択状態をリセットする。 */
    void clearSelection() {
        selectedPosition = null;
    }

    /**
     * マスのクリックを処理する。初回クリックで駒を選択し、2回目クリックで移動を試みる。
     * 同じマスをクリックした場合は選択解除、別の自駒をクリックした場合は再選択する。
     *
     * @param clickedPos クリックされたマスの位置
     * @return クリック結果
     */
    ClickOutcome handleClick(Position clickedPos) {
        if (game.isGameOver()) return ClickOutcome.none();
        if (!game.getCurrentPlayer().isHuman()) return ClickOutcome.none();

        Piece clickedPiece = game.getBoard().getPieceAt(clickedPos);

        if (selectedPosition == null) {
            if (isOwnPiece(clickedPiece)) {
                return select(clickedPos);
            }
            return ClickOutcome.none();
        }

        if (clickedPos.equals(selectedPosition)) {
            clearSelection();
            return ClickOutcome.deselected();
        }

        if (isOwnPiece(clickedPiece)) {
            clearSelection();
            return select(clickedPos);
        }

        Position from = selectedPosition;
        Piece moving = game.getBoard().getPieceAt(from);
        boolean success;
        if (moving != null && isPromotionMove(moving, clickedPos)) {
            PieceType choice = promotionResolver.apply(moving.getColor());
            success = game.makeMove(from, clickedPos, choice);
        } else {
            success = game.makeMove(from, clickedPos);
        }
        clearSelection();
        return ClickOutcome.moveAttempted(success);
    }

    private ClickOutcome select(Position pos) {
        selectedPosition = pos;
        return ClickOutcome.selected(pos, availableTargets(pos));
    }

    private boolean isOwnPiece(Piece piece) {
        return piece != null && piece.getColor() == game.getCurrentPlayer().getColor();
    }

    private List<Position> availableTargets(Position from) {
        return game.getAvailableMoves(from).stream().map(Move::getTo).collect(Collectors.toList());
    }

    private boolean isPromotionMove(Piece piece, Position to) {
        if (piece.getType() != PieceType.PAWN) return false;
        return game.getAvailableMoves(piece.getPosition())
                .stream().anyMatch(m -> m.getTo().equals(to) && m.isPromotion());
    }
}
