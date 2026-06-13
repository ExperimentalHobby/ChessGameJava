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

package com.chessgame.board.model;

import com.chessgame.model.Color;
import com.chessgame.model.piece.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 8×8 のチェス盤を表すクラス。各マスの状態と両キングの位置を管理する。
 * 初期化時にチェスの標準開始配置を自動的にセットアップする。
 */
public class Board {
    private final Square[][] squares;
    private Position whiteKingPosition;
    private Position blackKingPosition;

    /**
     * チェスの標準開始配置で盤面を初期化する。
     */
    public Board() {
        this(true);
    }

    /**
     * 盤面を生成する内部コンストラクタ。
     *
     * @param withStartingPosition true なら標準開始配置をセットする。false なら空盤面のまま
     */
    private Board(boolean withStartingPosition) {
        this.squares = new Square[Position.BOARD_SIZE][Position.BOARD_SIZE];
        initializeEmptyBoard();
        if (withStartingPosition) {
            initializeStartingPosition();
        }
    }

    /**
     * 全マスを空の {@link Square} で初期化する。
     */
    private void initializeEmptyBoard() {
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                squares[row][col] = new Square(Position.of(row, col));
            }
        }
    }

    /**
     * チェスの標準開始配置（バックランクとポーン行）を配置する。
     */
    private void initializeStartingPosition() {
        // 白のバックランクは row=7（ランク1）、黒のバックランクは row=0（ランク8）
        placeBackRow(Color.WHITE, 7);
        placeBackRow(Color.BLACK, 0);
        // 白ポーンは row=6（ランク2）、黒ポーンは row=1（ランク7）
        placePawns(Color.WHITE, 6);
        placePawns(Color.BLACK, 1);
    }

    /**
     * 指定した行にバックランク（ルーク・ナイト・ビショップ・クイーン・キング）を配置する。
     *
     * @param color 配置する駒の色
     * @param row   配置先の行番号
     */
    private void placeBackRow(Color color, int row) {
        placePiece(new Rook(color, Position.of(row, 0)), Position.of(row, 0));
        placePiece(new Knight(color, Position.of(row, 1)), Position.of(row, 1));
        placePiece(new Bishop(color, Position.of(row, 2)), Position.of(row, 2));
        placePiece(new Queen(color, Position.of(row, 3)), Position.of(row, 3));
        placePiece(new King(color, Position.of(row, 4)), Position.of(row, 4));
        placePiece(new Bishop(color, Position.of(row, 5)), Position.of(row, 5));
        placePiece(new Knight(color, Position.of(row, 6)), Position.of(row, 6));
        placePiece(new Rook(color, Position.of(row, 7)), Position.of(row, 7));

        if (color == Color.WHITE) {
            whiteKingPosition = Position.of(row, 4);
        } else {
            blackKingPosition = Position.of(row, 4);
        }
    }

    /**
     * 指定した行の全列にポーンを配置する。
     *
     * @param color 配置するポーンの色
     * @param row   配置先の行番号
     */
    private void placePawns(Color color, int row) {
        for (int col = 0; col < Position.BOARD_SIZE; col++) {
            placePiece(new Pawn(color, Position.of(row, col)), Position.of(row, col));
        }
    }

    /**
     * 指定した位置のマスを返す。
     *
     * @param position 位置
     * @return {@link Square}
     */
    public Square getSquare(Position position) {
        return squares[position.getRow()][position.getCol()];
    }

    /**
     * 指定した位置にある駒を返す。駒がなければ null。
     *
     * @param position 位置
     * @return 駒、または null
     */
    public Piece getPieceAt(Position position) {
        return getSquare(position).getPiece();
    }

    /**
     * 指定した位置に駒があるかどうかを返す。
     *
     * @param position 位置
     * @return 駒があれば true
     */
    public boolean isPieceAt(Position position) {
        return getSquare(position).hasPiece();
    }

    /**
     * 指定した位置に駒を置く。キングを置いた場合はキング位置も更新する。
     *
     * @param piece    置く駒
     * @param position 配置先の位置
     */
    public void placePiece(Piece piece, Position position) {
        getSquare(position).setPiece(piece);
        piece.setPosition(position);

        if (piece.getType() == PieceType.KING) {
            if (piece.getColor() == Color.WHITE) {
                whiteKingPosition = position;
            } else {
                blackKingPosition = position;
            }
        }
    }

    /**
     * 指定した位置の駒を取り除いて返す。
     *
     * @param position 位置
     * @return 取り除いた駒、または null
     */
    public Piece removePiece(Position position) {
        Piece removed = getSquare(position).removePiece();
        return removed;
    }

    /**
     * 指定した色のキングの現在位置を返す。
     *
     * @param color キングの色
     * @return キングの {@link Position}
     */
    public Position getKingPosition(Color color) {
        return color == Color.WHITE ? whiteKingPosition : blackKingPosition;
    }

    /**
     * 盤面上にある指定した色の全駒のリストを返す。
     *
     * @param color 取得する駒の色
     * @return 該当する全駒のリスト
     */
    public List<Piece> getAllPieces(Color color) {
        List<Piece> pieces = new ArrayList<>();
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                Piece piece = getPieceAt(Position.of(row, col));
                if (piece != null && piece.getColor() == color) {
                    pieces.add(piece);
                }
            }
        }
        return pieces;
    }

    /**
     * 盤面の深いコピーを返す。チェック検証などの仮実行に使用する。
     *
     * @return 盤面のコピー
     */
    public Board clone() {
        // 空盤面で生成することで、初期配置の生成→全消去という無駄を省く
        Board clonedBoard = new Board(false);

        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                Piece piece = getPieceAt(Position.of(row, col));
                if (piece != null) {
                    Piece clonedPiece = piece.clone();
                    clonedBoard.placePiece(clonedPiece, Position.of(row, col));
                }
            }
        }
        return clonedBoard;
    }

    /**
     * 全マスを空にする。リセット処理の内部ステップとして使用する。
     */
    private void clearBoard() {
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                getSquare(Position.of(row, col)).clear();
            }
        }
    }

    /**
     * 盤面を初期配置にリセットする。
     */
    public void resetBoard() {
        clearBoard();
        initializeStartingPosition();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                Piece piece = getPieceAt(Position.of(row, col));
                if (piece == null) {
                    sb.append(". ");
                } else {
                    sb.append(piece.toString()).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
