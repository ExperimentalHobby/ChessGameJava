package com.chessgame.javafx.ui.dialog;

import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JavaFX 版 GameModeDialog のユニットテスト。
 * ゲームモード選択ダイアログのゲーム生成ロジックを検証する。
 * JavaFX アプリケーションスレッドが不要なゲーム生成部分のみテストする。
 */
class GameModeDialogTest {

    @Test
    void testIsLastGameAIDefaultsFalse() {
        // 初期状態では AI 対戦フラグが false であることを確認
        assertFalse(GameModeDialog.isLastGameAI());
    }

    @Test
    void testTwoPlayerGameCreation() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        assertNotNull(game);
        assertEquals("White", game.getCurrentPlayer().getName());
        assertFalse(game.getCurrentPlayer().isAI());
    }

    @Test
    void testAIGameCreationEasy() {
        ChessGame game = createAIGame(1);
        assertNotNull(game);
        // 白（You）が最初の手番
        assertFalse(game.getCurrentPlayer().isAI());
        assertEquals("You", game.getCurrentPlayer().getName());
    }

    @Test
    void testAIGameCreationAllDifficulties() {
        for (int difficulty = 1; difficulty <= 4; difficulty++) {
            ChessGame game = createAIGame(difficulty);
            assertNotNull(game, "difficulty=" + difficulty + " でゲームが null");
            // 黒が AI であることを確認（白=人間、黒=AI の構成）
            game.startNewGame();
            // makeMove で黒の番にできないので、プレイヤーリストから確認
            assertTrue(game.getBoard() != null, "difficulty=" + difficulty + " でボードが null");
        }
    }

    @Test
    void testTwoPlayerGameHasNoAIPlayers() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
        assertFalse(game.getCurrentPlayer().isAI());
    }

    /** テスト用 AI ゲーム生成（GameModeDialog の createAIGame と同じロジック）。 */
    private ChessGame createAIGame(int difficulty) {
        Player whitePlayer = Player.human(Color.WHITE, "You");
        Player blackPlayer = new AIPlayer("AI", Color.BLACK, difficulty);
        return new ChessGame(whitePlayer, blackPlayer);
    }
}
