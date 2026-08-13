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

package com.chessgame.ui.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ClockFormat} のユニットテスト。
 * Swing版・JavaFX版の両 {@code ClockPanel} で重複していたテストを統合したもの。
 */
class ClockFormatTest {

    @Test
    void testFormatMillisFormatsAsMinutesSeconds() {
        assertEquals("00:00", ClockFormat.formatMillis(0));
        assertEquals("00:59", ClockFormat.formatMillis(59_999));
        assertEquals("01:00", ClockFormat.formatMillis(60_000));
        assertEquals("03:00", ClockFormat.formatMillis(180_000));
    }
}
