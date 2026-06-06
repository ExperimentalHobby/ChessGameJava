package com.chessgame.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * JavaFX 版コントロールパネル。新ゲーム・undo・投了ボタンを縦に並べる。
 * 各ボタンのアクションは setter で外部から設定する。
 */
public class ControlPanel extends VBox {
    private final Button newGameButton;
    private final Button undoButton;
    private final Button resignButton;

    public ControlPanel() {
        setPadding(new Insets(15));
        setSpacing(10);
        setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");
        setAlignment(Pos.TOP_CENTER);

        newGameButton = createButton("New Game");
        undoButton = createButton("Undo");
        resignButton = createButton("Resign");

        getChildren().addAll(newGameButton, undoButton, resignButton);
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
        button.setPrefWidth(120);
        button.setPrefHeight(35);
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

}
