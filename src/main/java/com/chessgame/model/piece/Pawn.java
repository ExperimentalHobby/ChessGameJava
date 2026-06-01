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
 * ポーンを表すクラス。白は上方向（行番号減少）、黒は下方向（行番号増加）に進む。
 * 攻撃マス（斜め前1マス）のみを返す。前進移動・2マス移動・アンパッサンは {@code MoveValidator} が処理する。
 */
public class Pawn extends Piece {
    /**
     * ポーンを生成する。
     *
     * @param color    駒の色
     * @param position 初期位置
     */
    public Pawn(Color color, Position position) {
        super(color, position);
    }

    // ポーンの駒種を返す
    @Override
    public PieceType getType() {
        return PieceType.PAWN;
    }

    // ポーンの攻撃マス（斜め前2方向）を返す。前進マスは含まない点に注意
    @Override
    public List<Position> getAttackedSquares(Board board) {
        List<Position> squares = new ArrayList<>();
        // 白は上方向（-1）、黒は下方向（+1）
        int direction = color == Color.WHITE ? -1 : 1;

        int[] diagonalCols = {-1, 1};
        for (int colOffset : diagonalCols) {
            int newRow = position.getRow() + direction;
            int newCol = position.getCol() + colOffset;

            if (newRow >= 0 && newRow < Position.BOARD_SIZE &&
                newCol >= 0 && newCol < Position.BOARD_SIZE) {
                squares.add(Position.of(newRow, newCol));
            }
        }
        return squares;
    }

    // moveCount を引き継いだ深いコピーを返す（moveCount でアンパッサン判定を行うため必須）
    @Override
    public Pawn clone() {
        Pawn cloned = new Pawn(this.color, this.position);
        cloned.moveCount = this.moveCount;
        return cloned;
    }
}
