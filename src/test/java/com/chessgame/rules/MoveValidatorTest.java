package com.chessgame.rules;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import com.chessgame.move.model.Move;
import com.chessgame.move.model.MoveType;
import com.chessgame.piece.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link MoveValidator} の擬似合法手生成を駒種・特殊手ごとに検証する。
 * 王手放置チェックは {@link com.chessgame.detection.rules.CheckmateDetector} が担うため、
 * ここでは擬似合法手（王手放置なし）の数と種類のみを検証する。
 */
public class MoveValidatorTest {
    private MoveValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MoveValidator();
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

    // ===================== Pawn =====================

    @Test
    void whitePawnAdvancesOneSquareFromMiddle() {
        Board board = emptyBoard();
        Pawn pawn = new Pawn(Color.WHITE, Position.of("e3"));
        board.placePiece(pawn, Position.of("e3"));

        List<Move> moves = validator.getValidMoves(pawn, board);

        assertThat(moves).hasSize(1);
        assertThat(moves.get(0).getTo()).isEqualTo(Position.of("e4"));
        assertThat(moves.get(0).getMoveType()).isEqualTo(MoveType.NORMAL);
    }

    @Test
    void whitePawnAdvancesTwoSquaresFromStartRow() {
        Board board = emptyBoard();
        Pawn pawn = new Pawn(Color.WHITE, Position.of("e2"));
        board.placePiece(pawn, Position.of("e2"));

        List<Move> moves = validator.getValidMoves(pawn, board);

        assertThat(moves).hasSize(2);
        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("e3")));
        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("e4")));
    }

    @Test
    void whitePawnCannotAdvanceThroughBlockingPiece() {
        Board board = emptyBoard();
        Pawn pawn = new Pawn(Color.WHITE, Position.of("e2"));
        board.placePiece(pawn, Position.of("e2"));
        board.placePiece(new Pawn(Color.BLACK, Position.of("e3")), Position.of("e3"));

        List<Move> moves = validator.getValidMoves(pawn, board);

        assertThat(moves).isEmpty();
    }

    @Test
    void whitePawnCapturesDiagonalEnemyPiece() {
        Board board = emptyBoard();
        Pawn pawn = new Pawn(Color.WHITE, Position.of("e4"));
        board.placePiece(pawn, Position.of("e4"));
        board.placePiece(new Pawn(Color.BLACK, Position.of("d5")), Position.of("d5"));

        List<Move> moves = validator.getValidMoves(pawn, board);

        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("d5")) && m.isCapture());
    }

    @Test
    void whitePawnGeneratesFourPromotionMovesOnLastRank() {
        Board board = emptyBoard();
        Pawn pawn = new Pawn(Color.WHITE, Position.of("e7"));
        board.placePiece(pawn, Position.of("e7"));

        List<Move> moves = validator.getValidMoves(pawn, board);

        // クイーン・ルーク・ビショップ・ナイトの4種の昇格手を生成する
        assertThat(moves).hasSize(4);
        assertThat(moves).allMatch(Move::isPromotion);
    }

    @Test
    void blackPawnAdvancesDownward() {
        Board board = emptyBoard();
        Pawn pawn = new Pawn(Color.BLACK, Position.of("e7"));
        board.placePiece(pawn, Position.of("e7"));

        List<Move> moves = validator.getValidMoves(pawn, board);

        assertThat(moves).hasSize(2);
        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("e6")));
        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("e5")));
    }

    @Test
    void enPassantCaptureIsGenerated() {
        Board board = emptyBoard();
        Pawn whitePawn = new Pawn(Color.WHITE, Position.of("e5"));
        Pawn blackPawn = new Pawn(Color.BLACK, Position.of("d5"));
        board.placePiece(whitePawn, Position.of("e5"));
        board.placePiece(blackPawn, Position.of("d5"));

        // 黒ポーンが d7→d5 と2マス移動した直後、d6 がアンパッサン対象になる
        List<Move> moves = validator.getValidMoves(whitePawn, board, Position.of("d6"));

        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("d6")) && m.isEnPassant());
    }

    // ===================== Knight =====================

    @Test
    void knightInCenterHasEightMoves() {
        Board board = emptyBoard();
        Knight knight = new Knight(Color.WHITE, Position.of("e4"));
        board.placePiece(knight, Position.of("e4"));

        List<Move> moves = validator.getValidMoves(knight, board);

        assertThat(moves).hasSize(8);
    }

    @Test
    void knightInCornerHasTwoMoves() {
        Board board = emptyBoard();
        Knight knight = new Knight(Color.WHITE, Position.of("a1"));
        board.placePiece(knight, Position.of("a1"));

        List<Move> moves = validator.getValidMoves(knight, board);

        assertThat(moves).hasSize(2);
    }

    @Test
    void knightSkipsOverInterveningPieces() {
        Board board = emptyBoard();
        Knight knight = new Knight(Color.WHITE, Position.of("e4"));
        board.placePiece(knight, Position.of("e4"));
        // 隣接する4マスをすべて塞いでも、L字の先は到達できる
        board.placePiece(new Pawn(Color.WHITE, Position.of("d4")), Position.of("d4"));
        board.placePiece(new Pawn(Color.WHITE, Position.of("f4")), Position.of("f4"));
        board.placePiece(new Pawn(Color.WHITE, Position.of("e3")), Position.of("e3"));
        board.placePiece(new Pawn(Color.WHITE, Position.of("e5")), Position.of("e5"));

        List<Move> moves = validator.getValidMoves(knight, board);

        assertThat(moves).hasSize(8);
    }

    // ===================== Bishop =====================

    @Test
    void bishopSlidesDiagonallyToEdge() {
        Board board = emptyBoard();
        Bishop bishop = new Bishop(Color.WHITE, Position.of("e4"));
        board.placePiece(bishop, Position.of("e4"));

        List<Move> moves = validator.getValidMoves(bishop, board);

        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("h7")));
        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("a8")));
        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("b1")));
    }

    @Test
    void bishopIsBlockedByFriendlyPiece() {
        Board board = emptyBoard();
        Bishop bishop = new Bishop(Color.WHITE, Position.of("e4"));
        board.placePiece(bishop, Position.of("e4"));
        // g6 に味方駒を置くと h7 へ到達できない
        board.placePiece(new Pawn(Color.WHITE, Position.of("g6")), Position.of("g6"));

        List<Move> moves = validator.getValidMoves(bishop, board);

        assertThat(moves).noneMatch(m -> m.getTo().equals(Position.of("h7")));
    }

    @Test
    void bishopCapturesEnemyAndCannotPassThrough() {
        Board board = emptyBoard();
        Bishop bishop = new Bishop(Color.WHITE, Position.of("e4"));
        board.placePiece(bishop, Position.of("e4"));
        board.placePiece(new Pawn(Color.BLACK, Position.of("g6")), Position.of("g6"));

        List<Move> moves = validator.getValidMoves(bishop, board);

        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("g6")) && m.isCapture());
        assertThat(moves).noneMatch(m -> m.getTo().equals(Position.of("h7")));
    }

    // ===================== Rook =====================

    @Test
    void rookSlidesAlongRankAndFile() {
        Board board = emptyBoard();
        Rook rook = new Rook(Color.WHITE, Position.of("e4"));
        board.placePiece(rook, Position.of("e4"));

        List<Move> moves = validator.getValidMoves(rook, board);

        // e4: e ファイル7マス + ランク4の7マス = 14
        assertThat(moves).hasSize(14);
    }

    @Test
    void rookCapturesEnemyAndStopsBeyond() {
        Board board = emptyBoard();
        Rook rook = new Rook(Color.WHITE, Position.of("e1"));
        board.placePiece(rook, Position.of("e1"));
        board.placePiece(new Pawn(Color.BLACK, Position.of("e5")), Position.of("e5"));

        List<Move> moves = validator.getValidMoves(rook, board);

        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("e5")) && m.isCapture());
        assertThat(moves).noneMatch(m -> m.getTo().equals(Position.of("e6")));
    }

    // ===================== Queen =====================

    @Test
    void queenMovesInAllEightDirections() {
        Board board = emptyBoard();
        Queen queen = new Queen(Color.WHITE, Position.of("e4"));
        board.placePiece(queen, Position.of("e4"));

        List<Move> moves = validator.getValidMoves(queen, board);

        // ルーク方向の代表点
        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("e8")));
        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("a4")));
        // ビショップ方向の代表点
        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("h7")));
        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("b1")));
    }

    // ===================== King =====================

    @Test
    void kingInCenterMovesToEightAdjacentSquares() {
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e4"));
        board.placePiece(king, Position.of("e4"));

        List<Move> moves = validator.getValidMoves(king, board);

        assertThat(moves).hasSize(8);
    }

    @Test
    void kingInCornerMovesToThreeSquares() {
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("a1"));
        board.placePiece(king, Position.of("a1"));

        List<Move> moves = validator.getValidMoves(king, board);

        // a1 コーナー: a2, b1, b2 の3マス
        assertThat(moves).hasSize(3);
    }

    // ===================== Castling =====================

    @Test
    void kingsideCastlingAvailableWhenPathClear() {
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e1"));
        Rook rook = new Rook(Color.WHITE, Position.of("h1"));
        board.placePiece(king, Position.of("e1"));
        board.placePiece(rook, Position.of("h1"));

        List<Move> moves = validator.getValidMoves(king, board);

        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("g1")) && m.isCastling());
    }

    @Test
    void queensideCastlingAvailableWhenPathClear() {
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e1"));
        Rook rook = new Rook(Color.WHITE, Position.of("a1"));
        board.placePiece(king, Position.of("e1"));
        board.placePiece(rook, Position.of("a1"));

        List<Move> moves = validator.getValidMoves(king, board);

        assertThat(moves).anyMatch(m -> m.getTo().equals(Position.of("c1")) && m.isCastling());
    }

    @Test
    void castlingUnavailableAfterKingMoves() {
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e1"));
        king.incrementMoveCount(); // キングが既に動いている
        Rook rook = new Rook(Color.WHITE, Position.of("h1"));
        board.placePiece(king, Position.of("e1"));
        board.placePiece(rook, Position.of("h1"));

        List<Move> moves = validator.getValidMoves(king, board);

        assertThat(moves).noneMatch(Move::isCastling);
    }

    @Test
    void castlingUnavailableWhenPathBlocked() {
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e1"));
        Rook rook = new Rook(Color.WHITE, Position.of("h1"));
        board.placePiece(king, Position.of("e1"));
        board.placePiece(rook, Position.of("h1"));
        // f1 に駒があるとキングサイドキャスリングは不可
        board.placePiece(new Bishop(Color.WHITE, Position.of("f1")), Position.of("f1"));

        List<Move> moves = validator.getValidMoves(king, board);

        assertThat(moves).noneMatch(m -> m.getTo().equals(Position.of("g1")) && m.isCastling());
    }
}
