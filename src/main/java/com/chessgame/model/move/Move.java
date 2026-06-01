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

import com.chessgame.model.board.Position;
import com.chessgame.model.piece.Piece;
import com.chessgame.model.piece.PieceType;
import java.util.Objects;

/**
 * チェスの1手を表すイミュータブルな値オブジェクト。
 * 移動元・移動先・手の種類・捕獲した駒・昇格先の駒種を保持する。
 * インスタンスは静的ファクトリメソッド（{@link #normal}、{@link #capture} など）で生成する。
 */
public final class Move {
    private final Position from;
    private final Position to;
    private final MoveType moveType;
    private final Piece capturedPiece;
    private final PieceType promotionPiece;

    private Move(Position from, Position to, MoveType moveType,
                 Piece capturedPiece, PieceType promotionPiece) {
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        this.moveType = Objects.requireNonNull(moveType);
        this.capturedPiece = capturedPiece;
        this.promotionPiece = promotionPiece;
    }

    /**
     * 通常移動を生成する。
     *
     * @param from 移動元
     * @param to   移動先
     * @return 通常移動の {@link Move}
     */
    public static Move normal(Position from, Position to) {
        return new Move(from, to, MoveType.NORMAL, null, null);
    }

    /**
     * 駒を取る移動を生成する。
     *
     * @param from          移動元
     * @param to            移動先
     * @param capturedPiece 取られる駒
     * @return 捕獲移動の {@link Move}
     */
    public static Move capture(Position from, Position to, Piece capturedPiece) {
        return new Move(from, to, MoveType.CAPTURE,
                       Objects.requireNonNull(capturedPiece), null);
    }

    /**
     * キャスリング移動を生成する。
     *
     * @param from キングの移動元
     * @param to   キングの移動先
     * @return キャスリングの {@link Move}
     */
    public static Move castling(Position from, Position to) {
        return new Move(from, to, MoveType.CASTLING, null, null);
    }

    /**
     * アンパッサン移動を生成する。
     *
     * @param from          移動元
     * @param to            移動先
     * @param capturedPiece 取られる相手ポーン
     * @return アンパッサンの {@link Move}
     */
    public static Move enPassant(Position from, Position to, Piece capturedPiece) {
        return new Move(from, to, MoveType.EN_PASSANT,
                       Objects.requireNonNull(capturedPiece), null);
    }

    /**
     * 駒を取らないポーン昇格移動を生成する。
     *
     * @param from           移動元
     * @param to             移動先（最終ランク）
     * @param promotionPiece 昇格先の駒種
     * @return 昇格移動の {@link Move}
     */
    public static Move promotion(Position from, Position to,
                                 PieceType promotionPiece) {
        return new Move(from, to, MoveType.PROMOTION, null,
                       Objects.requireNonNull(promotionPiece));
    }

    /**
     * 駒を取るポーン昇格移動を生成する。
     *
     * @param from           移動元
     * @param to             移動先（最終ランク）
     * @param capturedPiece  取られる駒
     * @param promotionPiece 昇格先の駒種
     * @return 捕獲を伴う昇格移動の {@link Move}
     */
    public static Move promotionCapture(Position from, Position to,
                                        Piece capturedPiece,
                                        PieceType promotionPiece) {
        return new Move(from, to, MoveType.PROMOTION,
                       Objects.requireNonNull(capturedPiece),
                       Objects.requireNonNull(promotionPiece));
    }

    /**
     * 移動元の位置を返す。
     *
     * @return 移動元 {@link Position}
     */
    public Position getFrom() {
        return from;
    }

    /**
     * 移動先の位置を返す。
     *
     * @return 移動先 {@link Position}
     */
    public Position getTo() {
        return to;
    }

    /**
     * 手の種類を返す。
     *
     * @return {@link MoveType}
     */
    public MoveType getMoveType() {
        return moveType;
    }

    /**
     * 取られた駒を返す。捕獲がない場合は null。
     *
     * @return 捕獲された駒、または null
     */
    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    /**
     * 昇格先の駒種を返す。昇格でない場合は null。
     *
     * @return 昇格先の {@link PieceType}、または null
     */
    public PieceType getPromotionPiece() {
        return promotionPiece;
    }

    /**
     * この手が駒を取る手かどうかを返す。
     *
     * @return 捕獲・アンパッサン・捕獲昇格のいずれかであれば true
     */
    public boolean isCapture() {
        return moveType == MoveType.CAPTURE ||
               moveType == MoveType.EN_PASSANT ||
               (moveType == MoveType.PROMOTION && capturedPiece != null);
    }

    /**
     * この手がポーン昇格かどうかを返す。
     *
     * @return 昇格であれば true
     */
    public boolean isPromotion() {
        return moveType == MoveType.PROMOTION;
    }

    /**
     * この手がキャスリングかどうかを返す。
     *
     * @return キャスリングであれば true
     */
    public boolean isCastling() {
        return moveType == MoveType.CASTLING;
    }

    /**
     * この手がアンパッサンかどうかを返す。
     *
     * @return アンパッサンであれば true
     */
    public boolean isEnPassant() {
        return moveType == MoveType.EN_PASSANT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return from.equals(move.from) &&
               to.equals(move.to) &&
               moveType == move.moveType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, moveType);
    }

    @Override
    public String toString() {
        return from.toAlgebraic() + to.toAlgebraic();
    }
}
