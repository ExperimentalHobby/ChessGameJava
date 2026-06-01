package com.chessgame.javafx;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JavaFX アプリケーションのランチャークラス。
 * <p>
 * Java 11 以降、JVM はメインクラスが {@code javafx.application.Application} を継承している場合、
 * {@code main()} 呼び出し前に JavaFX ランタイムの存在チェックを行う。
 * このチェックが失敗すると "JavaFX runtime components are missing" エラーが発生し、
 * コンソールのない GUI exe では無症状で終了する。
 * <p>
 * このクラスは {@code Application} を継承しないためチェックをバイパスし、
 * {@link ChessGameApp#main(String[])} 経由で {@code Application.launch()} を呼ぶ。
 * 初期化中に例外が発生した場合は {@code ~/ChessGameFX-error.log} に記録する。
 */
public class FXLauncher {

    /**
     * アプリケーションのエントリーポイント。
     *
     * @param args コマンドライン引数（JavaFX へそのまま転送される）
     */
    public static void main(String[] args) {
        try {
            ChessGameApp.main(args);
        } catch (Throwable t) {
            // コンソールのない GUI exe のため、エラーをホームディレクトリのログファイルに記録する
            Path log = Path.of(System.getProperty("user.home"), "ChessGameFX-error.log");
            try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(log))) {
                t.printStackTrace(pw);
            } catch (IOException ignored) {}
        }
    }
}
