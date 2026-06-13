package com.chessgame.swing.ui.panel;

import javax.swing.*;

/**
 * 新ゲーム・Undo・投了・終了ボタンを含むコントロールパネル。
 * 各ボタンのアクションは setter で外部から設定される。
 */
public class ControlPanel extends JPanel {
    private final JButton undoButton;
    private final JButton resignButton;
    private Runnable onNewGame;
    private Runnable onUndo;
    private Runnable onResign;

    /**
     * コントロールパネルを生成する。
     */
    public ControlPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Controls"));

        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> {
            if (onNewGame != null) onNewGame.run();
        });

        undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> {
            if (onUndo != null) onUndo.run();
        });

        resignButton = new JButton("Resign");
        resignButton.addActionListener(e -> {
            if (onResign != null) onResign.run();
        });

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> System.exit(0));

        add(Box.createVerticalStrut(5));
        add(newGameButton);
        add(Box.createVerticalStrut(10));
        add(undoButton);
        add(Box.createVerticalStrut(10));
        add(resignButton);
        add(Box.createVerticalStrut(10));
        add(quitButton);
        add(Box.createVerticalGlue());
    }

    /**
     * 新ゲーム開始時のコールバックを設定する。
     *
     * @param action 実行するアクション
     */
    public void setOnNewGame(Runnable action) {
        this.onNewGame = action;
    }

    /**
     * Undo 実行時のコールバックを設定する。
     *
     * @param action 実行するアクション
     */
    public void setOnUndo(Runnable action) {
        this.onUndo = action;
    }

    /**
     * 投了時のコールバックを設定する。
     *
     * @param action 実行するアクション
     */
    public void setOnResign(Runnable action) {
        this.onResign = action;
    }

    /**
     * Undo ボタンの有効・無効を設定する。
     *
     * @param enabled 有効にする場合 true
     */
    public void setUndoEnabled(boolean enabled) {
        undoButton.setEnabled(enabled);
    }

    /**
     * 投了ボタンの有効・無効を設定する。
     *
     * @param enabled 有効にする場合 true
     */
    public void setResignEnabled(boolean enabled) {
        resignButton.setEnabled(enabled);
    }
}
