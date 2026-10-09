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

import com.chessgame.model.Color;
import com.chessgame.ui.shared.dialog.GameModeSelection;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.Optional;

/**
 * ゲームモード選択ダイアログ（Human vs Human / AI 難易度4段階）と持ち時間選択ダイアログ。
 * 選択結果に応じて新しい ChessGame インスタンスを生成して返す。
 */
public class GameModeDialog {

    private GameModeDialog() {
    }

    /**
     * ゲームモード選択ダイアログ・（AI 対戦のみ）担当色選択ダイアログ・持ち時間選択ダイアログを順に表示し、
     * 選択結果を返す。いずれかのダイアログを × や Esc で閉じた場合はキャンセルとして空を返す
     * （先のダイアログを閉じた場合、以降のダイアログは表示しない）。
     *
     * @param parentFrame 親フレーム（ダイアログのオーナー）
     * @return 選択されたモード・持ち時間に応じた選択結果。キャンセルされた場合は空
     */
    public static Optional<GameModeSelection.Result> showDialog(JFrame parentFrame) {
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

        // AI 対戦のときだけ人間側の担当色を選ばせる（Human vs Human なら 0 のまま。閉じた場合はキャンセルを引き継ぐ）
        Object[] colorOptions = {"白（先手）", "黒（後手）"};
        int colorChoice = modeChoice <= 0 ? modeChoice
            : JOptionPane.showOptionDialog(parentFrame, "あなたの担当色を選択してください", "担当色選択",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, colorOptions, colorOptions[0]);

        Object[] timeOptions = {"無制限", "Blitz（3分+2秒）", "Rapid（10分+5秒）", "Classical（60分+30秒）"};
        // 先のダイアログを閉じた（キャンセル）場合は持ち時間ダイアログを出さずにキャンセルを引き継ぐ
        int timeChoice = colorChoice == JOptionPane.CLOSED_OPTION ? JOptionPane.CLOSED_OPTION
            : JOptionPane.showOptionDialog(parentFrame,
            "持ち時間を選択してください",
            "持ち時間選択",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            timeOptions,
            timeOptions[0]);

        return resolveSelection(modeChoice, timeChoice, colorChoice);
    }

    /**
     * JOptionPaneの選択結果から選択結果を生成する。どちらかが {@link JOptionPane#CLOSED_OPTION}
     * （ダイアログを閉じた）ならキャンセルとして空を返す。Human vs Human の選択と同一視すると、
     * 誤って閉じただけで進行中の対局が破棄されてしまうため。
     * ダイアログ表示を伴わないため単体テストから直接検証できる。
     *
     * @param modeChoice {@link JOptionPane#showOptionDialog}の戻り値（ゲームモード選択）
     * @param timeChoice {@link JOptionPane#showOptionDialog}の戻り値（持ち時間選択）
     * @return 選択結果。キャンセルされた場合は空
     */
    static Optional<GameModeSelection.Result> resolveSelection(int modeChoice, int timeChoice) {
        return resolveSelection(modeChoice, timeChoice, 0);
    }

    /**
     * 担当色の選択を含めて選択結果を生成する。いずれかが {@link JOptionPane#CLOSED_OPTION} ならキャンセル。
     *
     * @param modeChoice  ゲームモードの選択
     * @param timeChoice  持ち時間の選択
     * @param colorChoice 担当色の選択（0=白（先手）、1=黒（後手）。AI 対戦以外では無視される）
     * @return 選択結果。キャンセルされた場合は空
     */
    static Optional<GameModeSelection.Result> resolveSelection(int modeChoice, int timeChoice, int colorChoice) {
        if (modeChoice == JOptionPane.CLOSED_OPTION || timeChoice == JOptionPane.CLOSED_OPTION
                || colorChoice == JOptionPane.CLOSED_OPTION) {
            return Optional.empty();
        }
        Color humanColor = colorChoice == 1 ? Color.BLACK : Color.WHITE;
        return Optional.of(GameModeSelection.resolve(modeChoice, timeChoice, humanColor));
    }

    /**
     * 選択インデックスから、持ち時間無しで選択結果を生成する。
     * ダイアログ表示を伴わないため単体テストから直接検証できる。
     *
     * @param modeChoice ゲームモードの選択インデックス
     * @return 選択されたモードに応じた選択結果
     */
    static GameModeSelection.Result resolveGame(int modeChoice) {
        return resolveGame(modeChoice, 0);
    }

    /**
     * 選択インデックスから選択結果を生成する。キャンセル（{@link JOptionPane#CLOSED_OPTION}）は
     * {@link #resolveSelection} で除外済みであること。
     * ダイアログ表示を伴わないため単体テストから直接検証できる。
     *
     * @param modeChoice ゲームモードの選択インデックス
     * @param timeChoice 持ち時間の選択インデックス
     * @return 選択されたモード・持ち時間に応じた選択結果
     */
    static GameModeSelection.Result resolveGame(int modeChoice, int timeChoice) {
        return GameModeSelection.resolve(modeChoice, timeChoice);
    }
}
