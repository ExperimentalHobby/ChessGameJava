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
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;

/**
 * PGN 文字列から対局を読み込み、失敗を利用者向けのメッセージにまとめる。Swing版・JavaFX版の
 * {@code openPgn()} で例外の扱いが食い違っていた（Swing だけ想定外の例外を可視化していた）ため、
 * 方針をここに一本化した。
 *
 * <p>入力の不正（{@link IllegalArgumentException}）は「不正なPGN形式」、それ以外の実行時例外は
 * 実装側の想定漏れなので例外の型まで含めて可視化する。握りつぶすと原因不明のまま挙動が
 * おかしくなるため（Issue #240）。</p>
 */
public final class PgnLoader {

    private PgnLoader() {
    }

    /**
     * PGN の読み込み結果。成功なら {@link #game()}、失敗なら {@link #errorMessage()} が設定される。
     *
     * @param game         読み込んだ対局。失敗時は null
     * @param errorMessage 利用者に表示するエラーメッセージ。成功時は null
     */
    public record Result(ChessGame game, String errorMessage) {

        /**
         * 読み込みに成功したかを返す。
         *
         * @return 成功なら true
         */
        public boolean isLoaded() {
            return game != null;
        }
    }

    /**
     * PGN を人間 vs 人間の対局として読み込む。PGN には難易度・AI の情報が保存されないため、
     * 元が AI 対戦でも常に人間 vs 人間として復元する。
     *
     * @param pgn 読み込む PGN 文字列
     * @return 読み込み結果
     */
    public static Result load(String pgn) {
        try {
            ChessGame loaded = ChessGame.fromPgn(pgn,
                Player.human(Color.WHITE, "White"), Player.human(Color.BLACK, "Black"));
            return new Result(loaded, null);
        } catch (IllegalArgumentException e) {
            return new Result(null, "不正なPGN形式です: " + e.getMessage());
        } catch (RuntimeException e) {
            return new Result(null, "PGNの読み込み中に予期しないエラーが発生しました: " + e);
        }
    }
}
