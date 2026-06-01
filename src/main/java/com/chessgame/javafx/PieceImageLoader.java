package com.chessgame.javafx;

import com.chessgame.model.Color;
import com.chessgame.model.piece.Piece;
import com.chessgame.model.piece.PieceType;
import javafx.scene.image.ImageView;

/**
 * JavaFX 版駒画像のローダー。{@link PieceRenderer} が生成した画像を {@link ImageView} にラップして返す。
 * コンストラクタで全12種類の駒画像をプリレンダリングする。
 */
public class PieceImageLoader {

    /**
     * 全12種類の駒画像（白黒×6種）をプリレンダリングしてキャッシュする。
     */
    public PieceImageLoader() {
        // Pre-render all 12 piece images on the FX thread
        for (Color color : Color.values()) {
            for (PieceType type : PieceType.values()) {
                PieceRenderer.render(color, type);
            }
        }
    }

    /**
     * 指定した駒に対応する {@link ImageView} を返す。
     *
     * @param piece 表示する駒（null の場合は null を返す）
     * @return 駒画像を持つ {@link ImageView}、または null
     */
    public ImageView getPieceImageView(Piece piece) {
        if (piece == null) return null;
        ImageView view = new ImageView(PieceRenderer.render(piece.getColor(), piece.getType()));
        view.setPreserveRatio(true);
        view.setFitWidth(PieceRenderer.SIZE);
        view.setFitHeight(PieceRenderer.SIZE);
        return view;
    }
}
