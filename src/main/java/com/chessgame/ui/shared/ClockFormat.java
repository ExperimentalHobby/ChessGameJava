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

/**
 * 残り時間表示のフォーマットを行う。Swing版・JavaFX版の両 {@code ClockPanel} で
 * 同一の実装が重複していたため共通化した（Issue #174）。
 */
public final class ClockFormat {

    private ClockFormat() {
    }

    /**
     * ミリ秒を {@code mm:ss} 形式の文字列に変換する。
     *
     * @param millis 変換する時間（ミリ秒）
     * @return {@code mm:ss}形式の文字列
     */
    public static String formatMillis(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
