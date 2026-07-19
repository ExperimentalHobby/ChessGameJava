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

package com.chessgame.javafx.ui;

import com.chessgame.game.core.ChessGame;
import com.chessgame.model.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * JavaFX 版クロックパネル。白黒双方の持ち時間の残り時間を表示する。
 * 持ち時間ルール無しの対局では非表示になる。
 */
public class ClockPanel extends HBox {
    private ChessGame game;
    private final Label whiteClockLabel;
    private final Label blackClockLabel;

    /**
     * クロックパネルを生成する。
     *
     * @param game 表示対象のゲーム
     */
    public ClockPanel(ChessGame game) {
        this.game = game;

        setPadding(new Insets(10));
        setSpacing(20);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        whiteClockLabel = new Label("White: 00:00");
        blackClockLabel = new Label("Black: 00:00");

        getChildren().addAll(whiteClockLabel, blackClockLabel);
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
            setManaged(false);
            return;
        }
        setVisible(true);
        setManaged(true);
        whiteClockLabel.setText("White: " + formatMillis(game.getRemainingMillis(Color.WHITE)));
        blackClockLabel.setText("Black: " + formatMillis(game.getRemainingMillis(Color.BLACK)));
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
