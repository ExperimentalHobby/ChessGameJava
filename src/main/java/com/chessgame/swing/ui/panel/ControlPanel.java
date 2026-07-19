/*
 * MIT License
 *
 * Copyright (c) 2026 ChessGame Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.chessgame.swing.ui.panel;

import javax.swing.*;
import java.awt.*;

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
    private Runnable onSavePgn;
    private Runnable onOpenPgn;
    private Runnable onCopyFen;

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

        JButton savePgnButton = new JButton("Save PGN");
        savePgnButton.addActionListener(e -> {
            if (onSavePgn != null) onSavePgn.run();
        });

        JButton openPgnButton = new JButton("Open PGN");
        openPgnButton.addActionListener(e -> {
            if (onOpenPgn != null) onOpenPgn.run();
        });

        JButton copyFenButton = new JButton("Copy FEN");
        copyFenButton.addActionListener(e -> {
            if (onCopyFen != null) onCopyFen.run();
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

        // 最大 preferred 幅に統一し、縦位置もセンター揃えにする
        JButton[] buttons = { newGameButton, savePgnButton, openPgnButton, copyFenButton,
            undoButton, resignButton, quitButton };
        int maxWidth = 0;
        int btnHeight = newGameButton.getPreferredSize().height;
        for (JButton btn : buttons) {
            maxWidth = Math.max(maxWidth, btn.getPreferredSize().width);
        }
        Dimension buttonSize = new Dimension(maxWidth, btnHeight);
        for (JButton btn : buttons) {
            btn.setPreferredSize(buttonSize);
            btn.setMaximumSize(buttonSize);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        add(Box.createVerticalStrut(5));
        add(newGameButton);
        add(Box.createVerticalStrut(10));
        add(savePgnButton);
        add(Box.createVerticalStrut(10));
        add(openPgnButton);
        add(Box.createVerticalStrut(10));
        add(copyFenButton);
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
     * PGN保存実行時のコールバックを設定する。
     *
     * @param action 実行するアクション
     */
    public void setOnSavePgn(Runnable action) {
        this.onSavePgn = action;
    }

    /**
     * PGN読み込み実行時のコールバックを設定する。
     *
     * @param action 実行するアクション
     */
    public void setOnOpenPgn(Runnable action) {
        this.onOpenPgn = action;
    }

    /**
     * FENコピー実行時のコールバックを設定する。
     *
     * @param action 実行するアクション
     */
    public void setOnCopyFen(Runnable action) {
        this.onCopyFen = action;
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
