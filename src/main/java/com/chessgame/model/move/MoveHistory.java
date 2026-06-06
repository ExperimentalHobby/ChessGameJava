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

package com.chessgame.model.move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ゲーム中に行われた全移動を記録するクラス。
 * 手戻り（undo）と棋譜文字列の生成をサポートする。
 */
public class MoveHistory {
    private final List<Move> moves;

    /**
     * 空の移動履歴を生成する。
     */
    public MoveHistory() {
        this.moves = new ArrayList<>();
    }

    /**
     * 移動を履歴に追加する。null の場合は無視する。
     *
     * @param move 追加する移動
     */
    public void addMove(Move move) {
        if (move != null) {
            moves.add(move);
        }
    }

    /**
     * 最後に記録された移動を返す。履歴が空の場合は null。
     *
     * @return 最後の {@link Move}、または null
     */
    public Move getLastMove() {
        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(moves.size() - 1);
    }

    /**
     * 最後の移動を履歴から削除して返す。履歴が空の場合は null。
     *
     * @return 削除した {@link Move}、または null
     */
    public Move undoLastMove() {
        if (moves.isEmpty()) {
            return null;
        }
        return moves.remove(moves.size() - 1);
    }

    /**
     * 記録済みの移動数を返す。
     *
     * @return 移動数
     */
    public int size() {
        return moves.size();
    }

    /**
     * 移動履歴が空かどうかを返す。
     *
     * @return 空であれば true
     */
    public boolean isEmpty() {
        return moves.isEmpty();
    }

    /**
     * 全移動の変更不可能なリストを返す。
     *
     * @return 全移動のリスト（変更不可）
     */
    public List<Move> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(moves));
    }

    /**
     * 移動履歴を「1. e2e4 e7e5 2. ...」形式の文字列で返す。
     *
     * @return 棋譜文字列
     */
    public String getNotationString() {
        StringBuilder sb = new StringBuilder();
        int moveNumber = 1;

        for (int i = 0; i < moves.size(); i += 2) {
            sb.append(moveNumber).append(". ");
            sb.append(moves.get(i).toString());

            if (i + 1 < moves.size()) {
                sb.append(" ").append(moves.get(i + 1).toString()).append(" ");
            }
            moveNumber++;
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "MoveHistory{" +
                "moves=" + moves +
                ", size=" + moves.size() +
                '}';
    }
}
