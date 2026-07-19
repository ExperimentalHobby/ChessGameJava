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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * JavaFX 版コントロールパネル。新ゲーム・undo・投了・終了ボタンを縦に並べる。
 * 各ボタンのアクションは setter で外部から設定する。
 */
public class ControlPanel extends VBox {
    private final Button newGameButton;
    private final Button undoButton;
    private final Button resignButton;
    private final Button quitButton;

    public ControlPanel() {
        setPadding(new Insets(15));
        setSpacing(10);
        setPrefWidth(130);
        setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");
        setAlignment(Pos.TOP_CENTER);

        newGameButton = createButton("New Game");
        undoButton = createButton("Undo");
        resignButton = createButton("Resign");
        quitButton = createButton("Quit");

        undoButton.setDisable(true);

        getChildren().addAll(newGameButton, undoButton, resignButton, quitButton);
    }

    /**
     * 統一スタイルのボタンを生成して返す。
     *
     * @param text ボタンに表示するラベル文字列
     * @return スタイル設定済みの {@link Button}
     */
    private Button createButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-padding: 5px; -fx-cursor: hand;");
        return button;
    }

    /**
     * 新ゲームボタンのアクションを設定する。
     *
     * @param handler クリック時に実行する処理
     */
    public void setOnNewGame(Runnable handler) { newGameButton.setOnAction(e -> handler.run()); }

    /**
     * undo ボタンのアクションを設定する。
     *
     * @param handler クリック時に実行する処理
     */
    public void setOnUndo(Runnable handler) { undoButton.setOnAction(e -> handler.run()); }

    /**
     * 投了ボタンのアクションを設定する。
     *
     * @param handler クリック時に実行する処理
     */
    public void setOnResign(Runnable handler) { resignButton.setOnAction(e -> handler.run()); }

    /**
     * 終了ボタンのアクションを設定する。
     *
     * @param handler クリック時に実行する処理
     */
    public void setOnQuit(Runnable handler) { quitButton.setOnAction(e -> handler.run()); }

    /**
     * undo ボタンの有効・無効を設定する。
     *
     * @param disabled true で無効化、false で有効化
     */
    public void setUndoDisabled(boolean disabled) { undoButton.setDisable(disabled); }

}
