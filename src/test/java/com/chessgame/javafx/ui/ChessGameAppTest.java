package com.chessgame.javafx.ui;

import com.chessgame.gamestate.model.GameState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ChessGameApp#shouldScheduleAiMove} の単体テスト。
 * {@code ChessGameApp} は {@code Application} を継承しており、JavaFX Toolkit の
 * 初期化（ヘッドレスCIでは行えない）が必要になるため、このテストでは
 * インスタンス化しない。
 */
class ChessGameAppTest {

    // Issue #165: 王手中も次のAIの手をスケジュールすべき（Swing版と同じセマンティクス）
    @Test
    void schedulesAiMoveWhenGameIsInCheck() {
        boolean shouldSchedule = ChessGameApp.shouldScheduleAiMove(true, GameState.GameStatus.CHECK);

        assertThat(shouldSchedule).isTrue();
    }

    @Test
    void schedulesAiMoveWhenGameIsInProgress() {
        boolean shouldSchedule = ChessGameApp.shouldScheduleAiMove(true, GameState.GameStatus.IN_PROGRESS);

        assertThat(shouldSchedule).isTrue();
    }

    @Test
    void doesNotScheduleAiMoveWhenNotAiGame() {
        boolean shouldSchedule = ChessGameApp.shouldScheduleAiMove(false, GameState.GameStatus.CHECK);

        assertThat(shouldSchedule).isFalse();
    }

    @Test
    void doesNotScheduleAiMoveWhenGameIsOver() {
        boolean shouldSchedule = ChessGameApp.shouldScheduleAiMove(true, GameState.GameStatus.CHECKMATE);

        assertThat(shouldSchedule).isFalse();
    }
}
