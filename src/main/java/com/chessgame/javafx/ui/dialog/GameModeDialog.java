package com.chessgame.javafx.ui.dialog;

import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * ゲームモード選択ダイアログ（Human vs Human / AI 難易度4段階）。
 * 選択結果に応じて新しい ChessGame インスタンスを生成して返す。
 */
public class GameModeDialog {
    private static boolean isAIGame = false;

    /**
     * ゲームモード選択ダイアログを表示し、選択されたゲームを返す。
     *
     * @param owner 親ウィンドウ
     * @return 選択されたモードに応じた ChessGame インスタンス
     */
    public static ChessGame showDialog(Window owner) {
        ChessGame[] result = { ChessGame.createTwoPlayerGame("White", "Black") };
        isAIGame = false;

        Stage dialog = new Stage();
        dialog.setTitle("New Game");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setResizable(false);

        Button btnHumanVsHuman = createButton("Human vs Human");
        Button btnEasy        = createButton("Human vs AI (Easy)");
        Button btnMedium      = createButton("Human vs AI (Medium)");
        Button btnHard        = createButton("Human vs AI (Hard)");
        Button btnExpert      = createButton("Human vs AI (Expert)");

        btnHumanVsHuman.setOnAction(e -> {
            result[0] = ChessGame.createTwoPlayerGame("White", "Black");
            isAIGame = false;
            dialog.close();
        });
        btnEasy.setOnAction(e ->   { result[0] = createAIGame(1); isAIGame = true; dialog.close(); });
        btnMedium.setOnAction(e -> { result[0] = createAIGame(2); isAIGame = true; dialog.close(); });
        btnHard.setOnAction(e ->   { result[0] = createAIGame(3); isAIGame = true; dialog.close(); });
        btnExpert.setOnAction(e -> { result[0] = createAIGame(4); isAIGame = true; dialog.close(); });

        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(btnHumanVsHuman, btnEasy, btnMedium, btnHard, btnExpert);

        Label label = new Label("ゲームモードを選択してください");
        label.setStyle("-fx-font-size: 14;");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(label, hbox);

        dialog.setScene(new Scene(vbox, 700, 120));
        dialog.showAndWait();

        return result[0];
    }

    /**
     * 最後に選択されたゲームが AI 対戦かどうかを返す。
     *
     * @return AI 対戦の場合 true
     */
    public static boolean isLastGameAI() {
        return isAIGame;
    }

    private static Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(120);
        button.setPrefHeight(40);
        return button;
    }

    private static ChessGame createAIGame(int difficulty) {
        Player whitePlayer = Player.human(Color.WHITE, "You");
        Player blackPlayer = new AIPlayer("AI", Color.BLACK, difficulty);
        return new ChessGame(whitePlayer, blackPlayer);
    }
}
