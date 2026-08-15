/*
 * MIT License
 *
 * Copyright (c) 2026 ChessGame Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.chessgame.swing.board;

import com.chessgame.game.core.ChessGame;
import com.chessgame.model.Color;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;
import com.chessgame.swing.asset.PieceImageGenerator;
import com.chessgame.ui.shared.board.BoardSelectionController;
import com.chessgame.ui.shared.board.ClickOutcome;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Swing 版チェス盤パネル。8×8 のマスを直接 {@link Graphics2D} で描画する。
 * クリックによる駒の選択・移動・ハイライト表示を管理する。
 * ポーン昇格時は {@link javax.swing.JOptionPane} で駒種を選択させる。
 * マスサイズはパネルの実サイズから動的に計算するため、ウィンドウ内に余白が生じない。
 */
public class SwingChessBoardPanel extends JPanel {
    private static final int DEFAULT_SQUARE_SIZE = 70;
    private static final int BOARD_SIZE = 8;
    private static final java.awt.Color LIGHT_COLOR    = new java.awt.Color(240, 217, 181);
    private static final java.awt.Color DARK_COLOR     = new java.awt.Color(181, 136, 99);
    private static final java.awt.Color SELECTED_COLOR = new java.awt.Color(106, 168, 79, 200);
    private static final java.awt.Color HIGHLIGHT_COLOR = new java.awt.Color(255, 215, 0, 160);
    private static final java.awt.Color LAST_MOVE_COLOR = new java.awt.Color(100, 150, 220, 90);
    private static final java.awt.Color LABEL_LIGHT    = new java.awt.Color(181, 136, 99);
    private static final java.awt.Color LABEL_DARK     = new java.awt.Color(240, 217, 181);

    private ChessGame game;
    private final BoardSelectionController controller;
    private Position selectedSquare;
    private List<Position> highlightedSquares = new ArrayList<>();
    private Move lastMove;

    /**
     * 指定したゲームに紐づいた盤面パネルを生成する。
     *
     * @param game 表示対象の {@link ChessGame}
     */
    // テスト用サブクラス（showPromotionDialog() のみをオーバーライド）が存在し final にできないが、
    // そのサブクラスは構築中に呼ばれるメソッドを一切オーバーライドしないため this-escape の実害はない
    @SuppressWarnings("this-escape")
    public SwingChessBoardPanel(ChessGame game) {
        this.game = game;
        this.controller = new BoardSelectionController(game, this::showPromotionDialog);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleSquareClick(e.getX(), e.getY());
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(BOARD_SIZE * DEFAULT_SQUARE_SIZE, BOARD_SIZE * DEFAULT_SQUARE_SIZE);
    }

    /**
     * パネルの実サイズから正方形を維持したまま最大のマスサイズを返す。
     * pack() 前などパネルサイズが 0 の場合はデフォルト値を返す。
     */
    int squareSize() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return DEFAULT_SQUARE_SIZE;
        return Math.min(w, h) / BOARD_SIZE;
    }

    /**
     * 表示対象のゲームを切り替える。選択状態をリセットする。
     *
     * @param game 新しいゲーム
     */
    public void setGame(ChessGame game) {
        this.game = game;
        controller.setGame(game);
        clearSelection();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (game == null) return;

        int sq = squareSize();
        // パネル左上から描画する（CENTER が正方形なら余白ゼロ、非正方形でも右・下に余白が出るだけ）
        int offsetX = 0;
        int offsetY = 0;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                drawSquare(g2d, row, col, sq, offsetX, offsetY);
            }
        }
    }

    /**
     * 指定した行・列のマスを描画する。背景色・ハイライト・座標ラベル・駒画像を含む。
     *
     * @param g       描画コンテキスト
     * @param row     行番号
     * @param col     列番号
     * @param sq      マスのピクセルサイズ
     * @param offsetX 盤面全体の X オフセット（パネル内センタリング用）
     * @param offsetY 盤面全体の Y オフセット（パネル内センタリング用）
     */
    private void drawSquare(Graphics2D g, int row, int col, int sq, int offsetX, int offsetY) {
        int x = offsetX + col * sq;
        int y = offsetY + row * sq;
        boolean isLight = (row + col) % 2 == 0;

        Position pos = Position.of(row, col);

        // Background
        if (selectedSquare != null && selectedSquare.equals(pos)) {
            g.setColor(SELECTED_COLOR);
        } else if (highlightedSquares.contains(pos)) {
            g.setColor(isLight ? LIGHT_COLOR : DARK_COLOR);
            g.fillRect(x, y, sq, sq);
            // Overlay highlight dot
            g.setColor(HIGHLIGHT_COLOR);
            int dotSize = sq / 3;
            g.fillOval(x + (sq - dotSize) / 2, y + (sq - dotSize) / 2, dotSize, dotSize);
        } else {
            g.setColor(isLight ? LIGHT_COLOR : DARK_COLOR);
        }
        if (!highlightedSquares.contains(pos) || (selectedSquare != null && selectedSquare.equals(pos))) {
            g.fillRect(x, y, sq, sq);
        }

        // 直前の手のマスを半透明で重ね塗りする（選択中のマス自身には重ねない）
        boolean isLastMoveSquare = lastMove != null
            && (pos.equals(lastMove.getFrom()) || pos.equals(lastMove.getTo()));
        if (isLastMoveSquare && !(selectedSquare != null && selectedSquare.equals(pos))) {
            g.setColor(LAST_MOVE_COLOR);
            g.fillRect(x, y, sq, sq);
        }

        // Coordinate labels
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        java.awt.Color labelColor = isLight ? LABEL_LIGHT : LABEL_DARK;
        g.setColor(labelColor);
        if (col == 0) {
            g.drawString(String.valueOf(8 - row), x + 3, y + 14);
        }
        if (row == 7) {
            g.drawString(String.valueOf((char) ('a' + col)), x + sq - 13, y + sq - 3);
        }

        // Piece — マスサイズに比例してスケーリング
        Piece piece = game.getBoard().getPieceAt(pos);
        if (piece != null) {
            Image img = PieceImageGenerator.getPieceImage(piece.getColor(), piece.getType());
            int pieceSize = (int) (sq * 0.85);
            int margin = (sq - pieceSize) / 2;
            g.drawImage(img, x + margin, y + margin, pieceSize, pieceSize, null);
        }
    }

    /**
     * マウスクリック座標からマスを特定し、駒の選択または移動を処理する。
     * 選択・移動・昇格判定は {@link BoardSelectionController} に委譲し、
     * ここでは結果に応じた描画状態の更新のみを行う。
     *
     * @param x クリックの X 座標（ピクセル）
     * @param y クリックの Y 座標（ピクセル）
     */
    void handleSquareClick(int x, int y) {
        if (game == null) return;

        int sq = squareSize();
        int offsetX = 0;
        int offsetY = 0;

        int col = (x - offsetX) / sq;
        int row = (y - offsetY) / sq;
        if (col < 0 || col >= BOARD_SIZE || row < 0 || row >= BOARD_SIZE) return;

        Position clickedPos = Position.of(row, col);
        ClickOutcome outcome = controller.handleClick(clickedPos);

        switch (outcome.getType()) {
            case SELECTED:
                selectedSquare = outcome.getPosition();
                highlightedSquares = outcome.getHighlightTargets();
                repaint();
                break;
            case DESELECTED:
                clearSelection();
                repaint();
                break;
            case MOVE_ATTEMPTED:
                clearSelection();
                repaint();
                break;
            case NONE:
            default:
                break;
        }
    }

    /**
     * ポーン昇格の駒種を選択するダイアログを表示し、選択結果を返す。
     *
     * @param color 昇格するポーンの色（表示用）
     * @return 選択された駒種（キャンセルまたは未選択時はクイーン）
     */
    protected PieceType showPromotionDialog(Color color) {
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
     * 直前の手のハイライトも、その時点の履歴に合わせて更新する
     * （着手後は今の手、undo後は新しい最終手、New Game後は履歴が空になるため消える）。
     */
    public void updateBoard() {
        controller.clearSelection();
        clearSelection();
        lastMove = game.getMoveHistory().getLastMove();
        repaint();
    }
}
