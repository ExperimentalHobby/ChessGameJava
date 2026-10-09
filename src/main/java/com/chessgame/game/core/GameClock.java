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

package com.chessgame.game.core;

import com.chessgame.gamestate.model.GameState;
import com.chessgame.gamestate.model.TimeControl;
import com.chessgame.model.Color;
import java.util.function.LongSupplier;

/**
 * 持ち時間ルールの管理を担う。{@link ChessGame} から分離した責務で、持ち時間ルール・現在時刻・
 * 現在の手番の思考開始時刻を保持し、残り時間の算出・着手時の消費と加算・時間切れ判定を行う。
 * 残り時間そのものは {@link GameState} が保持するため、状態を引数で受け取る。
 *
 * <p>現在時刻の取得は注入可能で、テストでは実時刻に依存せず経過時間をシミュレートできる。</p>
 */
final class GameClock {
    /** 持ち時間ルール。時間管理無しの対局なら null。 */
    private final TimeControl timeControl;
    private final LongSupplier nowMillis;
    /** 現在の手番の思考開始時刻（{@link #nowMillis} 基準）。 */
    private long turnStartMillis;

    /**
     * 時計を生成する。思考開始時刻は生成時の現在時刻になる。
     *
     * @param timeControl 持ち時間ルール。時間管理無しなら null
     * @param nowMillis   現在時刻（エポックミリ秒）を返す関数
     */
    GameClock(TimeControl timeControl, LongSupplier nowMillis) {
        this.timeControl = timeControl;
        this.nowMillis = nowMillis;
        this.turnStartMillis = nowMillis.getAsLong();
    }

    /** 持ち時間ルールが設定されているかを返す。 */
    boolean isEnabled() {
        return timeControl != null;
    }

    /**
     * 持ち時間を初期値に張り直し、思考開始時刻を現在時刻へ戻す。
     * {@link GameState#resetGame()} は持ち時間を消すところまでしか行わない
     * （{@link GameState} は {@link TimeControl} を保持しておらず初期値を知らないため）。
     * 思考開始時刻を戻さないと、前局からの実経過時間が新規対局の初手に課金され、
     * 放置後の New Game が即座に時間切れ判定されてしまう。
     */
    void reset(GameState state) {
        if (timeControl != null) {
            state.initializeClock(timeControl);
        }
        turnStartMillis = nowMillis.getAsLong();
    }

    /**
     * 思考開始時刻だけを現在時刻へ戻す。undo 判断にかかった時間を次の一手に課金しないために使う
     * （残り時間の巻き戻しは行わない）。
     */
    void restartTurn() {
        turnStartMillis = nowMillis.getAsLong();
    }

    /**
     * 指定した色の残り時間を返す。現在の手番の色なら、思考中の経過時間を差し引いたライブ値。
     *
     * @param state 対局状態（保存済みの残り時間と手番を持つ）
     * @param color 対象の色
     * @return 残り時間（ミリ秒）。持ち時間ルールが無ければ 0
     */
    long liveRemainingMillis(GameState state, Color color) {
        long stored = state.getRemainingMillis(color);
        if (timeControl != null && color == state.getCurrentPlayerColor()) {
            return Math.max(0, stored - (nowMillis.getAsLong() - turnStartMillis));
        }
        return stored;
    }

    /**
     * 着手が確定したとき、手番側の思考時間を消費し加算時間を足して、次の手番の計測を始める。
     * 持ち時間ルールが無ければ何もしない。
     */
    void chargeMove(GameState state, Color mover) {
        if (timeControl == null) {
            return;
        }
        long now = nowMillis.getAsLong();
        state.consumeTime(mover, now - turnStartMillis);
        state.addIncrement(mover);
        turnStartMillis = now;
    }

    /**
     * 現在の手番側の持ち時間が切れているかを返す（ルールが無ければ常に false は呼び出し側で判定する）。
     *
     * @param state   対局状態
     * @param current 現在の手番の色
     * @return 思考中の経過時間が保存済みの残り時間以上なら true
     */
    boolean isExpired(GameState state, Color current) {
        return nowMillis.getAsLong() - turnStartMillis >= state.getRemainingMillis(current);
    }
}
