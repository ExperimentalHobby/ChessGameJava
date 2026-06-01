package com.chessgame.javafx;

import com.chessgame.model.Color;
import com.chessgame.model.piece.PieceType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates chess piece images programmatically using JavaFX Canvas.
 * No image files required — pieces are rendered from Unicode glyphs or
 * geometric fallback shapes.
 */
public class PieceRenderer {

    static final int SIZE = 56;

    private static final Map<String, Image> cache = new HashMap<>();

    // ── public API ────────────────────────────────────────────────────────────

    /**
     * 指定した色と種類の駒画像を返す。キャッシュがあれば再利用する。
     *
     * @param color 駒の色
     * @param type  駒の種類
     * @return 駒を描画した {@link Image}
     */
    public static Image render(Color color, PieceType type) {
        return cache.computeIfAbsent(color + "_" + type, k -> generate(color, type));
    }

    // ── image generation ──────────────────────────────────────────────────────

    /**
     * 駒画像を新規生成する。Unicode グリフ描画を試み、失敗時は幾何学図形で代替する。
     *
     * @param color 駒の色
     * @param type  駒の種類
     * @return 生成した {@link Image}
     */
    private static Image generate(Color color, PieceType type) {
        Canvas canvas = new Canvas(SIZE, SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, SIZE, SIZE);

        String ch = pieceChar(color, type);
        Font font = findFont(ch);

        if (font != null) {
            drawUnicode(gc, color, ch, font);
        } else {
            drawFallback(gc, color, type);
        }

        WritableImage img = new WritableImage(SIZE, SIZE);
        canvas.snapshot(null, img);
        return img;
    }

    // ── Unicode rendering ─────────────────────────────────────────────────────

    /**
     * Unicode チェス駒文字をフォントで描画する。影・塗り・輪郭を順に描く。
     *
     * @param gc    JavaFX グラフィクスコンテキスト
     * @param color 駒の色
     * @param ch    描画する Unicode 文字列
     * @param font  使用するフォント
     */
    private static void drawUnicode(GraphicsContext gc, Color color, String ch, Font font) {
        // Measure text to center it
        Text ruler = new Text(ch);
        ruler.setFont(font);
        var tb = ruler.getBoundsInLocal();

        // In fillText/strokeText: x = left edge, y = baseline
        // To center vertically: baseline_y = SIZE/2 - (minY + maxY)/2
        double x = (SIZE - tb.getWidth()) / 2.0 - tb.getMinX();
        double y = SIZE / 2.0 - (tb.getMinY() + tb.getMaxY()) / 2.0;

        // Drop shadow
        gc.setFont(font);
        gc.setFill(javafx.scene.paint.Color.color(0, 0, 0, 0.22));
        gc.fillText(ch, x + 2, y + 2.5);

        // Fill
        gc.setFill(color == Color.WHITE
            ? javafx.scene.paint.Color.rgb(255, 251, 230)
            : javafx.scene.paint.Color.rgb(28, 16, 6));
        gc.fillText(ch, x, y);

        // Outline
        gc.setStroke(color == Color.WHITE
            ? javafx.scene.paint.Color.rgb(45, 28, 8)
            : javafx.scene.paint.Color.rgb(215, 190, 155));
        gc.setLineWidth(1.3);
        gc.strokeText(ch, x, y);
    }

    // ── Unicode piece characters ──────────────────────────────────────────────

    /**
     * 駒の色と種類に対応する Unicode チェス駒文字列を返す。
     *
     * @param color 駒の色
     * @param type  駒の種類
     * @return Unicode チェス駒文字（例: "♔"、"♚"）
     */
    private static String pieceChar(Color color, PieceType type) {
        if (color == Color.WHITE) {
            switch (type) {
                case KING:   return "♔";
                case QUEEN:  return "♕";
                case ROOK:   return "♖";
                case BISHOP: return "♗";
                case KNIGHT: return "♘";
                default:     return "♙";
            }
        } else {
            switch (type) {
                case KING:   return "♚";
                case QUEEN:  return "♛";
                case ROOK:   return "♜";
                case BISHOP: return "♝";
                case KNIGHT: return "♞";
                default:     return "♟";
            }
        }
    }

    // ── Font selection ────────────────────────────────────────────────────────

    /**
     * 指定した文字を表示できるフォントを候補リストから探して返す。見つからなければ null。
     *
     * @param testChar フォントが対応しているか確認する文字列
     * @return 使用可能なフォント、または null
     */
    private static Font findFont(String testChar) {
        String[] candidates = {
            "Segoe UI Symbol",   // Windows 10/11
            "Arial Unicode MS",  // older Windows / macOS
            "Symbola",
            "FreeSerif",
            "DejaVu Serif",
            "Noto Sans Symbols2",
        };
        for (String name : candidates) {
            Font f = Font.font(name, SIZE * 0.85);
            Text t = new Text(testChar);
            t.setFont(f);
            // If bounds are non-zero, the font can display the char
            if (t.getBoundsInLocal().getWidth() > 2) return f;
        }
        return null;
    }

    // ── Geometric fallback ────────────────────────────────────────────────────

    /**
     * Unicode フォントが使えない場合の代替として、幾何学図形で駒を描画する。
     *
     * @param gc    JavaFX グラフィクスコンテキスト
     * @param color 駒の色
     * @param type  駒の種類
     */
    private static void drawFallback(GraphicsContext gc, Color color, PieceType type) {
        javafx.scene.paint.Color fill = color == Color.WHITE
            ? javafx.scene.paint.Color.rgb(255, 251, 230)
            : javafx.scene.paint.Color.rgb(28, 16, 6);
        javafx.scene.paint.Color stroke = color == Color.WHITE
            ? javafx.scene.paint.Color.rgb(45, 28, 8)
            : javafx.scene.paint.Color.rgb(215, 190, 155);

        gc.setFill(javafx.scene.paint.Color.color(0, 0, 0, 0.18));
        gc.fillOval(9, 11, SIZE - 14, SIZE - 14);

        gc.setLineWidth(2.0);
        switch (type) {
            case KING:   drawKing(gc, fill, stroke);   break;
            case QUEEN:  drawQueen(gc, fill, stroke);  break;
            case ROOK:   drawRook(gc, fill, stroke);   break;
            case BISHOP: drawBishop(gc, fill, stroke); break;
            case KNIGHT: drawKnight(gc, fill, stroke); break;
            default:     drawPawn(gc, fill, stroke);   break;
        }
    }

    private static final int CX = SIZE / 2;

    /** キングを幾何学図形で描画する。 */
    private static void drawKing(GraphicsContext gc, javafx.scene.paint.Color fill, javafx.scene.paint.Color stroke) {
        gc.setFill(fill);
        gc.fillRoundRect(CX - 13, 29, 26, 22, 8, 8);
        gc.fillOval(CX - 10, 48, 20, 9);
        gc.fillRect(CX - 2, 10, 5, 20);
        gc.fillRect(CX - 10, 16, 20, 5);
        gc.setStroke(stroke);
        gc.strokeRoundRect(CX - 13, 29, 26, 22, 8, 8);
        gc.strokeOval(CX - 10, 48, 20, 9);
        gc.strokeRect(CX - 2, 10, 5, 20);
        gc.strokeRect(CX - 10, 16, 20, 5);
    }

    /** クイーンを幾何学図形で描画する。 */
    private static void drawQueen(GraphicsContext gc, javafx.scene.paint.Color fill, javafx.scene.paint.Color stroke) {
        double[] xs = {CX - 20, CX - 15, CX - 7, CX, CX + 7, CX + 15, CX + 20, CX + 11, CX, CX - 11};
        double[] ys = {18, 40, 28, 40, 28, 40, 18, 49, 49, 49};
        gc.setFill(fill);
        gc.fillOval(CX - 20, 9, 9, 9); gc.fillOval(CX - 5, 6, 10, 10); gc.fillOval(CX + 11, 9, 9, 9);
        gc.fillPolygon(xs, ys, 10);
        gc.fillOval(CX - 11, 46, 22, 10);
        gc.setStroke(stroke);
        gc.strokeOval(CX - 20, 9, 9, 9); gc.strokeOval(CX - 5, 6, 10, 10); gc.strokeOval(CX + 11, 9, 9, 9);
        gc.strokePolygon(xs, ys, 10);
        gc.strokeOval(CX - 11, 46, 22, 10);
    }

    /** ルークを幾何学図形で描画する。 */
    private static void drawRook(GraphicsContext gc, javafx.scene.paint.Color fill, javafx.scene.paint.Color stroke) {
        gc.setFill(fill);
        gc.fillRect(CX - 15, 10, 9, 9); gc.fillRect(CX - 4, 10, 9, 9); gc.fillRect(CX + 7, 10, 9, 9);
        gc.fillRect(CX - 15, 17, 30, 31);
        gc.fillOval(CX - 13, 45, 26, 10);
        gc.setStroke(stroke);
        gc.strokeRect(CX - 15, 10, 9, 9); gc.strokeRect(CX - 4, 10, 9, 9); gc.strokeRect(CX + 7, 10, 9, 9);
        gc.strokeRect(CX - 15, 17, 30, 31);
        gc.strokeOval(CX - 13, 45, 26, 10);
    }

    /** ビショップを幾何学図形で描画する。 */
    private static void drawBishop(GraphicsContext gc, javafx.scene.paint.Color fill, javafx.scene.paint.Color stroke) {
        gc.setFill(fill);
        gc.fillOval(CX - 5, 7, 10, 10);
        gc.fillOval(CX - 10, 15, 20, 13);
        gc.fillPolygon(new double[]{CX - 13, CX, CX + 13}, new double[]{25, 49, 25}, 3);
        gc.fillOval(CX - 13, 45, 26, 10);
        gc.setStroke(stroke);
        gc.strokeOval(CX - 5, 7, 10, 10);
        gc.strokeOval(CX - 10, 15, 20, 13);
        gc.strokePolygon(new double[]{CX - 13, CX, CX + 13}, new double[]{25, 49, 25}, 3);
        gc.strokeOval(CX - 13, 45, 26, 10);
    }

    /** ナイトを幾何学図形で描画する。 */
    private static void drawKnight(GraphicsContext gc, javafx.scene.paint.Color fill, javafx.scene.paint.Color stroke) {
        double[] xs = {CX - 11, CX - 15, CX - 11, CX - 5, CX + 13, CX + 15, CX + 11, CX + 7};
        double[] ys = {49, 28, 13, 7, 15, 28, 49, 49};
        gc.setFill(fill);
        gc.fillPolygon(xs, ys, 8);
        gc.fillOval(CX - 13, 45, 26, 10);
        gc.setStroke(stroke);
        gc.strokePolygon(xs, ys, 8);
        gc.strokeOval(CX - 13, 45, 26, 10);
        gc.setFill(fill); gc.fillOval(CX + 3, 17, 5, 5);
        gc.setStroke(stroke); gc.strokeOval(CX + 3, 17, 5, 5);
    }

    /** ポーンを幾何学図形で描画する。 */
    private static void drawPawn(GraphicsContext gc, javafx.scene.paint.Color fill, javafx.scene.paint.Color stroke) {
        gc.setFill(fill);
        gc.fillOval(CX - 9, 10, 18, 18);
        gc.fillPolygon(new double[]{CX - 11, CX, CX + 11}, new double[]{26, 43, 26}, 3);
        gc.fillOval(CX - 13, 39, 26, 14);
        gc.setStroke(stroke);
        gc.strokeOval(CX - 9, 10, 18, 18);
        gc.strokePolygon(new double[]{CX - 11, CX, CX + 11}, new double[]{26, 43, 26}, 3);
        gc.strokeOval(CX - 13, 39, 26, 14);
    }
}
