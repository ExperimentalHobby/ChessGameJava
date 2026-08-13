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
import com.chessgame.game.player.AIPlayer;
import com.chessgame.move.model.Move;

/**
 * バックグラウンドで選択された AI の手を、UI スレッド側で安全に適用できるかを判定し、
 * 適用可能なら盤面に反映する。Swing版（{@code SwingChessGameFrame}）・JavaFX版
 * （{@code ChessGameApp}）の両方で同一の実装が重複していたため共通化した（Issue #174）。
 *
 * <p>{@code Task}/{@code SwingWorker}/UI Node に依存しない static メソッドとして
 * 切り出すことで、New Game・Undo による {@code game} インスタンス差し替えや
 * キャンセル時の競合防止ロジックを、GUI を介さず単体で結合テストできるようにしている。</p>
 */
public final class AiMoveApplier {

    private AiMoveApplier() {
    }

    /**
     * AI 思考完了後、選択された手を適用すべきかを判定し、適用可能なら盤面に反映する。
     *
     * @param gameAtStart 思考開始時点の {@link ChessGame} インスタンス
     * @param currentGame 現在（思考完了時点）の {@link ChessGame} インスタンス
     * @param move        AI が選択した手（取得失敗時は null）
     * @param cancelled   非同期タスクがキャンセルされていたか
     * @return 手を適用した場合 true
     */
    public static boolean applyAiMoveIfStillValid(ChessGame gameAtStart, ChessGame currentGame,
                                                   Move move, boolean cancelled) {
        if (!AIPlayer.isMoveStillApplicable(move, gameAtStart, currentGame, cancelled)) {
            return false;
        }
        return currentGame.makeMove(move.getFrom(), move.getTo(), move.getPromotionPiece());
    }
}
