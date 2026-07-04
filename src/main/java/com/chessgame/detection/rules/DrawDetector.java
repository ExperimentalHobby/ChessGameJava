package com.chessgame.detection.rules;

import com.chessgame.board.model.Board;
import com.chessgame.model.Color;
import com.chessgame.piece.model.Piece;
import com.chessgame.piece.model.PieceType;
import com.chessgame.board.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * 50手ルール・千日手・戦力不足による引き分けを判定するクラス。
 * いずれも副作用のない純粋な判定ロジックで、状態（ハーフムーブクロック・局面出現回数）は
 * 呼び出し側（{@link com.chessgame.gamestate.model.GameState}）が保持する。
 */
public class DrawDetector {
    /** 50手ルールの閾値（半手数）。50手×2 = 100。 */
    private static final int FIFTY_MOVE_HALFMOVE_LIMIT = 100;
    /** 千日手の閾値（同一局面の出現回数）。 */
    private static final int REPETITION_THRESHOLD = 3;

    /**
     * ハーフムーブクロックが50手ルールの閾値に達しているかを返す。
     *
     * @param halfmoveClock 直近のポーン移動・駒取りからの半手数
     * @return 50手ルールに該当する場合 true
     */
    public boolean isFiftyMoveRule(int halfmoveClock) {
        return halfmoveClock >= FIFTY_MOVE_HALFMOVE_LIMIT;
    }

    /**
     * 同一局面の出現回数が千日手の閾値に達しているかを返す。
     *
     * @param positionOccurrences 現在の局面の出現回数
     * @return 千日手に該当する場合 true
     */
    public boolean isThreefoldRepetition(int positionOccurrences) {
        return positionOccurrences >= REPETITION_THRESHOLD;
    }

    /**
     * 双方の戦力がチェックメイトを強制できないほど不足しているかを返す。
     *
     * @param board 現在の盤面
     * @return 戦力不足と判定される場合 true
     */
    public boolean isInsufficientMaterial(Board board) {
        List<Piece> whiteMinors = new ArrayList<>();
        List<Piece> blackMinors = new ArrayList<>();
        for (Color color : Color.values()) {
            for (Piece piece : board.getAllPieces(color)) {
                PieceType type = piece.getType();
                if (type == PieceType.KING) {
                    continue;
                }
                // ポーン・ルーク・クイーンが1枚でもあれば、それだけで十分な戦力とみなす
                if (type == PieceType.PAWN || type == PieceType.ROOK || type == PieceType.QUEEN) {
                    return false;
                }
                (color == Color.WHITE ? whiteMinors : blackMinors).add(piece);
            }
        }

        int totalMinors = whiteMinors.size() + blackMinors.size();
        if (totalMinors <= 1) {
            return true; // King vs King、または King+マイナーピース1枚 vs King
        }

        // 双方ちょうど1枚ずつのビショップで、かつ同色マスなら引き分け
        if (whiteMinors.size() == 1 && blackMinors.size() == 1) {
            Piece whitePiece = whiteMinors.get(0);
            Piece blackPiece = blackMinors.get(0);
            if (whitePiece.getType() == PieceType.BISHOP && blackPiece.getType() == PieceType.BISHOP
                    && isSameSquareColor(whitePiece.getPosition(), blackPiece.getPosition())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 2つの位置が同じ色のマス（明暗）にあるかを返す。
     */
    private boolean isSameSquareColor(Position a, Position b) {
        return (a.getRow() + a.getCol()) % 2 == (b.getRow() + b.getCol()) % 2;
    }
}
