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

package com.chessgame.ui.shared.board;

import com.chessgame.board.model.Position;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ClickOutcome} の値オブジェクトとしての契約（不変性）を検証する。
 */
class ClickOutcomeTest {

    @Test
    void selectedHighlightTargetsAreImmutable() {
        // Issue #235: 呼び出し側が保持するリストを渡しても、ClickOutcome 内部では
        // コピーを保持し、外から変更できないこと
        List<Position> mutableSource = new ArrayList<>(List.of(Position.of("e4"), Position.of("e5")));
        ClickOutcome outcome = ClickOutcome.selected(Position.of("e2"), mutableSource);

        assertThatThrownBy(() -> outcome.getHighlightTargets().clear())
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void selectedHighlightTargetsAreNotAffectedByMutatingTheSourceListAfterConstruction() {
        List<Position> mutableSource = new ArrayList<>(List.of(Position.of("e4")));
        ClickOutcome outcome = ClickOutcome.selected(Position.of("e2"), mutableSource);

        mutableSource.add(Position.of("e5"));

        assertThat(outcome.getHighlightTargets()).containsExactly(Position.of("e4"));
    }

    @Test
    void noneAndDeselectedAndMoveAttemptedExposeEmptyHighlightTargets() {
        assertThat(ClickOutcome.none().getHighlightTargets()).isEmpty();
        assertThat(ClickOutcome.deselected().getHighlightTargets()).isEmpty();
        assertThat(ClickOutcome.moveAttempted(true).getHighlightTargets()).isEmpty();
    }
}
