package com.chessgame.swing.ui.panel;

import com.chessgame.game.core.ChessGame;

import javax.swing.*;
import java.awt.*;

/**
 * 棋譜（手の履歴）を「1. e2e4 e7e5 2. ...」形式で表示するパネル。
 * スクロール可能な非編集テキストエリアに表示し、手が進むたびに末尾へ自動スクロールする。
 */
public class MoveHistoryPanel extends JPanel {
    private ChessGame game;
    private final JTextArea textArea;

    /**
     * 指定したゲームに紐づいた棋譜パネルを生成する。
     *
     * @param game 表示対象の {@link ChessGame}
     */
    public MoveHistoryPanel(ChessGame game) {
        this.game = game;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Moves"));
        setPreferredSize(new Dimension(160, 0));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        add(new JScrollPane(textArea), BorderLayout.CENTER);
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
        textArea.setCaretPosition(textArea.getDocument().getLength());
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
