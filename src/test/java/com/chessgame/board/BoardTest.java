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

package com.chessgame.board;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import com.chessgame.piece.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class BoardTest {
    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();
    }

    @Test
    public void testBoardInitialization() {
        // Verify white pieces are in correct positions
        assertThat(board.getPieceAt(Position.of("a1"))).isInstanceOf(Rook.class);
        assertThat(board.getPieceAt(Position.of("e1"))).isInstanceOf(King.class);
        assertThat(board.getPieceAt(Position.of("d1"))).isInstanceOf(Queen.class);

        // Verify black pieces
        assertThat(board.getPieceAt(Position.of("a8"))).isInstanceOf(Rook.class);
        assertThat(board.getPieceAt(Position.of("e8"))).isInstanceOf(King.class);
        assertThat(board.getPieceAt(Position.of("d8"))).isInstanceOf(Queen.class);
    }

    @Test
    public void testWhitePawns() {
        for (int col = 0; col < 8; col++) {
            Piece piece = board.getPieceAt(Position.of(6, col));
            assertThat(piece).isInstanceOf(Pawn.class);
            assertThat(piece.getColor()).isEqualTo(Color.WHITE);
        }
    }

    @Test
    public void testBlackPawns() {
        for (int col = 0; col < 8; col++) {
            Piece piece = board.getPieceAt(Position.of(1, col));
            assertThat(piece).isInstanceOf(Pawn.class);
            assertThat(piece.getColor()).isEqualTo(Color.BLACK);
        }
    }

    @Test
    public void testKingPositions() {
        Position whiteKingPos = board.getKingPosition(Color.WHITE);
        Position blackKingPos = board.getKingPosition(Color.BLACK);

        assertThat(whiteKingPos).isEqualTo(Position.of("e1"));
        assertThat(blackKingPos).isEqualTo(Position.of("e8"));
    }

    @Test
    public void testEmptySquares() {
        assertThat(board.isPieceAt(Position.of("e4"))).isFalse();
        assertThat(board.getPieceAt(Position.of("e4"))).isNull();
    }

    @Test
    public void testPiecePlacement() {
        Position pos = Position.of("e4");
        Piece pawn = new Pawn(Color.WHITE, pos);
        board.placePiece(pawn, pos);

        assertThat(board.isPieceAt(pos)).isTrue();
        assertThat(board.getPieceAt(pos)).isEqualTo(pawn);
    }

    @Test
    public void testPieceRemoval() {
        Position pos = Position.of("e4");
        Piece pawn = new Pawn(Color.WHITE, pos);
        board.placePiece(pawn, pos);
        board.removePiece(pos);

        assertThat(board.isPieceAt(pos)).isFalse();
    }

    @Test
    public void testBoardClone() {
        Board clonedBoard = board.clone();

        // Verify white pieces
        assertThat(clonedBoard.getPieceAt(Position.of("e1")))
            .isInstanceOf(King.class)
            .isNotSameAs(board.getPieceAt(Position.of("e1")));

        // Verify black pieces
        assertThat(clonedBoard.getPieceAt(Position.of("e8")))
            .isInstanceOf(King.class)
            .isNotSameAs(board.getPieceAt(Position.of("e8")));
    }

    @Test
    public void testResetBoard() {
        board.removePiece(Position.of("e4"));
        board.resetBoard();

        assertThat(board.getPieceAt(Position.of("e1"))).isInstanceOf(King.class);
        assertThat(board.getPieceAt(Position.of("e8"))).isInstanceOf(King.class);
    }

    @Test
    public void testGetAllWhitePieces() {
        assertThat(board.getAllPieces(Color.WHITE)).hasSize(16);
    }

    @Test
    public void testGetAllBlackPieces() {
        assertThat(board.getAllPieces(Color.BLACK)).hasSize(16);
    }
}
