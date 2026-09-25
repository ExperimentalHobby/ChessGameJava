package com.chessgame.swing.board;

import com.chessgame.board.model.Position;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import com.chessgame.piece.model.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

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

    @Test
    void selectedSquareKeepsUnderlyingBoardColor() {
        // Issue #236: 選択中のマスに下地のマス色を塗らないと、半透明の選択色が
        // パネル背景に重なるため、明マスを選んでも暗マスを選んでも同じ色になる。
        // e2 は明マス・d2 は暗マスなので、選択時の描画色は一致してはならない
        int lightRgb = renderWithSelection(Position.of("e2"));
        int darkRgb = renderWithSelection(Position.of("d2"));

        assertThat(lightRgb).isNotEqualTo(darkRgb);
    }

    /**
     * 指定したマスの駒を選択した状態で盤面を描画し、そのマスの背景ピクセル色を返す。
     * 駒画像はマスの85%を中央に描くため、駒に覆われないマス左上寄りを標本点にする。
     */
    private static int renderWithSelection(Position pos) {
        ChessGame freshGame = ChessGame.createTwoPlayerGame("White", "Black");
        freshGame.startNewGame();
        SwingChessBoardPanel targetPanel = new SwingChessBoardPanel(freshGame);

        int sq = targetPanel.squareSize();
        targetPanel.setSize(sq * 8, sq * 8);
        click(targetPanel, pos);

        BufferedImage image = new BufferedImage(sq * 8, sq * 8, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        targetPanel.paintComponent(g);
        g.dispose();

        return image.getRGB(pos.getCol() * sq + 2, pos.getRow() * sq + 2);
    }

    @Test
    void preferredSizeCoversEightSquares() {
        assertThat(panel.getPreferredSize().width).isEqualTo(panel.getPreferredSize().height);
        assertThat(panel.getPreferredSize().width % 8).isZero();
    }

    @Test
    void mouseClickIsRoutedToSquareHandling() {
        // 登録済みの MouseListener 経由でも選択が走ること（handleSquareClick 直呼びとの差分を埋める）
        int sq = panel.squareSize();
        Position e2 = Position.of("e2");
        MouseEvent event = new MouseEvent(panel, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
            e2.getCol() * sq + sq / 2, e2.getRow() * sq + sq / 2, 1, false);

        for (var listener : panel.getMouseListeners()) {
            listener.mouseClicked(event);
        }

        click(Position.of("e4"));
        assertThat(game.getMoveHistory().size()).isEqualTo(1);
    }

    @Test
    void setGameSwitchesTargetAndResetsSelection() {
        click(Position.of("e2")); // 選択状態を作る

        ChessGame replacement = ChessGame.createTwoPlayerGame("W2", "B2");
        replacement.startNewGame();
        panel.setGame(replacement);

        // 差し替え後は選択が消えているため、移動先をクリックしても何も起きない
        click(Position.of("e4"));
        assertThat(replacement.getMoveHistory().isEmpty()).isTrue();
        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }

    @Test
    void clickOutsideBoardIsIgnored() {
        int sq = panel.squareSize();

        panel.handleSquareClick(sq * 8 + 5, 0); // 盤面右端より外
        panel.handleSquareClick(0, sq * 8 + 5); // 盤面下端より外

        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }

    @Test
    void updateBoardHighlightsLastMoveAndPaintsWithoutGame() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        panel.updateBoard();

        int sq = panel.squareSize();
        panel.setSize(sq * 8, sq * 8);
        BufferedImage withLastMove = new BufferedImage(sq * 8, sq * 8, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = withLastMove.createGraphics();
        panel.paintComponent(g);
        g.dispose();

        // 直前の手の移動元(e2)には半透明の重ね塗りが入るため、素の明マス色とは異なる
        SwingChessBoardPanel plain = new SwingChessBoardPanel(ChessGame.createTwoPlayerGame("A", "B"));
        plain.setSize(sq * 8, sq * 8);
        BufferedImage baseline = new BufferedImage(sq * 8, sq * 8, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = baseline.createGraphics();
        plain.paintComponent(g2);
        g2.dispose();

        Position e2 = Position.of("e2");
        assertThat(withLastMove.getRGB(e2.getCol() * sq + 2, e2.getRow() * sq + 2))
            .isNotEqualTo(baseline.getRGB(e2.getCol() * sq + 2, e2.getRow() * sq + 2));
    }

    @Test
    void panelWithoutGameIgnoresClicksAndPaintsNothing() {
        panel.setGame(null);
        int sq = panel.squareSize();
        panel.setSize(sq * 8, sq * 8);

        panel.handleSquareClick(0, 0);

        BufferedImage image = new BufferedImage(sq * 8, sq * 8, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        panel.paintComponent(g); // game == null で早期 return する
        g.dispose();

        assertThat(game.getMoveHistory().isEmpty()).isTrue();
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
