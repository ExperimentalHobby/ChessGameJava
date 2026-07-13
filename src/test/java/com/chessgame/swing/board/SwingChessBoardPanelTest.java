package com.chessgame.swing.board;

import com.chessgame.board.model.Position;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import com.chessgame.piece.model.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SwingChessBoardPanel} のクリック操作と {@link ChessGame} 連携を検証する結合テスト。
 * {@code JPanel}(非 {@code Window})のため、ヘッドレス環境でも直接インスタンス化できる。
 */
class SwingChessBoardPanelTest {
    private ChessGame game;
    private SwingChessBoardPanel panel;

    @BeforeEach
    void setUp() {
        game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
        panel = new SwingChessBoardPanel(game);
    }

    /** 論理マス位置をクリックしたのと同じ処理を実行する（squareSize() は未表示時 DEFAULT_SQUARE_SIZE を返す）。 */
    private void click(Position pos) {
        click(panel, pos);
    }

    private static void click(SwingChessBoardPanel targetPanel, Position pos) {
        int sq = targetPanel.squareSize();
        targetPanel.handleSquareClick(pos.getCol() * sq + sq / 2, pos.getRow() * sq + sq / 2);
    }

    @Test
    void clickingOwnPieceThenLegalDestinationAppliesMove() {
        click(Position.of("e2")); // 白ポーン選択
        click(Position.of("e4")); // 合法な移動先

        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
        assertThat(game.getMoveHistory().size()).isEqualTo(1);
        assertThat(game.getBoard().getPieceAt(Position.of("e4"))).isNotNull();
    }

    @Test
    void clickingSameSquareAgainDeselectsAndSubsequentClickDoesNotMove() {
        click(Position.of("e2")); // 選択
        click(Position.of("e2")); // 再クリックで選択解除
        click(Position.of("e4")); // 選択されていないので何も起きないはず

        assertThat(game.getMoveHistory().isEmpty()).isTrue();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    void clickingAnotherOwnPieceSwitchesSelectionToNewPiece() {
        click(Position.of("e2")); // e2ポーンを選択
        click(Position.of("d2")); // 別の自駒(d2ポーン)を選択 → 選択が切り替わるはず
        click(Position.of("d4")); // d2ポーンの合法な移動先

        assertThat(game.getMoveHistory().size()).isEqualTo(1);
        assertThat(game.getBoard().getPieceAt(Position.of("d4"))).isNotNull();
        assertThat(game.getBoard().getPieceAt(Position.of("e2"))).isNotNull(); // e2ポーンは動いていない
    }

    @Test
    void clickDoesNothingAfterGameIsOver() {
        game.resign(Color.WHITE); // 任意の終局状態を作る

        click(Position.of("e7"));
        click(Position.of("e5"));

        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }

    @Test
    void clickDoesNothingDuringAiTurn() {
        ChessGame aiGame = new ChessGame(
                Player.human(Color.WHITE, "White"),
                new Player(Color.BLACK, "AI", false));
        aiGame.startNewGame();
        SwingChessBoardPanel aiPanel = new SwingChessBoardPanel(aiGame);

        assertThat(aiGame.makeMove(Position.of("e2"), Position.of("e4"))).isTrue(); // 手番をAI(黒)に渡す

        click(aiPanel, Position.of("e7")); // AI側の駒をクリックしても選択されないはず
        click(aiPanel, Position.of("e5"));

        assertThat(aiGame.getMoveHistory().size()).isEqualTo(1);
        assertThat(aiGame.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
    }

    @Test
    void promotionMoveAppliesDialogSelectedPieceType() {
        // b7 まで白ポーンを進め、a8 への昇格を伴う移動を用意する
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("c7"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("d5"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("c6"), Position.of("b7"))).isTrue();
        assertThat(game.makeMove(Position.of("a6"), Position.of("a5"))).isTrue();

        SwingChessBoardPanel promotionPanel = new FixedPromotionBoardPanel(game, PieceType.KNIGHT);
        click(promotionPanel, Position.of("b7"));
        click(promotionPanel, Position.of("a8"));

        var promoted = game.getBoard().getPieceAt(Position.of("a8"));
        assertThat(promoted).isNotNull();
        assertThat(promoted.getType()).isEqualTo(PieceType.KNIGHT);
    }

    /** テストで実際のモーダルダイアログを表示しないよう、選択結果を固定値に差し替えるサブクラス。 */
    private static class FixedPromotionBoardPanel extends SwingChessBoardPanel {
        private final PieceType fixedChoice;

        FixedPromotionBoardPanel(ChessGame game, PieceType fixedChoice) {
            super(game);
            this.fixedChoice = fixedChoice;
        }

        @Override
        protected PieceType showPromotionDialog(Color color) {
            return fixedChoice;
        }
    }
}
