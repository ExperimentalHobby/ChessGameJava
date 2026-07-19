package com.chessgame.swing.ui.panel;

import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.Player;
import com.chessgame.gamestate.model.TimeControl;
import com.chessgame.gamestate.model.TimeControlPreset;
import com.chessgame.model.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClockPanel のユニットテスト。
 * 持ち時間の残り時間表示・持ち時間ルール無し時の非表示切り替えを検証する。
 */
class ClockPanelTest {

    @Test
    void testFormatMillisFormatsAsMinutesSeconds() {
        assertEquals("00:00", ClockPanel.formatMillis(0));
        assertEquals("00:59", ClockPanel.formatMillis(59_999));
        assertEquals("01:00", ClockPanel.formatMillis(60_000));
        assertEquals("03:00", ClockPanel.formatMillis(180_000));
    }

    @Test
    void testUpdateClocksShowsInitialTimeForTimedGame() {
        ChessGame game = new ChessGame(
            Player.human(Color.WHITE, "White"), Player.human(Color.BLACK, "Black"),
            TimeControlPreset.BLITZ.toTimeControl());
        ClockPanel panel = new ClockPanel(game);

        panel.updateClocks();

        assertTrue(panel.isVisible());
        // 構築からupdateClocks()までにわずかでも実時間が経過すると02:59になりうるため、
        // 厳密な一致ではなく初期値(03:00)付近であることを確認する
        assertTrue(panel.getWhiteClockText().matches("White: 0[23]:\\d{2}"),
            () -> "unexpected: " + panel.getWhiteClockText());
        assertTrue(panel.getBlackClockText().matches("Black: 0[23]:\\d{2}"),
            () -> "unexpected: " + panel.getBlackClockText());
    }

    @Test
    void testPanelHiddenWhenGameHasNoTimeControl() {
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        ClockPanel panel = new ClockPanel(game);

        panel.updateClocks();

        assertFalse(panel.isVisible());
    }

    @Test
    void testSetGameSwitchesDisplayedGame() {
        ChessGame untimedGame = ChessGame.createTwoPlayerGame("White", "Black");
        ClockPanel panel = new ClockPanel(untimedGame);
        panel.updateClocks();
        assertFalse(panel.isVisible());

        ChessGame timedGame = new ChessGame(
            Player.human(Color.WHITE, "White"), Player.human(Color.BLACK, "Black"),
            new TimeControl(120_000, 0));
        panel.setGame(timedGame);
        panel.updateClocks();

        assertTrue(panel.isVisible());
        // わずかな実時間経過で01:59になりうるため、初期値(02:00)付近であることを確認する
        assertTrue(panel.getWhiteClockText().matches("White: 0[12]:\\d{2}"),
            () -> "unexpected: " + panel.getWhiteClockText());
    }
}
