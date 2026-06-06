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

package com.chessgame;

import com.chessgame.game.ChessGame;
import com.chessgame.game.GameObserver;
import com.chessgame.model.Color;
import com.chessgame.model.GameState;
import com.chessgame.model.board.Position;
import com.chessgame.model.move.Move;
import java.util.Scanner;

/**
 * コンソールで動作するインタラクティブなチェスゲームUI。
 * 標準入力からコマンドや移動（"e2e4" 形式）を受け取り、ゲームを進行させる。
 * {@link GameObserver} を実装し、王手・ゲーム終了などのイベントをコンソールに出力する。
 */
public class InteractiveGame implements GameObserver {
    private final ChessGame game;
    private final Scanner scanner;
    private boolean running;

    /**
     * ゲームを初期化する。2人対戦モードで開始し、自身をオブザーバーとして登録する。
     */
    public InteractiveGame() {
        this.game = ChessGame.createTwoPlayerGame("White Player", "Black Player");
        this.scanner = new Scanner(System.in);
        this.running = true;
        this.game.addObserver(this);
    }

    /**
     * ゲームを開始してメインループを実行する。ゲーム終了またはquitコマンドで終了する。
     */
    public void start() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   Java Chess Game - Interactive Mode   ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        game.startNewGame();
        displayBoard();
        displayHelp();

        while (running && !game.isGameOver()) {
            displayGameState();
            processPlayerInput();
        }

        if (game.isGameOver()) {
            displayGameOver();
        }

        scanner.close();
    }

    /**
     * 現在の手番・ゲーム状態・手数をコンソールに出力する。
     */
    private void displayGameState() {
        System.out.println("\n" + "═".repeat(50));
        System.out.println("Current Player: " + game.getCurrentPlayer().getName());
        System.out.println("Status: " + game.getGameStatus());
        System.out.println("Total Moves: " + game.getMoveHistory().size());
        System.out.println("═".repeat(50));
    }

    /**
     * 標準入力から1行読み込み、コマンドまたは移動として処理する。
     */
    private void processPlayerInput() {
        System.out.print("\nEnter move (format: e2e4) or command: ");
        // trim で前後の空白を除去し、toLowerCase で大文字小文字を統一する
        String input = scanner.nextLine().trim().toLowerCase();

        if (input.isEmpty()) {
            return;
        }

        switch (input) {
            case "help":
            case "?":
                displayHelp();
                break;
            case "board":
            case "b":
                displayBoard();
                break;
            case "undo":
            case "u":
                undoMove();
                break;
            case "resign":
            case "r":
                resignGame();
                break;
            case "new":
            case "n":
                startNewGame();
                break;
            case "quit":
            case "q":
                quit();
                break;
            case "moves":
            case "m":
                displayMoveHistory();
                break;
            default:
                // 4文字は "e2e4" 形式の移動入力として解釈する
                if (input.length() == 4) {
                    makeMove(input);
                } else {
                    System.out.println("Invalid input. Type 'help' for commands.");
                }
                break;
        }
    }

    /**
     * "e2e4" 形式の文字列を解析してゲームに手を送る。成功時は盤面を再表示する。
     *
     * @param moveStr "e2e4" 形式の移動文字列
     */
    private void makeMove(String moveStr) {
        try {
            // "e2e4" → from="e2", to="e4" に分割して Position に変換する
            Position from = Position.of(moveStr.substring(0, 2));
            Position to = Position.of(moveStr.substring(2, 4));

            if (game.makeMove(from, to)) {
                System.out.println("✓ Move: " + moveStr.toUpperCase());
                displayBoard();
            } else {
                System.out.println("✗ Invalid move. Try again.");
            }
        } catch (Exception ignored) {
            System.out.println("✗ Invalid format. Use: e2e4");
        }
    }

    /**
     * 直前の手を取り消す。履歴が空の場合はメッセージを表示して何もしない。
     */
    private void undoMove() {
        if (game.getMoveHistory().isEmpty()) {
            System.out.println("No moves to undo.");
            return;
        }

        game.undo();
        System.out.println("✓ Last move undone.");
        displayBoard();
    }

    /**
     * 確認プロンプトを表示し、承認された場合に現在のプレイヤーを投了させる。
     */
    private void resignGame() {
        System.out.print("Are you sure? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            Color current = game.getCurrentPlayer().getColor();
            game.resign(current);
            System.out.println("Game resigned.");
            running = false;
        }
    }

    /**
     * ゲームをリセットして新ゲームを開始し、盤面を再表示する。
     */
    private void startNewGame() {
        game.startNewGame();
        System.out.println("✓ New game started.");
        displayBoard();
    }

    /**
     * メインループを終了してゲームを終わらせる。
     */
    private void quit() {
        running = false;
        System.out.println("Thanks for playing!");
    }

    /**
     * 利用可能なコマンドの一覧をコンソールに出力する。
     */
    private void displayHelp() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            COMMANDS                   ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ e2e4      - Move piece from e2 to e4  ║");
        System.out.println("║ board(b)  - Display current board     ║");
        System.out.println("║ moves(m)  - Show move history         ║");
        System.out.println("║ undo(u)   - Undo last move           ║");
        System.out.println("║ resign(r) - Resign the game          ║");
        System.out.println("║ new(n)    - Start new game           ║");
        System.out.println("║ help(?)   - Display this help         ║");
        System.out.println("║ quit(q)   - Exit the game            ║");
        System.out.println("╚════════════════════════════════════════╝");
    }

    /**
     * 現在の盤面を代数記法の座標ラベル付きでコンソールに出力する。
     */
    private void displayBoard() {
        System.out.println("\n  a b c d e f g h");
        System.out.println("  ───────────────────");

        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " │");
            for (int col = 0; col < 8; col++) {
                Position pos = Position.of(row, col);
                var piece = game.getBoard().getPieceAt(pos);
                if (piece == null) {
                    System.out.print(" . ");
                } else {
                    System.out.print(" " + piece + " ");
                }
            }
            System.out.println("│ " + (8 - row));
        }
        System.out.println("  ───────────────────");
        System.out.println("  a b c d e f g h\n");
    }

    /**
     * これまでの移動履歴を棋譜形式でコンソールに出力する。
     */
    private void displayMoveHistory() {
        if (game.getMoveHistory().isEmpty()) {
            System.out.println("\nNo moves yet.");
            return;
        }

        System.out.println("\nMove History:");
        System.out.println(game.getMoveHistory().getNotationString());
    }

    /**
     * ゲーム終了時の結果（チェックメイト・ステールメイト・投了）をコンソールに出力する。
     */
    private void displayGameOver() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            GAME OVER                  ║");
        System.out.println("╠════════════════════════════════════════╣");

        GameState.GameStatus status = game.getGameStatus();
        switch (status) {
            case CHECKMATE:
                Color winner = game.getCurrentPlayer().getColor().opposite();
                System.out.println("║ CHECKMATE!                             ║");
                System.out.println("║ Winner: " + winner + " wins!                 ║");
                break;
            case STALEMATE:
                System.out.println("║ STALEMATE!                             ║");
                System.out.println("║ It's a draw.                          ║");
                break;
            case WHITE_RESIGNED:
                System.out.println("║ White resigned.                        ║");
                System.out.println("║ Black wins!                            ║");
                break;
            case BLACK_RESIGNED:
                System.out.println("║ Black resigned.                        ║");
                System.out.println("║ White wins!                            ║");
                break;
            default:
                break;
        }

        System.out.println("║ Total moves: " + game.getMoveHistory().size() + "                 ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    // 盤面変化の通知。コンソール版では makeMove 後に displayBoard() を呼ぶため空実装
    @Override
    public void onBoardChanged() {}

    // 手確定の通知。コンソール版では makeMove 内でログ済みのため空実装
    @Override
    public void onMoveMade(Move move) {}

    // ゲーム状態が変化したときに追加メッセージを出力する
    // IN_PROGRESS への遷移は通常の手番変更のため出力不要
    @Override
    public void onGameStateChanged(GameState.GameStatus newStatus) {
        switch (newStatus) {
            // 王手: 次の手番プレイヤーが王手されている
            case CHECK:
                System.out.println("⚠️  " + game.getCurrentPlayer().getColor() + " is in CHECK!");
                break;
            // チェックメイト: 合法手がなく、かつ王手状態
            case CHECKMATE:
                System.out.println("♟ CHECKMATE!");
                break;
            // ステールメイト: 合法手がないが王手ではない（引き分け）
            case STALEMATE:
                System.out.println("♟ STALEMATE!");
                break;
        }
    }

    // onGameStateChanged の CHECK でも通知されるが、王手発生時点で即座に出力するために両方実装している
    @Override
    public void onCheckDetected(Color kingColor) {
        System.out.println("⚠️  " + kingColor + " king is in CHECK!");
    }

    @Override
    public void onGameOver(Color winner) {
        // winner が null の場合はステールメイトによる引き分け
        if (winner != null) {
            System.out.println("🎉 " + winner + " wins!");
        } else {
            System.out.println("🎲 Draw!");
        }
    }

    public static void main(String[] args) {
        InteractiveGame game = new InteractiveGame();
        game.start();
    }
}
