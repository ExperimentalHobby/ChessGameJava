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

package com.chessgame.gamestate.model;

/**
 * 持ち時間ルール（初期持ち時間・1手ごとの加算）を表すイミュータブルな値オブジェクト。
 */
public final class TimeControl {
    private final long initialMillis;
    private final long incrementMillis;

    /**
     * 持ち時間ルールを生成する。
     *
     * @param initialMillis   初期持ち時間（ミリ秒、正の値）
     * @param incrementMillis 1手ごとの加算時間（ミリ秒、0以上）
     */
    public TimeControl(long initialMillis, long incrementMillis) {
        if (initialMillis <= 0) {
            throw new IllegalArgumentException("initialMillis must be positive: " + initialMillis);
        }
        if (incrementMillis < 0) {
            throw new IllegalArgumentException("incrementMillis must not be negative: " + incrementMillis);
        }
        this.initialMillis = initialMillis;
        this.incrementMillis = incrementMillis;
    }

    /**
     * 初期持ち時間を返す。
     *
     * @return 初期持ち時間（ミリ秒）
     */
    public long getInitialMillis() {
        return initialMillis;
    }

    /**
     * 1手ごとの加算時間を返す。
     *
     * @return 加算時間（ミリ秒）
     */
    public long getIncrementMillis() {
        return incrementMillis;
    }
}
