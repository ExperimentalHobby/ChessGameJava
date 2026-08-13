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

package com.chessgame.notation.rules;

import com.chessgame.gamestate.model.GameState;
import com.chessgame.model.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PGN（Portable Game Notation）文字列とヘッダタグ・手順テキストを相互変換する。
 * 副作用のない静的メソッドのみで構成する（{@code com.chessgame.game.core.ChessGame} への
 * 依存を持たない。{@code ChessGame} インスタンスの生成・{@code makeMove} 実行が本質的に
 * 必要な手順再生ロジックは呼び出し側の {@code ChessGame} に残す）。
 */
public class PgnCodec {

    private static final Pattern PGN_TAG_PATTERN = Pattern.compile("\\[(\\w+)\\s+\"([^\"]*)\"\\]");
    private static final Pattern MOVE_NUMBER_TOKEN_PATTERN = Pattern.compile("^\\d+\\.+$");
    private static final Pattern NAG_TOKEN_PATTERN = Pattern.compile("^\\$\\d+$");
    private static final Set<String> PGN_RESULT_TOKENS = Set.of("1-0", "0-1", "1/2-1/2", "*");

    private PgnCodec() {
    }

    /**
     * ヘッダタグ・手順テキスト・結果から PGN 全体を組み立てる。
     * 標準開始局面でない場合（{@code startingFen} が非 null）は {@code [FEN]}/{@code [SetUp]}
     * タグを付ける。
     *
     * @param whiteName   白プレイヤー名
     * @param blackName   黒プレイヤー名
     * @param result      結果タグ（{@link #resultTag}）
     * @param startingFen 開始局面の FEN。標準開始局面なら null
     * @param movetext    手順テキスト（末尾は空白で終わっていてよい）
     * @return PGN 文字列
     */
    public static String encode(String whiteName, String blackName, String result,
                                 String startingFen, String movetext) {
        StringBuilder pgn = new StringBuilder();
        pgn.append("[Event \"Casual Game\"]\n");
        pgn.append("[Site \"?\"]\n");
        pgn.append("[Date \"????.??.??\"]\n");
        pgn.append("[Round \"?\"]\n");
        pgn.append("[White \"").append(whiteName).append("\"]\n");
        pgn.append("[Black \"").append(blackName).append("\"]\n");
        pgn.append("[Result \"").append(result).append("\"]\n");
        if (startingFen != null) {
            pgn.append("[FEN \"").append(startingFen).append("\"]\n");
            pgn.append("[SetUp \"1\"]\n");
        }
        pgn.append('\n');
        pgn.append(movetext);
        pgn.append(result);

        return pgn.toString();
    }

    /**
     * PGN のヘッダタグ（例 {@code [FEN "..."]}）から指定した名前の値を取り出す。
     * 見つからなければ null。
     *
     * @param pgn     PGN 文字列
     * @param tagName 取り出すタグ名
     * @return タグの値、見つからなければ null
     */
    public static String extractTag(String pgn, String tagName) {
        Matcher matcher = PGN_TAG_PATTERN.matcher(pgn);
        while (matcher.find()) {
            if (matcher.group(1).equals(tagName)) {
                return matcher.group(2);
            }
        }
        return null;
    }

    /**
     * movetext からコメント {@code {...}}（ネストなし）と変化手 {@code (...)}（ネスト対応）を取り除く。
     *
     * @param movetext 対象の手順テキスト
     * @return コメント・変化手を除いた手順テキスト
     */
    public static String stripCommentsAndVariations(String movetext) {
        StringBuilder result = new StringBuilder();
        int variationDepth = 0;
        boolean inComment = false;
        for (int i = 0; i < movetext.length(); i++) {
            char c = movetext.charAt(i);
            if (inComment) {
                if (c == '}') {
                    inComment = false;
                }
                continue;
            }
            if (c == '{') {
                inComment = true;
                continue;
            }
            if (c == '(') {
                variationDepth++;
                continue;
            }
            if (c == ')') {
                if (variationDepth > 0) {
                    variationDepth--;
                }
                continue;
            }
            if (variationDepth > 0) {
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }

    /**
     * PGN 文字列からヘッダタグ・コメント・変化手を取り除き、手番号・NAG・結果トークンも
     * 除外した SAN トークンのリストを返す。
     * <p>対応範囲: 標準的な手番号・SAN のみ。コメント {@code {...}}・変化手 {@code (...)}・
     * NAG（{@code $n}）は非対応（読み飛ばす）。</p>
     *
     * @param pgn 読み込む PGN 文字列
     * @return SAN トークンのリスト（手番号・NAG・結果トークンを除く）
     */
    public static List<String> tokenizeMoves(String pgn) {
        String movetext = stripCommentsAndVariations(PGN_TAG_PATTERN.matcher(pgn).replaceAll("").trim());
        List<String> tokens = new ArrayList<>();
        for (String rawToken : movetext.split("\\s+")) {
            if (rawToken.isEmpty() || PGN_RESULT_TOKENS.contains(rawToken)
                    || NAG_TOKEN_PATTERN.matcher(rawToken).matches()) {
                continue;
            }
            // "1." や "5..." のような手番号トークン、"1.e4" のように SAN に手番号が
            // 直結しているトークンの両方に対応する
            String sanToken = rawToken.replaceFirst("^\\d+\\.+", "");
            if (sanToken.isEmpty() || MOVE_NUMBER_TOKEN_PATTERN.matcher(sanToken).matches()) {
                continue;
            }
            tokens.add(sanToken);
        }
        return tokens;
    }

    /**
     * 対局の勝敗を表す PGN の結果タグ（{@code 1-0}/{@code 0-1}/{@code 1/2-1/2}/{@code *}）を返す。
     *
     * @param isGameOver   対局が終了しているか
     * @param status       現在のゲーム状態
     * @param currentColor 現在の手番の色
     * @return 結果タグ文字列
     */
    public static String resultTag(boolean isGameOver, GameState.GameStatus status, Color currentColor) {
        if (!isGameOver) {
            return "*";
        }
        if (status == GameState.GameStatus.CHECKMATE) {
            // 詰みは「王手された側（現在の手番）」の負け
            return currentColor == Color.WHITE ? "0-1" : "1-0";
        }
        if (status == GameState.GameStatus.WHITE_RESIGNED) {
            return "0-1";
        }
        if (status == GameState.GameStatus.BLACK_RESIGNED) {
            return "1-0";
        }
        if (status == GameState.GameStatus.WHITE_TIMEOUT) {
            return "0-1";
        }
        if (status == GameState.GameStatus.BLACK_TIMEOUT) {
            return "1-0";
        }
        // ステールメイト・50手ルール・千日手・戦力不足はいずれも引き分け
        return "1/2-1/2";
    }
}
