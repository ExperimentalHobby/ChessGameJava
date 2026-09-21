package com.chessgame.piece;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import com.chessgame.piece.model.PieceType;
import com.chessgame.piece.model.Queen;
import com.chessgame.piece.model.Rook;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Queen} の駒種・攻撃マス・{@code clone()} を直接検証する。
 */
public class QueenTest {

    @Test
    void getTypeReturnsQueen() {
        Queen queen = new Queen(Color.WHITE, Position.of("d4"));

        assertThat(queen.getType()).isEqualTo(PieceType.QUEEN);
    }

    @Test
    void queenOnEmptyBoardAttacksAllEightDirectionsToEdge() {
        Board board = Board.empty();
        Queen queen = new Queen(Color.WHITE, Position.of("d4"));
        board.placePiece(queen, Position.of("d4"));

        List<Position> attacked = queen.getAttackedSquares(board);

        assertThat(attacked).containsExactlyInAnyOrder(
            // 縦横（ルーク方向）
            Position.of("d5"), Position.of("d6"), Position.of("d7"), Position.of("d8"),
            Position.of("d3"), Position.of("d2"), Position.of("d1"),
            Position.of("c4"), Position.of("b4"), Position.of("a4"),
            Position.of("e4"), Position.of("f4"), Position.of("g4"), Position.of("h4"),
            // 斜め（ビショップ方向）
            Position.of("c5"), Position.of("b6"), Position.of("a7"),
            Position.of("e5"), Position.of("f6"), Position.of("g7"), Position.of("h8"),
            Position.of("c3"), Position.of("b2"), Position.of("a1"),
            Position.of("e3"), Position.of("f2"), Position.of("g1")
        );
    }

    @Test
    void queenStopsAtBlockingPieceRegardlessOfColor() {
        Board board = Board.empty();
        Queen queen = new Queen(Color.WHITE, Position.of("d4"));
        board.placePiece(queen, Position.of("d4"));
        board.placePiece(new Rook(Color.WHITE, Position.of("d6")), Position.of("d6"));
        board.placePiece(new Rook(Color.BLACK, Position.of("f6")), Position.of("f6"));

        List<Position> attacked = queen.getAttackedSquares(board);

        assertThat(attacked).contains(Position.of("d5"), Position.of("d6"));
        assertThat(attacked).doesNotContain(Position.of("d7"), Position.of("d8"));
        assertThat(attacked).contains(Position.of("e5"), Position.of("f6"));
        assertThat(attacked).doesNotContain(Position.of("g7"), Position.of("h8"));
    }

    @Test
    void cloneProducesIndependentCopyWithSameMoveCount() {
        Queen original = new Queen(Color.WHITE, Position.of("d1"));
        original.incrementMoveCount();

        Queen cloned = original.clone();
        cloned.setPosition(Position.of("d4"));

        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getMoveCount()).isEqualTo(1);
        assertThat(original.getPosition()).isEqualTo(Position.of("d1"));
    }
}
