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

package com.chessgame.rules;

import com.chessgame.model.Color;
import com.chessgame.model.board.Board;
import com.chessgame.model.board.Position;
import com.chessgame.model.piece.Piece;
import java.util.ArrayList;
import java.util.List;

/**
 * 指定した色のキングが王手状態にあるかどうかを検出するクラス。
 * 副作用のない純粋なロジックで構成されており、盤面のコピーを変更しない。
 */
public class CheckDetector {

    /**
     * 指定した色のキングが王手状態かどうかを返す。
     *
     * @param color 調べるキングの色
     * @param board 現在の盤面
     * @return 王手であれば true
     */
    public boolean isInCheck(Color color, Board board) {
        Position kingPosition = board.getKingPosition(color);
        return isSquareAttacked(kingPosition, color, board);
    }

    /**
     * 指定したマスが相手から攻撃されているかどうかを返す。
     *
     * @param target        調べるマスの位置
     * @param defenderColor そのマスを守る側の色
     * @param board         現在の盤面
     * @return 攻撃されていれば true
     */
    public boolean isSquareAttacked(Position target, Color defenderColor, Board board) {
        Color attackerColor = defenderColor.opposite();
        List<Piece> attackerPieces = board.getAllPieces(attackerColor);

        for (Piece attacker : attackerPieces) {
            if (canAttackSquare(attacker, target, board)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 指定した攻撃駒がターゲットのマスを攻撃できるかどうかを返す。
     * スライディング駒の場合は経路の遮断も確認する。
     *
     * @param attacker 攻撃する駒
     * @param target   攻撃対象のマス
     * @param board    現在の盤面
     * @return 攻撃できれば true
     */
    private boolean canAttackSquare(Piece attacker, Position target, Board board) {
        List<Position> attackedSquares = attacker.getAttackedSquares(board);

        for (Position square : attackedSquares) {
            if (square.equals(target)) {
                // Additional check for sliding pieces - must have clear path
                if (isSlidingPiece(attacker)) {
                    return isClearPath(attacker.getPosition(), target, board);
                }
                return true;
            }
        }

        return false;
    }

    /**
     * 指定した駒がスライディング駒（ルーク・ビショップ・クイーン）かどうかを返す。
     *
     * @param piece 調べる駒
     * @return スライディング駒であれば true
     */
    private boolean isSlidingPiece(Piece piece) {
        switch (piece.getType()) {
            case ROOK:
            case BISHOP:
            case QUEEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * 指定した2点間の経路上に駒がないかどうかを返す。両端点は含まない。
     *
     * @param from  出発位置
     * @param to    到達位置
     * @param board 現在の盤面
     * @return 経路が空であれば true
     */
    private boolean isClearPath(Position from, Position to, Board board) {
        int rowDiff = Integer.compare(to.getRow(), from.getRow());
        int colDiff = Integer.compare(to.getCol(), from.getCol());

        int row = from.getRow() + rowDiff;
        int col = from.getCol() + colDiff;

        while (row != to.getRow() || col != to.getCol()) {
            if (board.isPieceAt(Position.of(row, col))) {
                return false;
            }
            row += rowDiff;
            col += colDiff;
        }

        return true;
    }

    /**
     * 指定した色のキングを攻撃している全駒のリストを返す。
     *
     * @param color 守る側（キング側）の色
     * @param board 現在の盤面
     * @return キングを攻撃している駒のリスト
     */
    public List<Piece> getAttackingPieces(Color color, Board board) {
        List<Piece> attacking = new ArrayList<>();
        Position kingPosition = board.getKingPosition(color);
        Color attackerColor = color.opposite();
        List<Piece> attackers = board.getAllPieces(attackerColor);

        for (Piece attacker : attackers) {
            if (canAttackSquare(attacker, kingPosition, board)) {
                attacking.add(attacker);
            }
        }

        return attacking;
    }
}
