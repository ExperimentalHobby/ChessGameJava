package com.chessgame;

import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InteractiveGame} の結合テスト。標準入力にコマンド列を差し込んで
 * {@code start()} を実行し、{@link com.chessgame.game.core.ChessGame} の状態が
 * 期待通りに変化することを検証する。
 */
class InteractiveGameTest {
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void suppressOutput() {
        System.setOut(new PrintStream(new ByteArrayOutputStream(), false, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    /** 入力スクリプトを標準入力に差し込み、Human vs Human で InteractiveGame を実行する。 */
    private InteractiveGame runWithInput(String script) {
        System.setIn(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)));
        InteractiveGame game = new InteractiveGame();
        game.start();
        return game;
    }

    @Test
    void legalMoveInputUpdatesBoardAndSwitchesTurn() {
        InteractiveGame game = runWithInput("0\ne2e4\nquit\n");

        assertThat(game.getGame().getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
        assertThat(game.getGame().getMoveHistory().size()).isEqualTo(1);
        assertThat(game.getGame().getBoard().getPieceAt(Position.of("e4"))).isNotNull();
    }

    @Test
    void undoCommandUndoesLastMove() {
        InteractiveGame game = runWithInput("0\ne2e4\nu\nquit\n");

        assertThat(game.getGame().getMoveHistory().isEmpty()).isTrue();
        assertThat(game.getGame().getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    void resignConfirmedEndsGameWithOpponentWinning() {
        InteractiveGame game = runWithInput("0\nr\ny\n");

        assertThat(game.getGame().isGameOver()).isTrue();
        assertThat(game.getGame().getGameStatus())
            .isEqualTo(com.chessgame.gamestate.model.GameState.GameStatus.WHITE_RESIGNED);
    }

    @Test
    void illegalMoveInputDoesNotChangeBoardOrTurn() {
        // e2 から e5 へは3マス移動となり非合法
        InteractiveGame game = runWithInput("0\ne2e5\nquit\n");

        assertThat(game.getGame().getMoveHistory().isEmpty()).isTrue();
        assertThat(game.getGame().getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    void newGameCommandResetsMoveHistory() {
        InteractiveGame game = runWithInput("0\ne2e4\nn\nquit\n");

        assertThat(game.getGame().getMoveHistory().isEmpty()).isTrue();
        assertThat(game.getGame().getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
    }
}
