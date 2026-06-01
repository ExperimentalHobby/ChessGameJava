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

/**
 * チェスの駒の種類を表す列挙型。
 * 代数記法の1文字表記と駒の素材価値（ポーンを1とした相対値）を保持する。
 */
public enum PieceType {
    /** キング（記号: K、価値: 0） */
    KING('K', 0),
    /** クイーン（記号: Q、価値: 9） */
    QUEEN('Q', 9),
    /** ルーク（記号: R、価値: 5） */
    ROOK('R', 5),
    /** ビショップ（記号: B、価値: 3） */
    BISHOP('B', 3),
    /** ナイト（記号: N、価値: 3） */
    KNIGHT('N', 3),
    /** ポーン（記号: P、価値: 1） */
    PAWN('P', 1);

    private final char notation;
    private final int materialValue;

    PieceType(char notation, int materialValue) {
        this.notation = notation;
        this.materialValue = materialValue;
    }

    /**
     * 代数記法の1文字表記を返す（例: キング→'K'）。
     *
     * @return 記法文字
     */
    public char getNotation() {
        return notation;
    }

    /**
     * 駒の素材価値を返す（ポーン=1 を基準とした相対値）。
     *
     * @return 素材価値
     */
    public int getMaterialValue() {
        return materialValue;
    }

    @Override
    public String toString() {
        return String.valueOf(notation);
    }
}
