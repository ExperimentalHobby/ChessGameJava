package com.chessgame.swing.ui;

import com.chessgame.gamestate.model.GameState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SwingChessGameFrame#shouldScheduleAiMove} の単体テスト。
 * {@code JFrame} はヘッドレス環境（Linux CI）で {@code HeadlessException} を
 * 起こすため、このテストでは一切インスタンス化しない。
 */
class SwingChessGameFrameTest {

    // Issue #67: 人間の着手でAI側キングがCHECKになった場合も次のAIの手をスケジュールすべき
    @Test
    void schedulesAiMoveWhenGameIsInCheck() {
        boolean shouldSchedule = SwingChessGameFrame.shouldScheduleAiMove(true, GameState.GameStatus.CHECK);

        assertThat(shouldSchedule).isTrue();
    }

    @Test
    void schedulesAiMoveWhenGameIsInProgress() {
        boolean shouldSchedule = SwingChessGameFrame.shouldScheduleAiMove(true, GameState.GameStatus.IN_PROGRESS);

        assertThat(shouldSchedule).isTrue();
    }

    @Test
    void doesNotScheduleAiMoveWhenNotAiGame() {
        boolean shouldSchedule = SwingChessGameFrame.shouldScheduleAiMove(false, GameState.GameStatus.CHECK);

        assertThat(shouldSchedule).isFalse();
    }

    @Test
    void doesNotScheduleAiMoveWhenGameIsOver() {
        boolean shouldSchedule = SwingChessGameFrame.shouldScheduleAiMove(true, GameState.GameStatus.CHECKMATE);

        assertThat(shouldSchedule).isFalse();
    }
}
