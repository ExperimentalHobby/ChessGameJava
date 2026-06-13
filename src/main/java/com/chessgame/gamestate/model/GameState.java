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

package com.chessgame.gamestate.model;

import com.chessgame.model.Color;
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.move.model.MoveHistory;

/**
 * チェスゲームの現在の状態を保持するクラス。
 * 手番・ゲーム進行状況・移動履歴・アンパッサンターゲットを管理する。
 */
public class GameState {
    private Board board;
    private Color currentPlayerColor;
    private GameStatus gameStatus;
    private MoveHistory moveHistory;
    private Position enPassantTarget;

    /**
     * ゲームの進行状況を表す列挙型。
     */
    public enum GameStatus {
        /** 進行中 */
        IN_PROGRESS,
        /** 王手 */
        CHECK,
        /** チェックメイト */
        CHECKMATE,
        /** ステールメイト（引き分け） */
        STALEMATE,
        /** 白が投了 */
        WHITE_RESIGNED,
        /** 黒が投了 */
        BLACK_RESIGNED
    }

    /**
     * ゲーム状態を初期化する。盤面を初期配置に設定し、手番を白に設定する。
     */
    public GameState() {
        this.board = new Board();
        this.currentPlayerColor = Color.WHITE;
        this.gameStatus = GameStatus.IN_PROGRESS;
        this.moveHistory = new MoveHistory();
        this.enPassantTarget = null;
    }

    /**
     * 現在の盤面を返す。
     *
     * @return 現在の {@link Board}
     */
    public Board getBoard() {
        return board;
    }

    /**
     * 現在の手番の色を返す。
     *
     * @return 現在の手番の {@link Color}
     */
    public Color getCurrentPlayerColor() {
        return currentPlayerColor;
    }

    /**
     * 相手プレイヤーの色を返す。
     *
     * @return 相手の {@link Color}
     */
    public Color getOpponentColor() {
        return currentPlayerColor.opposite();
    }

    /**
     * ゲームの進行状況を返す。
     *
     * @return 現在の {@link GameStatus}
     */
    public GameStatus getGameStatus() {
        return gameStatus;
    }

    /**
     * ゲームの進行状況を設定する。
     *
     * @param status 設定する {@link GameStatus}
     */
    public void setGameStatus(GameStatus status) {
        this.gameStatus = status;
    }

    /**
     * 移動履歴を返す。
     *
     * @return {@link MoveHistory}
     */
    public MoveHistory getMoveHistory() {
        return moveHistory;
    }

    /**
     * アンパッサンのターゲット位置を返す。アンパッサンが有効でない場合は null。
     *
     * @return アンパッサン対象の {@link Position}、または null
     */
    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    /**
     * アンパッサンのターゲット位置を設定する。
     *
     * @param position アンパッサン対象の {@link Position}
     */
    public void setEnPassantTarget(Position position) {
        this.enPassantTarget = position;
    }

    /**
     * 手番を相手プレイヤーに切り替える。アンパッサンターゲットはリセットされる。
     */
    public void switchPlayer() {
        this.currentPlayerColor = currentPlayerColor.opposite();
        this.enPassantTarget = null;
    }

    /**
     * 手番を直接設定する。undo のリプレイ開始時に初期プレイヤーへリセットするために使用する。
     */
    public void setCurrentPlayerColor(Color color) {
        this.currentPlayerColor = color;
    }

    /**
     * 移動を履歴に記録する。
     *
     * @param move 記録する {@link Move}
     */
    public void recordMove(Move move) {
        moveHistory.addMove(move);
    }

    /**
     * 記録済みの移動数を返す。
     *
     * @return 移動数
     */
    public int getMoveCount() {
        return moveHistory.size();
    }

    /**
     * ゲームが終了しているかどうかを返す。
     *
     * @return チェックメイト・ステールメイト・投了のいずれかであれば true
     */
    public boolean isGameOver() {
        return gameStatus == GameStatus.CHECKMATE ||
               gameStatus == GameStatus.STALEMATE ||
               gameStatus == GameStatus.WHITE_RESIGNED ||
               gameStatus == GameStatus.BLACK_RESIGNED;
    }

    /**
     * ゲームを初期状態にリセットする。盤面・手番・履歴をすべて初期化する。
     */
    public void resetGame() {
        this.board = new Board();
        this.currentPlayerColor = Color.WHITE;
        this.gameStatus = GameStatus.IN_PROGRESS;
        this.moveHistory = new MoveHistory();
        this.enPassantTarget = null;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "currentPlayer=" + currentPlayerColor +
                ", status=" + gameStatus +
                ", moveCount=" + moveHistory.size() +
                '}';
    }
}
