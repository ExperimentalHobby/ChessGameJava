package com.chessgame.ui.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PgnLoader} のユニットテスト。Swing/JavaFX で共有する PGN 読み込みの例外処理方針を固定する。
 */
class PgnLoaderTest {

    @Test
    void loadsValidPgnAsHumanVsHumanGame() {
        PgnLoader.Result result = PgnLoader.load("1. e4 e5 2. Nf3 *");

        assertThat(result.isLoaded()).isTrue();
        assertThat(result.errorMessage()).isNull();
        assertThat(result.game().getMoveHistory().size()).isEqualTo(3);
        assertThat(result.game().getBlackPlayer().isAI()).isFalse();
    }

    @Test
    void reportsInvalidPgnWithItsReason() {
        PgnLoader.Result result = PgnLoader.load("1. Qh8 *");

        assertThat(result.isLoaded()).isFalse();
        assertThat(result.errorMessage()).startsWith("不正なPGN形式です: ").contains("Qh8");
    }

    @Test
    void reportsUnexpectedRuntimeExceptionWithItsTypeInsteadOfCallingItInvalidPgn() {
        // Issue #240 の方針: 想定外の例外を「不正なPGN」に丸めず、型まで出して可視化する
        PgnLoader.Result result = PgnLoader.load(null);

        assertThat(result.isLoaded()).isFalse();
        assertThat(result.errorMessage()).contains("予期しないエラー").contains("NullPointerException");
    }
}
