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
import com.chessgame.board.model.Position;
import com.chessgame.board.model.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * ルークを表すクラス。縦横4方向に盤端まで移動できるスライディング駒。
 * 移動回数はキャスリング可否の判定に使用される。
 */
public class Rook extends Piece {
    /**
     * ルークを生成する。
     *
     * @param color    駒の色
     * @param position 初期位置
     */
    public Rook(Color color, Position position) {
        super(color, position);
    }

    // ルークの駒種を返す
    @Override
    public PieceType getType() {
        return PieceType.ROOK;
    }

    // ルークの攻撃マス（縦横4方向、利き筋）を返す
    @Override
    public List<Position> getAttackedSquares(Board board) {
        List<Position> squares = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            for (int i = 1; i < Position.BOARD_SIZE; i++) {
                int newRow = position.getRow() + dir[0] * i;
                int newCol = position.getCol() + dir[1] * i;

                if (newRow < 0 || newRow >= Position.BOARD_SIZE ||
                    newCol < 0 || newCol >= Position.BOARD_SIZE) break;

                Position target = Position.of(newRow, newCol);
                squares.add(target);
                // 駒が存在する場合はそこで利き筋を止める（駒の向こう側は攻撃できない）
                if (board.getPieceAt(target) != null) break;
            }
        }
        return squares;
    }

    // moveCount を引き継いだ深いコピーを返す（moveCount == 0 でキャスリング可否を判定するため必須）
    @Override
    public Rook clone() {
        Rook cloned = new Rook(this.color, this.position);
        cloned.moveCount = this.moveCount;
        return cloned;
    }
}
