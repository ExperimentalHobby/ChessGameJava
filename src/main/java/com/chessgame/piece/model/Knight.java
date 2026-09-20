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

package com.chessgame.piece.model;

import com.chessgame.model.Color;
import com.chessgame.board.model.Position;
import com.chessgame.board.model.Board;
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

    /**
     * ナイトの駒種を返す。
     * @return {@link PieceType#KNIGHT}
     */
    @Override
    public PieceType getType() {
        return PieceType.KNIGHT;
    }

    /**
     * L字8方向の利き筋を返す。駒を飛び越えられるため board は参照しない。
     * @param board 現在の盤面（未使用）
     * @return 攻撃対象の {@link Position} リスト
     */
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

            if (Position.isValid(newRow, newCol)) {
                squares.add(Position.of(newRow, newCol));
            }
        }
        return squares;
    }

    /**
     * moveCount を引き継いだ深いコピーを返す。
     * @return 同じ色・位置・移動回数を持つコピー
     */
    @Override
    public Knight clone() {
        Knight cloned = new Knight(this.color, this.position);
        cloned.moveCount = this.moveCount;
        return cloned;
    }
}
