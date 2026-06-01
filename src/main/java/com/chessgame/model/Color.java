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

package com.chessgame.model;

/**
 * チェスの駒の色を表す列挙型。WHITE（白）と BLACK（黒）の2値を持つ。
 */
public enum Color {
    WHITE, BLACK;

    /**
     * この色の反対の色を返す。
     *
     * @return WHITE なら BLACK、BLACK なら WHITE
     */
    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    /**
     * 色の表示名を返す。
     *
     * @return "White" または "Black"
     */
    // UI 表示や棋譜文字列に使用される人間可読な色名を返す
    @Override
    public String toString() {
        return this == WHITE ? "White" : "Black";
    }
}
