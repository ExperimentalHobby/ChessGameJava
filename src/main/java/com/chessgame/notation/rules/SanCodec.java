package com.chessgame.notation.rules;

import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;

import java.util.ArrayList;
import java.util.List;

/**
 * SAN（Standard Algebraic Notation、例 {@code Nf3}・{@code O-O}・{@code exd5=Q+}）と
 * {@link Move} を相互変換する。副作用のない静的メソッドのみで構成する。
 */
public class SanCodec {

    private SanCodec() {
    }

    /**
     * 手を SAN 文字列に変換する。
     *
     * @param boardBeforeMove          手を適用する前の盤面
     * @param move                     変換する手
     * @param allLegalMovesForMovingSide 手番側の全合法手（曖昧回避の判定に使用）
     * @param isCheck                  この手の後に相手が王手になるか
     * @param isCheckmate              この手の後に相手がチェックメイトになるか
     * @return SAN 文字列
     */
    public static String encode(Board boardBeforeMove, Move move, List<Move> allLegalMovesForMovingSide,
                                 boolean isCheck, boolean isCheckmate) {
        String core = encodeCore(boardBeforeMove, move, allLegalMovesForMovingSide);
        if (isCheckmate) {
            return core + "#";
        }
        if (isCheck) {
            return core + "+";
        }
        return core;
    }

    /**
     * SAN 文字列（末尾の {@code +}/{@code #} は無視する）に一致する手を、合法手リストから探す。
     * 各合法手を {@link #encodeCore} で（王手記号無しの）SAN に変換し、一致するものを返す。
     *
     * @param san                      解決したい SAN 文字列
     * @param boardBeforeMove          手を適用する前の盤面
     * @param allLegalMovesForMovingSide 手番側の全合法手
     * @return 一致する {@link Move}、見つからなければ null
     */
    public static Move decode(String san, Board boardBeforeMove, List<Move> allLegalMovesForMovingSide) {
        String normalized = san.replaceAll("[+#]$", "");
        for (Move candidate : allLegalMovesForMovingSide) {
            if (encodeCore(boardBeforeMove, candidate, allLegalMovesForMovingSide).equals(normalized)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 王手・チェックメイト記号を含まない SAN 本体を組み立てる。
     */
    private static String encodeCore(Board board, Move move, List<Move> allLegalMovesForMovingSide) {
        if (move.isCastling()) {
            return move.getTo().getCol() > move.getFrom().getCol() ? "O-O" : "O-O-O";
        }

        Piece piece = board.getPieceAt(move.getFrom());
        StringBuilder sb = new StringBuilder();

        if (piece.getType() == PieceType.PAWN) {
            if (move.isCapture()) {
                sb.append(fileChar(move.getFrom())).append('x');
            }
            sb.append(move.getTo().toAlgebraic());
            if (move.isPromotion()) {
                sb.append('=').append(move.getPromotionPiece().getNotation());
            }
        } else {
            sb.append(piece.getType().getNotation());
            sb.append(disambiguation(board, move, allLegalMovesForMovingSide));
            if (move.isCapture()) {
                sb.append('x');
            }
            sb.append(move.getTo().toAlgebraic());
        }

        return sb.toString();
    }

    /**
     * 同種・同色・同じ移動先を持つ他の合法手がある場合の曖昧回避文字列を返す
     * （曖昧でなければ空文字列）。ファイル→ランク→両方の順で最小限の情報を付加する。
     */
    private static String disambiguation(Board board, Move move, List<Move> allLegalMovesForMovingSide) {
        Piece movingPiece = board.getPieceAt(move.getFrom());
        List<Position> otherOrigins = new ArrayList<>();

        for (Move other : allLegalMovesForMovingSide) {
            if (other.getFrom().equals(move.getFrom()) || !other.getTo().equals(move.getTo())) {
                continue;
            }
            Piece otherPiece = board.getPieceAt(other.getFrom());
            if (otherPiece != null && otherPiece.getType() == movingPiece.getType()) {
                otherOrigins.add(other.getFrom());
            }
        }

        if (otherOrigins.isEmpty()) {
            return "";
        }

        boolean fileIsUnique = otherOrigins.stream().noneMatch(p -> p.getCol() == move.getFrom().getCol());
        if (fileIsUnique) {
            return String.valueOf(fileChar(move.getFrom()));
        }

        boolean rankIsUnique = otherOrigins.stream().noneMatch(p -> p.getRow() == move.getFrom().getRow());
        if (rankIsUnique) {
            return String.valueOf(rankChar(move.getFrom()));
        }

        return "" + fileChar(move.getFrom()) + rankChar(move.getFrom());
    }

    private static char fileChar(Position pos) {
        return pos.toAlgebraic().charAt(0);
    }

    private static char rankChar(Position pos) {
        return pos.toAlgebraic().charAt(1);
    }
}
