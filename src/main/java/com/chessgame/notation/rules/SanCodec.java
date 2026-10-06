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

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SAN（Standard Algebraic Notation、例 {@code Nf3}・{@code O-O}・{@code exd5=Q+}）と
 * {@link Move} を相互変換する。副作用のない静的メソッドのみで構成する。
 */
public class SanCodec {

    /** {@code =} を省略した昇格（{@code e8Q}・{@code exd8N}）。 */
    private static final Pattern PROMOTION_WITHOUT_EQUALS = Pattern.compile("((?:[a-h]x)?[a-h][18])([QRBNqrbn])");
    /** 駒の手: 駒種・曖昧回避ヒント（ファイル/ランク/マス）・取る記号・移動先。 */
    private static final Pattern PIECE_MOVE = Pattern.compile("([NBRQK])([a-h]?[1-8]?)(x?)([a-h][1-8])");

    private SanCodec() {
    }

    /**
     * 手を SAN 文字列に変換する。
     *
     * @param boardBeforeMove          手を適用する前の盤面
     * @param move                     変換する手
     * @param allLegalMovesForMovingSide 手番側の全合法手（曖昧回避の判定に使用）
     * @param isCheck                  この手の後に相手が王手になるか
     * @param isCheckmate              この手の後に相手がチェックメイトになるか
     * @return SAN 文字列
     */
    public static String encode(Board boardBeforeMove, Move move, List<Move> allLegalMovesForMovingSide,
                                 boolean isCheck, boolean isCheckmate) {
        String core = encodeCore(boardBeforeMove, move, allLegalMovesForMovingSide);
        if (isCheckmate) {
            return core + "#";
        }
        if (isCheck) {
            return core + "+";
        }
        return core;
    }

    /**
     * SAN 文字列に一致する手を、合法手リストから探す。
     * 各合法手を {@link #encodeCore} で（注釈無しの）SAN に変換し、一致するものを返す。
     *
     * <p>対応する注釈（いずれも手の同一性には影響しないため取り除く）:</p>
     * <ul>
     *   <li>王手・詰み記号: {@code +}、{@code #}、{@code ++}（ダブルチェック）</li>
     *   <li>評価記号（サフィックス注釈）: {@code !}、{@code ?}、{@code !?}、{@code ??} など</li>
     *   <li>アンパッサンの明示: {@code e.p.}（直前の空白の有無は問わない）</li>
     * </ul>
     *
     * @param san                      解決したい SAN 文字列
     * @param boardBeforeMove          手を適用する前の盤面
     * @param allLegalMovesForMovingSide 手番側の全合法手
     * @return 一致する {@link Move}、見つからなければ null
     */
    public static Move decode(String san, Board boardBeforeMove, List<Move> allLegalMovesForMovingSide) {
        String stripped = stripAnnotations(san);
        // 厳密な一致を最優先し、外部ツールの表記ゆれ（0-0・= 省略の昇格）は正規化して再照合する
        Move exact = decodeExact(stripped, boardBeforeMove, allLegalMovesForMovingSide);
        if (exact != null) {
            return exact;
        }
        String normalized = normalizeLenientNotation(stripped);
        if (!normalized.equals(stripped)) {
            Move normalizedMatch = decodeExact(normalized, boardBeforeMove, allLegalMovesForMovingSide);
            if (normalizedMatch != null) {
                return normalizedMatch;
            }
        }
        return decodeWithRedundantDisambiguation(normalized, allLegalMovesForMovingSide, boardBeforeMove);
    }

    private static Move decodeExact(String normalized, Board boardBeforeMove, List<Move> allLegalMoves) {
        for (Move candidate : allLegalMoves) {
            if (encodeCore(boardBeforeMove, candidate, allLegalMoves).equals(normalized)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 外部ツールが出力する表記ゆれを標準の SAN に直す。数字のゼロを使うキャスリング
     * ({@code 0-0}/{@code 0-0-0}) と、{@code =} を省略した昇格 ({@code e8Q}/{@code exd8N}) を対象にする。
     */
    private static String normalizeLenientNotation(String san) {
        if (san.matches("0-0(-0)?")) {
            return san.replace('0', 'O');
        }
        Matcher promotion = PROMOTION_WITHOUT_EQUALS.matcher(san);
        if (promotion.matches()) {
            return promotion.group(1) + "=" + promotion.group(2).toUpperCase(Locale.ROOT);
        }
        return san;
    }

    /**
     * 曖昧でない手に付けられた不要な曖昧回避（{@code Ngf3}・{@code N1f3}・{@code Ng1f3}）を許容して解決する。
     * 駒種・移動先が一致し、曖昧回避のヒントを満たす手がちょうど1つのときだけ採用する
     * （複数該当する曖昧な手や、ヒントが合わない手は解決しない）。取る記号 {@code x} の有無は
     * 同じ駒種・同じ移動先なら取りかどうかが決まるため問わない。
     */
    private static Move decodeWithRedundantDisambiguation(String san, List<Move> allLegalMoves, Board board) {
        Matcher m = PIECE_MOVE.matcher(san);
        if (!m.matches()) {
            return null;
        }
        char notation = m.group(1).charAt(0);
        String hint = m.group(2);
        Move found = null;
        for (Move candidate : allLegalMoves) {
            Piece piece = board.getPieceAt(candidate.getFrom());
            if (candidate.isCastling() || piece == null || piece.getType().getNotation() != notation
                    || !candidate.getTo().toAlgebraic().equals(m.group(4))
                    || !originMatchesHint(candidate.getFrom(), hint)) {
                continue;
            }
            if (found != null) {
                return null;
            }
            found = candidate;
        }
        return found;
    }

    /** ヒント（ファイル・ランク・両方、または空）が移動元と矛盾しないか。 */
    private static boolean originMatchesHint(Position origin, String hint) {
        return origin.toAlgebraic().contains(hint) && (hint.length() != 2 || origin.toAlgebraic().equals(hint));
    }

    /**
     * SAN 末尾の注釈（アンパッサン明示・王手/詰み記号・評価記号）を取り除く。
     * <p>{@code e.p.} → {@code +}/{@code #} → {@code !}/{@code ?} の順に現れるとは限らず、
     * {@code exd6e.p.+} のような組み合わせもあるため、末尾から繰り返し剥がす。</p>
     */
    private static String stripAnnotations(String san) {
        String result = san.trim();
        String previous;
        do {
            previous = result;
            result = result.replaceAll("[!?]+$", "");
            result = result.replaceAll("[+#]+$", "");
            result = result.replaceAll("\\s*e\\.p\\.$", "");
            result = result.trim();
        } while (!result.equals(previous));
        return result;
    }

    /**
     * 王手・チェックメイト記号を含まない SAN 本体を組み立てる。
     */
    private static String encodeCore(Board board, Move move, List<Move> allLegalMovesForMovingSide) {
        if (move.isCastling()) {
            return move.getTo().getCol() > move.getFrom().getCol() ? "O-O" : "O-O-O";
        }

        Piece piece = board.getPieceAt(move.getFrom());
        StringBuilder sb = new StringBuilder();

        if (piece.getType() == PieceType.PAWN) {
            if (move.isCapture()) {
                sb.append(fileChar(move.getFrom())).append('x');
            }
            sb.append(move.getTo().toAlgebraic());
            if (move.isPromotion()) {
                sb.append('=').append(move.getPromotionPiece().getNotation());
            }
        } else {
            sb.append(piece.getType().getNotation());
            sb.append(disambiguation(board, move, allLegalMovesForMovingSide));
            if (move.isCapture()) {
                sb.append('x');
            }
            sb.append(move.getTo().toAlgebraic());
        }

        return sb.toString();
    }

    /**
     * 同種・同色・同じ移動先を持つ他の合法手がある場合の曖昧回避文字列を返す
     * （曖昧でなければ空文字列）。ファイル→ランク→両方の順で最小限の情報を付加する。
     */
    private static String disambiguation(Board board, Move move, List<Move> allLegalMovesForMovingSide) {
        Piece movingPiece = board.getPieceAt(move.getFrom());
        List<Position> otherOrigins = new ArrayList<>();

        for (Move other : allLegalMovesForMovingSide) {
            if (other.getFrom().equals(move.getFrom()) || !other.getTo().equals(move.getTo())) {
                continue;
            }
            Piece otherPiece = board.getPieceAt(other.getFrom());
            if (otherPiece != null && otherPiece.getType() == movingPiece.getType()) {
                otherOrigins.add(other.getFrom());
            }
        }

        if (otherOrigins.isEmpty()) {
            return "";
        }

        boolean fileIsUnique = otherOrigins.stream().noneMatch(p -> p.getCol() == move.getFrom().getCol());
        if (fileIsUnique) {
            return String.valueOf(fileChar(move.getFrom()));
        }

        boolean rankIsUnique = otherOrigins.stream().noneMatch(p -> p.getRow() == move.getFrom().getRow());
        if (rankIsUnique) {
            return String.valueOf(rankChar(move.getFrom()));
        }

        return "" + fileChar(move.getFrom()) + rankChar(move.getFrom());
    }

    private static char fileChar(Position pos) {
        return pos.toAlgebraic().charAt(0);
    }

    private static char rankChar(Position pos) {
        return pos.toAlgebraic().charAt(1);
    }
}
