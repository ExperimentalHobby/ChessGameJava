package com.chessgame;

import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InteractiveGame} の結合テスト。標準入力にコマンド列を差し込んで
 * {@code start()} を実行し、{@link com.chessgame.game.core.ChessGame} の状態が
 * 期待通りに変化することを検証する。
 */
class InteractiveGameTest {
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream capturedOutput;

    @BeforeEach
    void suppressOutput() {
        capturedOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOutput, false, StandardCharsets.UTF_8));
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
    void undoCommandUndoesBothHumanAndAiMovesInAiGame() {
        InteractiveGame game = runWithInput("1\ne2e4\nu\nquit\n");

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

    @Test
    void saveCommandWritesPgnFileContainingMoveText(@TempDir Path tempDir) throws IOException {
        Path pgnFile = tempDir.resolve("game.pgn");

        runWithInput("0\ne2e4\nsave " + pgnFile + "\nquit\n");

        String content = Files.readString(pgnFile);
        assertThat(content).contains("1. e4");
    }

    @Test
    void loadCommandRestoresPositionFromPgnFile(@TempDir Path tempDir) throws IOException {
        Path pgnFile = tempDir.resolve("game.pgn");
        Files.writeString(pgnFile, "[Result \"*\"]\n\n1. e4 e5 2. Nf3 *");

        InteractiveGame game = runWithInput("0\nload " + pgnFile + "\nquit\n");

        assertThat(game.getGame().toFen())
            .isEqualTo("rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2");
    }

    @Test
    void loadCommandWithNonexistentFileShowsErrorAndKeepsCurrentGame(@TempDir Path tempDir) {
        Path missingFile = tempDir.resolve("does-not-exist.pgn");

        InteractiveGame game = runWithInput("0\ne2e4\nload " + missingFile + "\nquit\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("Failed to load");
        // loadに失敗しても直前の対局(1手指した状態)がそのまま残ること
        assertThat(game.getGame().getMoveHistory().size()).isEqualTo(1);
        assertThat(game.getGame().getBoard().getPieceAt(Position.of("e4"))).isNotNull();
    }

    @Test
    void fenCommandPrintsCurrentFenToConsole() {
        runWithInput("0\ne2e4\nfen\nquit\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8))
            .contains("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
    }
}
