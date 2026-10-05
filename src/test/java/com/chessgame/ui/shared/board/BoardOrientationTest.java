package com.chessgame.ui.shared.board;

import com.chessgame.board.model.Position;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BoardOrientation} のユニットテスト。黒視点の盤面反転の座標変換を固定する。
 */
class BoardOrientationTest {

    @Test
    void whitePerspectiveKeepsCoordinates() {
        assertThat(BoardOrientation.displayRow(0, false)).isEqualTo(0);
        assertThat(BoardOrientation.displayCol(7, false)).isEqualTo(7);
    }

    @Test
    void blackPerspectiveRotatesBoardByHalfTurn() {
        // 黒視点では a8（row0,col0）が右下、h1（row7,col7）が左上に来る
        assertThat(BoardOrientation.displayRow(0, true)).isEqualTo(7);
        assertThat(BoardOrientation.displayCol(0, true)).isEqualTo(7);
        assertThat(BoardOrientation.displayRow(7, true)).isEqualTo(0);
        assertThat(BoardOrientation.displayCol(7, true)).isEqualTo(0);
    }

    @Test
    void conversionIsItsOwnInverseSoClicksMapBackToLogicalSquares() {
        for (boolean flipped : new boolean[] {false, true}) {
            for (int i = 0; i < 8; i++) {
                assertThat(BoardOrientation.displayRow(BoardOrientation.displayRow(i, flipped), flipped)).isEqualTo(i);
                assertThat(BoardOrientation.displayCol(BoardOrientation.displayCol(i, flipped), flipped)).isEqualTo(i);
            }
        }
    }

    @Test
    void squareAtDisplayCellReturnsLogicalPositionUnderTheCell() {
        // 黒視点で画面の左上セル(0,0)は論理マス h1
        assertThat(BoardOrientation.squareAt(0, 0, true)).isEqualTo(Position.of("h1"));
        assertThat(BoardOrientation.squareAt(0, 0, false)).isEqualTo(Position.of("a8"));
        assertThat(BoardOrientation.squareAt(7, 7, true)).isEqualTo(Position.of("a8"));
    }
}
