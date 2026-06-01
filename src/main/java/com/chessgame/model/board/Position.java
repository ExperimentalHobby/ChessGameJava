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

package com.chessgame.model.board;

import java.util.Objects;

/**
 * チェス盤上のマスを表すイミュータブルな座標クラス。
 * 行（row）と列（col）は 0〜7 の範囲で管理される。
 * インスタンス生成には {@link #of(int, int)} または {@link #of(String)} を使用する。
 */
public final class Position {
    private final int row;
    private final int col;

    /** 盤面のサイズ（8×8）。 */
    public static final int BOARD_SIZE = 8;

    private Position(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            throw new IllegalArgumentException(
                String.format("Position (%d, %d) is out of bounds", row, col)
            );
        }
        this.row = row;
        this.col = col;
    }

    /**
     * 行・列番号から位置を生成する。
     *
     * @param row 行番号（0〜7、0 が 8 段目）
     * @param col 列番号（0〜7、0 が a ファイル）
     * @return 対応する {@link Position}
     * @throws IllegalArgumentException 範囲外の場合
     */
    public static Position of(int row, int col) {
        return new Position(row, col);
    }

    /**
     * 代数記法文字列（例: "e2"）から位置を生成する。
     *
     * @param algebraic "a1"〜"h8" 形式の文字列
     * @return 対応する {@link Position}
     * @throws IllegalArgumentException 無効な文字列の場合
     */
    public static Position of(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }
        char fileCh = algebraic.charAt(0);
        char rankCh = algebraic.charAt(1);

        if (fileCh < 'a' || fileCh > 'h' || rankCh < '1' || rankCh > '8') {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }

        int col = fileCh - 'a';
        int row = 8 - (rankCh - '0');
        return new Position(row, col);
    }

    /**
     * 内部行番号を返す（0 が 8 段目）。
     *
     * @return 行番号（0〜7）
     */
    public int getRow() {
        return row;
    }

    /**
     * 内部列番号を返す（0 が a ファイル）。
     *
     * @return 列番号（0〜7）
     */
    public int getCol() {
        return col;
    }

    /**
     * チェス記法のランク番号を返す（1〜8）。
     *
     * @return ランク番号
     */
    public int getRank() {
        return 8 - row;
    }

    /**
     * チェス記法のファイル文字を返す（'a'〜'h'）。
     *
     * @return ファイル文字
     */
    public char getFile() {
        return (char) ('a' + col);
    }

    /**
     * 代数記法文字列（例: "e2"）に変換する。
     *
     * @return 代数記法文字列
     */
    public String toAlgebraic() {
        return String.valueOf(getFile()) + getRank();
    }

    /**
     * この位置が盤面内かどうかを返す。
     *
     * @return 盤面内であれば true
     */
    public boolean isValid() {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }
}
