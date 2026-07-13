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
import java.util.List;

/**
 * ビショップを表すクラス。斜め4方向に盤端まで移動できるスライディング駒。
 */
public class Bishop extends Piece {
    /**
     * ビショップを生成する。
     *
     * @param color    駒の色
     * @param position 初期位置
     */
    public Bishop(Color color, Position position) {
        super(color, position);
    }

    // ビショップの駒種を返す
    @Override
    public PieceType getType() {
        return PieceType.BISHOP;
    }

    // ビショップの攻撃マス（斜め4方向、利き筋）を返す
    @Override
    public List<Position> getAttackedSquares(Board board) {
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        return slidingAttackedSquares(board, directions);
    }

    // moveCount を引き継いだ深いコピーを返す
    @Override
    public Bishop clone() {
        Bishop cloned = new Bishop(this.color, this.position);
        cloned.moveCount = this.moveCount;
        return cloned;
    }
}
