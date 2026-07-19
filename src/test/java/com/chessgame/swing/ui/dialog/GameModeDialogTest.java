package com.chessgame.swing.ui.dialog;

import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import org.junit.jupiter.api.Test;

import javax.swing.JOptionPane;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameModeDialog のユニットテスト。
 * ダイアログ表示(JOptionPane)はheadless CIで実行できないため、選択結果からゲームを
 * 生成する {@link GameModeDialog#resolveGame(int)} を直接呼び出して検証する。
 */
class GameModeDialogTest {

    @Test
    void testResolveGameHumanVsHuman() {
        ChessGame game = GameModeDialog.resolveGame(0);

        assertFalse(GameModeDialog.isLastGameAI());
        assertFalse(game.getCurrentPlayer().isAI());
        assertFalse(game.getBlackPlayer().isAI());
    }

    @Test
    void testResolveGameClosedOptionBehavesLikeHumanVsHuman() {
        // ダイアログを閉じた場合(CLOSED_OPTION)はHuman vs Human扱いになるはず
        ChessGame game = GameModeDialog.resolveGame(JOptionPane.CLOSED_OPTION);

        assertFalse(GameModeDialog.isLastGameAI());
        assertFalse(game.getBlackPlayer().isAI());
    }

    @Test
    void testResolveGameEasyDifficulty() {
        ChessGame game = GameModeDialog.resolveGame(1);

        assertTrue(GameModeDialog.isLastGameAI());
        Player black = game.getBlackPlayer();
        assertTrue(black instanceof AIPlayer);
        assertEquals(1, ((AIPlayer) black).getDifficulty());
    }

    @Test
    void testResolveGameMediumDifficulty() {
        ChessGame game = GameModeDialog.resolveGame(2);

        assertTrue(GameModeDialog.isLastGameAI());
        assertEquals(2, ((AIPlayer) game.getBlackPlayer()).getDifficulty());
    }

    @Test
    void testResolveGameHardDifficulty() {
        ChessGame game = GameModeDialog.resolveGame(3);

        assertTrue(GameModeDialog.isLastGameAI());
        assertEquals(3, ((AIPlayer) game.getBlackPlayer()).getDifficulty());
    }

    @Test
    void testResolveGameExpertDifficulty() {
        ChessGame game = GameModeDialog.resolveGame(4);

        assertTrue(GameModeDialog.isLastGameAI());
        assertEquals(4, ((AIPlayer) game.getBlackPlayer()).getDifficulty());
    }

    @Test
    void testResolveGameWithTimeChoiceUnlimitedHasNoTimeControl() {
        ChessGame game = GameModeDialog.resolveGame(0, 0);

        assertFalse(game.hasTimeControl());
    }

    @Test
    void testResolveGameWithTimeChoiceBlitzGivesBothPlayersBlitzTime() {
        ChessGame game = GameModeDialog.resolveGame(0, 1);

        assertTrue(game.hasTimeControl());
        assertEquals(3 * 60_000L, game.getRemainingMillis(Color.WHITE));
        assertEquals(3 * 60_000L, game.getRemainingMillis(Color.BLACK));
    }

    @Test
    void testResolveGameWithTimeChoiceRapidGivesBothPlayersRapidTime() {
        ChessGame game = GameModeDialog.resolveGame(0, 2);

        assertTrue(game.hasTimeControl());
        assertEquals(10 * 60_000L, game.getRemainingMillis(Color.WHITE));
    }

    @Test
    void testResolveGameWithTimeChoiceClassicalGivesBothPlayersClassicalTime() {
        ChessGame game = GameModeDialog.resolveGame(0, 3);

        assertTrue(game.hasTimeControl());
        assertEquals(60 * 60_000L, game.getRemainingMillis(Color.WHITE));
    }

    @Test
    void testResolveGameWithTimeChoiceAndAiDifficultyCombinesBoth() {
        ChessGame game = GameModeDialog.resolveGame(2, 1);

        assertTrue(GameModeDialog.isLastGameAI());
        assertEquals(2, ((AIPlayer) game.getBlackPlayer()).getDifficulty());
        assertTrue(game.hasTimeControl());
        assertEquals(3 * 60_000L, game.getRemainingMillis(Color.WHITE));
    }

    @Test
    void testResolveGameWithClosedTimeChoiceBehavesLikeUnlimited() {
        ChessGame game = GameModeDialog.resolveGame(0, JOptionPane.CLOSED_OPTION);

        assertFalse(game.hasTimeControl());
    }
}
