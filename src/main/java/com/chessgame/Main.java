package com.chessgame;

import com.chessgame.swing.ui.SwingChessGameFrame;
import javax.swing.SwingUtilities;

/**
 * Swing GUI を起動するアプリケーションのエントリーポイント。
 */
public class Main {
    /**
     * アプリケーションを起動する。EDT スレッドで {@link com.chessgame.swing.ui.SwingChessGameFrame} を表示する。
     *
     * @param args コマンドライン引数（未使用）
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingChessGameFrame frame = new SwingChessGameFrame();
            frame.setVisible(true);
        });
    }
}
