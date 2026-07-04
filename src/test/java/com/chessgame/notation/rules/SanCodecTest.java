package com.chessgame.notation.rules;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.King;
import com.chessgame.piece.model.Knight;
import com.chessgame.piece.model.Pawn;
import com.chessgame.piece.model.PieceType;
import com.chessgame.piece.model.Rook;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link SanCodec} の SAN（Standard Algebraic Notation）エンコード・デコードを検証する。
 */
public class SanCodecTest {

    @Test
    public void testEncodeSimplePawnMove() {
        Board board = new Board();
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));

        String san = SanCodec.encode(board, move, List.of(move), false, false);

        assertThat(san).isEqualTo("e4");
    }

    @Test
    public void testEncodePawnCapture() {
        Board board = Board.empty();
        Pawn whitePawn = new Pawn(Color.WHITE, Position.of("e4"));
        Pawn blackPawn = new Pawn(Color.BLACK, Position.of("d5"));
        board.placePiece(whitePawn, Position.of("e4"));
        board.placePiece(blackPawn, Position.of("d5"));
        Move move = Move.capture(Position.of("e4"), Position.of("d5"), blackPawn);

        String san = SanCodec.encode(board, move, List.of(move), false, false);

        assertThat(san).isEqualTo("exd5");
    }

    @Test
    public void testEncodeKnightMoveNoAmbiguity() {
        Board board = Board.empty();
        Knight knight = new Knight(Color.WHITE, Position.of("g1"));
        board.placePiece(knight, Position.of("g1"));
        Move move = Move.normal(Position.of("g1"), Position.of("f3"));

        String san = SanCodec.encode(board, move, List.of(move), false, false);

        assertThat(san).isEqualTo("Nf3");
    }

    @Test
    public void testEncodeKnightMoveWithFileDisambiguation() {
        Board board = Board.empty();
        Knight knightC3 = new Knight(Color.WHITE, Position.of("c3"));
        Knight knightG3 = new Knight(Color.WHITE, Position.of("g3"));
        board.placePiece(knightC3, Position.of("c3"));
        board.placePiece(knightG3, Position.of("g3"));
        Move moveFromC3 = Move.normal(Position.of("c3"), Position.of("e4"));
        Move moveFromG3 = Move.normal(Position.of("g3"), Position.of("e4"));
        List<Move> allMoves = List.of(moveFromC3, moveFromG3);

        String san = SanCodec.encode(board, moveFromC3, allMoves, false, false);

        assertThat(san).isEqualTo("Nce4");
    }

    @Test
    public void testEncodeKingsideCastling() {
        Board board = Board.empty();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Rook(Color.WHITE, Position.of("h1")), Position.of("h1"));
        Move move = Move.castling(Position.of("e1"), Position.of("g1"));

        String san = SanCodec.encode(board, move, List.of(move), false, false);

        assertThat(san).isEqualTo("O-O");
    }

    @Test
    public void testEncodeQueensideCastling() {
        Board board = Board.empty();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Rook(Color.WHITE, Position.of("a1")), Position.of("a1"));
        Move move = Move.castling(Position.of("e1"), Position.of("c1"));

        String san = SanCodec.encode(board, move, List.of(move), false, false);

        assertThat(san).isEqualTo("O-O-O");
    }

    @Test
    public void testEncodePromotion() {
        Board board = Board.empty();
        board.placePiece(new Pawn(Color.WHITE, Position.of("e7")), Position.of("e7"));
        Move move = Move.promotion(Position.of("e7"), Position.of("e8"), PieceType.QUEEN);

        String san = SanCodec.encode(board, move, List.of(move), false, false);

        assertThat(san).isEqualTo("e8=Q");
    }

    @Test
    public void testEncodeCapturePromotion() {
        Board board = Board.empty();
        board.placePiece(new Pawn(Color.WHITE, Position.of("e7")), Position.of("e7"));
        Rook capturedRook = new Rook(Color.BLACK, Position.of("d8"));
        board.placePiece(capturedRook, Position.of("d8"));
        Move move = Move.promotionCapture(Position.of("e7"), Position.of("d8"), capturedRook, PieceType.QUEEN);

        String san = SanCodec.encode(board, move, List.of(move), false, false);

        assertThat(san).isEqualTo("exd8=Q");
    }

    @Test
    public void testEncodeAppendsCheckSuffix() {
        Board board = Board.empty();
        board.placePiece(new Rook(Color.WHITE, Position.of("a1")), Position.of("a1"));
        Move move = Move.normal(Position.of("a1"), Position.of("a8"));

        String san = SanCodec.encode(board, move, List.of(move), true, false);

        assertThat(san).isEqualTo("Ra8+");
    }

    @Test
    public void testEncodeAppendsCheckmateSuffix() {
        Board board = Board.empty();
        board.placePiece(new Rook(Color.WHITE, Position.of("a1")), Position.of("a1"));
        Move move = Move.normal(Position.of("a1"), Position.of("a8"));

        String san = SanCodec.encode(board, move, List.of(move), false, true);

        assertThat(san).isEqualTo("Ra8#");
    }

    @Test
    public void testDecodeFindsMatchingMoveIgnoringCheckSuffix() {
        Board board = Board.empty();
        board.placePiece(new Knight(Color.WHITE, Position.of("g1")), Position.of("g1"));
        Move move = Move.normal(Position.of("g1"), Position.of("f3"));

        Move decoded = SanCodec.decode("Nf3+", board, List.of(move));

        assertThat(decoded).isEqualTo(move);
    }

    @Test
    public void testDecodeDisambiguatedMove() {
        Board board = Board.empty();
        Knight knightC3 = new Knight(Color.WHITE, Position.of("c3"));
        Knight knightG3 = new Knight(Color.WHITE, Position.of("g3"));
        board.placePiece(knightC3, Position.of("c3"));
        board.placePiece(knightG3, Position.of("g3"));
        Move moveFromC3 = Move.normal(Position.of("c3"), Position.of("e4"));
        Move moveFromG3 = Move.normal(Position.of("g3"), Position.of("e4"));
        List<Move> allMoves = List.of(moveFromC3, moveFromG3);

        Move decoded = SanCodec.decode("Nge4", board, allMoves);

        assertThat(decoded).isEqualTo(moveFromG3);
    }
}
