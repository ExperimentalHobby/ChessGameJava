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

package com.chessgame.notation.rules;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import com.chessgame.piece.model.Bishop;
import com.chessgame.piece.model.King;
import com.chessgame.piece.model.Knight;
import com.chessgame.piece.model.Pawn;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.Queen;
import com.chessgame.piece.model.Rook;

/**
 * FEN（Forsyth-Edwards Notation）文字列と盤面・局面情報を相互変換する。
 * 副作用のない静的メソッドのみで構成する。
 */
public class FenCodec {

    private FenCodec() {
    }

    /**
     * 局面情報から FEN 文字列を組み立てる。
     *
     * @param board          盤面
     * @param sideToMove     手番
     * @param whiteKingside  白のキングサイドキャスリング権
     * @param whiteQueenside 白のクイーンサイドキャスリング権
     * @param blackKingside  黒のキングサイドキャスリング権
     * @param blackQueenside 黒のクイーンサイドキャスリング権
     * @param enPassant      アンパッサン対象マス（無ければ null）
     * @param halfmove       ハーフムーブクロック
     * @param fullmove       フルムーブ番号
     * @return FEN 文字列
     */
    public static String encode(Board board, Color sideToMove,
                                 boolean whiteKingside, boolean whiteQueenside,
                                 boolean blackKingside, boolean blackQueenside,
                                 Position enPassant, int halfmove, int fullmove) {
        StringBuilder fen = new StringBuilder();

        for (int row = 0; row < 8; row++) {
            int emptyRun = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(Position.of(row, col));
                if (piece == null) {
                    emptyRun++;
                } else {
                    if (emptyRun > 0) {
                        fen.append(emptyRun);
                        emptyRun = 0;
                    }
                    char notation = piece.getType().getNotation();
                    fen.append(piece.getColor() == Color.WHITE
                        ? Character.toUpperCase(notation) : Character.toLowerCase(notation));
                }
            }
            if (emptyRun > 0) {
                fen.append(emptyRun);
            }
            if (row < 7) {
                fen.append('/');
            }
        }

        fen.append(' ').append(sideToMove == Color.WHITE ? 'w' : 'b');

        StringBuilder castling = new StringBuilder();
        if (whiteKingside) castling.append('K');
        if (whiteQueenside) castling.append('Q');
        if (blackKingside) castling.append('k');
        if (blackQueenside) castling.append('q');
        fen.append(' ').append(castling.length() == 0 ? "-" : castling.toString());

        fen.append(' ').append(enPassant != null ? enPassant.toAlgebraic() : "-");
        fen.append(' ').append(halfmove);
        fen.append(' ').append(fullmove);

        return fen.toString();
    }

    /**
     * FEN 文字列をパースした結果を保持する値オブジェクト。
     */
    public record ParsedFen(Board board, Color sideToMove,
                             boolean whiteKingside, boolean whiteQueenside,
                             boolean blackKingside, boolean blackQueenside,
                             Position enPassant, int halfmove, int fullmove) {
    }

    /**
     * FEN 文字列をパースする。
     * <p>構造の妥当性（フィールドの有無・ランク数・各ランクのマス数・数値フィールドの形式）を
     * 検証し、問題があれば理由を含む {@link IllegalArgumentException} を投げる。
     * 呼び出し側（PGN 読み込み UI など）が「入力が不正」として一様に扱えるよう、
     * 低レベルな例外を漏らさず例外の型を1つに揃えることを目的とする。
     * 王の数など局面としての合法性までは検証しない。</p>
     *
     * @param fen FEN 文字列
     * @return パース結果
     * @throws IllegalArgumentException FEN の構造が不正な場合
     */
    public static ParsedFen parse(String fen) {
        if (fen == null || fen.isBlank()) {
            throw new IllegalArgumentException("FEN が空です");
        }
        String[] parts = fen.trim().split("\\s+");

        String placement = parts[0];
        String[] ranks = placement.split("/", -1);
        if (ranks.length != 8) {
            throw new IllegalArgumentException(
                "FEN のランク数が8ではありません（" + ranks.length + "）: " + placement);
        }
        for (int i = 0; i < ranks.length; i++) {
            int squares = 0;
            for (char ch : ranks[i].toCharArray()) {
                if (Character.isDigit(ch)) {
                    squares += Character.getNumericValue(ch);
                } else if ("PNBRQKpnbrqk".indexOf(ch) >= 0) {
                    squares++;
                } else {
                    throw new IllegalArgumentException(
                        "FEN に不明な駒種表記が含まれています: " + ch);
                }
            }
            if (squares != 8) {
                // ランク番号は FEN の並び（先頭=8段目）に合わせて表示する
                throw new IllegalArgumentException(
                    "FEN のランク " + (8 - i) + " のマス数が8ではありません（" + squares + "）: " + ranks[i]);
            }
        }

        Board board = Board.empty();
        int row = 0;
        int col = 0;
        for (char ch : placement.toCharArray()) {
            if (ch == '/') {
                row++;
                col = 0;
            } else if (Character.isDigit(ch)) {
                col += Character.getNumericValue(ch);
            } else {
                Color color = Character.isUpperCase(ch) ? Color.WHITE : Color.BLACK;
                Position pos = Position.of(row, col);
                board.placePiece(createPiece(Character.toUpperCase(ch), color, pos), pos);
                col++;
            }
        }

        Color sideToMove = (parts.length > 1 && "b".equals(parts[1])) ? Color.BLACK : Color.WHITE;

        String castlingField = parts.length > 2 ? parts[2] : "-";
        boolean whiteKingside = castlingField.indexOf('K') >= 0;
        boolean whiteQueenside = castlingField.indexOf('Q') >= 0;
        boolean blackKingside = castlingField.indexOf('k') >= 0;
        boolean blackQueenside = castlingField.indexOf('q') >= 0;

        Position enPassant = null;
        if (parts.length > 3 && !"-".equals(parts[3])) {
            enPassant = Position.of(parts[3]);
        }

        int halfmove = parts.length > 4 ? parseCount(parts[4], "ハーフムーブクロック") : 0;
        int fullmove = parts.length > 5 ? parseCount(parts[5], "フルムーブ番号") : 1;

        return new ParsedFen(board, sideToMove, whiteKingside, whiteQueenside,
            blackKingside, blackQueenside, enPassant, halfmove, fullmove);
    }

    /**
     * FEN の手数フィールドを整数として読み取る。数値でなければ、どのフィールドが
     * 不正なのかを含む {@link IllegalArgumentException} に変換する
     * （{@code NumberFormatException} のままでは呼び出し側が FEN の問題だと判別できない）。
     */
    private static int parseCount(String field, String fieldName) {
        try {
            return Integer.parseInt(field);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "FEN の" + fieldName + "が数値ではありません: " + field, e);
        }
    }

    /**
     * FEN の駒種文字（大文字）から新しい駒インスタンスを生成する。
     */
    private static Piece createPiece(char upperNotation, Color color, Position pos) {
        return switch (upperNotation) {
            case 'P' -> new Pawn(color, pos);
            case 'N' -> new Knight(color, pos);
            case 'B' -> new Bishop(color, pos);
            case 'R' -> new Rook(color, pos);
            case 'Q' -> new Queen(color, pos);
            case 'K' -> new King(color, pos);
            default -> throw new IllegalArgumentException("不明な駒種表記: " + upperNotation);
        };
    }
}
