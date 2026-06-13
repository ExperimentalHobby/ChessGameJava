package com.chessgame.swing;

import com.chessgame.game.ChessGame;
import com.chessgame.model.Color;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Swing 版チェス盤パネル。8×8 のマスを直接 {@link Graphics2D} で描画する。
 * クリックによる駒の選択・移動・ハイライト表示を管理する。
 * ポーン昇格時は {@link javax.swing.JOptionPane} で駒種を選択させる。
 */
public class SwingChessBoardPanel extends JPanel {
    private static final int SQUARE_SIZE = 70;
    private static final int BOARD_SIZE = 8;
    private static final java.awt.Color LIGHT_COLOR    = new java.awt.Color(240, 217, 181);
    private static final java.awt.Color DARK_COLOR     = new java.awt.Color(181, 136, 99);
    private static final java.awt.Color SELECTED_COLOR = new java.awt.Color(106, 168, 79, 200);
    private static final java.awt.Color HIGHLIGHT_COLOR= new java.awt.Color(255, 215, 0, 160);
    private static final java.awt.Color LABEL_LIGHT    = new java.awt.Color(181, 136, 99);
    private static final java.awt.Color LABEL_DARK     = new java.awt.Color(240, 217, 181);

    private ChessGame game;
    private Position selectedSquare;
    private List<Position> highlightedSquares = new ArrayList<>();

    /**
     * 指定したゲームに紐づいた盤面パネルを生成する。
     *
     * @param game 表示対象の {@link ChessGame}
     */
    public SwingChessBoardPanel(ChessGame game) {
        this.game = game;
        setPreferredSize(new Dimension(BOARD_SIZE * SQUARE_SIZE, BOARD_SIZE * SQUARE_SIZE));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleSquareClick(e.getX(), e.getY());
            }
        });
    }

    /**
     * 表示対象のゲームを切り替える。選択状態をリセットする。
     *
     * @param game 新しいゲーム
     */
    public void setGame(ChessGame game) {
        this.game = game;
        clearSelection();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (game == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                drawSquare(g2d, row, col);
            }
        }
    }

    /**
     * 指定した行・列のマスを描画する。背景色・ハイライト・座標ラベル・駒画像を含む。
     *
     * @param g   描画コンテキスト
     * @param row 行番号
     * @param col 列番号
     */
    private void drawSquare(Graphics2D g, int row, int col) {
        int x = col * SQUARE_SIZE;
        int y = row * SQUARE_SIZE;
        boolean isLight = (row + col) % 2 == 0;

        Position pos = Position.of(row, col);

        // Background
        if (selectedSquare != null && selectedSquare.equals(pos)) {
            g.setColor(SELECTED_COLOR);
        } else if (highlightedSquares.contains(pos)) {
            g.setColor(isLight ? LIGHT_COLOR : DARK_COLOR);
            g.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
            // Overlay highlight dot
            g.setColor(HIGHLIGHT_COLOR);
            int dotSize = SQUARE_SIZE / 3;
            g.fillOval(x + (SQUARE_SIZE - dotSize) / 2, y + (SQUARE_SIZE - dotSize) / 2, dotSize, dotSize);
        } else {
            g.setColor(isLight ? LIGHT_COLOR : DARK_COLOR);
        }
        if (!highlightedSquares.contains(pos) || (selectedSquare != null && selectedSquare.equals(pos))) {
            g.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
        }

        // Coordinate labels
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        java.awt.Color labelColor = isLight ? LABEL_LIGHT : LABEL_DARK;
        g.setColor(labelColor);
        if (col == 0) {
            g.drawString(String.valueOf(8 - row), x + 3, y + 14);
        }
        if (row == 7) {
            g.drawString(String.valueOf((char) ('a' + col)), x + SQUARE_SIZE - 13, y + SQUARE_SIZE - 3);
        }

        // Piece
        Piece piece = game.getBoard().getPieceAt(pos);
        if (piece != null) {
            java.awt.Image img = PieceImageGenerator.getPieceImage(piece.getColor(), piece.getType());
            int margin = (SQUARE_SIZE - PieceImageGenerator.IMAGE_SIZE) / 2;
            g.drawImage(img, x + margin, y + margin, PieceImageGenerator.IMAGE_SIZE, PieceImageGenerator.IMAGE_SIZE, null);
        }
    }

    /**
     * マウスクリック座標からマスを特定し、駒の選択または移動を処理する。
     *
     * @param x クリックの X 座標（ピクセル）
     * @param y クリックの Y 座標（ピクセル）
     */
    private void handleSquareClick(int x, int y) {
        if (game == null || game.isGameOver()) return;

        int col = x / SQUARE_SIZE;
        int row = y / SQUARE_SIZE;
        if (col < 0 || col >= BOARD_SIZE || row < 0 || row >= BOARD_SIZE) return;

        Position clickedPos = Position.of(row, col);

        if (selectedSquare == null) {
            // 【未選択状態】 自駒をクリック → 選択してハイライト表示
            Piece piece = game.getBoard().getPieceAt(clickedPos);
            if (piece != null && piece.getColor() == game.getCurrentPlayer().getColor()) {
                selectedSquare = clickedPos;
                highlightedSquares = game.getAvailableMoves(clickedPos)
                    .stream()
                    .map(Move::getTo)
                    .collect(Collectors.toList());
                repaint();
            }
        } else {
            // 【選択済み状態】 同じマスを再クリック → 選択解除
            if (clickedPos.equals(selectedSquare)) {
                clearSelection();
                repaint();
                return;
            }

            // 別の自駒をクリック → 選択先を切り替え
            Piece clickedPiece = game.getBoard().getPieceAt(clickedPos);
            if (clickedPiece != null && clickedPiece.getColor() == game.getCurrentPlayer().getColor()) {
                selectedSquare = clickedPos;
                highlightedSquares = game.getAvailableMoves(clickedPos)
                    .stream()
                    .map(Move::getTo)
                    .collect(Collectors.toList());
                repaint();
                return;
            }

            // 上記以外（空マスまたは相手駒）→ 移動を試みる
            Piece movingPiece = game.getBoard().getPieceAt(selectedSquare);
            if (movingPiece != null && isPromotionMove(movingPiece, clickedPos)) {
                PieceType choice = showPromotionDialog(movingPiece.getColor());
                game.makeMove(selectedSquare, clickedPos, choice);
            } else {
                game.makeMove(selectedSquare, clickedPos);
            }

            clearSelection();
            repaint();
        }
    }

    /**
     * 指定した駒の指定マスへの移動がポーン昇格かどうかを返す。
     *
     * @param piece 動かす駒
     * @param to    移動先の位置
     * @return 昇格を伴う合法手であれば true
     */
    private boolean isPromotionMove(Piece piece, Position to) {
        if (piece.getType() != PieceType.PAWN) return false;
        // Verify the destination is actually a legal move
        boolean isLegal = game.getAvailableMoves(piece.getPosition())
            .stream().anyMatch(m -> m.getTo().equals(to) && m.isPromotion());
        return isLegal;
    }

    /**
     * ポーン昇格の駒種を選択するダイアログを表示し、選択結果を返す。
     *
     * @param color 昇格するポーンの色（表示用）
     * @return 選択された駒種（キャンセルまたは未選択時はクイーン）
     */
    private PieceType showPromotionDialog(Color color) {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(
            SwingUtilities.getWindowAncestor(this),
            "成駒を選択してください：",
            "ポーンの昇格",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        switch (choice) {
            case 1: return PieceType.ROOK;
            case 2: return PieceType.BISHOP;
            case 3: return PieceType.KNIGHT;
            default: return PieceType.QUEEN;
        }
    }

    /**
     * 選択中のマスとハイライトをリセットする。
     */
    private void clearSelection() {
        selectedSquare = null;
        highlightedSquares.clear();
    }

    /**
     * 選択状態をリセットして盤面を再描画する。移動確定後やundo後に呼ぶ。
     */
    public void updateBoard() {
        clearSelection();
        repaint();
    }
}
