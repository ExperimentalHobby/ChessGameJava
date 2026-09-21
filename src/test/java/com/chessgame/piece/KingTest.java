package com.chessgame.piece;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import com.chessgame.piece.model.King;
import com.chessgame.piece.model.PieceType;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link King} の駒種・攻撃マス・{@code clone()} を直接検証する。
 */
public class KingTest {

    @Test
    void getTypeReturnsKing() {
        King king = new King(Color.WHITE, Position.of("e1"));

        assertThat(king.getType()).isEqualTo(PieceType.KING);
    }

    @Test
    void kingFromCenterAttacksAllEightAdjacentSquares() {
        Board board = Board.empty();
        King king = new King(Color.WHITE, Position.of("d4"));
        board.placePiece(king, Position.of("d4"));

        List<Position> attacked = king.getAttackedSquares(board);

        assertThat(attacked).containsExactlyInAnyOrder(
            Position.of("c5"), Position.of("d5"), Position.of("e5"),
            Position.of("c4"),                     Position.of("e4"),
            Position.of("c3"), Position.of("d3"), Position.of("e3")
        );
    }

    @Test
    void kingFromCornerAttacksOnlyThreeSquares() {
        Board board = Board.empty();
        King king = new King(Color.WHITE, Position.of("a1"));
        board.placePiece(king, Position.of("a1"));

        List<Position> attacked = king.getAttackedSquares(board);

        assertThat(attacked).containsExactlyInAnyOrder(
            Position.of("a2"), Position.of("b2"), Position.of("b1")
        );
    }

    @Test
    void castlingSquaresAreNotIncludedInAttackedSquares() {
        // キャスリングは MoveValidator が別途処理するため、原位置のキングでも g1/c1 は含まれない
        Board board = Board.empty();
        King king = new King(Color.WHITE, Position.of("e1"));
        board.placePiece(king, Position.of("e1"));

        List<Position> attacked = king.getAttackedSquares(board);

        assertThat(attacked).doesNotContain(Position.of("g1"), Position.of("c1"));
    }

    @Test
    void cloneProducesIndependentCopyWithSameMoveCount() {
        King original = new King(Color.WHITE, Position.of("e1"));
        original.incrementMoveCount();

        King cloned = original.clone();
        cloned.setPosition(Position.of("e2"));

        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getMoveCount()).isEqualTo(1);
        assertThat(original.getPosition()).isEqualTo(Position.of("e1"));
    }
}
