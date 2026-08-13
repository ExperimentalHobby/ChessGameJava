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

/**
 * UI 層のタイマー間隔を定義する。Swing版・JavaFX版の両フレームで同一値の定数が
 * 重複していたため共通化した（Issue #174）。
 */
public final class GameTimings {

    /** AI が手を指すまでの遅延（ミリ秒）。即時実行だと UI 更新が追いつかないため遅延させる。 */
    public static final int AI_MOVE_DELAY_MS = 800;

    /** 持ち時間の残り時間表示・時間切れ検出をポーリングする間隔（ミリ秒）。 */
    public static final int CLOCK_TICK_MS = 200;

    private GameTimings() {
    }
}
