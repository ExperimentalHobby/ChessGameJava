package com.chessgame.piece;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import com.chessgame.piece.model.Knight;
import com.chessgame.piece.model.PieceType;
import com.chessgame.piece.model.Rook;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Knight} の駒種・攻撃マス・{@code clone()} を直接検証する。
 */
public class KnightTest {

    @Test
    void getTypeReturnsKnight() {
        Knight knight = new Knight(Color.WHITE, Position.of("d4"));

        assertThat(knight.getType()).isEqualTo(PieceType.KNIGHT);
    }

    @Test
    void knightFromCenterAttacksAllEightLShapedSquares() {
        Board board = Board.empty();
        Knight knight = new Knight(Color.WHITE, Position.of("d4"));
        board.placePiece(knight, Position.of("d4"));

        List<Position> attacked = knight.getAttackedSquares(board);

        assertThat(attacked).containsExactlyInAnyOrder(
            Position.of("c6"), Position.of("e6"), Position.of("b5"), Position.of("f5"),
            Position.of("b3"), Position.of("f3"), Position.of("c2"), Position.of("e2")
        );
    }

    @Test
    void knightFromCornerAttacksOnlyTwoSquares() {
        Board board = Board.empty();
        Knight knight = new Knight(Color.WHITE, Position.of("a1"));
        board.placePiece(knight, Position.of("a1"));

        List<Position> attacked = knight.getAttackedSquares(board);

        assertThat(attacked).containsExactlyInAnyOrder(Position.of("b3"), Position.of("c2"));
    }

    @Test
    void knightCanJumpOverBlockingPieces() {
        Board board = Board.empty();
        Knight knight = new Knight(Color.WHITE, Position.of("a1"));
        board.placePiece(knight, Position.of("a1"));
        board.placePiece(new Rook(Color.WHITE, Position.of("a2")), Position.of("a2"));
        board.placePiece(new Rook(Color.WHITE, Position.of("b1")), Position.of("b1"));
        board.placePiece(new Rook(Color.WHITE, Position.of("b2")), Position.of("b2"));

        List<Position> attacked = knight.getAttackedSquares(board);

        assertThat(attacked).containsExactlyInAnyOrder(Position.of("b3"), Position.of("c2"));
    }

    @Test
    void cloneProducesIndependentCopyWithSameMoveCount() {
        Knight original = new Knight(Color.WHITE, Position.of("d4"));
        original.incrementMoveCount();

        Knight cloned = original.clone();
        cloned.setPosition(Position.of("e6"));

        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getMoveCount()).isEqualTo(1);
        assertThat(original.getPosition()).isEqualTo(Position.of("d4"));
    }
}
