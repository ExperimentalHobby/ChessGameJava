package com.chessgame.swing.ui.panel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import java.awt.Component;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ControlPanel のユニットテスト。
 * ボタン状態制御とコールバック機能が正しく動作することを検証する。
 */
class ControlPanelTest {
    private ControlPanel controlPanel;

    @BeforeEach
    void setUp() {
        controlPanel = new ControlPanel();
    }

    @Test
    void testSetUndoEnabled() {
        controlPanel.setUndoEnabled(false);
        assertFalse(findButton("Undo").isEnabled());

        controlPanel.setUndoEnabled(true);
        assertTrue(findButton("Undo").isEnabled());
    }

    @Test
    void testSetResignEnabled() {
        controlPanel.setResignEnabled(false);
        assertFalse(findButton("Resign").isEnabled());

        controlPanel.setResignEnabled(true);
        assertTrue(findButton("Resign").isEnabled());
    }

    @Test
    void testNewGameButtonClickTriggersOnNewGameCallback() {
        boolean[] called = {false};
        controlPanel.setOnNewGame(() -> called[0] = true);

        findButton("New Game").doClick();

        assertTrue(called[0]);
    }

    @Test
    void testUndoButtonClickTriggersOnUndoCallback() {
        boolean[] called = {false};
        controlPanel.setOnUndo(() -> called[0] = true);

        findButton("Undo").doClick();

        assertTrue(called[0]);
    }

    @Test
    void testResignButtonClickTriggersOnResignCallback() {
        boolean[] called = {false};
        controlPanel.setOnResign(() -> called[0] = true);

        findButton("Resign").doClick();

        assertTrue(called[0]);
    }

    @Test
    void testDisabledUndoButtonClickDoesNotTriggerCallback() {
        boolean[] called = {false};
        controlPanel.setOnUndo(() -> called[0] = true);
        controlPanel.setUndoEnabled(false);

        findButton("Undo").doClick();

        assertFalse(called[0]);
    }

    @Test
    void testDisabledResignButtonClickDoesNotTriggerCallback() {
        boolean[] called = {false};
        controlPanel.setOnResign(() -> called[0] = true);
        controlPanel.setResignEnabled(false);

        findButton("Resign").doClick();

        assertFalse(called[0]);
    }

    @Test
    void testSavePgnButtonClickTriggersOnSavePgnCallback() {
        boolean[] called = {false};
        controlPanel.setOnSavePgn(() -> called[0] = true);

        findButton("Save PGN").doClick();

        assertTrue(called[0]);
    }

    @Test
    void testOpenPgnButtonClickTriggersOnOpenPgnCallback() {
        boolean[] called = {false};
        controlPanel.setOnOpenPgn(() -> called[0] = true);

        findButton("Open PGN").doClick();

        assertTrue(called[0]);
    }

    @Test
    void testCopyFenButtonClickTriggersOnCopyFenCallback() {
        boolean[] called = {false};
        controlPanel.setOnCopyFen(() -> called[0] = true);

        findButton("Copy FEN").doClick();

        assertTrue(called[0]);
    }

    /** テキストからボタンを探す（New Game ボタンはフィールド化・ゲッターがされていないため）。 */
    private JButton findButton(String text) {
        for (Component c : controlPanel.getComponents()) {
            if (c instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
        }
        throw new IllegalStateException("Button not found: " + text);
    }
}
