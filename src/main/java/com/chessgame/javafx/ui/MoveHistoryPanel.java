package com.chessgame.javafx.ui;

import com.chessgame.game.core.ChessGame;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 * 棋譜（手の履歴）を「1. e2e4 e7e5 2. ...」形式で表示するパネル。
 * スクロール可能な非編集テキストエリアに表示し、手が進むたびに末尾へ自動スクロールする。
 */
public class MoveHistoryPanel extends VBox {
    private ChessGame game;
    private final TextArea textArea;

    /**
     * 指定したゲームに紐づいた棋譜パネルを生成する。
     *
     * @param game 表示対象の {@link ChessGame}
     */
    public MoveHistoryPanel(ChessGame game) {
        this.game = game;

        setPadding(new Insets(10));
        setSpacing(5);
        setPrefWidth(150);
        setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Moves");
        title.setStyle("-fx-font-weight: bold;");

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);

        getChildren().addAll(title, textArea);
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
     * 棋譜表示を現在のゲームの手の履歴で更新し、末尾へ自動スクロールする。
     */
    public void updateMoveHistory() {
        textArea.setText(game.getMoveHistory().getNotationString());
        textArea.positionCaret(textArea.getText().length());
    }

    /**
     * 現在表示中の棋譜テキストを返す（テスト用）。
     *
     * @return 表示中の棋譜文字列
     */
    public String getDisplayedText() {
        return textArea.getText();
    }
}
