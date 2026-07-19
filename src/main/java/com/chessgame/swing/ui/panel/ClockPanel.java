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

import com.chessgame.game.core.ChessGame;
import com.chessgame.model.Color;

import javax.swing.*;
import java.awt.*;

/**
 * 白黒双方の持ち時間の残り時間を表示するパネル。
 * 持ち時間ルール無しの対局では非表示になる。
 */
public class ClockPanel extends JPanel {
    private ChessGame game;
    private final JLabel whiteClockLabel;
    private final JLabel blackClockLabel;

    /**
     * クロックパネルを生成する。
     *
     * @param game 表示対象のゲーム
     */
    public ClockPanel(ChessGame game) {
        this.game = game;

        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));
        setBorder(BorderFactory.createTitledBorder("Clock"));

        whiteClockLabel = new JLabel("White: 00:00");
        blackClockLabel = new JLabel("Black: 00:00");

        add(whiteClockLabel);
        add(blackClockLabel);
    }

    /**
     * 表示対象のゲームを切り替える。New Game で {@link ChessGame} インスタンスが
     * 差し替わった際に呼ぶ。
     *
     * @param game 新しい表示対象の {@link ChessGame}
     */
    public void setGame(ChessGame game) {
        this.game = game;
    }

    /**
     * 残り時間の表示を更新する。持ち時間ルール無しの対局では非表示にする。
     */
    public void updateClocks() {
        if (!game.hasTimeControl()) {
            setVisible(false);
            return;
        }
        setVisible(true);
        whiteClockLabel.setText("White: " + formatMillis(game.getRemainingMillis(Color.WHITE)));
        blackClockLabel.setText("Black: " + formatMillis(game.getRemainingMillis(Color.BLACK)));
    }

    /**
     * 現在表示中の白の残り時間テキストを返す（テスト用）。
     *
     * @return 白のクロックラベルの表示文字列
     */
    public String getWhiteClockText() {
        return whiteClockLabel.getText();
    }

    /**
     * 現在表示中の黒の残り時間テキストを返す（テスト用）。
     *
     * @return 黒のクロックラベルの表示文字列
     */
    public String getBlackClockText() {
        return blackClockLabel.getText();
    }

    /**
     * ミリ秒を{@code mm:ss}形式の文字列に変換する。
     *
     * @param millis 変換する時間（ミリ秒）
     * @return {@code mm:ss}形式の文字列
     */
    static String formatMillis(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
