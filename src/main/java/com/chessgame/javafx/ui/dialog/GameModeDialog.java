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

package com.chessgame.javafx.ui.dialog;

import com.chessgame.ui.shared.dialog.GameModeSelection;
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
 * ゲームモード選択ダイアログ（Human vs Human / AI 難易度4段階）と持ち時間選択ダイアログ。
 * 選択結果に応じて新しい ChessGame インスタンスを生成して返す。
 */
public class GameModeDialog {

    private GameModeDialog() {
    }

    /**
     * ゲームモード選択ダイアログ・持ち時間選択ダイアログを順に表示し、選択結果を返す。
     *
     * @param owner 親ウィンドウ
     * @return 選択されたモード・持ち時間に応じた選択結果
     */
    public static GameModeSelection.Result showDialog(Window owner) {
        int modeChoice = showModeDialog(owner);
        int timeChoice = showTimeDialog(owner);
        return resolveGame(modeChoice, timeChoice);
    }

    private static int showModeDialog(Window owner) {
        int[] choice = { 0 };

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

        btnHumanVsHuman.setOnAction(e -> { choice[0] = 0; dialog.close(); });
        btnEasy.setOnAction(e ->   { choice[0] = 1; dialog.close(); });
        btnMedium.setOnAction(e -> { choice[0] = 2; dialog.close(); });
        btnHard.setOnAction(e ->   { choice[0] = 3; dialog.close(); });
        btnExpert.setOnAction(e -> { choice[0] = 4; dialog.close(); });

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

        return choice[0];
    }

    private static int showTimeDialog(Window owner) {
        int[] choice = { 0 };

        Stage dialog = new Stage();
        dialog.setTitle("Time Control");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setResizable(false);

        Button btnUnlimited = createButton("無制限");
        Button btnBlitz     = createButton("Blitz（3分+2秒）");
        Button btnRapid     = createButton("Rapid（10分+5秒）");
        Button btnClassical = createButton("Classical（60分+30秒）");

        btnUnlimited.setOnAction(e -> { choice[0] = 0; dialog.close(); });
        btnBlitz.setOnAction(e ->     { choice[0] = 1; dialog.close(); });
        btnRapid.setOnAction(e ->     { choice[0] = 2; dialog.close(); });
        btnClassical.setOnAction(e -> { choice[0] = 3; dialog.close(); });

        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(btnUnlimited, btnBlitz, btnRapid, btnClassical);

        Label label = new Label("持ち時間を選択してください");
        label.setStyle("-fx-font-size: 14;");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(label, hbox);

        dialog.setScene(new Scene(vbox, 700, 120));
        dialog.showAndWait();

        return choice[0];
    }

    /**
     * 選択されたボタンのインデックス(0=Human vs Human, 1〜4=AI難易度)から、持ち時間無しで
     * 選択結果を生成する。Stage表示を伴わないため単体テストから直接検証できる。
     *
     * @param modeChoiceIndex ゲームモードの選択インデックス
     * @return 選択されたモードに応じた選択結果
     */
    static GameModeSelection.Result resolveGame(int modeChoiceIndex) {
        return resolveGame(modeChoiceIndex, 0);
    }

    /**
     * 選択されたボタンのインデックスから選択結果を生成する。
     * Stage表示を伴わないため単体テストから直接検証できる。
     *
     * @param modeChoiceIndex ゲームモードの選択インデックス(0=Human vs Human, 1〜4=AI難易度)
     * @param timeChoiceIndex 持ち時間の選択インデックス(0=無制限, 1=Blitz, 2=Rapid, 3=Classical)
     * @return 選択されたモード・持ち時間に応じた選択結果
     */
    static GameModeSelection.Result resolveGame(int modeChoiceIndex, int timeChoiceIndex) {
        return GameModeSelection.resolve(modeChoiceIndex, timeChoiceIndex);
    }

    private static Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(160);
        button.setPrefHeight(40);
        return button;
    }
}
