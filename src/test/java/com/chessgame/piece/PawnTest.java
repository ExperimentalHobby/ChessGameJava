package com.chessgame.piece;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import com.chessgame.piece.model.Pawn;
import com.chessgame.piece.model.PieceType;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Pawn} の駒種・攻撃マス・{@code clone()} を直接検証する。
 */
public class PawnTest {

    @Test
    void getTypeReturnsPawn() {
        Pawn pawn = new Pawn(Color.WHITE, Position.of("e2"));

        assertThat(pawn.getType()).isEqualTo(PieceType.PAWN);
    }

    @Test
    void whitePawnAttacksBothDiagonalsForwardOnly() {
        Board board = Board.empty();
        Pawn pawn = new Pawn(Color.WHITE, Position.of("e4"));
        board.placePiece(pawn, Position.of("e4"));

        List<Position> attacked = pawn.getAttackedSquares(board);

        assertThat(attacked).containsExactlyInAnyOrder(Position.of("d5"), Position.of("f5"));
    }

    @Test
    void blackPawnAttacksBothDiagonalsInOppositeDirection() {
        Board board = Board.empty();
        Pawn pawn = new Pawn(Color.BLACK, Position.of("e5"));
        board.placePiece(pawn, Position.of("e5"));

        List<Position> attacked = pawn.getAttackedSquares(board);

        assertThat(attacked).containsExactlyInAnyOrder(Position.of("d4"), Position.of("f4"));
    }

    @Test
    void pawnOnFileAOnlyAttacksSingleDiagonal() {
        Board board = Board.empty();
        Pawn pawn = new Pawn(Color.WHITE, Position.of("a4"));
        board.placePiece(pawn, Position.of("a4"));

        List<Position> attacked = pawn.getAttackedSquares(board);

        assertThat(attacked).containsExactly(Position.of("b5"));
    }

    @Test
    void cloneProducesIndependentCopyWithSameMoveCount() {
        Pawn original = new Pawn(Color.WHITE, Position.of("e2"));
        original.incrementMoveCount();

        Pawn cloned = original.clone();
        cloned.setPosition(Position.of("e4"));

        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getMoveCount()).isEqualTo(1);
        assertThat(original.getPosition()).isEqualTo(Position.of("e2"));
    }
}
