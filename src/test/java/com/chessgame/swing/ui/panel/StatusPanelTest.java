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

    @Test
    void testSetGameUpdatesToNewGamesStatus() {
        // New Game 相当: 別インスタンスに差し替えて1手指す
        ChessGame newGame = ChessGame.createTwoPlayerGame("Alice", "Bob");
        newGame.startNewGame();
        newGame.makeMove(com.chessgame.board.model.Position.of("e2"), com.chessgame.board.model.Position.of("e4"));

        statusPanel.setGame(newGame);
        statusPanel.updateStatus();

        // setGame が効いていなければ古い game（0手）の "Moves: 0" のままになる
        assertEquals("Moves: 1", statusPanel.getMoveCountText());
        assertTrue(statusPanel.getStatusText().contains("Bob"));
    }
}
