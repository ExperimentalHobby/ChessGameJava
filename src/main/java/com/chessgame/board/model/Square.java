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

package com.chessgame.board.model;

import com.chessgame.piece.model.Piece;

/**
 * チェス盤上の1マスを表すクラス。位置情報と、そのマスに置かれている駒（または null）を保持する。
 */
public class Square {
    private final Position position;
    private Piece piece;

    /**
     * 指定した位置の空マスを生成する。
     *
     * @param position このマスの位置
     */
    public Square(Position position) {
        this.position = position;
        this.piece = null;
    }

    /**
     * このマスの位置を返す。
     *
     * @return {@link Position}
     */
    public Position getPosition() {
        return position;
    }

    /**
     * このマスに置かれている駒を返す。駒がなければ null。
     *
     * @return {@link com.chessgame.piece.model.Piece}、または null
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * このマスに駒があるかどうかを返す。
     *
     * @return 駒があれば true
     */
    public boolean hasPiece() {
        return piece != null;
    }

    /**
     * このマスに駒を置く。
     *
     * @param piece 置く駒
     */
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    /**
     * このマスから駒を取り除いて返す。駒がなければ null を返す。
     *
     * @return 取り除いた駒、または null
     */
    public Piece removePiece() {
        Piece removed = this.piece;
        this.piece = null;
        return removed;
    }

    /**
     * このマスを空にする。
     */
    public void clear() {
        this.piece = null;
    }

    @Override
    public String toString() {
        return "Square{" +
                "position=" + position +
                ", piece=" + piece +
                '}';
    }
}
