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

import java.util.List;

/**
 * 駒描画で使う Unicode グリフ文字・キャッシュキー・フォント候補を提供する
 * （UIツールキット非依存）。Swing版 {@code PieceImageGenerator}・JavaFX版
 * {@code PieceRenderer} で重複していたロジックを共通化した（Issue #186）。
 * 実際の描画呼び出し（{@code Graphics2D}/{@code GraphicsContext}）や
 * フォント解決アルゴリズム自体はツールキット固有のため各UI実装に残る。
 */
public final class PieceGlyphs {

    /**
     * Unicode チェス駒文字を表示できるか試す候補フォント名（優先順）。
     * Windows/macOS/Linux の主要環境をカバーする。
     */
    public static final List<String> FONT_CANDIDATES = List.of(
        "Segoe UI Symbol",   // Windows 10/11
        "Arial Unicode MS",  // older Windows / macOS
        "Symbola",
        "FreeSerif",
        "DejaVu Serif",
        "Noto Sans Symbols2"
    );

    private PieceGlyphs() {
    }

    /**
     * 駒の色と種類に対応する Unicode チェス駒文字を返す（例: 白キング '♔'、黒キング '♚'）。
     *
     * @param color 駒の色
     * @param type  駒の種類
     * @return Unicode チェス駒文字
     */
    public static char pieceChar(Color color, PieceType type) {
        switch (type) {
            case KING:   return color == Color.WHITE ? '♔' : '♚';
            case QUEEN:  return color == Color.WHITE ? '♕' : '♛';
            case ROOK:   return color == Color.WHITE ? '♖' : '♜';
            case BISHOP: return color == Color.WHITE ? '♗' : '♝';
            case KNIGHT: return color == Color.WHITE ? '♘' : '♞';
            default:     return color == Color.WHITE ? '♙' : '♟';
        }
    }

    /**
     * 駒画像キャッシュのキーを組み立てる。
     *
     * @param color 駒の色
     * @param type  駒の種類
     * @return キャッシュキー（例: {@code "WHITE_KING"}）
     */
    public static String cacheKey(Color color, PieceType type) {
        return color + "_" + type;
    }
}
