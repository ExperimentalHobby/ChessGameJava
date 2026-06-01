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

package com.chessgame.model.piece;

import com.chessgame.model.Color;
import com.chessgame.model.board.Position;
import com.chessgame.model.board.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * ナイトを表すクラス。L字形に8方向へジャンプできる。駒を飛び越えられる唯一の駒。
 */
public class Knight extends Piece {
    /**
     * ナイトを生成する。
     *
     * @param color    駒の色
     * @param position 初期位置
     */
    public Knight(Color color, Position position) {
        super(color, position);
    }

    // ナイトの駒種を返す
    @Override
    public PieceType getType() {
        return PieceType.KNIGHT;
    }

    // ナイトの攻撃マス（L字8方向）を返す。盤面の駒を飛び越えられるため board は参照しない
    @Override
    public List<Position> getAttackedSquares(Board board) {
        List<Position> squares = new ArrayList<>();
        // L字形: 縦2横1 または 縦1横2 の8方向
        int[][] moves = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2},  {1, 2},  {2, -1},  {2, 1}
        };

        for (int[] move : moves) {
            int newRow = position.getRow() + move[0];
            int newCol = position.getCol() + move[1];

            if (newRow >= 0 && newRow < Position.BOARD_SIZE &&
                newCol >= 0 && newCol < Position.BOARD_SIZE) {
                squares.add(Position.of(newRow, newCol));
            }
        }
        return squares;
    }

    // moveCount を引き継いだ深いコピーを返す
    @Override
    public Knight clone() {
        Knight cloned = new Knight(this.color, this.position);
        cloned.moveCount = this.moveCount;
        return cloned;
    }
}
