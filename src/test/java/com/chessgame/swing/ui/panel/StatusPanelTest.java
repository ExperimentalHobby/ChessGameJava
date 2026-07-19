package com.chessgame.swing.ui.panel;

import com.chessgame.board.model.Position;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.Player;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.model.Color;
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
    void testUpdateStatusDuringNormalGame() {
        game.startNewGame();
        statusPanel.updateStatus();
        assertEquals(GameState.GameStatus.IN_PROGRESS, game.getGameStatus());
        assertEquals("White", game.getCurrentPlayer().getName());
        assertEquals(java.awt.Color.BLACK, statusPanel.getStatusColor());
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

    @Test
    void testUpdateStatusShowsCheckInRed() {
        ChessGame fenGame = ChessGame.fromFen("4r3/8/8/8/8/8/8/R3K2R w KQ - 0 1",
            Player.human(Color.WHITE, "White"), Player.human(Color.BLACK, "Black"));
        StatusPanel panel = new StatusPanel(fenGame);

        panel.updateStatus();

        assertTrue(panel.getStatusText().contains("王手"));
        assertEquals(java.awt.Color.RED, panel.getStatusColor());
    }

    @Test
    void testUpdateStatusShowsCheckmateInWinColor() {
        // フールズメイト: 1. f3 e5 2. g4 Qh4#
        game.startNewGame();
        game.makeMove(Position.of("f2"), Position.of("f3"));
        game.makeMove(Position.of("e7"), Position.of("e5"));
        game.makeMove(Position.of("g2"), Position.of("g4"));
        game.makeMove(Position.of("d8"), Position.of("h4"));

        statusPanel.updateStatus();

        assertTrue(statusPanel.getStatusText().contains("チェックメイト"));
        assertEquals(new java.awt.Color(0, 140, 0), statusPanel.getStatusColor());
    }

    @Test
    void testUpdateStatusShowsStalemateInBlue() {
        ChessGame fenGame = ChessGame.fromFen("k7/8/8/8/8/8/5q2/7K w - - 0 1",
            Player.human(Color.WHITE, "White"), Player.human(Color.BLACK, "Black"));
        StatusPanel panel = new StatusPanel(fenGame);

        panel.updateStatus();

        assertTrue(panel.getStatusText().contains("ステールメイト"));
        assertEquals(java.awt.Color.BLUE, panel.getStatusColor());
    }

    @Test
    void testUpdateStatusShowsFiftyMoveRuleInBlue() {
        ChessGame fenGame = ChessGame.fromFen("4k3/8/8/8/8/8/8/4K3 w - - 100 60",
            Player.human(Color.WHITE, "White"), Player.human(Color.BLACK, "Black"));
        StatusPanel panel = new StatusPanel(fenGame);

        panel.updateStatus();

        assertTrue(panel.getStatusText().contains("50手ルール"));
        assertEquals(java.awt.Color.BLUE, panel.getStatusColor());
    }

    @Test
    void testUpdateStatusShowsThreefoldRepetitionInBlue() {
        // ナイトを2往復させ、開始局面(白番)を3回出現させる
        game.startNewGame();
        for (int i = 0; i < 2; i++) {
            game.makeMove(Position.of("g1"), Position.of("f3"));
            game.makeMove(Position.of("g8"), Position.of("f6"));
            game.makeMove(Position.of("f3"), Position.of("g1"));
            game.makeMove(Position.of("f6"), Position.of("g8"));
        }

        statusPanel.updateStatus();

        assertEquals(GameState.GameStatus.THREEFOLD_REPETITION, game.getGameStatus());
        assertTrue(statusPanel.getStatusText().contains("同一局面3回"));
        assertEquals(java.awt.Color.BLUE, statusPanel.getStatusColor());
    }

    @Test
    void testUpdateStatusShowsInsufficientMaterialInBlue() {
        ChessGame fenGame = ChessGame.fromFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1",
            Player.human(Color.WHITE, "White"), Player.human(Color.BLACK, "Black"));
        StatusPanel panel = new StatusPanel(fenGame);

        panel.updateStatus();

        assertTrue(panel.getStatusText().contains("戦力不足"));
        assertEquals(java.awt.Color.BLUE, panel.getStatusColor());
    }

    @Test
    void testUpdateStatusShowsWhiteResignedInWinColor() {
        game.startNewGame();
        game.resign(Color.WHITE);

        statusPanel.updateStatus();

        assertTrue(statusPanel.getStatusText().contains("白が投了"));
        assertEquals(new java.awt.Color(0, 140, 0), statusPanel.getStatusColor());
    }

    @Test
    void testUpdateStatusShowsBlackResignedInWinColor() {
        game.startNewGame();
        game.resign(Color.BLACK);

        statusPanel.updateStatus();

        assertTrue(statusPanel.getStatusText().contains("黒が投了"));
        assertEquals(new java.awt.Color(0, 140, 0), statusPanel.getStatusColor());
    }

    @Test
    void testUpdateStatusShowsWhiteTimeoutInWinColor() throws InterruptedException {
        // 1ミリ秒の持ち時間+実時間のsleepで確実にWHITE_TIMEOUTを起こす
        ChessGame timedGame = new ChessGame(
            Player.human(Color.WHITE, "White"), Player.human(Color.BLACK, "Black"),
            new com.chessgame.gamestate.model.TimeControl(1, 0));
        Thread.sleep(5);
        assertTrue(timedGame.checkTimeout());
        StatusPanel panel = new StatusPanel(timedGame);

        panel.updateStatus();

        assertTrue(panel.getStatusText().contains("白が時間切れ"));
        assertEquals(new java.awt.Color(0, 140, 0), panel.getStatusColor());
    }

    @Test
    void testUpdateStatusShowsBlackTimeoutInWinColor() throws InterruptedException {
        ChessGame timedGame = new ChessGame(
            Player.human(Color.WHITE, "White"), Player.human(Color.BLACK, "Black"),
            new com.chessgame.gamestate.model.TimeControl(1, 0));
        assertTrue(timedGame.makeMove(Position.of("e2"), Position.of("e4")));
        Thread.sleep(5);
        assertTrue(timedGame.checkTimeout());
        StatusPanel panel = new StatusPanel(timedGame);

        panel.updateStatus();

        assertTrue(panel.getStatusText().contains("黒が時間切れ"));
        assertEquals(new java.awt.Color(0, 140, 0), panel.getStatusColor());
    }
}
