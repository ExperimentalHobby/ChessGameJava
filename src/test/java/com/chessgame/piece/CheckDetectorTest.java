package com.chessgame.piece;

import com.chessgame.model.Color;
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.piece.model.Bishop;
import com.chessgame.piece.model.King;
import com.chessgame.piece.model.Knight;
import com.chessgame.piece.model.Pawn;
import com.chessgame.piece.model.Queen;
import com.chessgame.piece.model.Rook;
import com.chessgame.piece.rules.CheckDetector;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * {@link CheckDetector} の王手判定をスライディング駒のブロッカー有無で検証する。
 * 特に「経路を遮る駒があれば王手は成立しない」ことを担保する。
 */
public class CheckDetectorTest {
    private final CheckDetector detector = new CheckDetector();

    /** 標準配置の盤を生成し、全マスから駒を取り除いて空盤面にする。 */
    private Board emptyBoard() {
        Board board = new Board();
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                board.removePiece(Position.of(row, col));
            }
        }
        return board;
    }

    @Test
    public void rookGivesCheckAlongClearFile() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Rook(Color.BLACK, Position.of("e8")), Position.of("e8"));
        assertThat(detector.isInCheck(Color.WHITE, board)).isTrue();
    }

    @Test
    public void rookCheckIsBlockedByInterveningPiece() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Rook(Color.BLACK, Position.of("e8")), Position.of("e8"));
        // e4 のポーンが e ファイルを遮るため王手にならない
        board.placePiece(new Pawn(Color.BLACK, Position.of("e4")), Position.of("e4"));
        assertThat(detector.isInCheck(Color.WHITE, board)).isFalse();
    }

    @Test
    public void bishopGivesCheckAlongClearDiagonal() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Bishop(Color.BLACK, Position.of("a5")), Position.of("a5"));
        assertThat(detector.isInCheck(Color.WHITE, board)).isTrue();
    }

    @Test
    public void bishopCheckIsBlockedByInterveningPiece() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        board.placePiece(new Bishop(Color.BLACK, Position.of("a5")), Position.of("a5"));
        // c3 のポーンが a5-e1 の対角線を遮るため王手にならない
        board.placePiece(new Pawn(Color.BLACK, Position.of("c3")), Position.of("c3"));
        assertThat(detector.isInCheck(Color.WHITE, board)).isFalse();
    }

    @Test
    public void knightGivesCheck() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        // d3 のナイトは e1 に利く（L字移動）
        board.placePiece(new Knight(Color.BLACK, Position.of("d3")), Position.of("d3"));
        assertThat(detector.isInCheck(Color.WHITE, board)).isTrue();
    }

    @Test
    public void pawnGivesCheck() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        // 黒ポーンは前進方向(rank減少)へ斜めに利くため、d2 のポーンは e1 に利く
        board.placePiece(new Pawn(Color.BLACK, Position.of("d2")), Position.of("d2"));
        assertThat(detector.isInCheck(Color.WHITE, board)).isTrue();
    }

    @Test
    public void queenGivesCheckAlongClearRank() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        // a1 のクイーンは同じ1段目(横列)に遮る駒がないため e1 に利く
        board.placePiece(new Queen(Color.BLACK, Position.of("a1")), Position.of("a1"));
        assertThat(detector.isInCheck(Color.WHITE, board)).isTrue();
    }

    @Test
    public void noCheckWhenNoAttackerThreatensKing() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e1")), Position.of("e1"));
        // a8 のナイトは e1 に利かないため王手ではない
        board.placePiece(new Knight(Color.BLACK, Position.of("a8")), Position.of("a8"));
        assertThat(detector.isInCheck(Color.WHITE, board)).isFalse();
    }
}
