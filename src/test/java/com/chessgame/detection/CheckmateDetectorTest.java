package com.chessgame.detection;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.detection.rules.CheckmateDetector;
import com.chessgame.game.core.ChessGame;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.model.Color;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link CheckmateDetector} のチェックメイト・ステールメイト判定を検証する。
 * 実際のゲームを進行して状態を確認するアプローチと、盤面を手動構築するアプローチの両方を使う。
 */
public class CheckmateDetectorTest {
    private CheckmateDetector detector;

    @BeforeEach
    void setUp() {
        detector = new CheckmateDetector();
    }

    /** 全マスから駒を取り除いた空盤面を返す。 */
    private Board emptyBoard() {
        Board board = new Board();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board.removePiece(Position.of(row, col));
            }
        }
        return board;
    }

    // ===================== Checkmate =====================

    /**
     * フールズメイト（最短チェックメイト）でチェックメイトが正しく検出されること。
     * 1. f3 e5  2. g4 Qh4#
     */
    @Test
    void foolsMateResultsInCheckmate() {
        ChessGame game = ChessGame.createTwoPlayerGame("W", "B");
        game.startNewGame();
        game.makeMove(Position.of("f2"), Position.of("f3"));
        game.makeMove(Position.of("e7"), Position.of("e5"));
        game.makeMove(Position.of("g2"), Position.of("g4"));
        game.makeMove(Position.of("d8"), Position.of("h4"));

        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.CHECKMATE);
        assertThat(game.isGameOver()).isTrue();
    }

    @Test
    void checkmateRequiresBothCheckAndNoLegalMoves() {
        // フールズメイト直前（Qh4 の直前）は王手ではない
        ChessGame game = ChessGame.createTwoPlayerGame("W", "B");
        game.startNewGame();
        game.makeMove(Position.of("f2"), Position.of("f3"));
        game.makeMove(Position.of("e7"), Position.of("e5"));
        game.makeMove(Position.of("g2"), Position.of("g4"));
        // ここはまだ黒番・IN_PROGRESS
        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.IN_PROGRESS);
    }

    // ===================== Stalemate =====================

    /**
     * 白キング h1、黒クイーン f2、黒キング a8 の局面で白がステールメイトとなる。
     * h1 のキングは王手ではないが合法手がない（g1・g2・h2 はすべてクイーンに攻撃されている）。
     */
    @Test
    void stalemateDetectedWhenKingHasNoLegalMoves() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE,  Position.of("h1")), Position.of("h1"));
        board.placePiece(new Queen(Color.BLACK, Position.of("f2")), Position.of("f2"));
        board.placePiece(new King(Color.BLACK,  Position.of("a8")), Position.of("a8"));

        assertThat(detector.isStalemate(Color.WHITE, board)).isTrue();
    }

    @Test
    void stalemateReturnsFalseWhenInCheck() {
        // 王手中はステールメイトでない
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE,  Position.of("e1")), Position.of("e1"));
        board.placePiece(new Rook(Color.BLACK,  Position.of("e8")), Position.of("e8"));
        board.placePiece(new King(Color.BLACK,  Position.of("a8")), Position.of("a8"));

        assertThat(detector.isStalemate(Color.WHITE, board)).isFalse();
    }

    @Test
    void stalemateReturnsFalseWhenLegalMovesExist() {
        // 合法手がある場合はステールメイトでない
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e4")), Position.of("e4"));
        board.placePiece(new King(Color.BLACK, Position.of("a8")), Position.of("a8"));

        assertThat(detector.isStalemate(Color.WHITE, board)).isFalse();
    }

    // ===================== isCheckmate =====================

    @Test
    void isCheckmateReturnsFalseWhenNotInCheck() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e4")), Position.of("e4"));
        board.placePiece(new King(Color.BLACK, Position.of("a8")), Position.of("a8"));

        assertThat(detector.isCheckmate(Color.WHITE, board)).isFalse();
    }

    // ===================== isLegalMove =====================

    @Test
    void isLegalMoveReturnsFalseWhenMoveLeavesKingInCheck() {
        // ルークが白キングを王手している。その前の自駒をどかすのは不合法。
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e1"));
        Rook blocker = new Rook(Color.WHITE, Position.of("e4")); // e ファイルのブロッカー
        Rook blackRook = new Rook(Color.BLACK, Position.of("e8"));
        board.placePiece(king, Position.of("e1"));
        board.placePiece(blocker, Position.of("e4"));
        board.placePiece(blackRook, Position.of("e8"));

        // blocker を d4 に動かすと王手になる → 不合法
        Move exposingMove = Move.normal(Position.of("e4"), Position.of("d4"));
        assertThat(detector.isLegalMove(exposingMove, blocker, board, Color.WHITE)).isFalse();
    }

    @Test
    void isLegalMoveReturnsTrueWhenKingIsSafe() {
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e1"));
        Rook rook = new Rook(Color.WHITE, Position.of("a1"));
        board.placePiece(king, Position.of("e1"));
        board.placePiece(rook, Position.of("a1"));

        // a1 → a5 は王に影響しない合法手
        Move safeMove = Move.normal(Position.of("a1"), Position.of("a5"));
        assertThat(detector.isLegalMove(safeMove, rook, board, Color.WHITE)).isTrue();
    }
}
