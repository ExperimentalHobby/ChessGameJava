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

import com.chessgame.game.player.Player;
import com.chessgame.gamestate.model.TimeControl;
import java.util.function.LongSupplier;

/**
 * テスト専用ファクトリ。{@link ChessGame} の現在時刻取得を差し替え可能な完全コンストラクタは
 * package-private のため、他パッケージのテスト（例: {@code swing.ui.panel} 配下）から
 * 偽クロックで {@link ChessGame} を構築できるようにするブリッジとして使う。
 *
 * <p>実時刻に依存した {@code Thread.sleep} を避け、経過時間をシミュレートして
 * 持ち時間切れ等を決定的に再現するために使う。{@code src/test/java} 配下のため
 * 本番の public API には影響しない。</p>
 */
public final class ChessGameTestFactory {
    private ChessGameTestFactory() {
    }

    /**
     * 現在時刻取得を差し替えた {@link ChessGame} を生成する。
     *
     * @param whitePlayer 白プレイヤー
     * @param blackPlayer 黒プレイヤー
     * @param timeControl 持ち時間ルール。時間管理無しの対局なら null
     * @param nowMillis   現在時刻（エポックミリ秒）を返す関数
     * @return 偽クロックを使う {@link ChessGame}
     */
    public static ChessGame withFakeClock(Player whitePlayer, Player blackPlayer,
                                           TimeControl timeControl, LongSupplier nowMillis) {
        return new ChessGame(whitePlayer, blackPlayer, timeControl, nowMillis);
    }
}
