package com.chessgame.javafx.ui;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link StatusBar} の終局メッセージ文言の単体テスト。
 * {@code StatusBar} は JavaFX Toolkit が必要な Control のため、インスタンス化せず
 * static の文言生成メソッドのみを検証する。
 */
class StatusBarTest {

    // Issue #263: 投了・時間切れが「CHECKMATE!」と誤表示されていた
    @Test
    void resignMessageNamesResignationAndWinner() {
        assertThat(StatusBar.resignMessage("Black")).isEqualTo("RESIGNED! Black wins!");
    }

    @Test
    void timeoutMessageNamesTimeoutAndWinner() {
        assertThat(StatusBar.timeoutMessage("White")).isEqualTo("TIME OUT! White wins!");
    }
}
