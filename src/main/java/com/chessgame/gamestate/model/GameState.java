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
import java.util.HashMap;
import java.util.Map;

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
    private int halfmoveClock;
    private final Map<String, Integer> positionCounts;
    private int halfmoveOffsetAtLoad;
    private Long whiteRemainingMillis;
    private Long blackRemainingMillis;
    private long incrementMillis;

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
        /** 50手ルールによる引き分け */
        FIFTY_MOVE_RULE,
        /** 千日手（同一局面3回出現）による引き分け */
        THREEFOLD_REPETITION,
        /** 戦力不足による引き分け */
        INSUFFICIENT_MATERIAL,
        /** 白が投了 */
        WHITE_RESIGNED,
        /** 黒が投了 */
        BLACK_RESIGNED,
        /** 白の持ち時間切れ */
        WHITE_TIMEOUT,
        /** 黒の持ち時間切れ */
        BLACK_TIMEOUT
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
        this.halfmoveClock = 0;
        this.positionCounts = new HashMap<>();
        this.halfmoveOffsetAtLoad = 0;
        this.whiteRemainingMillis = null;
        this.blackRemainingMillis = null;
        this.incrementMillis = 0;
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
     * 盤面を設定する。FEN 読み込みで任意の配置に差し替える用途に使う。
     *
     * @param board 設定する {@link Board}
     */
    public void setBoard(Board board) {
        this.board = board;
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
     * ハーフムーブクロック（直近のポーン移動・駒取りからの半手数）を返す。
     * 50手ルールの判定に使用する。
     *
     * @return ハーフムーブクロック
     */
    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    /**
     * ハーフムーブクロックを0にリセットする。ポーン移動・駒取りの直後に呼ぶ。
     */
    public void resetHalfmoveClock() {
        this.halfmoveClock = 0;
    }

    /**
     * ハーフムーブクロックを指定値に設定する。FEN 読み込み時に使用する。
     *
     * @param halfmoveClock 設定するハーフムーブクロック
     */
    public void setHalfmoveClock(int halfmoveClock) {
        this.halfmoveClock = halfmoveClock;
    }

    /**
     * ハーフムーブクロックを1増やす。ポーン移動・駒取り以外の手の後に呼ぶ。
     */
    public void incrementHalfmoveClock() {
        this.halfmoveClock++;
    }

    /**
     * 持ち時間ルールを適用し、両者の残り時間を初期値にセットする。
     *
     * @param timeControl 適用する持ち時間ルール
     */
    public void initializeClock(TimeControl timeControl) {
        this.whiteRemainingMillis = timeControl.getInitialMillis();
        this.blackRemainingMillis = timeControl.getInitialMillis();
        this.incrementMillis = timeControl.getIncrementMillis();
    }

    /**
     * 指定した色の残り時間を返す（持ち時間ルール未設定の場合は 0）。
     *
     * @param color 対象の色
     * @return 残り時間（ミリ秒）
     */
    public long getRemainingMillis(Color color) {
        Long remaining = (color == Color.WHITE) ? whiteRemainingMillis : blackRemainingMillis;
        return remaining != null ? remaining : 0;
    }

    /**
     * 指定した色の残り時間から経過時間を差し引く。0未満にはならない。
     *
     * @param color        対象の色
     * @param elapsedMillis 消費する時間（ミリ秒）
     */
    public void consumeTime(Color color, long elapsedMillis) {
        long remaining = Math.max(0, getRemainingMillis(color) - elapsedMillis);
        if (color == Color.WHITE) {
            whiteRemainingMillis = remaining;
        } else {
            blackRemainingMillis = remaining;
        }
    }

    /**
     * 指定した色の残り時間に1手ごとの加算時間を加える。
     *
     * @param color 対象の色
     */
    public void addIncrement(Color color) {
        if (color == Color.WHITE) {
            whiteRemainingMillis = getRemainingMillis(color) + incrementMillis;
        } else {
            blackRemainingMillis = getRemainingMillis(color) + incrementMillis;
        }
    }

    /**
     * 局面キーの出現を1回記録し、記録後の出現回数を返す。千日手の判定に使用する。
     *
     * @param positionKey 局面を一意に表す文字列
     * @return 記録後のその局面の出現回数
     */
    public int recordPosition(String positionKey) {
        return positionCounts.merge(positionKey, 1, Integer::sum);
    }

    /**
     * 記録済みの局面出現回数をすべて消去する。undo によるリプレイ開始時に使用する。
     */
    public void clearPositionCounts() {
        positionCounts.clear();
    }

    /**
     * FEN 読み込み時点までの半手数を設定する。通常のゲーム開始（半手数0からの記録）
     * の場合は 0 のままでよい。{@link #getFullmoveNumber()} の起点として使う。
     *
     * @param halfmoveOffset FEN のフルムーブ番号・手番から導出した半手数
     */
    public void setHalfmoveOffsetAtLoad(int halfmoveOffset) {
        this.halfmoveOffsetAtLoad = halfmoveOffset;
    }

    /**
     * 現在のフルムーブ番号を返す（PGN/FEN 出力用）。
     * 通常のゲーム開始からなら 1 手・2手目で 1、3・4手目で 2 ... と増える。
     * FEN 読み込みで開始した場合は {@link #setHalfmoveOffsetAtLoad(int)} で
     * 設定した半手数を起点に継続する。
     *
     * @return フルムーブ番号
     */
    public int getFullmoveNumber() {
        return 1 + (halfmoveOffsetAtLoad + moveHistory.size()) / 2;
    }

    /**
     * 手番を相手プレイヤーに切り替える。
     * アンパッサンターゲットの失効は {@code ChessGame.executeMoveOnBoard} が
     * 次の手の実行開始時に行うため、ここでは触れない
     * （ここでクリアすると、対象を設定した直後の手でクリアされてしまい、
     * 相手が一度もアンパッサンを行使できなくなる）。
     */
    public void switchPlayer() {
        this.currentPlayerColor = currentPlayerColor.opposite();
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
               gameStatus == GameStatus.FIFTY_MOVE_RULE ||
               gameStatus == GameStatus.THREEFOLD_REPETITION ||
               gameStatus == GameStatus.INSUFFICIENT_MATERIAL ||
               gameStatus == GameStatus.WHITE_RESIGNED ||
               gameStatus == GameStatus.BLACK_RESIGNED ||
               gameStatus == GameStatus.WHITE_TIMEOUT ||
               gameStatus == GameStatus.BLACK_TIMEOUT;
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
        this.halfmoveClock = 0;
        this.positionCounts.clear();
        this.halfmoveOffsetAtLoad = 0;
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
