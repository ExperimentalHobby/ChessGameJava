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

package com.chessgame.board;

import com.chessgame.board.model.Position;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class PositionTest {

    @Test
    public void testPositionCreation() {
        Position pos = Position.of(0, 0);
        assertThat(pos.getRow()).isEqualTo(0);
        assertThat(pos.getCol()).isEqualTo(0);
    }

    @Test
    public void testPositionAlgebraicNotation() {
        Position pos = Position.of("e4");
        assertThat(pos.toAlgebraic()).isEqualTo("e4");
        assertThat(pos.getFile()).isEqualTo('e');
        assertThat(pos.getRank()).isEqualTo(4);
    }

    @Test
    public void testPositionEquals() {
        Position pos1 = Position.of(3, 4);
        Position pos2 = Position.of(3, 4);
        assertThat(pos1).isEqualTo(pos2);
    }

    @Test
    public void testPositionHashCode() {
        Position pos1 = Position.of(3, 4);
        Position pos2 = Position.of(3, 4);
        assertThat(pos1.hashCode()).isEqualTo(pos2.hashCode());
    }

    @Test
    public void testPositionOutOfBounds() {
        assertThatThrownBy(() -> Position.of(-1, 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Position.of(8, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testAlgebraicConversions() {
        String[] algebraics = {"a1", "a8", "e4", "h1", "h8"};
        for (String alg : algebraics) {
            Position pos = Position.of(alg);
            assertThat(pos.toAlgebraic()).isEqualTo(alg);
        }
    }

    @Test
    public void testIsValidBoundaryValues() {
        // 境界値（四隅）は盤面内
        assertThat(Position.isValid(0, 0)).isTrue();
        assertThat(Position.isValid(7, 7)).isTrue();
        assertThat(Position.isValid(0, 7)).isTrue();
        assertThat(Position.isValid(7, 0)).isTrue();

        // 範囲外（行・列それぞれの下限未満・上限以上）
        assertThat(Position.isValid(-1, 0)).isFalse();
        assertThat(Position.isValid(0, -1)).isFalse();
        assertThat(Position.isValid(8, 0)).isFalse();
        assertThat(Position.isValid(0, 8)).isFalse();
    }

    @Test
    public void testInvalidAlgebraic() {
        assertThatThrownBy(() -> Position.of("i1"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Position.of("a9"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Position.of("a"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
