package com.chessgame.detection;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.detection.rules.CheckmateDetector;
import com.chessgame.game.core.ChessGame;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.model.Color;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link CheckmateDetector} のチェックメイト・ステールメイト判定を検証する。
 * 実際のゲームを進行して状態を確認するアプローチと、盤面を手動構築するアプローチの両方を使う。
 */
public class CheckmateDetectorTest {
    private CheckmateDetector detector;

    @BeforeEach
    void setUp() {
        detector = new CheckmateDetector();
    }

    /** 全マスから駒を取り除いた空盤面を返す。 */
    private Board emptyBoard() {
        Board board = new Board();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board.removePiece(Position.of(row, col));
            }
        }
        return board;
    }

    // ===================== Checkmate =====================

    /**
     * フールズメイト（最短チェックメイト）でチェックメイトが正しく検出されること。
     * 1. f3 e5  2. g4 Qh4#
     */
    @Test
    void foolsMateResultsInCheckmate() {
        ChessGame game = ChessGame.createTwoPlayerGame("W", "B");
        game.startNewGame();
        game.makeMove(Position.of("f2"), Position.of("f3"));
        game.makeMove(Position.of("e7"), Position.of("e5"));
        game.makeMove(Position.of("g2"), Position.of("g4"));
        game.makeMove(Position.of("d8"), Position.of("h4"));

        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.CHECKMATE);
        assertThat(game.isGameOver()).isTrue();
    }

    @Test
    void checkmateRequiresBothCheckAndNoLegalMoves() {
        // フールズメイト直前（Qh4 の直前）は王手ではない
        ChessGame game = ChessGame.createTwoPlayerGame("W", "B");
        game.startNewGame();
        game.makeMove(Position.of("f2"), Position.of("f3"));
        game.makeMove(Position.of("e7"), Position.of("e5"));
        game.makeMove(Position.of("g2"), Position.of("g4"));
        // ここはまだ黒番・IN_PROGRESS
        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.IN_PROGRESS);
    }

    // ===================== Stalemate =====================

    /**
     * 白キング h1、黒クイーン f2、黒キング a8 の局面で白がステールメイトとなる。
     * h1 のキングは王手ではないが合法手がない（g1・g2・h2 はすべてクイーンに攻撃されている）。
     */
    @Test
    void stalemateDetectedWhenKingHasNoLegalMoves() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE,  Position.of("h1")), Position.of("h1"));
        board.placePiece(new Queen(Color.BLACK, Position.of("f2")), Position.of("f2"));
        board.placePiece(new King(Color.BLACK,  Position.of("a8")), Position.of("a8"));

        assertThat(detector.isStalemate(Color.WHITE, board)).isTrue();
    }

    @Test
    void stalemateReturnsFalseWhenInCheck() {
        // 王手中はステールメイトでない
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE,  Position.of("e1")), Position.of("e1"));
        board.placePiece(new Rook(Color.BLACK,  Position.of("e8")), Position.of("e8"));
        board.placePiece(new King(Color.BLACK,  Position.of("a8")), Position.of("a8"));

        assertThat(detector.isStalemate(Color.WHITE, board)).isFalse();
    }

    @Test
    void stalemateReturnsFalseWhenLegalMovesExist() {
        // 合法手がある場合はステールメイトでない
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e4")), Position.of("e4"));
        board.placePiece(new King(Color.BLACK, Position.of("a8")), Position.of("a8"));

        assertThat(detector.isStalemate(Color.WHITE, board)).isFalse();
    }

    // ===================== isCheckmate =====================

    @Test
    void isCheckmateReturnsFalseWhenNotInCheck() {
        Board board = emptyBoard();
        board.placePiece(new King(Color.WHITE, Position.of("e4")), Position.of("e4"));
        board.placePiece(new King(Color.BLACK, Position.of("a8")), Position.of("a8"));

        assertThat(detector.isCheckmate(Color.WHITE, board)).isFalse();
    }

    // ===================== isLegalMove =====================

    @Test
    void isLegalMoveReturnsFalseWhenMoveLeavesKingInCheck() {
        // ルークが白キングを王手している。その前の自駒をどかすのは不合法。
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e1"));
        Rook blocker = new Rook(Color.WHITE, Position.of("e4")); // e ファイルのブロッカー
        Rook blackRook = new Rook(Color.BLACK, Position.of("e8"));
        board.placePiece(king, Position.of("e1"));
        board.placePiece(blocker, Position.of("e4"));
        board.placePiece(blackRook, Position.of("e8"));

        // blocker を d4 に動かすと王手になる → 不合法
        Move exposingMove = Move.normal(Position.of("e4"), Position.of("d4"));
        assertThat(detector.isLegalMove(exposingMove, blocker, board, Color.WHITE)).isFalse();
    }

    @Test
    void isLegalMoveReturnsTrueWhenKingIsSafe() {
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e1"));
        Rook rook = new Rook(Color.WHITE, Position.of("a1"));
        board.placePiece(king, Position.of("e1"));
        board.placePiece(rook, Position.of("a1"));

        // a1 → a5 は王に影響しない合法手
        Move safeMove = Move.normal(Position.of("a1"), Position.of("a5"));
        assertThat(detector.isLegalMove(safeMove, rook, board, Color.WHITE)).isTrue();
    }

    // ===================== simulateEnPassant =====================

    @Test
    void isLegalMoveReturnsFalseForEnPassantThatExposesDiscoveredCheck() {
        // アンパッサンのピン: b5xc6 e.p. で c5 のポーンが消えると、a5のルークからe5のキングまで
        // 5段目が開通して王手になるため不合法
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e5"));
        Pawn whitePawn = new Pawn(Color.WHITE, Position.of("b5"));
        Pawn blackPawn = new Pawn(Color.BLACK, Position.of("c5"));
        Rook blackRook = new Rook(Color.BLACK, Position.of("a5"));
        board.placePiece(king, Position.of("e5"));
        board.placePiece(whitePawn, Position.of("b5"));
        board.placePiece(blackPawn, Position.of("c5"));
        board.placePiece(blackRook, Position.of("a5"));

        Move enPassantMove = Move.enPassant(Position.of("b5"), Position.of("c6"), blackPawn);
        assertThat(detector.isLegalMove(enPassantMove, whitePawn, board, Color.WHITE)).isFalse();
    }

    @Test
    void isLegalMoveReturnsTrueForEnPassantThatDoesNotExposeCheck() {
        // 通常のアンパッサン捕獲。キングに影響しないため合法
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e1"));
        Pawn whitePawn = new Pawn(Color.WHITE, Position.of("d5"));
        Pawn blackPawn = new Pawn(Color.BLACK, Position.of("e5"));
        board.placePiece(king, Position.of("e1"));
        board.placePiece(whitePawn, Position.of("d5"));
        board.placePiece(blackPawn, Position.of("e5"));

        Move enPassantMove = Move.enPassant(Position.of("d5"), Position.of("e6"), blackPawn);
        assertThat(detector.isLegalMove(enPassantMove, whitePawn, board, Color.WHITE)).isTrue();
    }

    // ===================== simulateCastling =====================

    @Test
    void isLegalMoveReturnsTrueForKingsideCastlingWhenRookBlocksRankAttack() {
        // キングサイドキャスリング後、h1→f1 に移動したルークが1段目のクイーンの利きを
        // 遮るため王手にならない（ルークが正しく移動しないバグがあれば逆にfalseになる）
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e1"));
        Rook rook = new Rook(Color.WHITE, Position.of("h1"));
        Queen blackQueen = new Queen(Color.BLACK, Position.of("b1"));
        board.placePiece(king, Position.of("e1"));
        board.placePiece(rook, Position.of("h1"));
        board.placePiece(blackQueen, Position.of("b1"));

        Move castlingMove = Move.castling(Position.of("e1"), Position.of("g1"));
        assertThat(detector.isLegalMove(castlingMove, king, board, Color.WHITE)).isTrue();
    }

    @Test
    void isLegalMoveReturnsTrueForQueensideCastlingWhenRookBlocksRankAttack() {
        // クイーンサイドキャスリング後、a1→d1 に移動したルークが1段目のクイーンの利きを
        // 遮るため王手にならない（simulateCastlingのelse節を検証）
        Board board = emptyBoard();
        King king = new King(Color.WHITE, Position.of("e1"));
        Rook rook = new Rook(Color.WHITE, Position.of("a1"));
        Queen blackQueen = new Queen(Color.BLACK, Position.of("h1"));
        board.placePiece(king, Position.of("e1"));
        board.placePiece(rook, Position.of("a1"));
        board.placePiece(blackQueen, Position.of("h1"));

        Move castlingMove = Move.castling(Position.of("e1"), Position.of("c1"));
        assertThat(detector.isLegalMove(castlingMove, king, board, Color.WHITE)).isTrue();
    }
}
