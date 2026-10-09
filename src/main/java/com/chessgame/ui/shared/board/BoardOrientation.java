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

package com.chessgame.ui.shared.board;

import com.chessgame.board.model.Position;

/**
 * 盤面の向き（白視点／黒視点）に応じた、論理マス（{@link Position}）と画面上のセルの座標変換。
 * 黒視点では盤面を180度回転して表示する。Swing版・JavaFX版の盤面が共有する。
 *
 * <p>180度回転は行・列とも {@code 7 - x} への写像で、自分自身が逆写像になるため、
 * 「論理 → 画面」と「画面 → 論理」で同じ関数を使える。</p>
 */
public final class BoardOrientation {

    private static final int LAST_INDEX = 7;

    private BoardOrientation() {
    }

    /**
     * 論理的な行（または画面の行）を、向きに応じた画面の行（または論理的な行）に変換する。
     *
     * @param row     行番号（0〜7）
     * @param flipped 黒視点なら true
     * @return 変換後の行番号
     */
    public static int displayRow(int row, boolean flipped) {
        return flipped ? LAST_INDEX - row : row;
    }

    /**
     * 論理的な列（または画面の列）を、向きに応じた画面の列（または論理的な列）に変換する。
     *
     * @param col     列番号（0〜7）
     * @param flipped 黒視点なら true
     * @return 変換後の列番号
     */
    public static int displayCol(int col, boolean flipped) {
        return flipped ? LAST_INDEX - col : col;
    }

    /**
     * 画面上のセル位置が表す論理マスを返す（クリック座標からマスを特定するために使う）。
     *
     * @param displayRow 画面上の行（0〜7）
     * @param displayCol 画面上の列（0〜7）
     * @param flipped    黒視点なら true
     * @return そのセルにある論理マス
     */
    public static Position squareAt(int displayRow, int displayCol, boolean flipped) {
        return Position.of(displayRow(displayRow, flipped), displayCol(displayCol, flipped));
    }
}
