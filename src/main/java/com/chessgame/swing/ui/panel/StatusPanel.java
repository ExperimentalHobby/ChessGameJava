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
import com.chessgame.gamestate.model.GameState;

import javax.swing.*;
import java.awt.*;

/**
 * ゲームステータス（手番・手数・ゲーム状態）を表示するパネル。
 * 王手・チェックメイト・ステールメイト・投了に応じてテキスト色を変える。
 */
public class StatusPanel extends JPanel {
    /** 勝敗確定時のステータス表示に使う緑色。 */
    private static final Color WIN_COLOR = new Color(0, 140, 0);

    private ChessGame game;
    private final JLabel statusLabel;
    private final JLabel moveCountLabel;

    /**
     * ステータスパネルを生成する。
     *
     * @param game 表示対象のゲーム
     */
    public StatusPanel(ChessGame game) {
        this.game = game;

        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));
        setBorder(BorderFactory.createTitledBorder("Status"));

        statusLabel = new JLabel("White to move");
        statusLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 14));

        moveCountLabel = new JLabel("Moves: 0");
        moveCountLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));

        add(statusLabel);
        add(moveCountLabel);
    }

    /**
     * 表示対象のゲームを切り替える。New Game で {@link ChessGame} インスタンスが
     * 差し替わった際に呼ぶ（呼ばないと古いゲームの状態を表示し続けてしまう）。
     *
     * @param game 新しい表示対象の {@link ChessGame}
     */
    public void setGame(ChessGame game) {
        this.game = game;
    }

    /**
     * 現在表示中のステータステキストを返す（テスト用）。
     *
     * @return ステータスラベルの表示文字列
     */
    public String getStatusText() {
        return statusLabel.getText();
    }

    /**
     * 現在表示中の手数テキストを返す（テスト用）。
     *
     * @return 手数ラベルの表示文字列
     */
    public String getMoveCountText() {
        return moveCountLabel.getText();
    }

    /**
     * 現在のステータステキストの表示色を返す（テスト用）。
     *
     * @return ステータスラベルの前景色
     */
    public Color getStatusColor() {
        return statusLabel.getForeground();
    }

    /**
     * ゲーム状態に応じてステータスを更新する。
     */
    public void updateStatus() {
        GameState.GameStatus status = game.getGameStatus();
        String playerName = game.getCurrentPlayer().getName();
        String statusText;

        switch (status) {
            case CHECK:
                statusText = playerName + " は王手です！";
                statusLabel.setForeground(Color.RED);
                break;
            case CHECKMATE:
                String winner = game.getCurrentPlayer().getColor().opposite().toString();
                statusText = "チェックメイト！ " + winner + " の勝ち！";
                statusLabel.setForeground(WIN_COLOR);
                break;
            case STALEMATE:
                statusText = "ステールメイト！ 引き分け！";
                statusLabel.setForeground(Color.BLUE);
                break;
            case FIFTY_MOVE_RULE:
                statusText = "50手ルールにより引き分け！";
                statusLabel.setForeground(Color.BLUE);
                break;
            case THREEFOLD_REPETITION:
                statusText = "同一局面3回により引き分け！";
                statusLabel.setForeground(Color.BLUE);
                break;
            case INSUFFICIENT_MATERIAL:
                statusText = "戦力不足により引き分け！";
                statusLabel.setForeground(Color.BLUE);
                break;
            case WHITE_RESIGNED:
                statusText = "白が投了！ BLACK の勝ち！";
                statusLabel.setForeground(WIN_COLOR);
                break;
            case BLACK_RESIGNED:
                statusText = "黒が投了！ WHITE の勝ち！";
                statusLabel.setForeground(WIN_COLOR);
                break;
            case WHITE_TIMEOUT:
                statusText = "白が時間切れ！ BLACK の勝ち！";
                statusLabel.setForeground(WIN_COLOR);
                break;
            case BLACK_TIMEOUT:
                statusText = "黒が時間切れ！ WHITE の勝ち！";
                statusLabel.setForeground(WIN_COLOR);
                break;
            default:
                statusText = playerName + " の番";
                statusLabel.setForeground(Color.BLACK);
                break;
        }

        statusLabel.setText(statusText);
        moveCountLabel.setText("Moves: " + game.getMoveHistory().size());
    }

    /**
     * Undo ボタンの有効・無効を制御する。
     *
     * @param enabled 有効にする場合 true
     */
    public void setUndoEnabled(boolean enabled) {
        // StatusPanel は Undo ボタンを直接管理しないため、呼び出し側で ControlPanel を制御する
    }

    /**
     * 投了ボタンの有効・無効を制御する。
     *
     * @param enabled 有効にする場合 true
     */
    public void setResignEnabled(boolean enabled) {
        // StatusPanel は投了ボタンを直接管理しないため、呼び出し側で ControlPanel を制御する
    }
}
