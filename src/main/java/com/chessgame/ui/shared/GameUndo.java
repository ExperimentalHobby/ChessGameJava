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

import com.chessgame.game.core.ChessGame;
import com.chessgame.model.Color;

/**
 * Undo（待った）の手順を Swing版・JavaFX版で共通化する。AI 対戦時に「AI の手も合わせて戻す」条件が
 * 両者で食い違っていた（片方は履歴の有無のみ、もう片方は手番も見ていた）ため、判定を一本化した。
 */
public final class GameUndo {

    private GameUndo() {
    }

    /**
     * 直前の手を取り消す。AI 対戦では、取り消し後の手番が AI の間は人間の手番まで追加で取り消す
     * （AI の手だけ戻って AI の手番に戻る、あるいは人間の手を戻しすぎる、といったことを防ぐ）。
     *
     * @param game   対象の対局
     * @param aiGame AI 対戦か
     * @return 取り消せた場合 true。履歴が無い・投了や時間切れで拒否された場合は false
     */
    public static boolean undo(ChessGame game, boolean aiGame) {
        if (game.getMoveHistory().isEmpty()) {
            return false;
        }
        // 人間が黒のとき、履歴が AI（白）の初手だけなら取り消すものが無い（戻しても AI が指し直すだけ）
        if (aiGame && game.getHumanColor() == Color.BLACK && game.getMoveHistory().size() == 1) {
            return false;
        }
        if (!game.undo()) {
            return false;
        }
        if (aiGame && !game.getMoveHistory().isEmpty() && !game.getCurrentPlayer().isHuman()) {
            game.undo();
        }
        return true;
    }
}
