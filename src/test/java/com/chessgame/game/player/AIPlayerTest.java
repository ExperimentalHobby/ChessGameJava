package com.chessgame.game.player;

import com.chessgame.model.Color;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.game.core.ChessGame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    /** 難易度 3 は取れる駒の価値が最大の手を選ぶ（唯一の capture を確実に取る）。 */
    @Test
    public void testDifficulty3PrefersCapture() {
        playMovesToOfferBlackACapture();

        AIPlayer ai = new AIPlayer("AI", Color.BLACK, 3);
        Move move = ai.selectMove(game);

        assertThat(move).isNotNull();
        assertThat(move.getCapturedPiece()).isNotNull();
        assertThat(move.getTo()).isEqualTo(Position.of("d5"));
    }

    /** 難易度 2 は capture が存在すればそれを選ぶ。 */
    @Test
    public void testDifficulty2PrefersCapture() {
        playMovesToOfferBlackACapture();

        AIPlayer ai = new AIPlayer("AI", Color.BLACK, 2);
        Move move = ai.selectMove(game);

        assertThat(move).isNotNull();
        assertThat(move.getCapturedPiece()).isNotNull();
        assertThat(move.getTo()).isEqualTo(Position.of("d5"));
    }

    /** Python スクリプトが見つからない場合でも Java フォールバックで正しく選択する。 */
    @Test
    public void testFallsBackToJavaWhenPythonScriptMissing() {
        // 存在しないスクリプトを指定して Python 経路を強制的に失敗させる
        System.setProperty("chess.ai.script", "ai/__no_such_script__.py");
        playMovesToOfferBlackACapture();

        AIPlayer ai = new AIPlayer("AI", Color.BLACK, 3);
        Move move = ai.selectMove(game);

        assertThat(move).isNotNull();
        assertThat(move.getCapturedPiece()).isNotNull();
        assertThat(move.getTo()).isEqualTo(Position.of("d5"));
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
     * 難易度4はただ取りされたポーンを取り返す（d5 のポーンを取る）。
     * Python エンジン（駒得を回復）でも Java フォールバック（唯一の capture）でも成立する。
     */
    @Test
    public void testDifficulty4RecapturesPawn() {
        System.setProperty("chess.ai.depth", "2");
        playMovesToOfferBlackACapture();

        AIPlayer ai = new AIPlayer("AI", Color.BLACK, 4);
        Move move = ai.selectMove(game);

        assertThat(move).isNotNull();
        assertThat(move.getCapturedPiece()).isNotNull();
        assertThat(move.getTo()).isEqualTo(Position.of("d5"));
    }

    /** 難易度4も Python スクリプト不在時は Java（難易度3相当）にフォールバックする。 */
    @Test
    public void testDifficulty4FallsBackWhenPythonScriptMissing() {
        System.setProperty("chess.ai.script", "ai/__no_such_script__.py");
        playMovesToOfferBlackACapture();

        AIPlayer ai = new AIPlayer("AI", Color.BLACK, 4);
        Move move = ai.selectMove(game);

        assertThat(move).isNotNull();
        assertThat(move.getCapturedPiece()).isNotNull();
        assertThat(move.getTo()).isEqualTo(Position.of("d5"));
    }

    /**
     * 難易度4は、スクリプトは実行できても不正なUCI文字列を返した場合も
     * Java（難易度3相当）にフォールバックする。
     */
    @Test
    public void testDifficulty4FallsBackWhenEngineReturnsInvalidMove() {
        System.setProperty("chess.ai.script", "ai/invalid_bestmove_stub.py");
        System.setProperty("chess.ai.depth", "2");
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
}
