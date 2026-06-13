package com.chessgame;

import com.chessgame.game.ChessGame;
import com.chessgame.game.GameObserver;
import com.chessgame.model.Color;
import com.chessgame.model.GameState;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;

public class TestGame implements GameObserver {
    private final ChessGame game;

    public TestGame() {
        this.game = ChessGame.createTwoPlayerGame("Alice", "Bob");
        this.game.addObserver(this);
    }

    public void playDemoGame() {
        System.out.println("=== Chess Game Demo ===\n");
        System.out.println("Initial board:");
        System.out.println(game.getBoard());

        game.startNewGame();
        System.out.println("Game started! " + game.getCurrentPlayer() + " to move.\n");

        playMove("e2", "e4");
        playMove("e7", "e5");
        playMove("g1", "f3");
        playMove("b8", "c6");
        playMove("f1", "c4");

        System.out.println("\n=== Final Board State ===");
        System.out.println(game.getBoard());
        System.out.println("\nGame Status: " + game.getGameStatus());
        System.out.println("Current Player: " + game.getCurrentPlayer());
    }

    private void playMove(String fromAlg, String toAlg) {
        Position from = Position.of(fromAlg);
        Position to = Position.of(toAlg);

        String player = game.getCurrentPlayer().getName();
        System.out.println(player + " moves " + fromAlg + " to " + toAlg);

        // makeMove が false を返す場合は非合法手（相手の駒・王手放置など）
        if (game.makeMove(from, to)) {
            System.out.println("✓ Move successful");
        } else {
            System.out.println("✗ Move failed");
        }
        System.out.println();
    }

    // デモでは盤面を手動で出力するため、onBoardChanged は使用しない
    @Override public void onBoardChanged() {}
    // 手の確定は playMove 内でログ済みのため、onMoveMade は使用しない
    @Override public void onMoveMade(Move move) {}

    // ゲーム状態が変化するたびにコンソールへ通知ログを出力する
    @Override
    public void onGameStateChanged(GameState.GameStatus newStatus) {
        System.out.println("[Observer] Game status changed to: " + newStatus);
    }

    // 王手検出時に王手されているキングの色を出力する
    @Override
    public void onCheckDetected(Color kingColor) {
        System.out.println("[Observer] " + kingColor + " king is in CHECK!");
    }

    @Override
    public void onGameOver(Color winner) {
        // winner が null の場合はステールメイト（引き分け）
        if (winner != null) {
            System.out.println("[Observer] Game Over! " + winner + " wins!");
        } else {
            System.out.println("[Observer] Game Over! Stalemate!");
        }
    }

    public static void main(String[] args) {
        TestGame demo = new TestGame();
        demo.playDemoGame();
    }
}
