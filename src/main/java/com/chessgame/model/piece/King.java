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
 * キングを表すクラス。周囲8方向に1マス移動できる。
 * キャスリングは {@code MoveValidator} が別途処理する。
 */
public class King extends Piece {
    /**
     * キングを生成する。
     *
     * @param color    駒の色
     * @param position 初期位置
     */
    public King(Color color, Position position) {
        super(color, position);
    }

    // キングの駒種を返す
    @Override
    public PieceType getType() {
        return PieceType.KING;
    }

    // キングの攻撃マス（周囲8方向、1マス）を返す。キャスリングは含まない
    @Override
    public List<Position> getAttackedSquares(Board board) {
        List<Position> squares = new ArrayList<>();
        // 8方向: 左上・上・右上・左・右・左下・下・右下
        int[][] directions = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
        };

        for (int[] dir : directions) {
            int newRow = position.getRow() + dir[0];
            int newCol = position.getCol() + dir[1];

            if (newRow >= 0 && newRow < Position.BOARD_SIZE &&
                newCol >= 0 && newCol < Position.BOARD_SIZE) {
                squares.add(Position.of(newRow, newCol));
            }
        }
        return squares;
    }

    // moveCount を引き継いだ深いコピーを返す（moveCount == 0 でキャスリング可否を判定するため必須）
    @Override
    public King clone() {
        King cloned = new King(this.color, this.position);
        cloned.moveCount = this.moveCount;
        return cloned;
    }
}
