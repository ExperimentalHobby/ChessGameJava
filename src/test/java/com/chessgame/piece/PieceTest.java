package com.chessgame.piece;

import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import com.chessgame.piece.model.Rook;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link com.chessgame.piece.model.Piece} が持つ共通の状態管理ロジック
 * （色・位置・移動回数、{@code setPosition()}、{@code incrementMoveCount()}、{@code toString()}）を、
 * 具象クラス {@link Rook} を通じて検証する。
 */
public class PieceTest {

    @Test
    void newPieceHasInitialColorPositionAndZeroMoveCount() {
        Position start = Position.of("a1");
        Rook rook = new Rook(Color.WHITE, start);

        assertThat(rook.getColor()).isEqualTo(Color.WHITE);
        assertThat(rook.getPosition()).isEqualTo(start);
        assertThat(rook.getMoveCount()).isZero();
    }

    @Test
    void setPositionUpdatesPosition() {
        Rook rook = new Rook(Color.WHITE, Position.of("a1"));

        rook.setPosition(Position.of("a4"));

        assertThat(rook.getPosition()).isEqualTo(Position.of("a4"));
    }

    @Test
    void incrementMoveCountAccumulatesAcrossMultipleCalls() {
        Rook rook = new Rook(Color.WHITE, Position.of("a1"));

        rook.incrementMoveCount();
        rook.incrementMoveCount();

        assertThat(rook.getMoveCount()).isEqualTo(2);
    }

    @Test
    void toStringReturnsColorInitialAndType() {
        Rook whiteRook = new Rook(Color.WHITE, Position.of("a1"));
        Rook blackRook = new Rook(Color.BLACK, Position.of("a8"));

        assertThat(whiteRook.toString()).isEqualTo("WR");
        assertThat(blackRook.toString()).isEqualTo("BR");
    }
}
