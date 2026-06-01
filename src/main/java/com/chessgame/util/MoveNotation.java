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

package com.chessgame.util;

import com.chessgame.model.Color;
import com.chessgame.model.board.Position;
import com.chessgame.model.move.Move;
import com.chessgame.model.piece.PieceType;
import java.util.List;

/**
 * 移動の内部表現と代数記法文字列を相互変換するユーティリティクラス。
 * キャスリング・アンパッサン・昇格などの特殊な手にも対応する。
 */
public class MoveNotation {

    /**
     * 移動を代数記法文字列に変換する。
     *
     * @param move      変換する移動
     * @param isCapture 駒を取る手かどうか
     * @return 代数記法文字列（例: "e4"、"O-O"、"exd6 e.p."）
     */
    public static String toAlgebraic(Move move, boolean isCapture) {
        StringBuilder notation = new StringBuilder();

        switch (move.getMoveType()) {
            case CASTLING:
                int toCol = move.getTo().getCol();
                if (toCol == 6) {
                    notation.append("O-O");
                } else if (toCol == 2) {
                    notation.append("O-O-O");
                }
                break;

            case EN_PASSANT:
                notation.append(move.getFrom().getFile());
                if (isCapture) {
                    notation.append("x");
                }
                notation.append(move.getTo().toAlgebraic());
                notation.append(" e.p.");
                break;

            case PROMOTION:
                notation.append(move.getFrom().getFile());
                if (isCapture) {
                    notation.append("x");
                }
                notation.append(move.getTo().toAlgebraic());
                notation.append("=");
                notation.append(move.getPromotionPiece().toString());
                break;

            case CAPTURE:
                notation.append("x");
                notation.append(move.getTo().toAlgebraic());
                break;

            case NORMAL:
            default:
                notation.append(move.getTo().toAlgebraic());
                break;
        }

        return notation.toString();
    }

    /**
     * 王手・チェックメイト記号を付与した完全な代数記法文字列を返す。
     *
     * @param move        変換する移動
     * @param isCapture   駒を取る手かどうか
     * @param isCheck     王手になる手かどうか
     * @param isCheckmate チェックメイトになる手かどうか
     * @return 完全な代数記法文字列（例: "e4+"、"Qxf7#"）
     */
    public static String toFullNotation(Move move, boolean isCapture, boolean isCheck, boolean isCheckmate) {
        String notation = toAlgebraic(move, isCapture);

        if (isCheckmate) {
            notation += "#";
        } else if (isCheck) {
            notation += "+";
        }

        return notation;
    }

    /**
     * 移動を人間が読みやすい説明文字列に変換する。
     *
     * @param move 説明する移動
     * @return 説明文字列（例: "Move: e2 - e4"、"King-side castling (O-O)"）
     */
    public static String getMoveDescription(Move move) {
        switch (move.getMoveType()) {
            case CASTLING:
                int toCol = move.getTo().getCol();
                if (toCol == 6) {
                    return "King-side castling (O-O)";
                } else if (toCol == 2) {
                    return "Queen-side castling (O-O-O)";
                }
                break;

            case EN_PASSANT:
                return "En passant capture";

            case PROMOTION:
                return "Pawn promotion to " + move.getPromotionPiece();

            case CAPTURE:
                return "Capture: " + move.getFrom().toAlgebraic() + " x " + move.getTo().toAlgebraic();

            case NORMAL:
            default:
                return "Move: " + move.getFrom().toAlgebraic() + " - " + move.getTo().toAlgebraic();
        }

        return "Unknown move";
    }
}
