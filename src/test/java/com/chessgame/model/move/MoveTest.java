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

package com.chessgame.model.move;

import com.chessgame.model.board.Position;
import com.chessgame.model.piece.Pawn;
import com.chessgame.model.Color;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class MoveTest {

    @Test
    public void testNormalMove() {
        Position from = Position.of("e2");
        Position to = Position.of("e4");
        Move move = Move.normal(from, to);

        assertThat(move.getFrom()).isEqualTo(from);
        assertThat(move.getTo()).isEqualTo(to);
        assertThat(move.getMoveType()).isEqualTo(MoveType.NORMAL);
        assertThat(move.isCapture()).isFalse();
    }

    @Test
    public void testCaptureMove() {
        Position from = Position.of("e4");
        Position to = Position.of("d5");
        Pawn capturedPawn = new Pawn(Color.BLACK, to);
        Move move = Move.capture(from, to, capturedPawn);

        assertThat(move.getMoveType()).isEqualTo(MoveType.CAPTURE);
        assertThat(move.getCapturedPiece()).isEqualTo(capturedPawn);
        assertThat(move.isCapture()).isTrue();
    }

    @Test
    public void testCastlingMove() {
        Position from = Position.of("e1");
        Position to = Position.of("g1");
        Move move = Move.castling(from, to);

        assertThat(move.getMoveType()).isEqualTo(MoveType.CASTLING);
        assertThat(move.isCastling()).isTrue();
    }

    @Test
    public void testMoveEquality() {
        Move move1 = Move.normal(Position.of("e2"), Position.of("e4"));
        Move move2 = Move.normal(Position.of("e2"), Position.of("e4"));

        assertThat(move1).isEqualTo(move2);
        assertThat(move1.hashCode()).isEqualTo(move2.hashCode());
    }

    @Test
    public void testMoveToString() {
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));
        assertThat(move.toString()).isEqualTo("e2e4");
    }
}
