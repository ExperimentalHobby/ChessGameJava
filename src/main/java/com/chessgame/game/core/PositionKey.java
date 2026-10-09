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

package com.chessgame.game.core;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.detection.rules.CheckmateDetector;
import com.chessgame.model.Color;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;
import com.chessgame.rules.MoveValidator;

/**
 * 千日手（同一局面3回出現）の判定に使う局面キーを生成する。{@link ChessGame} から分離した責務で、
 * 盤面・手番・キャスリング権・（実際に取れる場合の）アンパッサン対象から、局面を一意に表す文字列を作る。
 * 状態を持たない純粋な計算のみで構成する。
 */
final class PositionKey {
    private static final MoveValidator MOVE_VALIDATOR = new MoveValidator();
    private static final CheckmateDetector CHECKMATE_DETECTOR = new CheckmateDetector();

    private PositionKey() {
    }

    /**
     * 局面を一意に表すキーを生成する。
     *
     * @param board            盤面
     * @param sideToMove       この局面で次に指す側の色
     * @param whiteKingside    白のキングサイド・キャスリング権
     * @param whiteQueenside   白のクイーンサイド・キャスリング権
     * @param blackKingside    黒のキングサイド・キャスリング権
     * @param blackQueenside   黒のクイーンサイド・キャスリング権
     * @param enPassantTarget  アンパッサン対象マス。無ければ null
     * @return 局面を一意に表す文字列
     */
    static String of(Board board, Color sideToMove,
                     boolean whiteKingside, boolean whiteQueenside,
                     boolean blackKingside, boolean blackQueenside,
                     Position enPassantTarget) {
        StringBuilder key = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(Position.of(row, col));
                if (piece == null) {
                    key.append('.');
                } else {
                    char notation = piece.getType().getNotation();
                    key.append(piece.getColor() == Color.WHITE
                        ? Character.toUpperCase(notation) : Character.toLowerCase(notation));
                }
            }
        }
        key.append(sideToMove == Color.WHITE ? 'w' : 'b');
        key.append(whiteKingside ? 'K' : '-');
        key.append(whiteQueenside ? 'Q' : '-');
        key.append(blackKingside ? 'k' : '-');
        key.append(blackQueenside ? 'q' : '-');
        key.append(enPassantTarget != null && isEnPassantCapturePossible(board, sideToMove, enPassantTarget)
            ? enPassantTarget.toAlgebraic() : "-");
        return key.toString();
    }

    /**
     * 手番側が、指定のアンパッサン対象マスへ実際に合法手として取りに行けるかを返す。
     * FIDE 9.2.3 は、アンパッサンが実際に可能な場合にのみ局面を区別すると定める。
     * 対象マスが設定されているだけで区別すると、ポーンを2マス進めた直後の局面が
     * 「取れないのに別局面」として数えられ、同一局面への復帰が千日手に数えられない。
     * キングを王手に晒す（ピンされた）ポーンによる捕獲は合法手ではないので含めない。
     */
    private static boolean isEnPassantCapturePossible(Board board, Color sideToMove, Position enPassantTarget) {
        // 取る側のポーンは、2マス進んだポーンの隣（対象マスの1段手前側）に居る
        int pawnRow = enPassantTarget.getRow() + (sideToMove == Color.WHITE ? 1 : -1);
        if (pawnRow < 0 || pawnRow > 7) {
            return false;
        }
        for (int colOffset = -1; colOffset <= 1; colOffset += 2) {
            int pawnCol = enPassantTarget.getCol() + colOffset;
            if (pawnCol < 0 || pawnCol > 7) {
                continue;
            }
            Piece pawn = board.getPieceAt(Position.of(pawnRow, pawnCol));
            if (pawn == null || pawn.getType() != PieceType.PAWN || pawn.getColor() != sideToMove) {
                continue;
            }
            for (Move move : MOVE_VALIDATOR.getValidMoves(pawn, board, enPassantTarget)) {
                if (move.isEnPassant() && CHECKMATE_DETECTOR.isLegalMove(move, pawn, board, sideToMove)) {
                    return true;
                }
            }
        }
        return false;
    }
}
