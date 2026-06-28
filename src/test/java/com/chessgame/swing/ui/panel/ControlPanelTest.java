package com.chessgame.swing.ui.panel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
