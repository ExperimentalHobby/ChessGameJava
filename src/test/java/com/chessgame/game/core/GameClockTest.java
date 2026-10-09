package com.chessgame.game.core;

import com.chessgame.gamestate.model.GameState;
import com.chessgame.gamestate.model.TimeControl;
import com.chessgame.model.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link GameClock} のユニットテスト。注入した偽の現在時刻で、実時間に依存せず検証する。
 */
class GameClockTest {
    private final long[] now = {1_000_000L};
    private GameState state;
    private GameClock clock;

    @BeforeEach
    void setUp() {
        state = new GameState();
        clock = new GameClock(new TimeControl(180_000L, 2_000L), () -> now[0]);
        clock.reset(state);
    }

    @Test
    void isDisabledWithoutTimeControl() {
        GameClock untimed = new GameClock(null, () -> now[0]);

        assertThat(untimed.isEnabled()).isFalse();
        assertThat(clock.isEnabled()).isTrue();
    }

    @Test
    void resetGivesBothSidesTheInitialTime() {
        assertThat(state.getRemainingMillis(Color.WHITE)).isEqualTo(180_000L);
        assertThat(state.getRemainingMillis(Color.BLACK)).isEqualTo(180_000L);
    }

    @Test
    void liveRemainingSubtractsElapsedThinkingTimeOnlyFromTheSideToMove() {
        now[0] += 5_000L;

        assertThat(clock.liveRemainingMillis(state, Color.WHITE)).isEqualTo(175_000L);
        assertThat(clock.liveRemainingMillis(state, Color.BLACK)).isEqualTo(180_000L);
    }

    @Test
    void chargeMoveConsumesElapsedTimeAndAddsIncrementThenStartsNextTurn() {
        now[0] += 5_000L;

        clock.chargeMove(state, Color.WHITE);

        assertThat(state.getRemainingMillis(Color.WHITE)).isEqualTo(180_000L - 5_000L + 2_000L);
        // 思考開始時刻が進むので、直後の経過は0
        assertThat(clock.isExpired(state, Color.WHITE)).isFalse();
    }

    @Test
    void chargeMoveIsNoOpWithoutTimeControl() {
        GameState untimedState = new GameState();
        GameClock untimed = new GameClock(null, () -> now[0]);
        now[0] += 5_000L;

        untimed.chargeMove(untimedState, Color.WHITE);

        assertThat(untimedState.getRemainingMillis(Color.WHITE)).isZero();
    }

    @Test
    void isExpiredWhenElapsedReachesRemainingTime() {
        now[0] += 179_999L;
        assertThat(clock.isExpired(state, Color.WHITE)).isFalse();

        now[0] += 1L;
        assertThat(clock.isExpired(state, Color.WHITE)).isTrue();
    }

    @Test
    void restartTurnForgetsElapsedTimeWithoutRewindingRemainingTime() {
        now[0] += 100_000L;

        clock.restartTurn();

        assertThat(clock.liveRemainingMillis(state, Color.WHITE)).isEqualTo(180_000L);
    }

    @Test
    void resetRestoresInitialTimeAndRestartsTurn() {
        now[0] += 100_000L;
        clock.chargeMove(state, Color.WHITE);
        now[0] += 50_000L;

        clock.reset(state);

        assertThat(state.getRemainingMillis(Color.WHITE)).isEqualTo(180_000L);
        assertThat(clock.liveRemainingMillis(state, Color.WHITE)).isEqualTo(180_000L);
    }
}
