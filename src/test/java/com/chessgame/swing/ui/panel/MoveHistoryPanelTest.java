package com.chessgame.swing.ui.panel;

import com.chessgame.board.model.Position;
import com.chessgame.game.core.ChessGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MoveHistoryPanel のユニットテスト。
 * 棋譜表示とゲーム差し替え時の追従を検証する。
 */
class MoveHistoryPanelTest {
    private MoveHistoryPanel panel;
    private ChessGame game;

    @BeforeEach
    void setUp() {
        game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
        panel = new MoveHistoryPanel(game);
    }

    @Test
    void testUpdateMoveHistoryShowsNotation() {
        game.makeMove(Position.of("e2"), Position.of("e4"));

        panel.updateMoveHistory();

        assertEquals(game.getMoveHistory().getNotationString(), panel.getDisplayedText());
    }

    @Test
    void testSetGameSwitchesToNewGamesHistory() {
        ChessGame newGame = ChessGame.createTwoPlayerGame("Alice", "Bob");
        newGame.startNewGame();
        newGame.makeMove(Position.of("d2"), Position.of("d4"));

        panel.setGame(newGame);
        panel.updateMoveHistory();

        assertEquals(newGame.getMoveHistory().getNotationString(), panel.getDisplayedText());
    }
}
