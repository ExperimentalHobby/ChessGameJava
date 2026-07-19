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

import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Swing 版駒画像を生成するクラス。
 * Unicode チェス駒文字を対応フォントで描画し、フォントが使えない場合は幾何学図形で代替する。
 * 生成した画像はキャッシュして再利用する。
 */
public class PieceImageGenerator {
    /** 生成する画像のサイズ（ピクセル）。 */
    public static final int IMAGE_SIZE = 60;

    private static final Map<String, BufferedImage> cache = new HashMap<>();
    private static Font chessFont = null;
    private static boolean fontSearchDone = false;

    /**
     * 指定した色と種類の駒画像を返す。キャッシュがあれば再利用する。
     *
     * @param color 駒の色
     * @param type  駒の種類
     * @return 駒を描画した {@link java.awt.Image}
     */
    public static java.awt.Image getPieceImage(Color color, PieceType type) {
        String key = color + "_" + type;
        return cache.computeIfAbsent(key, k -> generateImage(color, type));
    }

    /** Backward-compat accessor used by old code. */
    public static ImageIcon getPieceIcon(Color color, PieceType type) {
        return new ImageIcon(getPieceImage(color, type));
    }

    // ── image generation ──────────────────────────────────────────────────────

    /**
     * 指定した色と種類の駒を描画した {@link BufferedImage} を生成する。
     *
     * @param color 駒の色
     * @param type  駒の種類
     * @return 駒を描画した画像
     */
    private static BufferedImage generateImage(Color color, PieceType type) {
        BufferedImage img = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,   RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,      RenderingHints.VALUE_STROKE_PURE);

        Font font = resolveChessFont();
        char ch = pieceChar(color, type);

        if (font != null && font.canDisplay(ch)) {
            drawUnicode(g, color, ch, font);
        } else {
            drawFallback(g, color, type);
        }

        g.dispose();
        return img;
    }

    // ── Unicode rendering ─────────────────────────────────────────────────────

    /**
     * Unicode チェス駒文字をフォントで描画する。影・塗り・輪郭を順に描く。
     *
     * @param g     描画コンテキスト
     * @param color 駒の色
     * @param ch    描画する Unicode 文字
     * @param font  使用するフォント
     */
    private static void drawUnicode(Graphics2D g, Color color, char ch, Font font) {
        Font scaledFont = font.deriveFont(52f);
        FontRenderContext frc = g.getFontRenderContext();
        TextLayout layout = new TextLayout(String.valueOf(ch), scaledFont, frc);
        Rectangle2D bounds = layout.getBounds();

        // Center glyph inside the image
        float dx = (float) ((IMAGE_SIZE - bounds.getWidth())  / 2.0 - bounds.getX());
        float dy = (float) ((IMAGE_SIZE - bounds.getHeight()) / 2.0 - bounds.getY());

        // Drop shadow
        Shape shadow = layout.getOutline(AffineTransform.getTranslateInstance(dx + 2.0, dy + 2.5));
        g.setColor(new java.awt.Color(0, 0, 0, 60));
        g.fill(shadow);

        // Glyph outline
        Shape glyph = layout.getOutline(AffineTransform.getTranslateInstance(dx, dy));

        java.awt.Color fill, outline;
        if (color == Color.WHITE) {
            fill    = new java.awt.Color(255, 251, 230);
            outline = new java.awt.Color(45, 28, 8);
        } else {
            fill    = new java.awt.Color(28, 16, 6);
            outline = new java.awt.Color(215, 190, 155);
        }

        g.setColor(fill);
        g.fill(glyph);

        g.setColor(outline);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(glyph);
    }

    // ── Unicode character selection ───────────────────────────────────────────

    /**
     * Returns the Unicode chess piece character.
     * White pieces (♔♕♖♗♘♙) are hollow outlines; Black pieces (♚♛♜♝♞♟) are solid.
     * Java2D honours the font's winding rules, so white glyphs render with decorative holes
     * and black glyphs render as filled silhouettes.
     */
    private static char pieceChar(Color color, PieceType type) {
        switch (type) {
            case KING:   return color == Color.WHITE ? '♔' : '♚';
            case QUEEN:  return color == Color.WHITE ? '♕' : '♛';
            case ROOK:   return color == Color.WHITE ? '♖' : '♜';
            case BISHOP: return color == Color.WHITE ? '♗' : '♝';
            case KNIGHT: return color == Color.WHITE ? '♘' : '♞';
            case PAWN:   return color == Color.WHITE ? '♙' : '♟';
            default:     return '?';
        }
    }

    // ── Font resolution ───────────────────────────────────────────────────────

    /**
     * チェス駒の Unicode 文字を表示できるフォントをシステムから探して返す。
     * 見つからない場合は null を返す。結果はキャッシュする。
     *
     * @return 使用可能なフォント、または null
     */
    private static Font resolveChessFont() {
        if (fontSearchDone) return chessFont;
        fontSearchDone = true;

        String[] preferred = {
            "Segoe UI Symbol",    // Windows 10/11
            "Arial Unicode MS",   // older Windows
            "Symbola",            // universal
            "FreeSerif",          // Linux
            "DejaVu Serif",
            "Noto Sans Symbols2",
        };

        Set<String> available = new HashSet<>();
        for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            available.add(name);
        }

        for (String name : preferred) {
            if (available.contains(name)) {
                Font f = new Font(name, Font.PLAIN, 52);
                if (f.canDisplay('♔') && f.canDisplay('♚')) {
                    chessFont = f;
                    return chessFont;
                }
            }
        }

        // Scan all fonts as last resort
        for (String name : available) {
            Font f = new Font(name, Font.PLAIN, 52);
            if (f.canDisplay('♔') && f.canDisplay('♚')) {
                chessFont = f;
                return chessFont;
            }
        }

        return null;
    }

    // ── Fallback procedural rendering ─────────────────────────────────────────

    /**
     * Unicode フォントが使えない場合の代替として、幾何学図形で駒を描画する。
     *
     * @param g     描画コンテキスト
     * @param color 駒の色
     * @param type  駒の種類
     */
    private static void drawFallback(Graphics2D g, Color color, PieceType type) {
        java.awt.Color fill   = (color == Color.WHITE)
            ? new java.awt.Color(255, 251, 230)
            : new java.awt.Color(28, 16, 6);
        java.awt.Color stroke = (color == Color.WHITE)
            ? new java.awt.Color(45, 28, 8)
            : new java.awt.Color(215, 190, 155);

        // subtle shadow disc
        g.setColor(new java.awt.Color(0, 0, 0, 45));
        g.fillOval(9, 11, IMAGE_SIZE - 14, IMAGE_SIZE - 14);

        switch (type) {
            case KING:   drawKing(g, fill, stroke);   break;
            case QUEEN:  drawQueen(g, fill, stroke);  break;
            case ROOK:   drawRook(g, fill, stroke);   break;
            case BISHOP: drawBishop(g, fill, stroke); break;
            case KNIGHT: drawKnight(g, fill, stroke); break;
            case PAWN:   drawPawn(g, fill, stroke);   break;
        }
    }

    private static final BasicStroke STROKE = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    /** キングを幾何学図形で描画する。 */
    private static void drawKing(Graphics2D g, java.awt.Color fill, java.awt.Color stroke) {
        int cx = IMAGE_SIZE / 2;
        g.setColor(fill);
        g.fillRoundRect(cx - 13, 29, 26, 22, 8, 8);
        g.fillOval(cx - 10, 48, 20, 9);
        g.fillRect(cx - 2, 10, 5, 20);
        g.fillRect(cx - 10, 16, 20, 5);
        g.setColor(stroke); g.setStroke(STROKE);
        g.drawRoundRect(cx - 13, 29, 26, 22, 8, 8);
        g.drawOval(cx - 10, 48, 20, 9);
        g.drawRect(cx - 2, 10, 5, 20);
        g.drawRect(cx - 10, 16, 20, 5);
    }

    /** クイーンを幾何学図形で描画する。 */
    private static void drawQueen(Graphics2D g, java.awt.Color fill, java.awt.Color stroke) {
        int cx = IMAGE_SIZE / 2;
        g.setColor(fill);
        g.fillOval(cx - 20, 9, 9, 9); g.fillOval(cx - 5, 6, 10, 10); g.fillOval(cx + 11, 9, 9, 9);
        int[] xs = {cx - 20, cx - 15, cx - 7, cx, cx + 7, cx + 15, cx + 20, cx + 11, cx, cx - 11};
        int[] ys = {18, 40, 28, 40, 28, 40, 18, 49, 49, 49};
        g.fillPolygon(xs, ys, 10);
        g.fillOval(cx - 11, 46, 22, 10);
        g.setColor(stroke); g.setStroke(STROKE);
        g.drawOval(cx - 20, 9, 9, 9); g.drawOval(cx - 5, 6, 10, 10); g.drawOval(cx + 11, 9, 9, 9);
        g.drawPolygon(xs, ys, 10); g.drawOval(cx - 11, 46, 22, 10);
    }

    /** ルークを幾何学図形で描画する。 */
    private static void drawRook(Graphics2D g, java.awt.Color fill, java.awt.Color stroke) {
        int cx = IMAGE_SIZE / 2;
        g.setColor(fill);
        g.fillRect(cx - 15, 10, 9, 9); g.fillRect(cx - 4, 10, 9, 9); g.fillRect(cx + 7, 10, 9, 9);
        g.fillRect(cx - 15, 17, 30, 31); g.fillOval(cx - 13, 45, 26, 10);
        g.setColor(stroke); g.setStroke(STROKE);
        g.drawRect(cx - 15, 10, 9, 9); g.drawRect(cx - 4, 10, 9, 9); g.drawRect(cx + 7, 10, 9, 9);
        g.drawRect(cx - 15, 17, 30, 31); g.drawOval(cx - 13, 45, 26, 10);
    }

    /** ビショップを幾何学図形で描画する。 */
    private static void drawBishop(Graphics2D g, java.awt.Color fill, java.awt.Color stroke) {
        int cx = IMAGE_SIZE / 2;
        g.setColor(fill);
        g.fillOval(cx - 5, 7, 10, 10); g.fillOval(cx - 10, 15, 20, 13);
        g.fillPolygon(new int[]{cx - 13, cx, cx + 13}, new int[]{25, 49, 25}, 3);
        g.fillOval(cx - 13, 45, 26, 10);
        g.setColor(stroke); g.setStroke(STROKE);
        g.drawOval(cx - 5, 7, 10, 10); g.drawOval(cx - 10, 15, 20, 13);
        g.drawPolygon(new int[]{cx - 13, cx, cx + 13}, new int[]{25, 49, 25}, 3);
        g.drawOval(cx - 13, 45, 26, 10);
    }

    /** ナイトを幾何学図形で描画する。 */
    private static void drawKnight(Graphics2D g, java.awt.Color fill, java.awt.Color stroke) {
        int cx = IMAGE_SIZE / 2;
        g.setColor(fill);
        int[] xs = {cx - 11, cx - 15, cx - 11, cx - 5, cx + 13, cx + 15, cx + 11, cx + 7};
        int[] ys = {49, 28, 13, 7, 15, 28, 49, 49};
        g.fillPolygon(xs, ys, 8); g.fillOval(cx - 13, 45, 26, 10);
        g.setColor(stroke); g.setStroke(STROKE);
        g.drawPolygon(xs, ys, 8); g.drawOval(cx - 13, 45, 26, 10);
        // eye
        g.setColor(fill); g.fillOval(cx + 3, 17, 5, 5);
        g.setColor(stroke); g.drawOval(cx + 3, 17, 5, 5);
    }

    /** ポーンを幾何学図形で描画する。 */
    private static void drawPawn(Graphics2D g, java.awt.Color fill, java.awt.Color stroke) {
        int cx = IMAGE_SIZE / 2;
        g.setColor(fill);
        g.fillOval(cx - 9, 10, 18, 18);
        g.fillPolygon(new int[]{cx - 11, cx, cx + 11}, new int[]{26, 43, 26}, 3);
        g.fillOval(cx - 13, 39, 26, 14);
        g.setColor(stroke); g.setStroke(STROKE);
        g.drawOval(cx - 9, 10, 18, 18);
        g.drawPolygon(new int[]{cx - 11, cx, cx + 11}, new int[]{26, 43, 26}, 3);
        g.drawOval(cx - 13, 39, 26, 14);
    }
}
