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

package com.chessgame.move.model;

/**
 * チェスの手の種類を表す列挙型。
 */
public enum MoveType {
    /** 通常移動（駒を取らない移動）。 */
    NORMAL,
    /** 駒を取る通常の捕獲。 */
    CAPTURE,
    /** キャスリング（キングとルークの特殊移動）。 */
    CASTLING,
    /** アンパッサン（ポーンの特殊な捕獲）。 */
    EN_PASSANT,
    /** ポーンが最終ランクに達したときの昇格。 */
    PROMOTION;

    // 列挙定数名を小文字に変換して返す（例: NORMAL → "normal"）
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
