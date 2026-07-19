package com.chessgame.game.player;

import com.chessgame.model.Color;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.PieceType;
import com.chessgame.game.core.ChessGame;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AIPlayer の着手選択を検証するテスト。
 *
 * <p>Python ブリッジ経由・Java フォールバックのどちらでも成立する「挙動上の性質」を
 * 検証する。Python が利用できない環境（CI 等）でも安定して通るよう、検証結果は
 * RNG のシード列ではなく難易度ごとの選択ロジックに対して行う。</p>
 */
public class AIPlayerTest {

    private ChessGame game;

    @BeforeEach
    public void setUp() {
        game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
    }

    @AfterEach
    public void tearDown() {
        // フォールバック検証で上書きしたシステムプロパティを必ず元に戻す
        System.clearProperty("chess.ai.script");
        System.clearProperty("chess.ai.python");
        System.clearProperty("chess.ai.depth");
        System.clearProperty("chess.ai.timeout");
    }

    /** 詰み局面では合法手が0のため、各難易度とも selectMove は null を返す。 */
    @Test
    public void testSelectMoveReturnsNullOnCheckmate() {
        ChessGame checkmateGame = ChessGame.fromFen("R5k1/5ppp/8/8/8/8/8/4K3 b - - 0 1",
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));

        for (int difficulty = 1; difficulty <= 4; difficulty++) {
            AIPlayer ai = new AIPlayer("AI", Color.BLACK, difficulty);
            assertThat(ai.selectMove(checkmateGame)).as("difficulty %d", difficulty).isNull();
        }
    }

    /** ステイルメイト局面では合法手が0のため、各難易度とも selectMove は null を返す。 */
    @Test
    public void testSelectMoveReturnsNullOnStalemate() {
        ChessGame stalemateGame = ChessGame.fromFen("k7/8/8/8/8/8/5q2/7K w - - 0 1",
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));

        for (int difficulty = 1; difficulty <= 4; difficulty++) {
            AIPlayer ai = new AIPlayer("AI", Color.WHITE, difficulty);
            assertThat(ai.selectMove(stalemateGame)).as("difficulty %d", difficulty).isNull();
        }
    }

    /** 初期局面では各難易度とも合法手（白の手）を 1 つ返す。 */
    @Test
    public void testSelectsLegalMoveFromInitialPosition() {
        for (int difficulty = 1; difficulty <= 3; difficulty++) {
            AIPlayer ai = new AIPlayer("AI", Color.WHITE, difficulty);
            Move move = ai.selectMove(game);
            assertThat(move).as("difficulty %d", difficulty).isNotNull();
            assertThat(game.getAvailableMoves(move.getFrom())).contains(move);
        }
    }

    /**
     * 難易度2〜4は、唯一のcaptureが存在する局面で確実にそれを選ぶ（駒得優先ロジック）。
     * Python連携が正常・スクリプト欠如・不正応答のいずれの状態でも同じ結果になるべきで
     * あり、難易度によって成立するPython状態が異なる（難易度4のみ不正UCI応答の経路を持つ）
     * ため、実際に意味のある6組み合わせをパラメータ化して検証する。
     */
    @ParameterizedTest(name = "difficulty={0}, pythonState={1}")
    @MethodSource("capturePreferenceScenarios")
    public void selectMovePrefersCaptureAcrossDifficultyAndPythonState(
            int difficulty, String pythonState, String scriptOverride, String depthOverride) {
        if (scriptOverride != null) {
            System.setProperty("chess.ai.script", scriptOverride);
        }
        if (depthOverride != null) {
            System.setProperty("chess.ai.depth", depthOverride);
        }
        playMovesToOfferBlackACapture();

        AIPlayer ai = new AIPlayer("AI", Color.BLACK, difficulty);
        Move move = ai.selectMove(game);

        assertThat(move).isNotNull();
        assertThat(move.getCapturedPiece()).isNotNull();
        assertThat(move.getTo()).isEqualTo(Position.of("d5"));
    }

    private static Stream<Arguments> capturePreferenceScenarios() {
        return Stream.of(
            Arguments.of(2, "normal", null, null),
            Arguments.of(3, "normal", null, null),
            Arguments.of(4, "normal", null, "2"),
            Arguments.of(3, "script missing", "ai/__no_such_script__.py", null),
            Arguments.of(4, "script missing", "ai/__no_such_script__.py", "2"),
            Arguments.of(4, "invalid engine response", "ai/invalid_bestmove_stub.py", "2")
        );
    }

    /** 難易度1〜3で Python が合法手数を超える範囲外indexを返した場合はJavaにフォールバックする。 */
    @Test
    public void testFallsBackToJavaWhenPythonReturnsOutOfRangeIndex() {
        System.setProperty("chess.ai.script", "ai/out_of_range_index_stub.py");
        playMovesToOfferBlackACapture();

        for (int difficulty = 1; difficulty <= 3; difficulty++) {
            AIPlayer ai = new AIPlayer("AI", Color.BLACK, difficulty);
            Move move = ai.selectMove(game);
            assertThat(move).as("difficulty %d", difficulty).isNotNull();
            assertThat(game.getAvailableMoves(move.getFrom())).as("difficulty %d", difficulty).contains(move);
        }
    }

    /** 難易度1〜3で Python が非数値の出力を返した場合はJavaにフォールバックする。 */
    @Test
    public void testFallsBackToJavaWhenPythonReturnsNonNumericIndex() {
        System.setProperty("chess.ai.script", "ai/non_numeric_index_stub.py");
        playMovesToOfferBlackACapture();

        for (int difficulty = 1; difficulty <= 3; difficulty++) {
            AIPlayer ai = new AIPlayer("AI", Color.BLACK, difficulty);
            Move move = ai.selectMove(game);
            assertThat(move).as("difficulty %d", difficulty).isNotNull();
            assertThat(game.getAvailableMoves(move.getFrom())).as("difficulty %d", difficulty).contains(move);
        }
    }

    /** 難易度4は合法手を返す（Python エンジン経由、または難易度3フォールバック）。 */
    @Test
    public void testDifficulty4ReturnsLegalMove() {
        System.setProperty("chess.ai.depth", "2"); // テスト高速化のため浅め
        AIPlayer ai = new AIPlayer("AI", Color.WHITE, 4);

        Move move = ai.selectMove(game);

        assertThat(move).isNotNull();
        assertThat(game.getAvailableMoves(move.getFrom())).contains(move);
    }

    /**
     * Pythonプロセスがタイムアウトした場合、強制終了(destroyForcibly)してJava
     * （難易度3相当）にフォールバックする。runPythonは難易度1〜3・4共通の処理のため、
     * タイムアウトを短縮できる難易度4で検証する（難易度1〜3は固定5秒でテストが遅くなる）。
     */
    @Test
    public void testFallsBackToJavaWhenPythonProcessTimesOut() {
        System.setProperty("chess.ai.script", "ai/hanging_stub.py");
        System.setProperty("chess.ai.depth", "2");
        System.setProperty("chess.ai.timeout", "1"); // タイムアウトを短縮してテストを高速化
        playMovesToOfferBlackACapture();

        AIPlayer ai = new AIPlayer("AI", Color.BLACK, 4);
        Move move = ai.selectMove(game);

        assertThat(move).isNotNull();
        assertThat(move.getCapturedPiece()).isNotNull();
        assertThat(move.getTo()).isEqualTo(Position.of("d5"));
    }

    /** Python コマンドが不正でも Java フォールバックに切り替わる。 */
    @Test
    public void testFallsBackToJavaWhenPythonCommandInvalid() {
        System.setProperty("chess.ai.python", "definitely-not-a-real-python-command");
        AIPlayer ai = new AIPlayer("AI", Color.WHITE, 1);

        Move move = ai.selectMove(game);

        assertThat(move).isNotNull();
        assertThat(game.getAvailableMoves(move.getFrom())).contains(move);
    }

    /**
     * 非同期（SwingWorker/JavaFX Task）で選択した手を安全に適用できるかを判定する
     * {@code isMoveStillApplicable} のテスト。GUI を介さない純粋な判定ロジックのため
     * ここで単体テストする。
     */
    @Test
    public void testIsMoveStillApplicableTrueWhenNothingChanged() {
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        boolean result = AIPlayer.isMoveStillApplicable(move, game, game, false);

        assertThat(result).isTrue();
    }

    /** タスクがキャンセル済みの場合は適用しない。 */
    @Test
    public void testIsMoveStillApplicableFalseWhenCancelled() {
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        boolean result = AIPlayer.isMoveStillApplicable(move, game, game, true);

        assertThat(result).isFalse();
    }

    /** 選択された手が null（合法手なし等）の場合は適用しない。 */
    @Test
    public void testIsMoveStillApplicableFalseWhenMoveIsNull() {
        boolean result = AIPlayer.isMoveStillApplicable(null, game, game, false);

        assertThat(result).isFalse();
    }

    /** New Game 等で選択時と別の ChessGame インスタンスに切り替わっていた場合は適用しない。 */
    @Test
    public void testIsMoveStillApplicableFalseWhenGameInstanceChanged() {
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));
        ChessGame differentGame = ChessGame.createTwoPlayerGame("White", "Black");
        differentGame.startNewGame();

        boolean result = AIPlayer.isMoveStillApplicable(move, game, differentGame, false);

        assertThat(result).isFalse();
    }

    /** ゲームが既に終了している（投了・チェックメイト等）場合は適用しない。 */
    @Test
    public void testIsMoveStillApplicableFalseWhenGameIsOver() {
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));
        game.resign(Color.WHITE);

        boolean result = AIPlayer.isMoveStillApplicable(move, game, game, false);

        assertThat(result).isFalse();
    }

    /** 初期局面の buildFen 出力が標準開始局面の FEN と一致する。 */
    @Test
    public void testBuildFenStartingPosition() {
        AIPlayer ai = new AIPlayer("AI", Color.WHITE, 4);

        String fen = ai.buildFen(game);

        assertThat(fen).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    /** キャスリング権の一部喪失・アンパッサン対象ありの局面で buildFen が正しい FEN を返す。 */
    @Test
    public void testBuildFenReflectsCastlingRightsAndEnPassant() {
        assertThat(game.makeMove(Position.of("a2"), Position.of("a4"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a5"))).isTrue();
        // 白のルークを動かして白のクイーンサイドキャスリング権を喪失させる
        assertThat(game.makeMove(Position.of("a1"), Position.of("a3"))).isTrue();
        assertThat(game.makeMove(Position.of("b7"), Position.of("b6"))).isTrue();
        // 黒の2マス前進でアンパッサン対象を発生させる
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("f7"), Position.of("f5"))).isTrue();

        AIPlayer ai = new AIPlayer("AI", Color.WHITE, 4);
        String fen = ai.buildFen(game);

        assertThat(fen).isEqualTo("rnbqkbnr/2ppp1pp/1p6/p4p2/P3P3/R7/1PPP1PPP/1NBQKBNR w Kkq f6 0 1");
    }

    /**
     * 1. e4 d5 2. exd5 と進め、黒に唯一の capture（Qd8xd5）を提示する局面を作る。
     * 実行後の手番は黒。
     */
    private void playMovesToOfferBlackACapture() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("d5"))).isTrue();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
    }

    /** resolveUciMove は "e7e8q" のような昇格付きUCIを、昇格先が一致する Move に解決する。 */
    @Test
    public void testResolveUciMoveSelectsQueenPromotion() {
        AIPlayer ai = new AIPlayer("AI", Color.WHITE, 4);
        List<Move> moves = promotionCandidates();

        Move resolved = ai.resolveUciMove("e7e8q", moves);

        assertThat(resolved).isNotNull();
        assertThat(resolved.getPromotionPiece()).isEqualTo(PieceType.QUEEN);
    }

    /** resolveUciMove は "e7e8r" を ROOK 昇格の Move に解決する。 */
    @Test
    public void testResolveUciMoveSelectsRookPromotion() {
        AIPlayer ai = new AIPlayer("AI", Color.WHITE, 4);
        List<Move> moves = promotionCandidates();

        Move resolved = ai.resolveUciMove("e7e8r", moves);

        assertThat(resolved).isNotNull();
        assertThat(resolved.getPromotionPiece()).isEqualTo(PieceType.ROOK);
    }

    /** resolveUciMove は "e7e8b" を BISHOP 昇格の Move に解決する。 */
    @Test
    public void testResolveUciMoveSelectsBishopPromotion() {
        AIPlayer ai = new AIPlayer("AI", Color.WHITE, 4);
        List<Move> moves = promotionCandidates();

        Move resolved = ai.resolveUciMove("e7e8b", moves);

        assertThat(resolved).isNotNull();
        assertThat(resolved.getPromotionPiece()).isEqualTo(PieceType.BISHOP);
    }

    /** resolveUciMove は "e7e8n" を KNIGHT 昇格の Move に解決する。 */
    @Test
    public void testResolveUciMoveSelectsKnightPromotion() {
        AIPlayer ai = new AIPlayer("AI", Color.WHITE, 4);
        List<Move> moves = promotionCandidates();

        Move resolved = ai.resolveUciMove("e7e8n", moves);

        assertThat(resolved).isNotNull();
        assertThat(resolved.getPromotionPiece()).isEqualTo(PieceType.KNIGHT);
    }

    /** resolveUciMove は不正な昇格文字（q/r/b/n 以外）を QUEEN 昇格にフォールバックする。 */
    @Test
    public void testResolveUciMoveFallsBackToQueenOnInvalidPromotionChar() {
        AIPlayer ai = new AIPlayer("AI", Color.WHITE, 4);
        List<Move> moves = promotionCandidates();

        Move resolved = ai.resolveUciMove("e7e8z", moves);

        assertThat(resolved).isNotNull();
        assertThat(resolved.getPromotionPiece()).isEqualTo(PieceType.QUEEN);
    }

    /** e7e8/e8 への4種の昇格先すべてを候補として持つ合法手リストを返す。 */
    private List<Move> promotionCandidates() {
        Position from = Position.of("e7");
        Position to = Position.of("e8");
        return List.of(
            Move.promotion(from, to, PieceType.QUEEN),
            Move.promotion(from, to, PieceType.ROOK),
            Move.promotion(from, to, PieceType.BISHOP),
            Move.promotion(from, to, PieceType.KNIGHT)
        );
    }
}
