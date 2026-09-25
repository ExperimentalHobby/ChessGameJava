package com.chessgame;

import com.chessgame.board.model.Position;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import com.chessgame.move.model.Move;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.piece.model.PieceType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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
            .isEqualTo(GameState.GameStatus.WHITE_RESIGNED);
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
    void saveCommandDoesNotDoubleUpUppercaseExtension(@TempDir Path tempDir) throws IOException {
        // Issue #239: 拡張子判定が大文字小文字を区別すると game.PGN が game.PGN.pgn になる
        Path pgnFile = tempDir.resolve("game.PGN");

        runWithInput("0\ne2e4\nsave " + pgnFile + "\nquit\n");

        assertThat(Files.exists(pgnFile)).isTrue();
        assertThat(Files.exists(tempDir.resolve("game.PGN.pgn"))).isFalse();
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
    void executeAIMoveDoesNothingWhenCurrentPlayerIsNotAnAiPlayer() {
        // isAI()==true だが AIPlayer型ではない Player（Issue #189 が再現する不正な状態）
        ChessGame fakeAiGame = new ChessGame(
            Player.human(Color.WHITE, "White"),
            new Player(Color.BLACK, "AI", false));
        fakeAiGame.startNewGame();
        assertThat(fakeAiGame.makeMove(Position.of("e2"), Position.of("e4"))).isTrue(); // 手番を黒(偽AI)に渡す

        InteractiveGame game = new InteractiveGame();
        game.setGameForTesting(fakeAiGame);

        game.executeAIMove();

        // AIPlayerではないため手は指されず、手番・履歴とも変化しないはず
        assertThat(fakeAiGame.getMoveHistory().size()).isEqualTo(1);
        assertThat(fakeAiGame.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
    }

    @Test
    void endOfInputTerminatesGracefullyInsteadOfThrowing() {
        // Issue #238: hasNextLine() を確認せずに nextLine() を呼ぶため、パイプ入力が
        // 尽きた時点で NoSuchElementException が送出されて異常終了していた。
        // quit を打たずに入力が終わるスクリプトで再現する
        InteractiveGame game = runWithInput("0\ne2e4\n");

        assertThat(game.isRunningForTesting()).isFalse();
        assertThat(game.getGame().getMoveHistory().size()).isEqualTo(1);
    }

    @Test
    void endOfInputAtGameModePromptTerminatesGracefully() {
        // モード選択の時点で入力が尽きるケース（最初の nextLine() で EOF）
        InteractiveGame game = runWithInput("");

        assertThat(game.isRunningForTesting()).isFalse();
    }

    @Test
    void executeAIMoveStopsLoopWhenAiCannotChooseAMove() {
        // Issue #237: selectMove() が null を返すと何も進まないまま戻るため、
        // 呼び出し元の while ループが「1秒待って何もしない」を延々と繰り返す
        ChessGame stuckAiGame = new ChessGame(
            Player.human(Color.WHITE, "White"),
            new NullMoveAiPlayer(Color.BLACK));
        stuckAiGame.startNewGame();
        assertThat(stuckAiGame.makeMove(Position.of("e2"), Position.of("e4"))).isTrue(); // 手番をAIへ渡す

        InteractiveGame game = new InteractiveGame();
        game.setGameForTesting(stuckAiGame);

        game.executeAIMove();

        assertThat(game.isRunningForTesting()).isFalse();
        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("AI が手を選べませんでした");
    }

    /** selectMove() が常に null を返す AI。手が選べない状態を決定的に再現する。 */
    private static class NullMoveAiPlayer extends AIPlayer {
        NullMoveAiPlayer(Color color) {
            super("AI", color, 1);
        }

        @Override
        public Move selectMove(ChessGame game) {
            return null;
        }
    }

    @Test
    void fenCommandPrintsCurrentFenToConsole() {
        runWithInput("0\ne2e4\nfen\nquit\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8))
            .contains("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
    }

    // ===== コンソールコマンドの網羅（Issue #249） =====

    @Test
    void helpAndBoardCommandsPrintOutput() {
        runWithInput("0\nhelp\n?\nboard\nb\nquit\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("board(b)  - Display current board");
    }

    @Test
    void movesCommandPrintsPlaceholderWhenEmptyThenHistory() {
        runWithInput("0\nm\ne2e4\nmoves\nquit\n");

        String out = capturedOutput.toString(StandardCharsets.UTF_8);
        assertThat(out).contains("No moves yet.");
        assertThat(out).contains("Move History:");
    }

    @Test
    void undoWithoutHistoryPrintsMessage() {
        runWithInput("0\nu\nquit\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("No moves to undo.");
    }

    @Test
    void emptyInputIsIgnoredAndGameContinues() {
        InteractiveGame game = runWithInput("0\n\n   \ne2e4\nquit\n");

        assertThat(game.getGame().getMoveHistory().size()).isEqualTo(1);
    }

    @Test
    void unknownCommandPrintsHint() {
        runWithInput("0\nxyz\nquit\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("Invalid input.");
    }

    @Test
    void malformedFourCharacterMoveIsReportedAsFormatError() {
        // 4文字は指し手として解釈されるが、Position に変換できない
        runWithInput("0\nzzzz\nquit\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("Invalid format.");
    }

    @Test
    void illegalMoveIsRejectedWithoutChangingTheBoard() {
        InteractiveGame game = runWithInput("0\ne2e5\nquit\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("Invalid move.");
        assertThat(game.getGame().getMoveHistory().isEmpty()).isTrue();
    }

    @Test
    void resignCommandDeclinedKeepsGameRunning() {
        InteractiveGame game = runWithInput("0\nr\nn\ne2e4\nquit\n");

        assertThat(game.getGame().isGameOver()).isFalse();
        assertThat(game.getGame().getMoveHistory().size()).isEqualTo(1);
    }

    @Test
    void resignCommandEndsGameAndPrintsGameOverSummary() {
        // e2e4 の後は黒番。2人対戦では getResigningColor() が現在の手番を返すため黒が投了する
        InteractiveGame game = runWithInput("0\ne2e4\nresign\ny\n");

        assertThat(game.getGame().getGameStatus()).isEqualTo(GameState.GameStatus.BLACK_RESIGNED);
        String out = capturedOutput.toString(StandardCharsets.UTF_8);
        assertThat(out).contains("GAME OVER");
        assertThat(out).contains("Black resigned.");
    }

    @Test
    void checkmateEndsGameAndPrintsResult() {
        // Fool's mate: 1.f3 e5 2.g4 Qh4#
        InteractiveGame game = runWithInput("0\nf2f3\ne7e5\ng2g4\nd8h4\n");

        assertThat(game.getGame().getGameStatus()).isEqualTo(GameState.GameStatus.CHECKMATE);
        String out = capturedOutput.toString(StandardCharsets.UTF_8);
        assertThat(out).contains("CHECKMATE!");
        assertThat(out).contains("Black wins!");
    }

    @Test
    void threefoldRepetitionEndsGameAndPrintsDraw() {
        // ナイトの往復を2往復させて同一局面を3回出現させる
        String shuffle = "b1c3\nb8c6\nc3b1\nc6b8\n".repeat(2);
        InteractiveGame game = runWithInput("0\n" + shuffle);

        assertThat(game.getGame().getGameStatus()).isEqualTo(GameState.GameStatus.THREEFOLD_REPETITION);
        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("Threefold repetition.");
    }

    @Test
    void promotionPromptAppliesSelectedPieceType() {
        // b7 まで白ポーンを進め、a8 のルークを取りながら昇格する
        InteractiveGame game = runWithInput("0\ne2e4\nd7d5\ne4d5\nc7c6\nd5c6\na7a6\n"
            + "c6b7\na6a5\nb7a8\nr\nquit\n");

        var promoted = game.getGame().getBoard().getPieceAt(Position.of("a8"));
        assertThat(promoted).isNotNull();
        assertThat(promoted.getType()).isEqualTo(PieceType.ROOK);
        assertThat(promoted.getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    void saveToUnwritablePathReportsFailure(@TempDir Path tempDir) {
        // 存在しない中間ディレクトリを含むパスは書き込みに失敗する
        Path unwritable = tempDir.resolve("missing-dir").resolve("game.pgn");

        runWithInput("0\ne2e4\nsave " + unwritable + "\nquit\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("Failed to save");
    }

    @Test
    void loadOfInvalidPgnReportsFailureAndKeepsGame(@TempDir Path tempDir) throws IOException {
        Path broken = tempDir.resolve("broken.pgn");
        Files.writeString(broken, "[Result \"*\"]\n\n1. Qh8 *");

        InteractiveGame game = runWithInput("0\ne2e4\nload " + broken + "\nquit\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("Invalid PGN:");
        assertThat(game.getGame().getMoveHistory().size()).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({
        "'4k3/8/8/8/8/8/8/4K3 w - - 0 1', 'Insufficient material.'",
        "'7k/5Q2/6K1/8/8/8/8/8 b - - 0 1', 'STALEMATE!'",
        "'4k3/8/8/8/8/8/4R3/4K3 w - - 100 60', 'Fifty-move rule.'"
    })
    void loadedTerminalPositionPrintsMatchingGameOverReason(String fen, String expectedReason,
                                                             @TempDir Path tempDir) throws IOException {
        // 終局種別ごとの GAME OVER 表示は、対局を指して到達させるのが難しいものがある。
        // 終局済みの局面を FEN タグ付き PGN で読み込ませて分岐を通す
        Path pgnFile = tempDir.resolve("terminal.pgn");
        Files.writeString(pgnFile, "[FEN \"" + fen + "\"]\n[SetUp \"1\"]\n\n*");

        runWithInput("0\nload " + pgnFile + "\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains(expectedReason);
    }

    @Test
    void saveAndLoadWithoutFilenamePrintUsage() {
        runWithInput("0\nsave\nload\nquit\n");

        String out = capturedOutput.toString(StandardCharsets.UTF_8);
        assertThat(out).contains("Usage: save <filename>");
        assertThat(out).contains("Usage: load <filename>");
    }

    @Test
    void loadOfMissingFileReportsFailureAndKeepsGame(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("does-not-exist.pgn");

        InteractiveGame game = runWithInput("0\ne2e4\nload " + missing + "\nquit\n");

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("Failed to load");
        assertThat(game.getGame().getMoveHistory().size()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"b", "n", "q", "x"})
    void promotionPromptAcceptsEveryPieceChoice(String choice) {
        // 無効な入力("x")と明示的な"q"はいずれも既定のクイーンになる
        InteractiveGame game = runWithInput("0\ne2e4\nd7d5\ne4d5\nc7c6\nd5c6\na7a6\n"
            + "c6b7\na6a5\nb7a8\n" + choice + "\nquit\n");

        PieceType expected = switch (choice) {
            case "b" -> PieceType.BISHOP;
            case "n" -> PieceType.KNIGHT;
            default -> PieceType.QUEEN;
        };
        assertThat(game.getGame().getBoard().getPieceAt(Position.of("a8")).getType()).isEqualTo(expected);
    }

    @Test
    void resignAcceptsSpelledOutYes() {
        InteractiveGame game = runWithInput("0\nresign\nyes\n");

        assertThat(game.getGame().getGameStatus()).isEqualTo(GameState.GameStatus.WHITE_RESIGNED);
    }

    @Test
    void newCommandResetsTheBoard() {
        InteractiveGame game = runWithInput("0\ne2e4\nnew\nquit\n");

        assertThat(game.getGame().getMoveHistory().isEmpty()).isTrue();
        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("New game started.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "3", "4"})
    void aiModeSelectionSetsUpAiOpponentForEveryDifficulty(String modeChoice) {
        // モード選択直後に quit するため AI の着手は走らない（1手ごとの遅延を避ける）
        InteractiveGame game = runWithInput(modeChoice + "\nquit\n");

        assertThat(game.getGame().getBlackPlayer().isAI()).isTrue();
        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("Human vs AI");
    }

    // ===== GameObserver コールバック =====

    @ParameterizedTest
    @EnumSource(GameState.GameStatus.class)
    void onGameStateChangedHandlesEveryStatusWithoutThrowing(GameState.GameStatus status) {
        InteractiveGame game = new InteractiveGame();

        game.onGameStateChanged(status);

        // IN_PROGRESS と投了は通知メッセージを出さない仕様
        boolean silent = status == GameState.GameStatus.IN_PROGRESS
            || status == GameState.GameStatus.WHITE_RESIGNED
            || status == GameState.GameStatus.BLACK_RESIGNED;
        assertThat(capturedOutput.toString(StandardCharsets.UTF_8).isEmpty()).isEqualTo(silent);
    }

    @Test
    void onCheckDetectedPrintsWarning() {
        InteractiveGame game = new InteractiveGame();

        game.onCheckDetected(Color.BLACK);

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("Black king is in CHECK!");
    }

    @Test
    void onGameOverPrintsWinnerOrDraw() {
        InteractiveGame game = new InteractiveGame();

        game.onGameOver(Color.WHITE);
        game.onGameOver(null);

        String out = capturedOutput.toString(StandardCharsets.UTF_8);
        assertThat(out).contains("White wins!");
        assertThat(out).contains("Draw!");
    }

    @Test
    void mainEntryPointRunsAGameToCompletion() {
        System.setIn(new ByteArrayInputStream("0\ne2e4\nquit\n".getBytes(StandardCharsets.UTF_8)));

        InteractiveGame.main(new String[0]);

        assertThat(capturedOutput.toString(StandardCharsets.UTF_8)).contains("Thanks for playing!");
    }
}
