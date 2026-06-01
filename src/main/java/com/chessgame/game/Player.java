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

package com.chessgame.game;

import com.chessgame.model.Color;
import java.util.Objects;

/**
 * チェスのプレイヤーを表すクラス。色・名前・人間かAIかのフラグを保持する。
 */
public class Player {
    private final Color color;
    private final String name;
    private final boolean isHuman;

    /**
     * プレイヤーを生成する。
     *
     * @param color   担当する色
     * @param name    プレイヤー名
     * @param isHuman 人間プレイヤーなら true、AI なら false
     */
    public Player(Color color, String name, boolean isHuman) {
        this.color = Objects.requireNonNull(color);
        this.name = Objects.requireNonNull(name);
        this.isHuman = isHuman;
    }

    /**
     * 人間プレイヤーを生成する。
     *
     * @param color 担当する色
     * @param name  プレイヤー名
     * @return 人間プレイヤー
     */
    public static Player human(Color color, String name) {
        return new Player(color, name, true);
    }

    /**
     * プレイヤーの色を返す。
     *
     * @return {@link Color}
     */
    public Color getColor() {
        return color;
    }

    /**
     * プレイヤー名を返す。
     *
     * @return プレイヤー名
     */
    public String getName() {
        return name;
    }

    /**
     * 人間プレイヤーかどうかを返す。
     *
     * @return 人間なら true
     */
    public boolean isHuman() {
        return isHuman;
    }

    /**
     * AIプレイヤーかどうかを返す。
     *
     * @return AI なら true
     */
    public boolean isAI() {
        return !isHuman;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return color == player.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color);
    }

    @Override
    public String toString() {
        return name + " (" + color + ")";
    }
}
