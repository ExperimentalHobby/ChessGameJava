package com.chessgame.model.piece;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * {@link PieceType} の素材価値と記法文字の契約を固定するテスト。
 * AIPlayer の評価ロジックがこの値に依存するため、回帰を検出する。
 */
public class PieceTypeTest {

    @Test
    public void materialValuesMatchStandardChessWeights() {
        assertThat(PieceType.PAWN.getMaterialValue()).isEqualTo(1);
        assertThat(PieceType.KNIGHT.getMaterialValue()).isEqualTo(3);
        assertThat(PieceType.BISHOP.getMaterialValue()).isEqualTo(3);
        assertThat(PieceType.ROOK.getMaterialValue()).isEqualTo(5);
        assertThat(PieceType.QUEEN.getMaterialValue()).isEqualTo(9);
        assertThat(PieceType.KING.getMaterialValue()).isEqualTo(0);
    }

    @Test
    public void notationCharactersAreStandard() {
        assertThat(PieceType.KING.getNotation()).isEqualTo('K');
        assertThat(PieceType.QUEEN.getNotation()).isEqualTo('Q');
        assertThat(PieceType.ROOK.getNotation()).isEqualTo('R');
        assertThat(PieceType.BISHOP.getNotation()).isEqualTo('B');
        assertThat(PieceType.KNIGHT.getNotation()).isEqualTo('N');
        assertThat(PieceType.PAWN.getNotation()).isEqualTo('P');
    }
}
