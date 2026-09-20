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

    /**
     * ルークの駒種を返す。
     * @return {@link PieceType#ROOK}
     */
    @Override
    public PieceType getType() {
        return PieceType.ROOK;
    }

    /**
     * 縦横4方向の利き筋を返す。
     * @param board 現在の盤面
     * @return 攻撃対象の {@link Position} リスト
     */
    @Override
    public List<Position> getAttackedSquares(Board board) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        return slidingAttackedSquares(board, directions);
    }

    /**
     * moveCount を引き継いだ深いコピーを返す（moveCount == 0 でキャスリング可否を判定するため必須）。
     * @return 同じ色・位置・移動回数を持つコピー
     */
    @Override
    public Rook clone() {
        Rook cloned = new Rook(this.color, this.position);
        cloned.moveCount = this.moveCount;
        return cloned;
    }
}
