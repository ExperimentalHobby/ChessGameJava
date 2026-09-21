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
 * {@link Rook} の駒種・攻撃マス・{@code clone()} を直接検証する。
 */
public class RookTest {

    @Test
    void getTypeReturnsRook() {
        Rook rook = new Rook(Color.WHITE, Position.of("d4"));

        assertThat(rook.getType()).isEqualTo(PieceType.ROOK);
    }

    @Test
    void rookOnEmptyBoardAttacksAllFourStraightLinesToEdge() {
        Board board = Board.empty();
        Rook rook = new Rook(Color.WHITE, Position.of("d4"));
        board.placePiece(rook, Position.of("d4"));

        List<Position> attacked = rook.getAttackedSquares(board);

        assertThat(attacked).containsExactlyInAnyOrder(
            // 上（d5〜d8）
            Position.of("d5"), Position.of("d6"), Position.of("d7"), Position.of("d8"),
            // 下（d3〜d1）
            Position.of("d3"), Position.of("d2"), Position.of("d1"),
            // 左（c4〜a4）
            Position.of("c4"), Position.of("b4"), Position.of("a4"),
            // 右（e4〜h4）
            Position.of("e4"), Position.of("f4"), Position.of("g4"), Position.of("h4")
        );
    }

    @Test
    void rookStopsAtBlockingPieceRegardlessOfColor() {
        Board board = Board.empty();
        Rook rook = new Rook(Color.WHITE, Position.of("d4"));
        board.placePiece(rook, Position.of("d4"));
        board.placePiece(new Bishop(Color.WHITE, Position.of("d6")), Position.of("d6"));
        board.placePiece(new Bishop(Color.BLACK, Position.of("b4")), Position.of("b4"));

        List<Position> attacked = rook.getAttackedSquares(board);

        assertThat(attacked).contains(Position.of("d5"), Position.of("d6"));
        assertThat(attacked).doesNotContain(Position.of("d7"), Position.of("d8"));
        assertThat(attacked).contains(Position.of("c4"), Position.of("b4"));
        assertThat(attacked).doesNotContain(Position.of("a4"));
    }

    @Test
    void cloneProducesIndependentCopyWithSameMoveCount() {
        Rook original = new Rook(Color.WHITE, Position.of("a1"));
        original.incrementMoveCount();

        Rook cloned = original.clone();
        cloned.setPosition(Position.of("a4"));

        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getMoveCount()).isEqualTo(1);
        assertThat(original.getPosition()).isEqualTo(Position.of("a1"));
    }
}
