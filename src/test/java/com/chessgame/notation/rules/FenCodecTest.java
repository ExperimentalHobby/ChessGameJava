package com.chessgame.notation.rules;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.model.Color;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link FenCodec} の FEN エンコード・デコードを検証する。
 */
public class FenCodecTest {
    private static final String STARTPOS =
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Test
    public void testEncodeStartingPosition() {
        Board board = new Board();
        String fen = FenCodec.encode(board, Color.WHITE, true, true, true, true, null, 0, 1);
        assertThat(fen).isEqualTo(STARTPOS);
    }

    @Test
    public void testParseStartingPosition() {
        FenCodec.ParsedFen parsed = FenCodec.parse(STARTPOS);

        assertThat(parsed.board().getPieceAt(Position.of("e1")).getType().getNotation()).isEqualTo('K');
        assertThat(parsed.board().getPieceAt(Position.of("e1")).getColor()).isEqualTo(Color.WHITE);
        assertThat(parsed.board().getPieceAt(Position.of("e8")).getColor()).isEqualTo(Color.BLACK);
        assertThat(parsed.sideToMove()).isEqualTo(Color.WHITE);
        assertThat(parsed.whiteKingside()).isTrue();
        assertThat(parsed.whiteQueenside()).isTrue();
        assertThat(parsed.blackKingside()).isTrue();
        assertThat(parsed.blackQueenside()).isTrue();
        assertThat(parsed.enPassant()).isNull();
        assertThat(parsed.halfmove()).isEqualTo(0);
        assertThat(parsed.fullmove()).isEqualTo(1);
    }

    @Test
    public void testParsePartialCastlingRights() {
        // 白のキングサイドと黒のクイーンサイドのみ権利あり
        FenCodec.ParsedFen parsed = FenCodec.parse(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w Kq - 0 1");

        assertThat(parsed.whiteKingside()).isTrue();
        assertThat(parsed.whiteQueenside()).isFalse();
        assertThat(parsed.blackKingside()).isFalse();
        assertThat(parsed.blackQueenside()).isTrue();
    }

    @Test
    public void testEnPassantFieldRoundTrip() {
        String fen = "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3";
        FenCodec.ParsedFen parsed = FenCodec.parse(fen);

        assertThat(parsed.enPassant()).isEqualTo(Position.of("d6"));

        String reencoded = FenCodec.encode(parsed.board(), parsed.sideToMove(),
            parsed.whiteKingside(), parsed.whiteQueenside(),
            parsed.blackKingside(), parsed.blackQueenside(),
            parsed.enPassant(), parsed.halfmove(), parsed.fullmove());
        assertThat(reencoded).isEqualTo(fen);
    }

    @Test
    public void testEncodeParseRoundTripForMidGamePosition() {
        // Kiwipete（AiEngineParityTest 等で使われる代表的な中盤局面）
        String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
        FenCodec.ParsedFen parsed = FenCodec.parse(fen);

        String reencoded = FenCodec.encode(parsed.board(), parsed.sideToMove(),
            parsed.whiteKingside(), parsed.whiteQueenside(),
            parsed.blackKingside(), parsed.blackQueenside(),
            parsed.enPassant(), parsed.halfmove(), parsed.fullmove());

        assertThat(reencoded).isEqualTo(fen);
    }
}
