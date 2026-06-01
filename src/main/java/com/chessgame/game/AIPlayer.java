package com.chessgame.game;

import com.chessgame.model.move.Move;
import com.chessgame.model.board.Position;
import com.chessgame.model.piece.Piece;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * AIプレイヤーの実装クラス。難易度に応じた手選択ロジックを提供する。
 * 難易度1: ランダム選択、難易度2: 駒取り優先、難易度3: 素材価値最大化。
 */
public class AIPlayer extends Player {
    private static final Random random = new Random();
    private final int difficulty;

    /**
     * AIプレイヤーを生成する。
     *
     * @param name       プレイヤー名
     * @param color      担当する色
     * @param difficulty 難易度（1=ランダム、2=駒取り優先、3=最善手優先）
     */
    public AIPlayer(String name, com.chessgame.model.Color color, int difficulty) {
        super(color, name, false);
        this.difficulty = difficulty;
    }

    /**
     * 現在のゲーム状態から難易度に応じた手を選んで返す。
     * 合法手が存在しない場合は null を返す。
     *
     * @param game 現在のゲーム
     * @return 選択した {@link Move}、または null
     */
    public Move selectMove(ChessGame game) {
        List<Move> availableMoves = collectAllAvailableMoves(game);

        if (availableMoves.isEmpty()) {
            return null;
        }

        // difficulty 1（default）: ランダム選択
        // difficulty 2: 駒取り優先
        // difficulty 3: 素材価値が最大になる手を選択
        switch (difficulty) {
            case 2:
                return selectMoveWithPreference(availableMoves);
            case 3:
                return selectBestMove(availableMoves);
            default:
                return availableMoves.get(random.nextInt(availableMoves.size()));
        }
    }

    /**
     * 現在の盤面で AI の色の全駒が指せる合法手をまとめて返す。
     *
     * @param game 現在のゲーム
     * @return 全合法手のリスト
     */
    private List<Move> collectAllAvailableMoves(ChessGame game) {
        List<Move> allMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = Position.of(row, col);
                Piece piece = game.getBoard().getPieceAt(pos);
                if (piece != null && piece.getColor() == getColor()) {
                    allMoves.addAll(game.getAvailableMoves(pos));
                }
            }
        }
        return allMoves;
    }

    /**
     * 難易度2用。駒を取る手を優先して選択し、なければランダムに選ぶ。
     *
     * @param availableMoves 選択候補の合法手リスト
     * @return 選択した手
     */
    private Move selectMoveWithPreference(List<Move> availableMoves) {
        List<Move> captures = availableMoves.stream()
            .filter(m -> m.getCapturedPiece() != null)
            .toList();

        if (!captures.isEmpty()) {
            return captures.get(random.nextInt(captures.size()));
        }
        return availableMoves.get(random.nextInt(availableMoves.size()));
    }

    /**
     * 難易度3用。取れる駒の素材価値が最大になる手を選ぶ。
     *
     * @param availableMoves 選択候補の合法手リスト
     * @return 選択した手
     */
    private Move selectBestMove(List<Move> availableMoves) {
        Move bestMove = availableMoves.get(0);
        int bestScore = Integer.MIN_VALUE;

        for (Move move : availableMoves) {
            int score = getPieceValue(move.getCapturedPiece());
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    /**
     * 指定した駒の素材価値を返す。null の場合は 0。
     *
     * @param piece 価値を調べる駒（null 可）
     * @return 素材価値（ポーン=1、ナイト/ビショップ=3、ルーク=5、クイーン=9、キング=0）
     */
    private int getPieceValue(Piece piece) {
        if (piece == null) return 0;
        return switch (piece.getType()) {
            case PAWN -> 1;
            case KNIGHT -> 3;
            case BISHOP -> 3;
            case ROOK -> 5;
            case QUEEN -> 9;
            case KING -> 0;
        };
    }
}

