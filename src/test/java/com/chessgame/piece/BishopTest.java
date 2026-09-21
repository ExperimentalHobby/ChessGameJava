package com.chessgame.piece;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import com.chessgame.piece.model.Bishop;
import com.chessgame.piece.model.PieceType;
import com.chessgame.piece.model.Rook;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Bishop} の駒種・攻撃マス・{@code clone()} を直接検証する。
 */
public class BishopTest {

    @Test
    void getTypeReturnsBishop() {
        Bishop bishop = new Bishop(Color.WHITE, Position.of("d4"));

        assertThat(bishop.getType()).isEqualTo(PieceType.BISHOP);
    }

    @Test
    void bishopOnEmptyBoardAttacksAllFourDiagonalsToEdge() {
        Board board = Board.empty();
        Bishop bishop = new Bishop(Color.WHITE, Position.of("d4"));
        board.placePiece(bishop, Position.of("d4"));

        List<Position> attacked = bishop.getAttackedSquares(board);

        assertThat(attacked).containsExactlyInAnyOrder(
            // 左上（a7方向）
            Position.of("c5"), Position.of("b6"), Position.of("a7"),
            // 右上（h8方向）
            Position.of("e5"), Position.of("f6"), Position.of("g7"), Position.of("h8"),
            // 左下（a1方向）
            Position.of("c3"), Position.of("b2"), Position.of("a1"),
            // 右下（g1方向）
            Position.of("e3"), Position.of("f2"), Position.of("g1")
        );
    }

    @Test
    void bishopStopsAtBlockingPieceRegardlessOfColor() {
        Board board = Board.empty();
        Bishop bishop = new Bishop(Color.WHITE, Position.of("d4"));
        board.placePiece(bishop, Position.of("d4"));
        board.placePiece(new Rook(Color.WHITE, Position.of("f6")), Position.of("f6"));
        board.placePiece(new Rook(Color.BLACK, Position.of("b6")), Position.of("b6"));

        List<Position> attacked = bishop.getAttackedSquares(board);

        // 駒のあるマス自体は含むが、その先（g7・h8 や a7）へは進まない
        assertThat(attacked).contains(Position.of("e5"), Position.of("f6"));
        assertThat(attacked).doesNotContain(Position.of("g7"), Position.of("h8"));
        assertThat(attacked).contains(Position.of("c5"), Position.of("b6"));
        assertThat(attacked).doesNotContain(Position.of("a7"));
    }

    @Test
    void cloneProducesIndependentCopyWithSameMoveCount() {
        Bishop original = new Bishop(Color.WHITE, Position.of("d4"));
        original.incrementMoveCount();

        Bishop cloned = original.clone();
        cloned.setPosition(Position.of("a7"));

        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getMoveCount()).isEqualTo(1);
        assertThat(original.getPosition()).isEqualTo(Position.of("d4"));
    }
}
