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

package com.chessgame.game.observer;

import com.chessgame.model.Color;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.move.model.Move;

/**
 * チェスゲームのイベントを受け取るオブザーバーインターフェース。
 * UIやログなどゲームロジックと疎結合に連携したいクラスが実装する。
 */
public interface GameObserver {

    /** 盤面の状態が変化したときに呼ばれる。 */
    void onBoardChanged();

    /**
     * 手が確定したときに呼ばれる。
     *
     * @param move 実行された手
     */
    void onMoveMade(Move move);

    /**
     * ゲームの進行状況が変化したときに呼ばれる。
     *
     * @param newStatus 新しい {@link GameState.GameStatus}
     */
    void onGameStateChanged(GameState.GameStatus newStatus);

    /**
     * 王手が検出されたときに呼ばれる。
     *
     * @param kingColor 王手されているキングの色
     */
    void onCheckDetected(Color kingColor);

    /**
     * ゲームが終了したときに呼ばれる。
     *
     * @param winner 勝者の色。引き分けの場合は null
     */
    void onGameOver(Color winner);
}
