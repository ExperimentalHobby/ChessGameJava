package com.chessgame.gamestate;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.model.Color;
import com.chessgame.move.model.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link GameState} の単体テスト。{@link com.chessgame.game.core.ChessGameTest} 経由で
 * 間接的にしか検証されていなかったメソッド（resetGame・getFullmoveNumber・getOpponentColor・
 * clearPositionCounts・setHalfmoveOffsetAtLoad・isGameOver の全ステータス分岐）を直接検証する。
 */
class GameStateTest {

    private GameState gameState;

    @BeforeEach
    void setUp() {
        gameState = new GameState();
    }

    @Test
    void testResetGameRestoresInitialState() {
        gameState.setBoard(Board.empty());
        gameState.setCurrentPlayerColor(Color.BLACK);
        gameState.setGameStatus(GameState.GameStatus.CHECK);
        gameState.recordMove(Move.normal(Position.of("e2"), Position.of("e4")));
        gameState.setEnPassantTarget(Position.of("e3"));
        gameState.setHalfmoveClock(7);
        gameState.setHalfmoveOffsetAtLoad(10);
        gameState.recordPosition("dummy-key");
        gameState.recordPosition("dummy-key");

        gameState.resetGame();

        assertThat(gameState.getBoard().getAllPieces(Color.WHITE)).hasSize(16);
        assertThat(gameState.getBoard().getPieceAt(Position.of("e2"))).isNotNull();
        assertThat(gameState.getCurrentPlayerColor()).isEqualTo(Color.WHITE);
        assertThat(gameState.getGameStatus()).isEqualTo(GameState.GameStatus.IN_PROGRESS);
        assertThat(gameState.getMoveHistory().isEmpty()).isTrue();
        assertThat(gameState.getEnPassantTarget()).isNull();
        assertThat(gameState.getHalfmoveClock()).isZero();
        assertThat(gameState.getFullmoveNumber()).isEqualTo(1);
        assertThat(gameState.recordPosition("dummy-key")).isEqualTo(1);
    }

    @Test
    void testGetFullmoveNumberIncrementsAfterBlackMoves() {
        assertThat(gameState.getFullmoveNumber()).isEqualTo(1);

        gameState.recordMove(Move.normal(Position.of("e2"), Position.of("e4")));
        assertThat(gameState.getFullmoveNumber()).isEqualTo(1);

        gameState.recordMove(Move.normal(Position.of("e7"), Position.of("e5")));
        assertThat(gameState.getFullmoveNumber()).isEqualTo(2);

        gameState.recordMove(Move.normal(Position.of("g1"), Position.of("f3")));
        assertThat(gameState.getFullmoveNumber()).isEqualTo(2);
    }

    @Test
    void testGetFullmoveNumberUsesHalfmoveOffsetAtLoad() {
        gameState.setHalfmoveOffsetAtLoad(5);

        assertThat(gameState.getFullmoveNumber()).isEqualTo(3);
    }

    @Test
    void testGetOpponentColorReturnsOppositeOfCurrentPlayer() {
        assertThat(gameState.getOpponentColor()).isEqualTo(Color.BLACK);

        gameState.switchPlayer();

        assertThat(gameState.getOpponentColor()).isEqualTo(Color.WHITE);
    }

    @Test
    void testClearPositionCountsResetsOccurrenceCounts() {
        gameState.recordPosition("pos-a");
        gameState.recordPosition("pos-a");

        gameState.clearPositionCounts();

        assertThat(gameState.recordPosition("pos-a")).isEqualTo(1);
    }

    @Test
    void testRecordPositionReturnsIncrementingOccurrenceCount() {
        assertThat(gameState.recordPosition("pos-b")).isEqualTo(1);
        assertThat(gameState.recordPosition("pos-b")).isEqualTo(2);
        assertThat(gameState.recordPosition("pos-b")).isEqualTo(3);
    }

    @Test
    void testIsGameOverTrueForAllTerminalStatuses() {
        GameState.GameStatus[] terminalStatuses = {
            GameState.GameStatus.CHECKMATE,
            GameState.GameStatus.STALEMATE,
            GameState.GameStatus.FIFTY_MOVE_RULE,
            GameState.GameStatus.THREEFOLD_REPETITION,
            GameState.GameStatus.INSUFFICIENT_MATERIAL,
            GameState.GameStatus.WHITE_RESIGNED,
            GameState.GameStatus.BLACK_RESIGNED
        };

        for (GameState.GameStatus status : terminalStatuses) {
            gameState.setGameStatus(status);
            assertThat(gameState.isGameOver()).as("status %s", status).isTrue();
        }
    }

    @Test
    void testIsGameOverFalseForNonTerminalStatuses() {
        GameState.GameStatus[] nonTerminalStatuses = {
            GameState.GameStatus.IN_PROGRESS,
            GameState.GameStatus.CHECK
        };

        for (GameState.GameStatus status : nonTerminalStatuses) {
            gameState.setGameStatus(status);
            assertThat(gameState.isGameOver()).as("status %s", status).isFalse();
        }
    }
}
