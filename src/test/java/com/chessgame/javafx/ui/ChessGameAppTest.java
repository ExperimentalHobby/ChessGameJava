package com.chessgame.javafx.ui;

import com.chessgame.board.model.Position;
import com.chessgame.game.core.ChessGame;
import com.chessgame.move.model.Move;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ChessGameApp#applyAiMoveIfStillValid} の結合テスト。
 * Swing版（{@code SwingChessGameFrameTest}）と対をなす。AI思考中に New Game・
 * キャンセルが発生した場合に古い着手が適用されないこと、通常時は
 * {@link ChessGame#makeMove} まで正しく連携することを検証する。
 * {@code ChessGameApp} は {@code Application} を継承しており、JavaFX Toolkit の
 * 初期化（ヘッドレスCIでは行えない）が必要になるため、このテストでは
 * インスタンス化しない。
 */
class ChessGameAppTest {

    @Test
    void appliesMoveWhenGameUnchangedAndNotCancelled() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        boolean applied = ChessGameApp.applyAiMoveIfStillValid(game, game, move, false);

        assertThat(applied).isTrue();
        assertThat(game.getBoard().getPieceAt(Position.of("e4"))).isNotNull();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(com.chessgame.model.Color.BLACK);
    }

    @Test
    void doesNotApplyMoveWhenGameInstanceWasReplaced() {
        // New Game 等で ChessGameApp.game が別インスタンスに差し替わったケースを再現する
        ChessGame gameAtStart = ChessGame.createTwoPlayerGame("White", "Black");
        gameAtStart.startNewGame();
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        ChessGame currentGame = ChessGame.createTwoPlayerGame("Alice", "Bob");
        currentGame.startNewGame();

        boolean applied = ChessGameApp.applyAiMoveIfStillValid(gameAtStart, currentGame, move, false);

        assertThat(applied).isFalse();
        assertThat(currentGame.getMoveHistory().isEmpty()).isTrue();
        assertThat(currentGame.getCurrentPlayer().getColor()).isEqualTo(com.chessgame.model.Color.WHITE);
    }

    @Test
    void doesNotApplyMoveWhenTaskWasCancelled() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        boolean applied = ChessGameApp.applyAiMoveIfStillValid(game, game, move, true);

        assertThat(applied).isFalse();
        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }

    @Test
    void doesNotApplyMoveAndDoesNotThrowWhenMoveIsNull() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();

        boolean applied = ChessGameApp.applyAiMoveIfStillValid(game, game, null, false);

        assertThat(applied).isFalse();
        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }
}
