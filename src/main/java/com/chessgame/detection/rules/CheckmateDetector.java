package com.chessgame.detection.rules;

import com.chessgame.model.Color;
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.rules.MoveValidator;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.rules.CheckDetector;
import java.util.List;

/**
 * チェックメイトとステールメイトを判別するクラス。
 * 合法手が1手も存在しないかどうかを {@link MoveValidator} と {@link CheckDetector} を使って判定する。
 */
public class CheckmateDetector {
    private final MoveValidator moveValidator;
    private final CheckDetector checkDetector;

    public CheckmateDetector() {
        this.moveValidator = new MoveValidator();
        this.checkDetector = new CheckDetector();
    }

    /**
     * 指定した色がチェックメイトかどうかを返す（アンパッサンなし）。
     *
     * @param color 調べるプレイヤーの色
     * @param board 現在の盤面
     * @return チェックメイトであれば true
     */
    public boolean isCheckmate(Color color, Board board) {
        return isCheckmate(color, board, null);
    }

    /**
     * 指定した色がチェックメイトかどうかを返す。
     *
     * @param color            調べるプレイヤーの色
     * @param board            現在の盤面
     * @param enPassantTarget  アンパッサン対象位置（なければ null）
     * @return チェックメイトであれば true
     */
    public boolean isCheckmate(Color color, Board board, Position enPassantTarget) {
        if (!checkDetector.isInCheck(color, board)) {
            return false;
        }
        return !hasLegalMoves(color, board, enPassantTarget);
    }

    /**
     * 指定した色がステールメイトかどうかを返す（アンパッサンなし）。
     *
     * @param color 調べるプレイヤーの色
     * @param board 現在の盤面
     * @return ステールメイトであれば true
     */
    public boolean isStalemate(Color color, Board board) {
        return isStalemate(color, board, null);
    }

    /**
     * 指定した色がステールメイトかどうかを返す。
     *
     * @param color           調べるプレイヤーの色
     * @param board           現在の盤面
     * @param enPassantTarget アンパッサン対象位置（なければ null）
     * @return ステールメイトであれば true
     */
    public boolean isStalemate(Color color, Board board, Position enPassantTarget) {
        if (checkDetector.isInCheck(color, board)) {
            return false;
        }
        return !hasLegalMoves(color, board, enPassantTarget);
    }

    /**
     * 指定した色に合法手が1手以上あるかどうかを返す（アンパッサンなし）。
     *
     * @param color 調べるプレイヤーの色
     * @param board 現在の盤面
     * @return 合法手があれば true
     */
    public boolean hasLegalMoves(Color color, Board board) {
        return hasLegalMoves(color, board, null);
    }

    /**
     * 指定した色に合法手が1手以上あるかどうかを返す。
     *
     * @param color           調べるプレイヤーの色
     * @param board           現在の盤面
     * @param enPassantTarget アンパッサン対象位置（なければ null）
     * @return 合法手があれば true
     */
    public boolean hasLegalMoves(Color color, Board board, Position enPassantTarget) {
        List<Piece> pieces = board.getAllPieces(color);

        for (Piece piece : pieces) {
            List<Move> possibleMoves = moveValidator.getValidMoves(piece, board, enPassantTarget);

            for (Move move : possibleMoves) {
                if (isLegalMove(move, piece, board, color)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 指定した手を実行後に自陣のキングが王手になっていないかを確認する。
     *
     * @param move        確認する手
     * @param piece       動かす駒
     * @param board       現在の盤面
     * @param playerColor 動かすプレイヤーの色
     * @return 合法手であれば true
     */
    public boolean isLegalMove(Move move, Piece piece, Board board, Color playerColor) {
        Board testBoard = board.clone();
        simulateMove(move, piece, testBoard);
        return !checkDetector.isInCheck(playerColor, testBoard);
    }

    /**
     * 指定した手をコピーした盤面上で仮実行する。チェック判定のための内部処理。
     *
     * @param move  仮実行する手
     * @param piece 動かす駒
     * @param board 仮実行対象の盤面（破壊的に変更される）
     */
    private void simulateMove(Move move, Piece piece, Board board) {
        Position from = move.getFrom();
        Position to = move.getTo();

        if (move.isCapture()) {
            board.removePiece(to);
        }

        board.removePiece(from);
        board.placePiece(piece.clone(), to);

        if (move.isCastling()) {
            simulateCastling(move, board);
        } else if (move.isEnPassant()) {
            simulateEnPassant(move, board);
        }
    }

    /**
     * キャスリングのルーク移動を仮実行する。キングサイド・クイーンサイドを自動判定する。
     *
     * @param move  キャスリングの手
     * @param board 仮実行対象の盤面
     */
    private void simulateCastling(Move move, Board board) {
        Position kingFrom = move.getFrom();
        Position kingTo = move.getTo();

        int col = kingTo.getCol();
        // キングの移動先列が元の列より大きければキングサイド（右方向）
        if (col > kingFrom.getCol()) {
            // Kingside: ルーク h ファイル(col=7) → f ファイル(col=5)
            Position rookFrom = Position.of(kingFrom.getRow(), 7);
            Position rookTo = Position.of(kingFrom.getRow(), 5);
            Piece rook = board.getPieceAt(rookFrom);
            if (rook != null) {
                board.removePiece(rookFrom);
                board.placePiece(rook.clone(), rookTo);
            }
        } else {
            // Queenside: ルーク a ファイル(col=0) → d ファイル(col=3)
            Position rookFrom = Position.of(kingFrom.getRow(), 0);
            Position rookTo = Position.of(kingFrom.getRow(), 3);
            Piece rook = board.getPieceAt(rookFrom);
            if (rook != null) {
                board.removePiece(rookFrom);
                board.placePiece(rook.clone(), rookTo);
            }
        }
    }

    /**
     * アンパッサンで取られるポーンを仮実行で盤面から取り除く。
     *
     * @param move  アンパッサンの手
     * @param board 仮実行対象の盤面
     */
    private void simulateEnPassant(Move move, Board board) {
        Position capturePosition = Position.of(move.getFrom().getRow(), move.getTo().getCol());
        board.removePiece(capturePosition);
    }
}
