package com.chessgame.game;

import com.chessgame.model.Color;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
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
