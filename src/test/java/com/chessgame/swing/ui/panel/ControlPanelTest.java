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
    void testControlPanelInitialization() {
        assertNotNull(controlPanel);
        // ControlPanel is a JPanel
        assertTrue(controlPanel instanceof javax.swing.JPanel);
    }

    @Test
    void testSetUndoEnabled() {
        controlPanel.setUndoEnabled(true);
        // Undo button should be enabled
        controlPanel.setUndoEnabled(false);
        // Undo button should be disabled
    }

    @Test
    void testSetResignEnabled() {
        controlPanel.setResignEnabled(true);
        // Resign button should be enabled
        controlPanel.setResignEnabled(false);
        // Resign button should be disabled
    }

    @Test
    void testSetCallbacks() {
        boolean[] callbackExecuted = {false};

        controlPanel.setOnNewGame(() -> callbackExecuted[0] = true);
        controlPanel.setOnUndo(() -> callbackExecuted[0] = true);
        controlPanel.setOnResign(() -> callbackExecuted[0] = true);

        // Callbacks are set successfully
        assertFalse(callbackExecuted[0]);
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
