package com.chessgame.swing.ui.dialog;

import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import com.chessgame.ui.shared.dialog.GameModeSelection;
import org.junit.jupiter.api.Test;

import javax.swing.JOptionPane;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameModeDialog のユニットテスト。
 * ダイアログ表示(JOptionPane)はheadless CIで実行できないため、選択結果からゲームを
 * 生成する {@link GameModeDialog#resolveGame(int)} を直接呼び出して検証する。
 * 解決ロジック自体は {@code GameModeSelectionTest} で検証済みのため、ここでは
 * Swing固有のCLOSED_OPTION（キャンセル）の扱いを中心に検証する。
 */
class GameModeDialogTest {

    @Test
    void testResolveGameHumanVsHuman() {
        GameModeSelection.Result result = GameModeDialog.resolveGame(0);

        assertFalse(result.aiGame());
        assertFalse(result.game().getCurrentPlayer().isAI());
        assertFalse(result.game().getBlackPlayer().isAI());
    }

    @Test
    void testResolveSelectionIsCancelledWhenModeDialogIsClosed() {
        // Issue #267: ×/Esc で閉じたことを Human vs Human の選択と同一視すると、
        // 進行中の対局が破棄されてしまう。閉じた場合はキャンセルとして区別する
        assertTrue(GameModeDialog.resolveSelection(JOptionPane.CLOSED_OPTION, 0).isEmpty());
    }

    @Test
    void testResolveSelectionIsCancelledWhenTimeDialogIsClosed() {
        assertTrue(GameModeDialog.resolveSelection(0, JOptionPane.CLOSED_OPTION).isEmpty());
    }

    @Test
    void testResolveSelectionReturnsResultForNormalChoices() {
        GameModeSelection.Result result = GameModeDialog.resolveSelection(2, 1).orElseThrow();

        assertTrue(result.aiGame());
        assertEquals(2, ((AIPlayer) result.game().getBlackPlayer()).getDifficulty());
        assertTrue(result.game().hasTimeControl());
    }

    @Test
    void testResolveGameEasyDifficulty() {
        GameModeSelection.Result result = GameModeDialog.resolveGame(1);

        assertTrue(result.aiGame());
        Player black = result.game().getBlackPlayer();
        assertTrue(black instanceof AIPlayer);
        assertEquals(1, ((AIPlayer) black).getDifficulty());
    }

    @Test
    void testResolveGameMediumDifficulty() {
        GameModeSelection.Result result = GameModeDialog.resolveGame(2);

        assertTrue(result.aiGame());
        assertEquals(2, ((AIPlayer) result.game().getBlackPlayer()).getDifficulty());
    }

    @Test
    void testResolveGameHardDifficulty() {
        GameModeSelection.Result result = GameModeDialog.resolveGame(3);

        assertTrue(result.aiGame());
        assertEquals(3, ((AIPlayer) result.game().getBlackPlayer()).getDifficulty());
    }

    @Test
    void testResolveGameExpertDifficulty() {
        GameModeSelection.Result result = GameModeDialog.resolveGame(4);

        assertTrue(result.aiGame());
        assertEquals(4, ((AIPlayer) result.game().getBlackPlayer()).getDifficulty());
    }

    @Test
    void testResolveGameWithTimeChoiceUnlimitedHasNoTimeControl() {
        GameModeSelection.Result result = GameModeDialog.resolveGame(0, 0);

        assertFalse(result.game().hasTimeControl());
    }

    @Test
    void testResolveGameWithTimeChoiceBlitzGivesBothPlayersBlitzTime() {
        GameModeSelection.Result result = GameModeDialog.resolveGame(0, 1);

        assertTrue(result.game().hasTimeControl());
        assertRemainingMillisCloseTo(3 * 60_000L, result.game().getRemainingMillis(Color.WHITE));
        assertRemainingMillisCloseTo(3 * 60_000L, result.game().getRemainingMillis(Color.BLACK));
    }

    @Test
    void testResolveGameWithTimeChoiceRapidGivesBothPlayersRapidTime() {
        GameModeSelection.Result result = GameModeDialog.resolveGame(0, 2);

        assertTrue(result.game().hasTimeControl());
        assertRemainingMillisCloseTo(10 * 60_000L, result.game().getRemainingMillis(Color.WHITE));
    }

    @Test
    void testResolveGameWithTimeChoiceClassicalGivesBothPlayersClassicalTime() {
        GameModeSelection.Result result = GameModeDialog.resolveGame(0, 3);

        assertTrue(result.game().hasTimeControl());
        assertRemainingMillisCloseTo(60 * 60_000L, result.game().getRemainingMillis(Color.WHITE));
    }

    @Test
    void testResolveGameWithTimeChoiceAndAiDifficultyCombinesBoth() {
        GameModeSelection.Result result = GameModeDialog.resolveGame(2, 1);

        assertTrue(result.aiGame());
        assertEquals(2, ((AIPlayer) result.game().getBlackPlayer()).getDifficulty());
        assertTrue(result.game().hasTimeControl());
        assertRemainingMillisCloseTo(3 * 60_000L, result.game().getRemainingMillis(Color.WHITE));
    }

    /**
     * {@link com.chessgame.game.core.ChessGame#getRemainingMillis(Color)} は現在の手番であれば
     * 実経過時間を差し引くライブ値を返すため、生成直後でも実行環境の遅延次第で初期値と
     * 完全一致しないことがある。初期値を超えないこと・誤差が1秒以内であることを検証する。
     */
    private static void assertRemainingMillisCloseTo(long expectedMillis, long actualMillis) {
        assertTrue(actualMillis <= expectedMillis,
            "残り時間が初期値を超えています: expected<=" + expectedMillis + " but was " + actualMillis);
        assertTrue(expectedMillis - actualMillis <= 1000,
            "残り時間の誤差が大きすぎます: expected~" + expectedMillis + " but was " + actualMillis);
    }
}
