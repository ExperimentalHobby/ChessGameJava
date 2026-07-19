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
     *
     * @param fen FEN 文字列
     * @return パース結果
     */
    public static ParsedFen parse(String fen) {
        String[] parts = fen.trim().split("\\s+");

        Board board = Board.empty();
        int row = 0;
        int col = 0;
        for (char ch : parts[0].toCharArray()) {
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

        int halfmove = parts.length > 4 ? Integer.parseInt(parts[4]) : 0;
        int fullmove = parts.length > 5 ? Integer.parseInt(parts[5]) : 1;

        return new ParsedFen(board, sideToMove, whiteKingside, whiteQueenside,
            blackKingside, blackQueenside, enPassant, halfmove, fullmove);
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
