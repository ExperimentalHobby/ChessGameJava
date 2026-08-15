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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PiecePalette} のユニットテスト。
 * Swing版・JavaFX版の両描画クラスで重複していたRGBカラー値を統合したもの。
 */
class PiecePaletteTest {

    @Test
    void testWhiteFillMatchesKnownRgb() {
        assertThat(PiecePalette.WHITE_FILL).isEqualTo(new PiecePalette.Rgb(255, 251, 230));
    }

    @Test
    void testWhiteOutlineMatchesKnownRgb() {
        assertThat(PiecePalette.WHITE_OUTLINE).isEqualTo(new PiecePalette.Rgb(45, 28, 8));
    }

    @Test
    void testBlackFillMatchesKnownRgb() {
        assertThat(PiecePalette.BLACK_FILL).isEqualTo(new PiecePalette.Rgb(28, 16, 6));
    }

    @Test
    void testBlackOutlineMatchesKnownRgb() {
        assertThat(PiecePalette.BLACK_OUTLINE).isEqualTo(new PiecePalette.Rgb(215, 190, 155));
    }

    @Test
    void testFillReturnsColorSpecificPalette() {
        assertThat(PiecePalette.fill(Color.WHITE)).isEqualTo(PiecePalette.WHITE_FILL);
        assertThat(PiecePalette.fill(Color.BLACK)).isEqualTo(PiecePalette.BLACK_FILL);
    }

    @Test
    void testOutlineReturnsColorSpecificPalette() {
        assertThat(PiecePalette.outline(Color.WHITE)).isEqualTo(PiecePalette.WHITE_OUTLINE);
        assertThat(PiecePalette.outline(Color.BLACK)).isEqualTo(PiecePalette.BLACK_OUTLINE);
    }

    @Test
    void testFillDiffersFromOutlineForEachColor() {
        assertThat(PiecePalette.fill(Color.WHITE)).isNotEqualTo(PiecePalette.outline(Color.WHITE));
        assertThat(PiecePalette.fill(Color.BLACK)).isNotEqualTo(PiecePalette.outline(Color.BLACK));
    }
}
