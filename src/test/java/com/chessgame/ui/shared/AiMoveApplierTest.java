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

package com.chessgame.ui.shared;

import com.chessgame.board.model.Position;
import com.chessgame.game.core.ChessGame;
import com.chessgame.move.model.Move;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AiMoveApplier} のユニットテスト。
 * Swing版（{@code SwingChessGameFrame}）・JavaFX版（{@code ChessGameApp}）の両方で
 * 重複していたテストを統合したもの。AI思考中に New Game・キャンセルが発生した場合に
 * 古い着手が適用されないこと、通常時は {@link ChessGame#makeMove} まで正しく連携する
 * ことを検証する。
 */
class AiMoveApplierTest {

    @Test
    void appliesMoveWhenGameUnchangedAndNotCancelled() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        boolean applied = AiMoveApplier.applyAiMoveIfStillValid(game, game, move, false);

        assertThat(applied).isTrue();
        assertThat(game.getBoard().getPieceAt(Position.of("e4"))).isNotNull();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(com.chessgame.model.Color.BLACK);
    }

    @Test
    void doesNotApplyMoveWhenGameInstanceWasReplaced() {
        // New Game 等で参照している game が別インスタンスに差し替わったケースを再現する
        ChessGame gameAtStart = ChessGame.createTwoPlayerGame("White", "Black");
        gameAtStart.startNewGame();
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        ChessGame currentGame = ChessGame.createTwoPlayerGame("Alice", "Bob");
        currentGame.startNewGame();

        boolean applied = AiMoveApplier.applyAiMoveIfStillValid(gameAtStart, currentGame, move, false);

        assertThat(applied).isFalse();
        assertThat(currentGame.getMoveHistory().isEmpty()).isTrue();
        assertThat(currentGame.getCurrentPlayer().getColor()).isEqualTo(com.chessgame.model.Color.WHITE);
    }

    @Test
    void doesNotApplyMoveWhenTaskWasCancelled() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        boolean applied = AiMoveApplier.applyAiMoveIfStillValid(game, game, move, true);

        assertThat(applied).isFalse();
        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }

    @Test
    void doesNotApplyMoveAndDoesNotThrowWhenMoveIsNull() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();

        boolean applied = AiMoveApplier.applyAiMoveIfStillValid(game, game, null, false);

        assertThat(applied).isFalse();
        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }
}
