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

package com.chessgame.javafx.board;

import com.chessgame.board.model.Position;

import java.util.Collections;
import java.util.List;

/**
 * {@link BoardSelectionController#handleClick} の結果を表す値オブジェクト（JavaFX非依存）。
 * {@link ChessBoardView} はこの結果に応じて描画のみを行う。
 */
final class ClickOutcome {

    /** クリック結果の種類。 */
    enum Type {
        /** 何も起きなかった（ゲーム終了・AI手番・無効なクリック）。 */
        NONE,
        /** 駒を選択した。 */
        SELECTED,
        /** 選択を解除した。 */
        DESELECTED,
        /** 移動を試みた（成否は {@link #isMoveSucceeded()} で判定）。 */
        MOVE_ATTEMPTED
    }

    private final Type type;
    private final Position position;
    private final List<Position> highlightTargets;
    private final boolean moveSucceeded;

    private ClickOutcome(Type type, Position position, List<Position> highlightTargets, boolean moveSucceeded) {
        this.type = type;
        this.position = position;
        this.highlightTargets = highlightTargets;
        this.moveSucceeded = moveSucceeded;
    }

    static ClickOutcome none() {
        return new ClickOutcome(Type.NONE, null, Collections.emptyList(), false);
    }

    static ClickOutcome selected(Position position, List<Position> highlightTargets) {
        return new ClickOutcome(Type.SELECTED, position, highlightTargets, false);
    }

    static ClickOutcome deselected() {
        return new ClickOutcome(Type.DESELECTED, null, Collections.emptyList(), false);
    }

    static ClickOutcome moveAttempted(boolean succeeded) {
        return new ClickOutcome(Type.MOVE_ATTEMPTED, null, Collections.emptyList(), succeeded);
    }

    Type getType() {
        return type;
    }

    /** {@link Type#SELECTED} のときに選択されたマスの位置を返す。それ以外は null。 */
    Position getPosition() {
        return position;
    }

    /** {@link Type#SELECTED} のときにハイライトすべき移動先一覧を返す。それ以外は空リスト。 */
    List<Position> getHighlightTargets() {
        return highlightTargets;
    }

    /** {@link Type#MOVE_ATTEMPTED} のときに移動が成立したかどうかを返す。 */
    boolean isMoveSucceeded() {
        return moveSucceeded;
    }
}
