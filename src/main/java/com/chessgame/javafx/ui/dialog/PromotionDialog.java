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

import com.chessgame.model.Color;
import com.chessgame.piece.model.PieceType;
import com.chessgame.javafx.asset.PieceRenderer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Window;

/**
 * ポーン昇格時に駒種（クイーン・ルーク・ビショップ・ナイト）を選択させるダイアログ。
 * {@link Dialog#showAndWait()} で選択結果を {@link PieceType} として取得する。
 */
public final class PromotionDialog extends Dialog<PieceType> {

    /**
     * 昇格ダイアログを生成する。
     *
     * @param color 昇格するポーンの色（ボタンに表示する駒画像の色に使用）
     * @param owner 親ウィンドウ
     */
    public PromotionDialog(Color color, Window owner) {
        initOwner(owner);
        setTitle("Pawn Promotion");
        setHeaderText("Choose a piece to promote to:");
        getDialogPane().setStyle("-fx-font-size: 13px;");

        PieceType[] choices = {PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT};
        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));

        for (PieceType type : choices) {
            Button btn = createPieceButton(type, color);
            btn.setOnAction(e -> {
                setResult(type);
                close();
            });
            buttons.getChildren().add(btn);
        }

        getDialogPane().setContent(buttons);
        getDialogPane().getButtonTypes().clear();

        setResultConverter(bt -> null);
    }

    /**
     * 駒の画像とラベルを持つ昇格選択ボタンを生成する。
     *
     * @param type  選択肢の駒種
     * @param color ポーンの色（画像の色に使用）
     * @return 昇格選択ボタン
     */
    private Button createPieceButton(PieceType type, Color color) {
        ImageView icon = new ImageView(PieceRenderer.render(color, type));
        icon.setFitWidth(PieceRenderer.SIZE);
        icon.setFitHeight(PieceRenderer.SIZE);

        Text label = new Text(typeName(type));
        label.setFont(Font.font("Arial", 11));

        VBox content = new VBox(4, icon, label);
        content.setAlignment(Pos.CENTER);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.setPrefSize(80, 90);
        btn.setStyle("-fx-cursor: hand;");
        return btn;
    }

    /**
     * 駒種を英語の表示名に変換する。
     *
     * @param type 駒種
     * @return 表示名（例: "Queen"、"Knight"）
     */
    private String typeName(PieceType type) {
        switch (type) {
            case QUEEN:  return "Queen";
            case ROOK:   return "Rook";
            case BISHOP: return "Bishop";
            default:     return "Knight";
        }
    }
}
