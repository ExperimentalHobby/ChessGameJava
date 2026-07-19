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

package com.chessgame.gamestate;

import com.chessgame.gamestate.model.TimeControl;
import com.chessgame.gamestate.model.TimeControlPreset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimeControlPresetTest {

    @Test
    void blitzIsThreeMinutesPlusTwoSeconds() {
        TimeControl tc = TimeControlPreset.BLITZ.toTimeControl();
        assertThat(tc.getInitialMillis()).isEqualTo(3 * 60_000L);
        assertThat(tc.getIncrementMillis()).isEqualTo(2_000L);
    }

    @Test
    void rapidIsTenMinutesPlusFiveSeconds() {
        TimeControl tc = TimeControlPreset.RAPID.toTimeControl();
        assertThat(tc.getInitialMillis()).isEqualTo(10 * 60_000L);
        assertThat(tc.getIncrementMillis()).isEqualTo(5_000L);
    }

    @Test
    void classicalIsSixtyMinutesPlusThirtySeconds() {
        TimeControl tc = TimeControlPreset.CLASSICAL.toTimeControl();
        assertThat(tc.getInitialMillis()).isEqualTo(60 * 60_000L);
        assertThat(tc.getIncrementMillis()).isEqualTo(30_000L);
    }
}
