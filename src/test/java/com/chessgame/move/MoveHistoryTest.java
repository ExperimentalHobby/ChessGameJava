package com.chessgame.move;

import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.move.model.MoveHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link MoveHistory} の追加・取得・Undo・棋譜フォーマットを検証する。
 */
public class MoveHistoryTest {
    private MoveHistory history;

    @BeforeEach
    void setUp() {
        history = new MoveHistory();
    }

    @Test
    void newHistoryIsEmpty() {
        assertThat(history.isEmpty()).isTrue();
        assertThat(history.size()).isEqualTo(0);
        assertThat(history.getLastMove()).isNull();
    }

    @Test
    void addMoveIncreasesSize() {
        history.addMove(Move.normal(Position.of("e2"), Position.of("e4")));

        assertThat(history.size()).isEqualTo(1);
        assertThat(history.isEmpty()).isFalse();
    }

    @Test
    void getLastMoveReturnsLatestMove() {
        Move first  = Move.normal(Position.of("e2"), Position.of("e4"));
        Move second = Move.normal(Position.of("e7"), Position.of("e5"));
        history.addMove(first);
        history.addMove(second);

        assertThat(history.getLastMove()).isEqualTo(second);
    }

    @Test
    void undoLastMoveRemovesAndReturnsIt() {
        Move move = Move.normal(Position.of("e2"), Position.of("e4"));
        history.addMove(move);

        Move removed = history.undoLastMove();

        assertThat(removed).isEqualTo(move);
        assertThat(history.isEmpty()).isTrue();
    }

    @Test
    void undoLastMoveOnEmptyHistoryReturnsNull() {
        assertThat(history.undoLastMove()).isNull();
    }

    @Test
    void addNullMoveIsIgnored() {
        history.addMove(null);

        assertThat(history.isEmpty()).isTrue();
    }

    @Test
    void getAllReturnsUnmodifiableSnapshot() {
        history.addMove(Move.normal(Position.of("e2"), Position.of("e4")));

        var all = history.getAll();

        assertThat(all).hasSize(1);
        assertThatThrownBy(() -> all.add(Move.normal(Position.of("a1"), Position.of("a2"))))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void notationStringFormatsMovePairs() {
        history.addMove(Move.normal(Position.of("e2"), Position.of("e4")));
        history.addMove(Move.normal(Position.of("e7"), Position.of("e5")));

        String notation = history.getNotationString();

        // "1. e2e4 e7e5 " の形式
        assertThat(notation).startsWith("1. e2e4");
        assertThat(notation).contains("e7e5");
    }
}
