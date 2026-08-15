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

package com.chessgame.ui.shared.dialog;

import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link GameModeSelection} のユニットテスト。
 * Swing版・JavaFX版の両 {@code GameModeDialog} で重複していた解決ロジックを統合したもの。
 */
class GameModeSelectionTest {

    @Test
    void testResolveHumanVsHumanIsNotAiGame() {
        GameModeSelection.Result result = GameModeSelection.resolve(0, 0);

        assertThat(result.aiGame()).isFalse();
        assertThat(result.game().getCurrentPlayer().isAI()).isFalse();
        assertThat(result.game().getBlackPlayer().isAI()).isFalse();
    }

    @Test
    void testResolveEasyDifficultyIsAiGame() {
        GameModeSelection.Result result = GameModeSelection.resolve(1, 0);

        assertThat(result.aiGame()).isTrue();
        Player black = result.game().getBlackPlayer();
        assertThat(black).isInstanceOf(AIPlayer.class);
        assertThat(((AIPlayer) black).getDifficulty()).isEqualTo(1);
    }

    @Test
    void testResolveMediumDifficulty() {
        GameModeSelection.Result result = GameModeSelection.resolve(2, 0);

        assertThat(result.aiGame()).isTrue();
        assertThat(((AIPlayer) result.game().getBlackPlayer()).getDifficulty()).isEqualTo(2);
    }

    @Test
    void testResolveHardDifficulty() {
        GameModeSelection.Result result = GameModeSelection.resolve(3, 0);

        assertThat(result.aiGame()).isTrue();
        assertThat(((AIPlayer) result.game().getBlackPlayer()).getDifficulty()).isEqualTo(3);
    }

    @Test
    void testResolveExpertDifficulty() {
        GameModeSelection.Result result = GameModeSelection.resolve(4, 0);

        assertThat(result.aiGame()).isTrue();
        assertThat(((AIPlayer) result.game().getBlackPlayer()).getDifficulty()).isEqualTo(4);
    }

    @Test
    void testResolveWithTimeChoiceUnlimitedHasNoTimeControl() {
        GameModeSelection.Result result = GameModeSelection.resolve(0, 0);

        assertThat(result.game().hasTimeControl()).isFalse();
    }

    @Test
    void testResolveWithTimeChoiceBlitzGivesBothPlayersBlitzTime() {
        GameModeSelection.Result result = GameModeSelection.resolve(0, 1);

        assertThat(result.game().hasTimeControl()).isTrue();
        assertRemainingMillisCloseTo(3 * 60_000L, result.game().getRemainingMillis(Color.WHITE));
        assertRemainingMillisCloseTo(3 * 60_000L, result.game().getRemainingMillis(Color.BLACK));
    }

    @Test
    void testResolveWithTimeChoiceRapidGivesBothPlayersRapidTime() {
        GameModeSelection.Result result = GameModeSelection.resolve(0, 2);

        assertThat(result.game().hasTimeControl()).isTrue();
        assertRemainingMillisCloseTo(10 * 60_000L, result.game().getRemainingMillis(Color.WHITE));
    }

    @Test
    void testResolveWithTimeChoiceClassicalGivesBothPlayersClassicalTime() {
        GameModeSelection.Result result = GameModeSelection.resolve(0, 3);

        assertThat(result.game().hasTimeControl()).isTrue();
        assertRemainingMillisCloseTo(60 * 60_000L, result.game().getRemainingMillis(Color.WHITE));
    }

    @Test
    void testResolveWithUnknownTimeChoiceBehavesLikeUnlimited() {
        GameModeSelection.Result result = GameModeSelection.resolve(0, 99);

        assertThat(result.game().hasTimeControl()).isFalse();
    }

    @Test
    void testResolveWithTimeChoiceAndAiDifficultyCombinesBoth() {
        GameModeSelection.Result result = GameModeSelection.resolve(2, 1);

        assertThat(result.aiGame()).isTrue();
        assertThat(((AIPlayer) result.game().getBlackPlayer()).getDifficulty()).isEqualTo(2);
        assertThat(result.game().hasTimeControl()).isTrue();
        assertRemainingMillisCloseTo(3 * 60_000L, result.game().getRemainingMillis(Color.WHITE));
    }

    @Test
    void testResolveAiGameWhitePlayerIsNamedYou() {
        GameModeSelection.Result result = GameModeSelection.resolve(1, 0);

        assertThat(result.game().getCurrentPlayer().getName()).isEqualTo("You");
    }

    @Test
    void testResolveHumanVsHumanWhitePlayerIsNamedWhite() {
        GameModeSelection.Result result = GameModeSelection.resolve(0, 0);

        assertThat(result.game().getCurrentPlayer().getName()).isEqualTo("White");
    }

    /**
     * {@link ChessGame#getRemainingMillis(Color)} は現在の手番であれば実経過時間を
     * 差し引くライブ値を返すため、生成直後でも実行環境の遅延次第で初期値と
     * 完全一致しないことがある。初期値を超えないこと・誤差が1秒以内であることを検証する。
     */
    private static void assertRemainingMillisCloseTo(long expectedMillis, long actualMillis) {
        assertThat(actualMillis).isLessThanOrEqualTo(expectedMillis);
        assertThat(expectedMillis - actualMillis).isLessThanOrEqualTo(1000);
    }
}
