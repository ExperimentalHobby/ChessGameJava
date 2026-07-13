package com.chessgame.swing.ui;

import com.chessgame.board.model.Position;
import com.chessgame.game.core.ChessGame;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.move.model.Move;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SwingChessGameFrame#applyAiMoveIfStillValid} の結合テスト。
 * AI思考中に New Game・キャンセルが発生した場合に古い着手が適用されないこと、
 * 通常時は {@link ChessGame#makeMove} まで正しく連携することを検証する。
 * {@code JFrame} はヘッドレス環境（Linux CI）で {@code HeadlessException} を
 * 起こすため、このテストでは一切インスタンス化しない。
 */
class SwingChessGameFrameTest {

    @Test
    void appliesMoveWhenGameUnchangedAndNotCancelled() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        boolean applied = SwingChessGameFrame.applyAiMoveIfStillValid(game, game, move, false);

        assertThat(applied).isTrue();
        assertThat(game.getBoard().getPieceAt(Position.of("e4"))).isNotNull();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(com.chessgame.model.Color.BLACK);
    }

    @Test
    void doesNotApplyMoveWhenGameInstanceWasReplaced() {
        // New Game 等で SwingChessGameFrame.game が別インスタンスに差し替わったケースを再現する
        ChessGame gameAtStart = ChessGame.createTwoPlayerGame("White", "Black");
        gameAtStart.startNewGame();
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        ChessGame currentGame = ChessGame.createTwoPlayerGame("Alice", "Bob");
        currentGame.startNewGame();

        boolean applied = SwingChessGameFrame.applyAiMoveIfStillValid(gameAtStart, currentGame, move, false);

        assertThat(applied).isFalse();
        assertThat(currentGame.getMoveHistory().isEmpty()).isTrue();
        assertThat(currentGame.getCurrentPlayer().getColor()).isEqualTo(com.chessgame.model.Color.WHITE);
    }

    @Test
    void doesNotApplyMoveWhenWorkerWasCancelled() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        boolean applied = SwingChessGameFrame.applyAiMoveIfStillValid(game, game, move, true);

        assertThat(applied).isFalse();
        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }

    @Test
    void doesNotApplyMoveAndDoesNotThrowWhenMoveIsNull() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();

        boolean applied = SwingChessGameFrame.applyAiMoveIfStillValid(game, game, null, false);

        assertThat(applied).isFalse();
        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }

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
