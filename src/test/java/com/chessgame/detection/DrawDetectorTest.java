package com.chessgame.detection;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.detection.rules.DrawDetector;
import com.chessgame.model.Color;
import com.chessgame.piece.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link DrawDetector} の50手ルール・千日手・戦力不足判定を検証する。
 */
public class DrawDetectorTest {
    private DrawDetector detector;

    @BeforeEach
    void setUp() {
        detector = new DrawDetector();
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

    // ===================== 50手ルール =====================

    @Test
    public void testFiftyMoveRuleBoundary() {
        assertThat(detector.isFiftyMoveRule(99)).isFalse();
        assertThat(detector.isFiftyMoveRule(100)).isTrue();
    }

    // ===================== 千日手 =====================

    @Test
    public void testThreefoldRepetitionBoundary() {
        assertThat(detector.isThreefoldRepetition(2)).isFalse();
        assertThat(detector.isThreefoldRepetition(3)).isTrue();
    }

    // ===================== 戦力不足 =====================

    @Test
    public void testInsufficientMaterialKingVsKing() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new King(Color.BLACK, Position.of("e8")), Position.of("e8"));

        assertThat(detector.isInsufficientMaterial(board)).isTrue();
    }

    @Test
    public void testInsufficientMaterialKingBishopVsKing() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Bishop(Color.WHITE, Position.of("c1")), Position.of("c1"));
        board.placePiece(new King(Color.BLACK, Position.of("e8")), Position.of("e8"));

        assertThat(detector.isInsufficientMaterial(board)).isTrue();
    }

    @Test
    public void testSufficientMaterialKingQueenVsKing() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Queen(Color.WHITE, Position.of("d1")), Position.of("d1"));
        board.placePiece(new King(Color.BLACK, Position.of("e8")), Position.of("e8"));

        assertThat(detector.isInsufficientMaterial(board)).isFalse();
    }

    @Test
    public void testInsufficientMaterialKingKnightVsKing() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Knight(Color.WHITE, Position.of("b1")), Position.of("b1"));
        board.placePiece(new King(Color.BLACK, Position.of("e8")), Position.of("e8"));

        assertThat(detector.isInsufficientMaterial(board)).isTrue();
    }

    @Test
    public void testSufficientMaterialKingRookVsKing() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Rook(Color.WHITE, Position.of("a1")), Position.of("a1"));
        board.placePiece(new King(Color.BLACK, Position.of("e8")), Position.of("e8"));

        assertThat(detector.isInsufficientMaterial(board)).isFalse();
    }

    @Test
    public void testSufficientMaterialKingPawnVsKing() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Pawn(Color.WHITE, Position.of("e2")), Position.of("e2"));
        board.placePiece(new King(Color.BLACK, Position.of("e8")), Position.of("e8"));

        assertThat(detector.isInsufficientMaterial(board)).isFalse();
    }

    /** c1（暗マス）と f8（暗マス）は同色マスのビショップ同士。 */
    @Test
    public void testInsufficientMaterialSameColorBishops() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Bishop(Color.WHITE, Position.of("c1")), Position.of("c1"));
        board.placePiece(new King(Color.BLACK, Position.of("e8")), Position.of("e8"));
        board.placePiece(new Bishop(Color.BLACK, Position.of("f8")), Position.of("f8"));

        assertThat(detector.isInsufficientMaterial(board)).isTrue();
    }

    /** c1（暗マス）と e8（明マス）は異なる色のマスのビショップ同士。 */
    @Test
    public void testSufficientMaterialOppositeColorBishops() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Bishop(Color.WHITE, Position.of("c1")), Position.of("c1"));
        board.placePiece(new King(Color.BLACK, Position.of("d8")), Position.of("d8"));
        board.placePiece(new Bishop(Color.BLACK, Position.of("e8")), Position.of("e8"));

        assertThat(detector.isInsufficientMaterial(board)).isFalse();
    }

    @Test
    public void testSufficientMaterialKnightVsKnight() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Knight(Color.WHITE, Position.of("b1")), Position.of("b1"));
        board.placePiece(new King(Color.BLACK, Position.of("e8")), Position.of("e8"));
        board.placePiece(new Knight(Color.BLACK, Position.of("b8")), Position.of("b8"));

        assertThat(detector.isInsufficientMaterial(board)).isFalse();
    }
}
