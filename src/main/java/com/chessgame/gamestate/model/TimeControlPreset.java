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
 * よく使われる持ち時間ルールのプリセット。
 */
public enum TimeControlPreset {
    /** 3分 + 1手ごとに2秒加算。 */
    BLITZ(3 * 60_000L, 2_000L),
    /** 10分 + 1手ごとに5秒加算。 */
    RAPID(10 * 60_000L, 5_000L),
    /** 60分 + 1手ごとに30秒加算。 */
    CLASSICAL(60 * 60_000L, 30_000L);

    private final long initialMillis;
    private final long incrementMillis;

    TimeControlPreset(long initialMillis, long incrementMillis) {
        this.initialMillis = initialMillis;
        this.incrementMillis = incrementMillis;
    }

    /**
     * このプリセットに対応する {@link TimeControl} を返す。
     *
     * @return 対応する {@link TimeControl}
     */
    public TimeControl toTimeControl() {
        return new TimeControl(initialMillis, incrementMillis);
    }
}
