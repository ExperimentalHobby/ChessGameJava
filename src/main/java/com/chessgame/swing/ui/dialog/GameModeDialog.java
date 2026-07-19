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

import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.Player;
import com.chessgame.gamestate.model.TimeControl;
import com.chessgame.gamestate.model.TimeControlPreset;
import com.chessgame.model.Color;

import javax.swing.*;

/**
 * ゲームモード選択ダイアログ（Human vs Human / AI 難易度4段階）と持ち時間選択ダイアログ。
 * 選択結果に応じて新しい ChessGame インスタンスを生成して返す。
 */
public class GameModeDialog {
    private static boolean isAIGame = false;

    /**
     * ゲームモード選択ダイアログ・持ち時間選択ダイアログを順に表示し、選択されたゲームを返す。
     *
     * @param parentFrame 親フレーム（ダイアログのオーナー）
     * @return 選択されたモード・持ち時間に応じた ChessGame インスタンス
     */
    public static ChessGame showDialog(JFrame parentFrame) {
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
     * JOptionPaneの選択結果(CLOSED_OPTIONを含む)から、持ち時間無しでゲームを生成する。
     * ダイアログ表示を伴わないため単体テストから直接検証できる。
     *
     * @param modeChoice {@link JOptionPane#showOptionDialog}の戻り値（ゲームモード選択）
     * @return 選択されたモードに応じたChessGameインスタンス
     */
    static ChessGame resolveGame(int modeChoice) {
        return resolveGame(modeChoice, 0);
    }

    /**
     * JOptionPaneの選択結果(CLOSED_OPTIONを含む)からゲームを生成する。
     * ダイアログ表示を伴わないため単体テストから直接検証できる。
     *
     * @param modeChoice {@link JOptionPane#showOptionDialog}の戻り値（ゲームモード選択）
     * @param timeChoice {@link JOptionPane#showOptionDialog}の戻り値（持ち時間選択）
     * @return 選択されたモード・持ち時間に応じたChessGameインスタンス
     */
    static ChessGame resolveGame(int modeChoice, int timeChoice) {
        if (modeChoice == JOptionPane.CLOSED_OPTION) modeChoice = 0;
        if (timeChoice == JOptionPane.CLOSED_OPTION) timeChoice = 0;

        isAIGame = (modeChoice != 0);
        TimeControl timeControl = resolveTimeControl(timeChoice);
        if (modeChoice == 0) {
            return (timeControl != null)
                ? new ChessGame(Player.human(Color.WHITE, "White"), Player.human(Color.BLACK, "Black"), timeControl)
                : ChessGame.createTwoPlayerGame("White", "Black");
        } else {
            return createAIGame(modeChoice, timeControl);
        }
    }

    /**
     * 持ち時間選択の選択肢インデックスから対応する {@link TimeControl} を返す。
     * 「無制限」（未知の値を含む）の場合は null を返す。
     *
     * @param timeChoice {@link JOptionPane#showOptionDialog}の戻り値（持ち時間選択）
     * @return 対応する {@link TimeControl}、無制限の場合は null
     */
    private static TimeControl resolveTimeControl(int timeChoice) {
        switch (timeChoice) {
            case 1: return TimeControlPreset.BLITZ.toTimeControl();
            case 2: return TimeControlPreset.RAPID.toTimeControl();
            case 3: return TimeControlPreset.CLASSICAL.toTimeControl();
            default: return null;
        }
    }

    /**
     * 最後に選択されたゲームが AI 対戦かどうかを返す。
     *
     * @return AI 対戦の場合 true
     */
    public static boolean isLastGameAI() {
        return isAIGame;
    }

    /**
     * AI 対戦ゲームを生成する。
     *
     * @param difficulty  AI の難易度（1=Easy, 2=Medium, 3=Hard, 4=Expert）
     * @param timeControl 持ち時間ルール。無制限なら null
     * @return AI 対戦ゲーム
     */
    private static ChessGame createAIGame(int difficulty, TimeControl timeControl) {
        Player whitePlayer = Player.human(Color.WHITE, "You");
        Player blackPlayer = new AIPlayer("AI", Color.BLACK, difficulty);
        return (timeControl != null)
            ? new ChessGame(whitePlayer, blackPlayer, timeControl)
            : new ChessGame(whitePlayer, blackPlayer);
    }
}
