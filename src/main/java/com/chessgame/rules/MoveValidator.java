package com.chessgame.rules;

import com.chessgame.model.Color;
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.*;
import com.chessgame.piece.rules.CheckDetector;
import java.util.ArrayList;
import java.util.List;

/**
 * 駒種ごとの擬似合法手（王手放置チェックなし）を生成するクラス。
 * 合法性の最終確認（自陣への王手）は {@link CheckmateDetector#isLegalMove} が行う。
 */
public class MoveValidator {
    private final CheckDetector checkDetector;

    /**
     * {@link CheckDetector} を生成してバリデーターを初期化する。
     */
    public MoveValidator() {
        this.checkDetector = new CheckDetector();
    }

    /**
     * 指定した駒の擬似合法手を返す（アンパッサンなし）。
     *
     * @param piece 調べる駒
     * @param board 現在の盤面
     * @return 擬似合法手のリスト
     */
    public List<Move> getValidMoves(Piece piece, Board board) {
        return getValidMoves(piece, board, null);
    }

    /**
     * 指定した駒の擬似合法手を返す。
     *
     * @param piece           調べる駒
     * @param board           現在の盤面
     * @param enPassantTarget アンパッサン対象位置（なければ null）
     * @return 擬似合法手のリスト
     */
    public List<Move> getValidMoves(Piece piece, Board board, Position enPassantTarget) {
        List<Move> validMoves = new ArrayList<>();
        if (piece == null) return validMoves;

        switch (piece.getType()) {
            case PAWN:
                addPawnMoves(piece, board, validMoves, enPassantTarget);
                break;
            case KNIGHT:
                addKnightMoves(piece, board, validMoves);
                break;
            case BISHOP:
            case ROOK:
            case QUEEN:
                addSlidingPieceMoves(piece, board, validMoves);
                break;
            case KING:
                addKingMoves(piece, board, validMoves);
                addCastlingMoves(piece, board, validMoves);
                break;
        }

        return validMoves;
    }

    /**
     * ポーンの擬似合法手（前進・2マス移動・斜め捕獲・アンパッサン・昇格）を生成してリストに追加する。
     *
     * @param piece           ポーン
     * @param board           現在の盤面
     * @param moves           追加先のリスト
     * @param enPassantTarget アンパッサン対象位置（なければ null）
     */
    private void addPawnMoves(Piece piece, Board board, List<Move> moves, Position enPassantTarget) {
        // 行番号は上が 0 のため、白（上方向）は -1、黒（下方向）は +1
        int direction = piece.getColor() == Color.WHITE ? -1 : 1;
        // 白のポーンは row=6（ランク2）、黒は row=1（ランク7）がスタート行
        int startRow = piece.getColor() == Color.WHITE ? 6 : 1;
        // 白は row=0（ランク8）、黒は row=7（ランク1）で昇格
        int promotionRow = piece.getColor() == Color.WHITE ? 0 : 7;

        Position current = piece.getPosition();
        int row = current.getRow();
        int col = current.getCol();

        int newRow = row + direction;
        if (isInBounds(newRow, col)) {
            Position target = Position.of(newRow, col);
            if (!board.isPieceAt(target)) {
                if (newRow == promotionRow) {
                    moves.add(Move.promotion(current, target, PieceType.QUEEN));
                    moves.add(Move.promotion(current, target, PieceType.ROOK));
                    moves.add(Move.promotion(current, target, PieceType.BISHOP));
                    moves.add(Move.promotion(current, target, PieceType.KNIGHT));
                } else {
                    moves.add(Move.normal(current, target));
                }

                // 2マス前進は初期位置からのみ有効。かつ1マス目が空の場合のみチェック（1マス目の空チェックは上の if を通過済み）
                if (row == startRow) {
                    int doubleRow = row + 2 * direction;
                    Position doubleTarget = Position.of(doubleRow, col);
                    if (!board.isPieceAt(doubleTarget)) {
                        moves.add(Move.normal(current, doubleTarget));
                    }
                }
            }
        }

        // Diagonal captures and en passant
        for (int colOffset : new int[]{-1, 1}) {
            int captureCol = col + colOffset;
            int captureRow = row + direction;

            if (!isInBounds(captureRow, captureCol)) continue;

            Position target = Position.of(captureRow, captureCol);

            if (board.isPieceAt(target)) {
                Piece targetPiece = board.getPieceAt(target);
                if (targetPiece.getColor() != piece.getColor()) {
                    if (captureRow == promotionRow) {
                        moves.add(Move.promotionCapture(current, target, targetPiece, PieceType.QUEEN));
                        moves.add(Move.promotionCapture(current, target, targetPiece, PieceType.ROOK));
                        moves.add(Move.promotionCapture(current, target, targetPiece, PieceType.BISHOP));
                        moves.add(Move.promotionCapture(current, target, targetPiece, PieceType.KNIGHT));
                    } else {
                        moves.add(Move.capture(current, target, targetPiece));
                    }
                }
            }

            // アンパッサン: target（斜め前のマス）が直前の相手ポーン2マス移動で生じた通過マスと一致する場合に成立
            // 取られるポーンは target の真横（移動前の行・移動先の列）に存在する
            if (enPassantTarget != null && enPassantTarget.equals(target)) {
                Position capturedPawnPos = Position.of(row, captureCol);
                Piece capturedPawn = board.getPieceAt(capturedPawnPos);
                if (capturedPawn != null && capturedPawn.getColor() != piece.getColor()
                        && capturedPawn.getType() == PieceType.PAWN) {
                    moves.add(Move.enPassant(current, target, capturedPawn));
                }
            }
        }
    }

    /**
     * ナイトの擬似合法手（L字8方向）を生成してリストに追加する。
     *
     * @param piece ナイト
     * @param board 現在の盤面
     * @param moves 追加先のリスト
     */
    private void addKnightMoves(Piece piece, Board board, List<Move> moves) {
        Position current = piece.getPosition();
        List<Position> attacked = piece.getAttackedSquares(board);

        for (Position target : attacked) {
            if (!board.isPieceAt(target)) {
                moves.add(Move.normal(current, target));
            } else {
                Piece targetPiece = board.getPieceAt(target);
                if (targetPiece.getColor() != piece.getColor()) {
                    moves.add(Move.capture(current, target, targetPiece));
                }
            }
        }
    }

    /**
     * スライディング駒（ビショップ・ルーク・クイーン）の擬似合法手を生成してリストに追加する。
     * 駒がある位置で利き筋を止める。
     *
     * @param piece スライディング駒
     * @param board 現在の盤面
     * @param moves 追加先のリスト
     */
    private void addSlidingPieceMoves(Piece piece, Board board, List<Move> moves) {
        Position current = piece.getPosition();
        List<Position> attacked = piece.getAttackedSquares(board);

        for (Position target : attacked) {
            if (!board.isPieceAt(target)) {
                moves.add(Move.normal(current, target));
            } else {
                Piece targetPiece = board.getPieceAt(target);
                if (targetPiece.getColor() != piece.getColor()) {
                    moves.add(Move.capture(current, target, targetPiece));
                }
            }
        }
    }

    /**
     * キングの通常移動（周囲8マス）の擬似合法手を生成してリストに追加する。
     * キャスリングは {@link #addCastlingMoves} が別途処理する。
     *
     * @param piece キング
     * @param board 現在の盤面
     * @param moves 追加先のリスト
     */
    private void addKingMoves(Piece piece, Board board, List<Move> moves) {
        Position current = piece.getPosition();
        List<Position> attacked = piece.getAttackedSquares(board);

        for (Position target : attacked) {
            if (!board.isPieceAt(target)) {
                moves.add(Move.normal(current, target));
            } else {
                Piece targetPiece = board.getPieceAt(target);
                if (targetPiece.getColor() != piece.getColor()) {
                    moves.add(Move.capture(current, target, targetPiece));
                }
            }
        }
    }

    /**
     * キャスリング条件（未移動・経路空・王手経由なし）を満たす場合に
     * キングサイド・クイーンサイドのキャスリング手をリストに追加する。
     *
     * @param king  キング駒
     * @param board 現在の盤面
     * @param moves 追加先のリスト
     */
    private void addCastlingMoves(Piece king, Board board, List<Move> moves) {
        if (king.getMoveCount() != 0) return;

        Color color = king.getColor();
        int row = king.getPosition().getRow();
        int col = king.getPosition().getCol();

        // キャスリングはキングが e ファイル（列4）にいる場合のみ有効
        if (col != 4) return;

        // 王手中はキャスリング不可
        if (checkDetector.isInCheck(color, board)) return;

        // キングサイド: ルークは h ファイル（列7）、キングは g ファイル（列6）へ移動
        // 通過マス（f=5）が攻撃されていないことも確認（g=6 の攻撃チェックは isLegalMove で保証）
        Piece kRook = board.getPieceAt(Position.of(row, 7));
        if (kRook != null && kRook.getType() == PieceType.ROOK
                && kRook.getColor() == color && kRook.getMoveCount() == 0
                && !board.isPieceAt(Position.of(row, 5))
                && !board.isPieceAt(Position.of(row, 6))
                && !checkDetector.isSquareAttacked(Position.of(row, 5), color, board)) {
            moves.add(Move.castling(king.getPosition(), Position.of(row, 6)));
        }

        // クイーンサイド: ルークは a ファイル（列0）、キングは c ファイル（列2）へ移動
        // b ファイル（列1）は経路の空き確認のみ（キングは通過しない）
        Piece qRook = board.getPieceAt(Position.of(row, 0));
        if (qRook != null && qRook.getType() == PieceType.ROOK
                && qRook.getColor() == color && qRook.getMoveCount() == 0
                && !board.isPieceAt(Position.of(row, 1))
                && !board.isPieceAt(Position.of(row, 2))
                && !board.isPieceAt(Position.of(row, 3))
                && !checkDetector.isSquareAttacked(Position.of(row, 3), color, board)) {
            moves.add(Move.castling(king.getPosition(), Position.of(row, 2)));
        }
    }

    /**
     * 指定した行・列が盤面内かどうかを返す。
     *
     * @param row 行番号
     * @param col 列番号
     * @return 盤面内であれば true
     */
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < Position.BOARD_SIZE && col >= 0 && col < Position.BOARD_SIZE;
    }

}
