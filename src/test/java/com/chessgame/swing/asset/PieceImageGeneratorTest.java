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

package com.chessgame.swing.asset;

import com.chessgame.model.Color;
import com.chessgame.piece.model.PieceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PieceImageGenerator} の特性テスト。
 * Issue #186（PieceRendererとのロジック統合）に先立ち、現状の描画結果（サイズ・
 * 非空・キャッシュ動作・駒種ごとの差異）を固定し、リファクタリングの安全網とする。
 * オフスクリーン {@link BufferedImage} 描画のため headless 環境でも直接検証できる。
 */
class PieceImageGeneratorTest {

    @Test
    void testGetPieceImageReturnsConfiguredSize() {
        Image img = PieceImageGenerator.getPieceImage(Color.WHITE, PieceType.KING);

        assertThat(img.getWidth(null)).isEqualTo(PieceImageGenerator.IMAGE_SIZE);
        assertThat(img.getHeight(null)).isEqualTo(PieceImageGenerator.IMAGE_SIZE);
    }

    @Test
    void testGetPieceImageReturnsSameCachedInstanceOnRepeatedCalls() {
        Image first = PieceImageGenerator.getPieceImage(Color.WHITE, PieceType.QUEEN);
        Image second = PieceImageGenerator.getPieceImage(Color.WHITE, PieceType.QUEEN);

        assertThat(first).isSameAs(second);
    }

    @Test
    void testGetPieceImageProducesNonEmptyImage() {
        BufferedImage img = (BufferedImage) PieceImageGenerator.getPieceImage(Color.BLACK, PieceType.PAWN);

        assertThat(hasOpaquePixel(img)).as("生成画像が完全に透明ではないこと").isTrue();
    }

    @Test
    void testDifferentPieceTypesProduceDifferentImages() {
        BufferedImage king = (BufferedImage) PieceImageGenerator.getPieceImage(Color.WHITE, PieceType.KING);
        BufferedImage queen = (BufferedImage) PieceImageGenerator.getPieceImage(Color.WHITE, PieceType.QUEEN);

        assertThat(pixelsOf(king)).isNotEqualTo(pixelsOf(queen));
    }

    @Test
    void testDifferentColorsProduceDifferentImagesForSameType() {
        BufferedImage white = (BufferedImage) PieceImageGenerator.getPieceImage(Color.WHITE, PieceType.ROOK);
        BufferedImage black = (BufferedImage) PieceImageGenerator.getPieceImage(Color.BLACK, PieceType.ROOK);

        assertThat(pixelsOf(white)).isNotEqualTo(pixelsOf(black));
    }

    // ===== フォント非依存の代替描画 =====
    // チェス駒フォントが入っている環境では Unicode 描画しか通らないため、
    // render() に font=null を渡して代替描画を直接検証する（Issue #249）

    @ParameterizedTest
    @EnumSource(PieceType.class)
    void testFallbackRenderingProducesNonEmptyImageForEveryPieceType(PieceType type) {
        BufferedImage white = PieceImageGenerator.render(Color.WHITE, type, null, ' ');
        BufferedImage black = PieceImageGenerator.render(Color.BLACK, type, null, ' ');

        assertThat(white.getWidth()).isEqualTo(PieceImageGenerator.IMAGE_SIZE);
        assertThat(hasOpaquePixel(white)).as("%s(白)の代替描画が空でないこと", type).isTrue();
        assertThat(hasOpaquePixel(black)).as("%s(黒)の代替描画が空でないこと", type).isTrue();
    }

    @Test
    void testFallbackRenderingDistinguishesPieceTypes() {
        // 代替描画でも駒種ごとに違う図形が描かれること（全種類が同じ丸などになっていない）
        Set<List<Integer>> renderings = new HashSet<>();
        for (PieceType type : PieceType.values()) {
            BufferedImage img = PieceImageGenerator.render(Color.WHITE, type, null, ' ');
            renderings.add(Arrays.stream(pixelsOf(img)).boxed().toList());
        }

        assertThat(renderings).hasSize(PieceType.values().length);
    }

    @Test
    void testFallbackRenderingDistinguishesColors() {
        BufferedImage white = PieceImageGenerator.render(Color.WHITE, PieceType.KNIGHT, null, ' ');
        BufferedImage black = PieceImageGenerator.render(Color.BLACK, PieceType.KNIGHT, null, ' ');

        assertThat(pixelsOf(white)).isNotEqualTo(pixelsOf(black));
    }

    @Test
    void testRenderingWithFontDiffersFromFallback() {
        // 同じ駒でも Unicode 描画と代替描画は別物であること（分岐が効いている確認）
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 52);
        BufferedImage withFont = PieceImageGenerator.render(Color.WHITE, PieceType.KING, font, '♔');
        BufferedImage fallback = PieceImageGenerator.render(Color.WHITE, PieceType.KING, null, '♔');

        assertThat(pixelsOf(withFont)).isNotEqualTo(pixelsOf(fallback));
    }

    private static boolean hasOpaquePixel(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int alpha = (img.getRGB(x, y) >>> 24) & 0xFF;
                if (alpha > 0) return true;
            }
        }
        return false;
    }

    private static int[] pixelsOf(BufferedImage img) {
        return img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
    }
}
