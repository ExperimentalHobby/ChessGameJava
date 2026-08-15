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
import com.chessgame.piece.model.PieceType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link PieceGlyphs} のユニットテスト。
 * Swing版・JavaFX版の両描画クラスで重複していたグリフ選択ロジックを統合したもの。
 */
class PieceGlyphsTest {

    @Test
    void testPieceCharDiffersBetweenWhiteAndBlackForEveryType() {
        for (PieceType type : PieceType.values()) {
            char white = PieceGlyphs.pieceChar(Color.WHITE, type);
            char black = PieceGlyphs.pieceChar(Color.BLACK, type);
            assertThat(white).as("type=%s", type).isNotEqualTo(black);
        }
    }

    @Test
    void testPieceCharIsUniquePerTypeForWhite() {
        Set<Character> chars = new HashSet<>();
        for (PieceType type : PieceType.values()) {
            chars.add(PieceGlyphs.pieceChar(Color.WHITE, type));
        }
        assertThat(chars).hasSize(PieceType.values().length);
    }

    @Test
    void testPieceCharReturnsKnownKingGlyphs() {
        assertThat(PieceGlyphs.pieceChar(Color.WHITE, PieceType.KING)).isEqualTo('♔');
        assertThat(PieceGlyphs.pieceChar(Color.BLACK, PieceType.KING)).isEqualTo('♚');
    }

    @Test
    void testCacheKeyIncludesColorAndType() {
        assertThat(PieceGlyphs.cacheKey(Color.WHITE, PieceType.KING))
            .isEqualTo(Color.WHITE + "_" + PieceType.KING);
        assertThat(PieceGlyphs.cacheKey(Color.BLACK, PieceType.PAWN))
            .isEqualTo(Color.BLACK + "_" + PieceType.PAWN);
    }

    @Test
    void testCacheKeyDiffersForEachColorTypeCombination() {
        Set<String> keys = new HashSet<>();
        for (Color color : Color.values()) {
            for (PieceType type : PieceType.values()) {
                keys.add(PieceGlyphs.cacheKey(color, type));
            }
        }
        assertThat(keys).hasSize(Color.values().length * PieceType.values().length);
    }

    @Test
    void testFontCandidatesIsNonEmptyAndImmutable() {
        assertThat(PieceGlyphs.FONT_CANDIDATES).isNotEmpty();
        assertThatThrownBy(() -> PieceGlyphs.FONT_CANDIDATES.add("x"))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
