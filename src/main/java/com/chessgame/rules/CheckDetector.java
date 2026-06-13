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
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.piece.Piece;
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
     * スライディング駒の {@code getAttackedSquares} は既にブロッカーで利き筋を止めているため、
     * ここでは攻撃マスに含まれるかどうかのみを確認すればよい。
     *
     * @param attacker 攻撃する駒
     * @param target   攻撃対象のマス
     * @param board    現在の盤面
     * @return 攻撃できれば true
     */
    private boolean canAttackSquare(Piece attacker, Position target, Board board) {
        return attacker.getAttackedSquares(board).contains(target);
    }
}
