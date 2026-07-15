package com.chessgame.swing.ui.dialog;

import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;

import javax.swing.*;

/**
 * ゲームモード選択ダイアログ（Human vs Human / AI 難易度4段階）。
 * 選択結果に応じて新しい ChessGame インスタンスを生成して返す。
 */
public class GameModeDialog {
    private static boolean isAIGame = false;

    /**
     * ゲームモード選択ダイアログを表示し、選択されたゲームを返す。
     *
     * @param parentFrame 親フレーム（ダイアログのオーナー）
     * @return 選択されたモードに応じた ChessGame インスタンス
     */
    public static ChessGame showDialog(JFrame parentFrame) {
        Object[] options = {"Human vs Human", "Human vs AI（Easy）", "Human vs AI（Medium）",
            "Human vs AI（Hard）", "Human vs AI（Expert）"};
        int choice = JOptionPane.showOptionDialog(parentFrame,
            "ゲームモードを選択してください",
            "ゲームモード選択",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        return resolveGame(choice);
    }

    /**
     * JOptionPaneの選択結果(CLOSED_OPTIONを含む)からゲームを生成する。
     * ダイアログ表示を伴わないため単体テストから直接検証できる。
     *
     * @param choice {@link JOptionPane#showOptionDialog}の戻り値
     * @return 選択されたモードに応じたChessGameインスタンス
     */
    static ChessGame resolveGame(int choice) {
        if (choice == JOptionPane.CLOSED_OPTION) choice = 0;

        isAIGame = (choice != 0);
        if (choice == 0) {
            return ChessGame.createTwoPlayerGame("White", "Black");
        } else {
            return createAIGame(choice);
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
     * @param difficulty AI の難易度（1=Easy, 2=Medium, 3=Hard, 4=Expert）
     * @return AI 対戦ゲーム
     */
    private static ChessGame createAIGame(int difficulty) {
        Player whitePlayer = Player.human(Color.WHITE, "You");
        Player blackPlayer = new AIPlayer("AI", Color.BLACK, difficulty);
        return new ChessGame(whitePlayer, blackPlayer);
    }
}
