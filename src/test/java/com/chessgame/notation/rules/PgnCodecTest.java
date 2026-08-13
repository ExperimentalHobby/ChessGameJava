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
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PgnCodec} のユニットテスト。
 */
class PgnCodecTest {

    @Test
    void testResultTagReturnsAsteriskWhenGameNotOver() {
        String result = PgnCodec.resultTag(false, GameState.GameStatus.IN_PROGRESS, Color.WHITE);

        assertThat(result).isEqualTo("*");
    }

    /** チェックメイト: 現在の手番（王手された側）が黒なら白の勝ち。 */
    @Test
    void testResultTagCheckmateWithBlackToMoveIsWhiteWin() {
        String result = PgnCodec.resultTag(true, GameState.GameStatus.CHECKMATE, Color.BLACK);

        assertThat(result).isEqualTo("1-0");
    }

    /** チェックメイト: 現在の手番（王手された側）が白なら黒の勝ち。 */
    @Test
    void testResultTagCheckmateWithWhiteToMoveIsBlackWin() {
        String result = PgnCodec.resultTag(true, GameState.GameStatus.CHECKMATE, Color.WHITE);

        assertThat(result).isEqualTo("0-1");
    }

    @Test
    void testResultTagWhiteResignedIsBlackWin() {
        String result = PgnCodec.resultTag(true, GameState.GameStatus.WHITE_RESIGNED, Color.BLACK);

        assertThat(result).isEqualTo("0-1");
    }

    @Test
    void testResultTagBlackResignedIsWhiteWin() {
        String result = PgnCodec.resultTag(true, GameState.GameStatus.BLACK_RESIGNED, Color.WHITE);

        assertThat(result).isEqualTo("1-0");
    }

    @Test
    void testResultTagWhiteTimeoutIsBlackWin() {
        String result = PgnCodec.resultTag(true, GameState.GameStatus.WHITE_TIMEOUT, Color.BLACK);

        assertThat(result).isEqualTo("0-1");
    }

    @Test
    void testResultTagBlackTimeoutIsWhiteWin() {
        String result = PgnCodec.resultTag(true, GameState.GameStatus.BLACK_TIMEOUT, Color.WHITE);

        assertThat(result).isEqualTo("1-0");
    }

    @Test
    void testResultTagStalemateIsDraw() {
        String result = PgnCodec.resultTag(true, GameState.GameStatus.STALEMATE, Color.WHITE);

        assertThat(result).isEqualTo("1/2-1/2");
    }

    @Test
    void testResultTagFiftyMoveRuleIsDraw() {
        String result = PgnCodec.resultTag(true, GameState.GameStatus.FIFTY_MOVE_RULE, Color.WHITE);

        assertThat(result).isEqualTo("1/2-1/2");
    }

    @Test
    void testResultTagThreefoldRepetitionIsDraw() {
        String result = PgnCodec.resultTag(true, GameState.GameStatus.THREEFOLD_REPETITION, Color.WHITE);

        assertThat(result).isEqualTo("1/2-1/2");
    }

    @Test
    void testResultTagInsufficientMaterialIsDraw() {
        String result = PgnCodec.resultTag(true, GameState.GameStatus.INSUFFICIENT_MATERIAL, Color.WHITE);

        assertThat(result).isEqualTo("1/2-1/2");
    }

    @Test
    void testExtractTagFindsMatchingTagValue() {
        String pgn = "[Event \"Casual Game\"]\n[FEN \"8/8/8/8/8/8/8/4K3 w - - 0 1\"]\n\n1. e4 *";

        String fen = PgnCodec.extractTag(pgn, "FEN");

        assertThat(fen).isEqualTo("8/8/8/8/8/8/8/4K3 w - - 0 1");
    }

    @Test
    void testExtractTagReturnsNullWhenTagAbsent() {
        String pgn = "[Event \"Casual Game\"]\n\n1. e4 *";

        String fen = PgnCodec.extractTag(pgn, "FEN");

        assertThat(fen).isNull();
    }

    @Test
    void testStripCommentsAndVariationsRemovesBraceComments() {
        String result = PgnCodec.stripCommentsAndVariations("1. e4 {good move} e5");

        assertThat(result).isEqualTo("1. e4  e5");
    }

    @Test
    void testStripCommentsAndVariationsRemovesNestedVariations() {
        String result = PgnCodec.stripCommentsAndVariations("1. e4 (1. d4 d5) e5");

        assertThat(result).isEqualTo("1. e4  e5");
    }

    @Test
    void testStripCommentsAndVariationsHandlesCommentsAndVariationsTogether() {
        String result = PgnCodec.stripCommentsAndVariations("1. e4 {comment} (1. d4) e5 $1");

        assertThat(result).isEqualTo("1. e4   e5 $1");
    }

    @Test
    void testTokenizeMovesReturnsSanTokensWithoutMoveNumbers() {
        String pgn = "[Event \"Casual Game\"]\n\n1. e4 e5 2. Nf3 Nc6 1-0";

        List<String> tokens = PgnCodec.tokenizeMoves(pgn);

        assertThat(tokens).containsExactly("e4", "e5", "Nf3", "Nc6");
    }

    @Test
    void testTokenizeMovesSkipsCommentsVariationsAndNag() {
        String pgn = "[Event \"Casual Game\"]\n\n1. e4 {good} e5 $1 (1... c5 2. Nf3) 2. Nf3 *";

        List<String> tokens = PgnCodec.tokenizeMoves(pgn);

        assertThat(tokens).containsExactly("e4", "e5", "Nf3");
    }

    @Test
    void testTokenizeMovesHandlesBlackStartingMoveNumberToken() {
        // FEN読み込みで黒番スタートの場合、"5..." のような手番号トークンが単独で現れる
        String pgn = "[Event \"Casual Game\"]\n\n5... Qh4 6. g3 *";

        List<String> tokens = PgnCodec.tokenizeMoves(pgn);

        assertThat(tokens).containsExactly("Qh4", "g3");
    }

    @Test
    void testEncodeContainsHeaderTagsMovetextAndResult() {
        String pgn = PgnCodec.encode("Alice", "Bob", "1-0", null, "1. e4 e5 2. Nf3 ");

        assertThat(pgn).contains("[White \"Alice\"]");
        assertThat(pgn).contains("[Black \"Bob\"]");
        assertThat(pgn).contains("[Result \"1-0\"]");
        assertThat(pgn).contains("1. e4 e5 2. Nf3 ");
        assertThat(pgn).endsWith("1-0");
        assertThat(pgn).doesNotContain("[FEN");
        assertThat(pgn).doesNotContain("[SetUp");
    }

    @Test
    void testEncodeIncludesFenAndSetUpTagsWhenStartingFenGiven() {
        String startingFen = "4k3/8/8/8/8/8/8/4K3 w - - 0 1";

        String pgn = PgnCodec.encode("Alice", "Bob", "*", startingFen, "1. Kd2 *");

        assertThat(pgn).contains("[FEN \"" + startingFen + "\"]");
        assertThat(pgn).contains("[SetUp \"1\"]");
    }
}
