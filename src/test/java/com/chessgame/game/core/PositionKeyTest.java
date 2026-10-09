package com.chessgame.game.core;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import com.chessgame.notation.rules.FenCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PositionKey} のユニットテスト。千日手判定に使う局面キーの区別の仕方を固定する。
 */
class PositionKeyTest {

    private static String key(String fen, Position enPassant) {
        FenCodec.ParsedFen p = FenCodec.parse(fen);
        return PositionKey.of(p.board(), p.sideToMove(),
            p.whiteKingside(), p.whiteQueenside(), p.blackKingside(), p.blackQueenside(), enPassant);
    }

    @Test
    void sameArrangementGivesSameKey() {
        String start = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

        assertThat(key(start, null)).isEqualTo(key(start, null));
    }

    @Test
    void sideToMoveDistinguishesPositions() {
        assertThat(PositionKey.of(new Board(), Color.WHITE, true, true, true, true, null))
            .isNotEqualTo(PositionKey.of(new Board(), Color.BLACK, true, true, true, true, null));
    }

    @Test
    void castlingRightsDistinguishPositions() {
        assertThat(PositionKey.of(new Board(), Color.WHITE, true, true, true, true, null))
            .isNotEqualTo(PositionKey.of(new Board(), Color.WHITE, false, true, true, true, null));
    }

    @Test
    void enPassantTargetIsIgnoredWhenNoPawnCanCaptureIt() {
        // 1.e4 直後: 黒にアンパッサンを指せるポーンが居ないので、対象マスは区別に使わない
        String afterE4 = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";

        assertThat(key(afterE4, Position.of("e3"))).isEqualTo(key(afterE4, null));
    }

    @Test
    void enPassantTargetDistinguishesPositionWhenCaptureIsActuallyPossible() {
        // 黒ポーン d4 が居るので、1.e4 直後は dxe3 が可能。取れる場合は別の局面として区別する
        String capturable = "4k3/8/8/8/3pP3/8/8/4K3 b - e3 0 1";

        assertThat(key(capturable, Position.of("e3"))).isNotEqualTo(key(capturable, null));
    }

    @Test
    void pinnedPawnDoesNotMakeEnPassantCapturePossible() {
        // 黒ポーン d4 は、e3 へ取ると黒キング a4 が白ルーク h4 に晒される（ピン）ので dxe3 は合法でない
        String pinned = "8/8/8/8/k2pP2R/8/8/4K3 b - e3 0 1";

        assertThat(key(pinned, Position.of("e3"))).isEqualTo(key(pinned, null));
    }
}
