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
 * クイーンを表すクラス。縦・横・斜めの全8方向に盤端まで移動できる最強の駒。
 */
public class Queen extends Piece {
    /**
     * クイーンを生成する。
     *
     * @param color    駒の色
     * @param position 初期位置
     */
    public Queen(Color color, Position position) {
        super(color, position);
    }

    /**
     * クイーンの駒種を返す。
     * @return {@link PieceType#QUEEN}
     */
    @Override
    public PieceType getType() {
        return PieceType.QUEEN;
    }

    /**
     * 縦・横・斜め全8方向の利き筋を返す。
     * @param board 現在の盤面
     * @return 攻撃対象の {@link Position} リスト
     */
    @Override
    public List<Position> getAttackedSquares(Board board) {
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},   // 縦横（ルーク方向）
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}   // 斜め（ビショップ方向）
        };
        return slidingAttackedSquares(board, directions);
    }

    /**
     * moveCount を引き継いだ深いコピーを返す。
     * @return 同じ色・位置・移動回数を持つコピー
     */
    @Override
    public Queen clone() {
        Queen cloned = new Queen(this.color, this.position);
        cloned.moveCount = this.moveCount;
        return cloned;
    }
}
