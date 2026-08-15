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

package com.chessgame.ui.shared.asset;

import com.chessgame.model.Color;

/**
 * 駒描画の塗り・輪郭色を提供する（UIツールキット非依存）。Swing版
 * {@code PieceImageGenerator}・JavaFX版 {@code PieceRenderer} で重複していた
 * RGBカラー値を共通化した（Issue #186）。各UI実装は {@link Rgb} の成分から
 * 自身のツールキット固有の色オブジェクト（{@code java.awt.Color}・
 * {@code javafx.scene.paint.Color} 等）を構築する。
 */
public final class PiecePalette {

    /** RGB各成分（0〜255）を保持する不変値オブジェクト。 */
    public record Rgb(int r, int g, int b) {
    }

    public static final Rgb WHITE_FILL = new Rgb(255, 251, 230);
    public static final Rgb WHITE_OUTLINE = new Rgb(45, 28, 8);
    public static final Rgb BLACK_FILL = new Rgb(28, 16, 6);
    public static final Rgb BLACK_OUTLINE = new Rgb(215, 190, 155);

    private PiecePalette() {
    }

    /**
     * 指定した色の駒の塗り色を返す。
     *
     * @param color 駒の色
     * @return 塗り色
     */
    public static Rgb fill(Color color) {
        return color == Color.WHITE ? WHITE_FILL : BLACK_FILL;
    }

    /**
     * 指定した色の駒の輪郭色を返す。
     *
     * @param color 駒の色
     * @return 輪郭色
     */
    public static Rgb outline(Color color) {
        return color == Color.WHITE ? WHITE_OUTLINE : BLACK_OUTLINE;
    }
}
