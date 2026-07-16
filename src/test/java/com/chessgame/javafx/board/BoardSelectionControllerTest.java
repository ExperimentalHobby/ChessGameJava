package com.chessgame.javafx.board;

import com.chessgame.board.model.Position;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import com.chessgame.piece.model.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BoardSelectionController} のクリック操作と {@link ChessGame} 連携を検証する結合テスト。
 * {@link ChessBoardView} はJavaFX Toolkit初期化(Canvas.snapshot)が必要でheadless環境から
 * インスタンス化できないため、選択・移動・昇格ロジックを抽出したこのクラスを直接検証する。
 * Swing版 {@code SwingChessBoardPanelTest} と対をなす。
 */
class BoardSelectionControllerTest {
    private ChessGame game;
    private BoardSelectionController controller;

    @BeforeEach
    void setUp() {
        game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
        controller = new BoardSelectionController(game, color -> PieceType.QUEEN);
    }

    @Test
    void clickingOwnPieceThenLegalDestinationAppliesMove() {
        ClickOutcome first = controller.handleClick(Position.of("e2")); // 白ポーン選択
        ClickOutcome second = controller.handleClick(Position.of("e4")); // 合法な移動先

        assertThat(first.getType()).isEqualTo(ClickOutcome.Type.SELECTED);
        assertThat(second.getType()).isEqualTo(ClickOutcome.Type.MOVE_ATTEMPTED);
        assertThat(second.isMoveSucceeded()).isTrue();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
        assertThat(game.getMoveHistory().size()).isEqualTo(1);
        assertThat(game.getBoard().getPieceAt(Position.of("e4"))).isNotNull();
    }

    @Test
    void selectingOwnPieceHighlightsItsLegalDestinations() {
        ClickOutcome outcome = controller.handleClick(Position.of("e2"));

        assertThat(outcome.getPosition()).isEqualTo(Position.of("e2"));
        assertThat(outcome.getHighlightTargets())
            .containsExactlyInAnyOrder(Position.of("e3"), Position.of("e4"));
    }

    @Test
    void clickingSameSquareAgainDeselectsAndSubsequentClickDoesNotMove() {
        controller.handleClick(Position.of("e2")); // 選択
        ClickOutcome deselect = controller.handleClick(Position.of("e2")); // 再クリックで選択解除
        ClickOutcome afterDeselect = controller.handleClick(Position.of("e4")); // 選択されていないので何も起きないはず

        assertThat(deselect.getType()).isEqualTo(ClickOutcome.Type.DESELECTED);
        assertThat(afterDeselect.getType()).isEqualTo(ClickOutcome.Type.NONE);
        assertThat(game.getMoveHistory().isEmpty()).isTrue();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    void clickingAnotherOwnPieceSwitchesSelectionToNewPiece() {
        controller.handleClick(Position.of("e2")); // e2ポーンを選択
        ClickOutcome switched = controller.handleClick(Position.of("d2")); // 別の自駒(d2ポーン)を選択 → 選択が切り替わるはず
        controller.handleClick(Position.of("d4")); // d2ポーンの合法な移動先

        assertThat(switched.getType()).isEqualTo(ClickOutcome.Type.SELECTED);
        assertThat(switched.getPosition()).isEqualTo(Position.of("d2"));
        assertThat(game.getMoveHistory().size()).isEqualTo(1);
        assertThat(game.getBoard().getPieceAt(Position.of("d4"))).isNotNull();
        assertThat(game.getBoard().getPieceAt(Position.of("e2"))).isNotNull(); // e2ポーンは動いていない
    }

    @Test
    void clickDoesNothingAfterGameIsOver() {
        game.resign(Color.WHITE); // 任意の終局状態を作る

        ClickOutcome outcome1 = controller.handleClick(Position.of("e7"));
        ClickOutcome outcome2 = controller.handleClick(Position.of("e5"));

        assertThat(outcome1.getType()).isEqualTo(ClickOutcome.Type.NONE);
        assertThat(outcome2.getType()).isEqualTo(ClickOutcome.Type.NONE);
        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }

    @Test
    void clickDoesNothingDuringAiTurn() {
        ChessGame aiGame = new ChessGame(
                Player.human(Color.WHITE, "White"),
                new Player(Color.BLACK, "AI", false));
        aiGame.startNewGame();
        BoardSelectionController aiController = new BoardSelectionController(aiGame, color -> PieceType.QUEEN);

        assertThat(aiGame.makeMove(Position.of("e2"), Position.of("e4"))).isTrue(); // 手番をAI(黒)に渡す

        ClickOutcome outcome1 = aiController.handleClick(Position.of("e7")); // AI側の駒をクリックしても選択されないはず
        ClickOutcome outcome2 = aiController.handleClick(Position.of("e5"));

        assertThat(outcome1.getType()).isEqualTo(ClickOutcome.Type.NONE);
        assertThat(outcome2.getType()).isEqualTo(ClickOutcome.Type.NONE);
        assertThat(aiGame.getMoveHistory().size()).isEqualTo(1);
        assertThat(aiGame.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
    }

    @Test
    void promotionMoveAppliesInjectedResolverChoice() {
        // b7 まで白ポーンを進め、a8 への昇格を伴う移動を用意する
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("c7"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("d5"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("c6"), Position.of("b7"))).isTrue();
        assertThat(game.makeMove(Position.of("a6"), Position.of("a5"))).isTrue();

        BoardSelectionController promotionController =
            new BoardSelectionController(game, color -> PieceType.KNIGHT);
        promotionController.handleClick(Position.of("b7"));
        ClickOutcome outcome = promotionController.handleClick(Position.of("a8"));

        assertThat(outcome.isMoveSucceeded()).isTrue();
        var promoted = game.getBoard().getPieceAt(Position.of("a8"));
        assertThat(promoted).isNotNull();
        assertThat(promoted.getType()).isEqualTo(PieceType.KNIGHT);
    }

    @Test
    void clearSelectionResetsSelectedPosition() {
        controller.handleClick(Position.of("e2"));
        assertThat(controller.getSelectedPosition()).isEqualTo(Position.of("e2"));

        controller.clearSelection();

        assertThat(controller.getSelectedPosition()).isNull();
    }
}
