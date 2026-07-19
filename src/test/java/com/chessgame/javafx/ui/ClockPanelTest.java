package com.chessgame.javafx.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ClockPanel のユニットテスト。
 * {@code ClockPanel} は {@code HBox} を継承しており、JavaFX Toolkit の初期化
 * （ヘッドレスCIでは行えない）が必要になるため、このテストでは一切インスタンス化しない。
 * {@link ClockPanel#formatMillis} は JavaFX に依存しない純粋関数のためこれのみ検証する。
 */
class ClockPanelTest {

    @Test
    void testFormatMillisFormatsAsMinutesSeconds() {
        assertEquals("00:00", ClockPanel.formatMillis(0));
        assertEquals("00:59", ClockPanel.formatMillis(59_999));
        assertEquals("01:00", ClockPanel.formatMillis(60_000));
        assertEquals("03:00", ClockPanel.formatMillis(180_000));
    }
}
