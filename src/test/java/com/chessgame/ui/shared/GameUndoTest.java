package com.chessgame.ui.shared;

import com.chessgame.board.model.Position;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link GameUndo} のユニットテスト。Swing/JavaFX で共有する Undo 手順の挙動を固定する。
 */
class GameUndoTest {

    private static ChessGame humanVsAi() {
        return new ChessGame(Player.human(Color.WHITE, "You"), new AIPlayer("AI", Color.BLACK, 1));
    }

    @Test
    void returnsFalseWhenThereIsNothingToUndo() {
        ChessGame game = ChessGame.createTwoPlayerGame("W", "B");

        assertThat(GameUndo.undo(game, false)).isFalse();
    }

    @Test
    void undoesOnlyOneMoveInTwoPlayerGame() {
        ChessGame game = ChessGame.createTwoPlayerGame("W", "B");
        game.makeMove(Position.of("e2"), Position.of("e4"));
        game.makeMove(Position.of("e7"), Position.of("e5"));

        assertThat(GameUndo.undo(game, false)).isTrue();

        assertThat(game.getMoveHistory().size()).isEqualTo(1);
    }

    @Test
    void undoesAiMoveTogetherWithHumanMoveInAiGame() {
        ChessGame game = humanVsAi();
        game.makeMove(Position.of("e2"), Position.of("e4"));
        game.makeMove(Position.of("e7"), Position.of("e5")); // AI の応手

        assertThat(GameUndo.undo(game, true)).isTrue();

        assertThat(game.getMoveHistory().isEmpty()).isTrue();
        assertThat(game.getCurrentPlayer().isHuman()).isTrue();
    }

    @Test
    void doesNotUndoTooFarWhenHumanMoveIsTheLastMoveInAiGame() {
        // 人間の手の直後（AI がまだ指していない）に undo しても、戻すのは人間の手1つだけ
        ChessGame game = humanVsAi();
        game.makeMove(Position.of("e2"), Position.of("e4"));
        game.makeMove(Position.of("e7"), Position.of("e5"));
        game.makeMove(Position.of("g1"), Position.of("f3")); // 人間の2手目。AI の手番

        assertThat(GameUndo.undo(game, true)).isTrue();

        // Nf3 を戻すと手番は人間に戻るので追加の取り消しは起きない
        assertThat(game.getMoveHistory().size()).isEqualTo(2);
        assertThat(game.getCurrentPlayer().isHuman()).isTrue();
    }

    @Test
    void returnsFalseWhenGameEndedByResignation() {
        ChessGame game = ChessGame.createTwoPlayerGame("W", "B");
        game.makeMove(Position.of("e2"), Position.of("e4"));
        game.resign(Color.BLACK);

        assertThat(GameUndo.undo(game, false)).isFalse();
        assertThat(game.getMoveHistory().size()).isEqualTo(1);
    }

    @Test
    void doesNotUndoAiOpeningMoveWhenHumanPlaysBlack() {
        // 人間が黒のとき、履歴が AI（白）の初手だけなら取り消すものが無い。
        // 戻すと AI の手番に戻り、AI が同じように指し直すだけになる
        ChessGame game = new ChessGame(new AIPlayer("AI", Color.WHITE, 1), Player.human(Color.BLACK, "You"));
        game.makeMove(Position.of("e2"), Position.of("e4"));

        assertThat(GameUndo.undo(game, true)).isFalse();
        assertThat(game.getMoveHistory().size()).isEqualTo(1);
    }

    @Test
    void undoesHumanMoveAndAiReplyWhenHumanPlaysBlack() {
        ChessGame game = new ChessGame(new AIPlayer("AI", Color.WHITE, 1), Player.human(Color.BLACK, "You"));
        game.makeMove(Position.of("e2"), Position.of("e4")); // AI の初手
        game.makeMove(Position.of("e7"), Position.of("e5")); // 人間
        game.makeMove(Position.of("g1"), Position.of("f3")); // AI の応手

        assertThat(GameUndo.undo(game, true)).isTrue();

        // 人間の手(e5)まで戻り、AI の初手だけが残る。手番は人間（黒）
        assertThat(game.getMoveHistory().size()).isEqualTo(1);
        assertThat(game.getCurrentPlayer().isHuman()).isTrue();
    }
}
