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
import java.util.List;
import java.util.Set;

/**
 * チェスの駒を表す抽象基底クラス。
 * 色・位置・移動回数を保持する。移動回数はキャスリング可否の判定に使用される。
 * 各駒種は {@link #getType()}・{@link #getAttackedSquares(Board)}・{@link #clone()} を実装する。
 */
public abstract class Piece {
    protected final Color color;
    protected Position position;
    protected int moveCount;

    /**
     * 駒を生成する。
     *
     * @param color    駒の色
     * @param position 初期位置
     */
    protected Piece(Color color, Position position) {
        this.color = color;
        this.position = position;
        this.moveCount = 0;
    }

    /**
     * 駒の色を返す。
     *
     * @return {@link Color}
     */
    public Color getColor() {
        return color;
    }

    /**
     * 駒の現在位置を返す。
     *
     * @return {@link Position}
     */
    public Position getPosition() {
        return position;
    }

    /**
     * 駒の位置を更新する。
     *
     * @param position 新しい位置
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * この駒が移動した回数を返す。キャスリング可否の判定に使用する。
     *
     * @return 移動回数
     */
    public int getMoveCount() {
        return moveCount;
    }

    /**
     * 移動回数を1増やす。駒が実際に動いたときに呼ぶ。
     */
    public void incrementMoveCount() {
        this.moveCount++;
    }

    /**
     * 駒の種類を返す。
     *
     * @return {@link PieceType}
     */
    public abstract PieceType getType();

    /**
     * この駒が攻撃できる全マスのリストを返す。盤面の状態を参照して利き筋を計算する。
     * ルールによる合法手チェックは含まない（擬似合法手）。
     *
     * @param board 現在の盤面
     * @return 攻撃対象の {@link Position} リスト
     */
    public abstract List<Position> getAttackedSquares(Board board);

    /**
     * この駒の深いコピーを返す。盤面の仮実行などに使用する。
     *
     * @return 同じ色・位置・移動回数を持つコピー
     */
    public abstract Piece clone();

    @Override
    public String toString() {
        return color.name().charAt(0) + getType().toString();
    }
}
