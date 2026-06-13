package com.chessgame.javafx.board;

import com.chessgame.board.model.Position;
import com.chessgame.piece.model.Piece;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * チェス盤の1マスを表す JavaFX コンポーネント。
 * 背景色・駒画像・ハイライト（選択中・移動可能）の描画を担当する。
 */
public class SquareView extends StackPane {
    private static final int SQUARE_SIZE = 60;
    private static final Color LIGHT_COLOR = Color.web("#F0D9B5");
    private static final Color DARK_COLOR = Color.web("#B58863");
    private static final Color HIGHLIGHT_COLOR = Color.web("#FFD700", 0.627);
    private static final Color SELECTED_COLOR = Color.web("#6AA84F", 0.784);

    private final Position position;
    private final Rectangle background;
    private ImageView pieceImageView;
    private Circle highlightCircle;
    private Piece piece;
    private Runnable onClickHandler;

    /**
     * 指定した位置のマスビューを生成する。マスの色は位置の奇偶で自動決定する。
     *
     * @param position このマスの位置
     */
    public SquareView(Position position) {
        this.position = position;
        this.background = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);

        boolean isLight = (position.getRow() + position.getCol()) % 2 == 0;
        background.setFill(isLight ? LIGHT_COLOR : DARK_COLOR);

        getChildren().add(background);
        setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
        setAlignment(Pos.CENTER);
        setOnMouseClicked(this::handleClick);
    }

    /**
     * このマスの位置を返す。
     *
     * @return {@link Position}
     */
    public Position getPosition() { return position; }

    /**
     * このマスに駒と駒画像を設定する。既存の画像があれば置き換える。
     *
     * @param piece     設定する駒
     * @param imageView 駒の画像ビュー
     */
    public void setPiece(Piece piece, ImageView imageView) {
        this.piece = piece;
        if (pieceImageView != null) getChildren().remove(pieceImageView);
        if (imageView != null) {
            this.pieceImageView = imageView;
            pieceImageView.setFitWidth(SQUARE_SIZE - 4);
            pieceImageView.setFitHeight(SQUARE_SIZE - 4);
            getChildren().add(pieceImageView);
        }
    }

    /**
     * このマスから駒と駒画像を取り除く。
     */
    public void removePiece() {
        this.piece = null;
        if (pieceImageView != null) {
            getChildren().remove(pieceImageView);
            pieceImageView = null;
        }
    }

    public Piece getPiece() { return piece; }
    public boolean hasPiece() { return piece != null; }

    /**
     * このマスをハイライト表示する。
     *
     * @param type ハイライトの種類（選択中・移動可能）
     */
    public void highlight(HighlightType type) {
        if (highlightCircle != null) getChildren().remove(highlightCircle);
        highlightCircle = new Circle(SQUARE_SIZE / 4.0);
        highlightCircle.setFill(type == HighlightType.SELECTED ? SELECTED_COLOR : HIGHLIGHT_COLOR);
        highlightCircle.setOpacity(0.7);
        getChildren().add(highlightCircle);
    }

    /**
     * ハイライトを消去する。
     */
    public void clearHighlight() {
        if (highlightCircle != null) {
            getChildren().remove(highlightCircle);
            highlightCircle = null;
        }
    }

    /**
     * マスがクリックされたときに呼ばれるハンドラを設定する。
     *
     * @param handler クリック時に実行する処理
     */
    public void setOnClickHandler(Runnable handler) { this.onClickHandler = handler; }

    private void handleClick(MouseEvent event) {
        if (onClickHandler != null) onClickHandler.run();
    }

    /**
     * ハイライトの種類を表す列挙型。
     */
    public enum HighlightType {
        /** 選択中のマス。 */
        SELECTED,
        /** 移動可能なマス。 */
        AVAILABLE
    }
}
