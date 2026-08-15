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

package com.chessgame.swing.ui.dialog;

import com.chessgame.ui.shared.dialog.GameModeSelection;

import javax.swing.*;

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
     * @param parentFrame 親フレーム（ダイアログのオーナー）
     * @return 選択されたモード・持ち時間に応じた選択結果
     */
    public static GameModeSelection.Result showDialog(JFrame parentFrame) {
        Object[] modeOptions = {"Human vs Human", "Human vs AI（Easy）", "Human vs AI（Medium）",
            "Human vs AI（Hard）", "Human vs AI（Expert）"};
        int modeChoice = JOptionPane.showOptionDialog(parentFrame,
            "ゲームモードを選択してください",
            "ゲームモード選択",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            modeOptions,
            modeOptions[0]);

        Object[] timeOptions = {"無制限", "Blitz（3分+2秒）", "Rapid（10分+5秒）", "Classical（60分+30秒）"};
        int timeChoice = JOptionPane.showOptionDialog(parentFrame,
            "持ち時間を選択してください",
            "持ち時間選択",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            timeOptions,
            timeOptions[0]);

        return resolveGame(modeChoice, timeChoice);
    }

    /**
     * JOptionPaneの選択結果(CLOSED_OPTIONを含む)から、持ち時間無しで選択結果を生成する。
     * ダイアログ表示を伴わないため単体テストから直接検証できる。
     *
     * @param modeChoice {@link JOptionPane#showOptionDialog}の戻り値（ゲームモード選択）
     * @return 選択されたモードに応じた選択結果
     */
    static GameModeSelection.Result resolveGame(int modeChoice) {
        return resolveGame(modeChoice, 0);
    }

    /**
     * JOptionPaneの選択結果(CLOSED_OPTIONを含む)から選択結果を生成する。
     * ダイアログ表示を伴わないため単体テストから直接検証できる。
     *
     * @param modeChoice {@link JOptionPane#showOptionDialog}の戻り値（ゲームモード選択）
     * @param timeChoice {@link JOptionPane#showOptionDialog}の戻り値（持ち時間選択）
     * @return 選択されたモード・持ち時間に応じた選択結果
     */
    static GameModeSelection.Result resolveGame(int modeChoice, int timeChoice) {
        if (modeChoice == JOptionPane.CLOSED_OPTION) modeChoice = 0;
        if (timeChoice == JOptionPane.CLOSED_OPTION) timeChoice = 0;

        return GameModeSelection.resolve(modeChoice, timeChoice);
    }
}
