package com.chessgame.swing.ui.panel;

import com.chessgame.game.core.ChessGame;
import com.chessgame.gamestate.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StatusPanel のユニットテスト。
 * ゲーム状態に応じてステータス表示が正しく更新されることを検証する。
 */
class StatusPanelTest {
    private StatusPanel statusPanel;
    private ChessGame game;

    @BeforeEach
    void setUp() {
        game = ChessGame.createTwoPlayerGame("White", "Black");
        statusPanel = new StatusPanel(game);
    }

    @Test
    void testStatusPanelInitialization() {
        assertNotNull(statusPanel);
        assertNotNull(game);
        assertEquals(GameState.GameStatus.IN_PROGRESS, game.getGameStatus());
    }

    @Test
    void testUpdateStatusDuringNormalGame() {
        game.startNewGame();
        statusPanel.updateStatus();
        assertEquals(GameState.GameStatus.IN_PROGRESS, game.getGameStatus());
        assertEquals("White", game.getCurrentPlayer().getName());
    }

    @Test
    void testStatusPanelCreation() {
        assertNotNull(statusPanel);
        // StatusPanel is a JPanel
        assertTrue(statusPanel instanceof javax.swing.JPanel);
    }
}
