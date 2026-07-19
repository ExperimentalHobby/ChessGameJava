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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeControlTest {

    @Test
    void constructorRejectsNonPositiveInitialMillis() {
        assertThatThrownBy(() -> new TimeControl(0, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorRejectsNegativeIncrementMillis() {
        assertThatThrownBy(() -> new TimeControl(1000, -1))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
