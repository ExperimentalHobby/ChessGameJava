package com.chessgame.game.core;

import com.chessgame.model.Color;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.game.player.AIPlayer;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Java のルール層と Python エンジン（{@code ai/engine.py}）の合法手生成が一致することを検証する。
 *
 * <p>ランダム対局を進めながら各局面を {@link AIPlayer#buildFen} で FEN 化し、Python の
 * {@code movegen} コマンドが返す合法手集合と、Java の {@code getAvailableMoves} が返す
 * 合法手集合（UCI 表記）が完全一致することを確認する。これにより FEN 生成の正しさと、
 * Python 側 move-gen の Java ルールとの整合性を担保する。</p>
 *
 * <p>Python が実行できない環境ではスキップする（move-gen 自体は Python の perft テストで
 * 別途検証済み）。</p>
 */
public class AiEngineParityTest {

    private static final String STARTPOS_FEN =
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Test
    public void pythonMoveGenMatchesJavaRules() {
        assumeTrue(runMovegen(STARTPOS_FEN) != null, "Python が実行できないためスキップ");

        Random rng = new Random(20260607L);
        for (int gameIndex = 0; gameIndex < 2; gameIndex++) {
            ChessGame game = ChessGame.createTwoPlayerGame("W", "B");
            game.startNewGame();

            for (int ply = 0; ply < 24 && !game.isGameOver(); ply++) {
                Color side = game.getCurrentPlayer().getColor();
                List<Move> legalMoves = collectLegalMoves(game, side);
                if (legalMoves.isEmpty()) {
                    break;
                }

                String fen = new AIPlayer("ai", side, 4).buildFen(game);
                Set<String> javaUci = toUciSet(legalMoves);
                Set<String> pythonUci = runMovegen(fen);

                assertThat(pythonUci)
                    .as("FEN=%s", fen)
                    .isEqualTo(javaUci);

                Move chosen = legalMoves.get(rng.nextInt(legalMoves.size()));
                applyMove(game, chosen);
            }
        }
    }

    /** 指定色の現在の合法手をすべて集める。 */
    private List<Move> collectLegalMoves(ChessGame game, Color side) {
        List<Move> moves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = Position.of(row, col);
                var piece = game.getBoard().getPieceAt(pos);
                if (piece != null && piece.getColor() == side) {
                    moves.addAll(game.getAvailableMoves(pos));
                }
            }
        }
        return moves;
    }

    /** Move のリストを UCI 文字列の集合に変換する。 */
    private Set<String> toUciSet(List<Move> moves) {
        Set<String> set = new HashSet<>();
        for (Move move : moves) {
            String uci = move.getFrom().toAlgebraic() + move.getTo().toAlgebraic();
            if (move.isPromotion() && move.getPromotionPiece() != null) {
                uci += Character.toLowerCase(move.getPromotionPiece().getNotation());
            }
            set.add(uci);
        }
        return set;
    }

    /** ランダムに選んだ手を盤面に適用する（昇格は既定のクイーンで進める）。 */
    private void applyMove(ChessGame game, Move move) {
        if (move.isPromotion() && move.getPromotionPiece() != null) {
            game.makeMove(move.getFrom(), move.getTo(), move.getPromotionPiece());
        } else {
            game.makeMove(move.getFrom(), move.getTo());
        }
    }

    /**
     * Python の movegen コマンドを実行し、合法手の UCI 集合を返す。
     * 実行できない場合は null（テストはスキップ）。
     */
    private Set<String> runMovegen(String fen) {
        String json = "{\"command\":\"movegen\",\"fen\":\"" + fen + "\"}";
        for (String command : new String[]{"py", "python3", "python"}) {
            Process process = null;
            try {
                ProcessBuilder builder = new ProcessBuilder(command, "ai/chess_ai.py");
                builder.redirectError(ProcessBuilder.Redirect.DISCARD);
                process = builder.start();
                try (OutputStream stdin = process.getOutputStream()) {
                    stdin.write(json.getBytes(StandardCharsets.UTF_8));
                }
                String line;
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    line = reader.readLine();
                }
                if (!process.waitFor(10, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    continue;
                }
                if (process.exitValue() != 0 || line == null) {
                    continue;
                }
                Set<String> result = new HashSet<>();
                if (!line.isBlank()) {
                    result.addAll(Arrays.asList(line.trim().split("\\s+")));
                }
                return result;
            } catch (Exception e) {
                if (process != null) {
                    process.destroyForcibly();
                }
            }
        }
        return null;
    }
}
