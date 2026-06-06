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

package com.chessgame.game;

import com.chessgame.model.Color;
import com.chessgame.model.GameState;
import com.chessgame.model.board.Position;
import com.chessgame.model.move.Move;
import com.chessgame.model.piece.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class ChessGameTest {
    private ChessGame game;

    @BeforeEach
    public void setUp() {
        game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
    }

    @Test
    public void testGameInitialization() {
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.IN_PROGRESS);
        assertThat(game.isGameOver()).isFalse();
    }

    @Test
    public void testWhitePawnMove() {
        boolean moved = game.makeMove(Position.of("e2"), Position.of("e4"));
        assertThat(moved).isTrue();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
    }

    @Test
    public void testInvalidMove() {
        boolean moved = game.makeMove(Position.of("e4"), Position.of("e5"));
        assertThat(moved).isFalse();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    public void testSimpleGame() {
        // 1. e4
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        // 1... e5
        assertThat(game.makeMove(Position.of("e7"), Position.of("e5"))).isTrue();
        // 2. Nf3
        assertThat(game.makeMove(Position.of("g1"), Position.of("f3"))).isTrue();
        // 2... Nc6
        assertThat(game.makeMove(Position.of("b8"), Position.of("c6"))).isTrue();

        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.IN_PROGRESS);
        assertThat(game.isGameOver()).isFalse();
    }

    @Test
    public void testGetAvailableMoves() {
        // White pawn at e2 should have moves to e3 or e4
        var moves = game.getAvailableMoves(Position.of("e2"));
        assertThat(moves).hasSize(2);
    }

    @Test
    public void testKnightMoves() {
        // Knight at g1 can move to f3 or h3
        var moves = game.getAvailableMoves(Position.of("g1"));
        assertThat(moves).hasSize(2);
    }

    @Test
    public void testPlayerSwitching() {
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
        game.makeMove(Position.of("e2"), Position.of("e4"));
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
        game.makeMove(Position.of("e7"), Position.of("e5"));
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    public void testGameObserver() {
        TestGameObserver observer = new TestGameObserver();
        game.addObserver(observer);

        game.makeMove(Position.of("e2"), Position.of("e4"));

        assertThat(observer.moveMadeCount).isEqualTo(1);
        assertThat(observer.boardChangedCount).isEqualTo(1);
    }

    @Test
    public void testPawnAutoPromotesToQueen() {
        // 昇格先を指定しない makeMove(from, to) はクイーンへ自動昇格する。
        // 連続した捕獲でポーンを a8 まで進めて昇格させる。
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("d5"))).isTrue(); // exd5
        assertThat(game.makeMove(Position.of("c7"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("d5"), Position.of("c6"))).isTrue(); // dxc6
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("c6"), Position.of("b7"))).isTrue(); // cxb7
        assertThat(game.makeMove(Position.of("a6"), Position.of("a5"))).isTrue();
        assertThat(game.makeMove(Position.of("b7"), Position.of("a8"))).isTrue(); // bxa8=Q

        var promoted = game.getBoard().getPieceAt(Position.of("a8"));
        assertThat(promoted).isNotNull();
        assertThat(promoted.getType()).isEqualTo(PieceType.QUEEN);
        assertThat(promoted.getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    public void testPawnPromotesToSpecifiedPiece() {
        // 昇格先を明示した場合はその駒種に昇格する。
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("c7"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("d5"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("c6"), Position.of("b7"))).isTrue();
        assertThat(game.makeMove(Position.of("a6"), Position.of("a5"))).isTrue();
        assertThat(game.makeMove(Position.of("b7"), Position.of("a8"), PieceType.KNIGHT)).isTrue();

        var promoted = game.getBoard().getPieceAt(Position.of("a8"));
        assertThat(promoted).isNotNull();
        assertThat(promoted.getType()).isEqualTo(PieceType.KNIGHT);
    }

    @Test
    public void testResign() {
        assertThat(game.isGameOver()).isFalse();
        game.resign(Color.WHITE);
        assertThat(game.isGameOver()).isTrue();
        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.WHITE_RESIGNED);
    }

    // Test observer implementation
    private static class TestGameObserver implements GameObserver {
        int moveMadeCount = 0;
        int boardChangedCount = 0;

        @Override
        public void onBoardChanged() {
            boardChangedCount++;
        }

        @Override
        public void onMoveMade(Move move) {
            moveMadeCount++;
        }

        @Override
        public void onGameStateChanged(GameState.GameStatus newStatus) {}

        @Override
        public void onCheckDetected(Color kingColor) {}

        @Override
        public void onGameOver(Color winner) {}
    }
}
