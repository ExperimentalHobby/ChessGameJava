package com.chessgame.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * JavaFX 版ステータスバー。現在のゲーム状態と手数を表示する。
 * 王手・チェックメイト・ステールメイトに応じてテキスト色を変える。
 */
public class StatusBar extends HBox {
    /** 通常時のステータステキスト色。 */
    private static final Color DEFAULT_TEXT_COLOR = Color.web("#333333");
    /** 初期表示・リセット時のステータス文言。 */
    private static final String INITIAL_STATUS = "Game initialized";

    private final Label statusLabel;
    private final Label moveCountLabel;

    public StatusBar() {
        setPadding(new Insets(10));
        setSpacing(20);
        setStyle("-fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label(INITIAL_STATUS);
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statusLabel.setTextFill(DEFAULT_TEXT_COLOR);

        moveCountLabel = new Label("Moves: 0");
        moveCountLabel.setFont(Font.font("Arial", 12));
        moveCountLabel.setTextFill(Color.web("#666666"));

        getChildren().addAll(statusLabel, moveCountLabel);
    }

    /**
     * ステータスラベルのテキストを更新する。
     *
     * @param status 表示するテキスト
     */
    public void updateStatus(String status) { statusLabel.setText(status); }

    /**
     * 手数ラベルを更新する。
     *
     * @param count 現在の手数
     */
    public void updateMoveCount(int count) { moveCountLabel.setText("Moves: " + count); }

    /**
     * 王手状態を表示する。テキストを赤色にする。
     *
     * @param playerColor 王手されているプレイヤーの色名
     */
    public void setCheckStatus(String playerColor) {
        statusLabel.setText(playerColor + " is in CHECK!");
        statusLabel.setTextFill(Color.web("#CC0000"));
    }

    /**
     * チェックメイト（または投了）の結果を表示する。テキストを緑色にする。
     *
     * @param winner 勝者の色名
     */
    public void setCheckmateStatus(String winner) {
        statusLabel.setText("CHECKMATE! " + winner + " wins!");
        statusLabel.setTextFill(Color.web("#008000"));
    }

    /**
     * ステールメイト（引き分け）を表示する。テキストを青色にする。
     */
    public void setStalemateStatus() {
        statusLabel.setText("STALEMATE! Draw!");
        statusLabel.setTextFill(Color.web("#0000CC"));
    }

    /**
     * ステータス表示を初期状態に戻す。新ゲーム開始時に使う。
     */
    public void resetStatus() {
        statusLabel.setText(INITIAL_STATUS);
        statusLabel.setTextFill(DEFAULT_TEXT_COLOR);
    }
}
