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

import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.observer.GameObserver;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.PieceType;
import java.util.Scanner;

/**
 * コンソールで動作するインタラクティブなチェスゲームUI。
 * 標準入力からコマンドや移動（"e2e4" 形式）を受け取り、ゲームを進行させる。
 * {@link GameObserver} を実装し、王手・ゲーム終了などのイベントをコンソールに出力する。
 * 2人対戦またはAI対戦（難易度1〜4）をサポート。
 */
public class InteractiveGame implements GameObserver {
    private ChessGame game;  // final を外し、selectGameMode() で置き換え可能に
    private final Scanner scanner;
    private boolean running;

    /**
     * ゲームを初期化する。初期値として2人対戦モードで生成し、自身をオブザーバーとして登録する。
     */
    public InteractiveGame() {
        this.game = ChessGame.createTwoPlayerGame("White Player", "Black Player");
        this.scanner = new Scanner(System.in);
        this.running = true;
        this.game.addObserver(this);
    }

    /**
     * 現在の {@link ChessGame} インスタンスを返す（テストでの状態確認用）。
     */
    ChessGame getGame() {
        return game;
    }

    /**
     * ゲームを開始してメインループを実行する。ゲーム開始前にゲームモード（2人対戦・AI難易度）を選択する。
     * ゲーム終了またはquitコマンドで終了する。
     */
    public void start() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   Java Chess Game - Interactive Mode   ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        selectGameMode();
        game.startNewGame();
        displayBoard();
        displayHelp();

        while (running && !game.isGameOver()) {
            displayGameState();
            // AI の手番なら自動実行
            if (game.getCurrentPlayer().isAI()) {
                executeAIMove();
            } else {
                processPlayerInput();
            }
        }

        if (game.isGameOver()) {
            displayGameOver();
        }

        scanner.close();
    }

    /**
     * ゲームモード（2人対戦またはAI難易度）を選択する。
     * 選択に応じてゲームのプレイヤーを再構成する。
     */
    private void selectGameMode() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║        SELECT GAME MODE                ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ 0. Human vs Human (default)            ║");
        System.out.println("║ 1. Human vs AI (Easy)                  ║");
        System.out.println("║ 2. Human vs AI (Medium)                ║");
        System.out.println("║ 3. Human vs AI (Hard)                  ║");
        System.out.println("║ 4. Human vs AI (Expert)                ║");
        System.out.println("╚════════════════════════════════════════╝");

        System.out.print("\nSelect mode (0-4): ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                setupAIGame(1);
                break;
            case "2":
                setupAIGame(2);
                break;
            case "3":
                setupAIGame(3);
                break;
            case "4":
                setupAIGame(4);
                break;
            default:
                // 0 or invalid → 2人対戦のまま
                break;
        }
    }

    /**
     * AI対戦ゲームをセットアップする。黒がAIで、難易度を指定する。
     *
     * @param difficulty AI難易度（1=Easy, 2=Medium, 3=Hard, 4=Expert）
     */
    private void setupAIGame(int difficulty) {
        game.removeObserver(this);
        Player whitePlayer = Player.human(Color.WHITE, "You");
        Player blackPlayer = new AIPlayer("AI", Color.BLACK, difficulty);
        this.game = new ChessGame(whitePlayer, blackPlayer);
        this.game.addObserver(this);
        System.out.println("\n✓ Game Mode: Human vs AI (" + getDifficultyLabel(difficulty) + ")\n");
    }

    /**
     * 難易度を日本語ラベルに変換する。
     *
     * @param difficulty 難易度（1=Easy, 2=Medium, 3=Hard, 4=Expert）
     * @return 難易度のラベル
     */
    private String getDifficultyLabel(int difficulty) {
        return switch (difficulty) {
            case 1 -> "Easy";
            case 2 -> "Medium";
            case 3 -> "Hard";
            case 4 -> "Expert";
            default -> "Unknown";
        };
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
     * "e2e4" 形式の文字列を解析してゲームに手を送る。昇格の場合は駒種を聞く。
     * 成功時は盤面を再表示する。
     *
     * @param moveStr "e2e4" 形式の移動文字列
     */
    private void makeMove(String moveStr) {
        try {
            // "e2e4" → from="e2", to="e4" に分割して Position に変換する
            Position from = Position.of(moveStr.substring(0, 2));
            Position to = Position.of(moveStr.substring(2, 4));

            // 昇格の可能性をチェック（ターゲットが最終ランクか）
            boolean isPromotionMove = isPromotionTarget(from, to, game.getBoard().getPieceAt(from));
            PieceType promotionType = null;

            if (isPromotionMove) {
                promotionType = selectPromotionPiece();
            }

            boolean success = (promotionType != null)
                ? game.makeMove(from, to, promotionType)
                : game.makeMove(from, to);

            if (success) {
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
     * 移動が昇格対象かどうかを判定する。
     *
     * @param from 移動元位置
     * @param to   移動先位置
     * @param piece 移動する駒
     * @return 昇格対象なら true
     */
    private boolean isPromotionTarget(Position from, Position to, com.chessgame.piece.model.Piece piece) {
        if (piece == null || piece.getType() != PieceType.PAWN) {
            return false;
        }
        // 白ポーンが row=0（ランク8）へ、または黒ポーンが row=7（ランク1）へ移動したら昇格
        return (piece.getColor() == Color.WHITE && to.getRow() == 0)
            || (piece.getColor() == Color.BLACK && to.getRow() == 7);
    }

    /**
     * ユーザーに昇格先の駒種（Q/R/B/N）を選ばせるプロンプト。
     *
     * @return 選択された駒種、または null（無効入力の場合は既定値 QUEEN を返す）
     */
    private PieceType selectPromotionPiece() {
        System.out.print("Promotion: [Q]ueen, [R]ook, [B]ishop, [N]ight (default: Q): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        return switch (choice) {
            case "r" -> PieceType.ROOK;
            case "b" -> PieceType.BISHOP;
            case "n" -> PieceType.KNIGHT;
            default -> PieceType.QUEEN;  // デフォルト: クイーン
        };
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
     * 確認プロンプトを表示し、承認された場合に人間側のプレイヤーを投了させる。
     * Human vs AI では AI の手番中でも常に人間側を投了させる（2人対戦では現在の手番のまま）。
     */
    private void resignGame() {
        System.out.print("Are you sure? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            game.resign(game.getResigningColor());
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
     * AI の手を自動実行する。少しの遅延を入れてユーザー体験を向上させる。
     */
    private void executeAIMove() {
        try {
            // UI の応答性のため、短い遅延を入れる
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
            // 無視
        }

        if (game.getCurrentPlayer().isAI()) {
            AIPlayer ai = (AIPlayer) game.getCurrentPlayer();
            Move aiMove = ai.selectMove(game);
            if (aiMove != null) {
                game.makeMove(aiMove);
                System.out.println("\n➜ AI Move: " + aiMove.getFrom().toAlgebraic() + aiMove.getTo().toAlgebraic());
                displayBoard();
            }
        }
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
        String result1 = "";
        String result2 = "";

        switch (status) {
            case CHECKMATE:
                Color winner = game.getCurrentPlayer().getColor().opposite();
                result1 = "CHECKMATE!";
                result2 = winner + " wins!";
                break;
            case STALEMATE:
                result1 = "STALEMATE!";
                result2 = "It's a draw.";
                break;
            case FIFTY_MOVE_RULE:
                result1 = "DRAW!";
                result2 = "Fifty-move rule.";
                break;
            case THREEFOLD_REPETITION:
                result1 = "DRAW!";
                result2 = "Threefold repetition.";
                break;
            case INSUFFICIENT_MATERIAL:
                result1 = "DRAW!";
                result2 = "Insufficient material.";
                break;
            case WHITE_RESIGNED:
                result1 = "White resigned.";
                result2 = "Black wins!";
                break;
            case BLACK_RESIGNED:
                result1 = "Black resigned.";
                result2 = "White wins!";
                break;
            default:
                break;
        }

        printBoxLine(result1);
        printBoxLine(result2);
        String moveCountStr = "Total moves: " + game.getMoveHistory().size();
        printBoxLine(moveCountStr);
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    /**
     * ボックス内に左詰めで行を出力する（幅固定）。
     *
     * @param text 出力するテキスト
     */
    private void printBoxLine(String text) {
        int boxWidth = 40;
        int padding = boxWidth - text.length();
        System.out.print("║ " + text);
        for (int i = 0; i < padding; i++) {
            System.out.print(" ");
        }
        System.out.println("║");
    }

    // 盤面変化の通知。コンソール版では makeMove 後に displayBoard() を呼ぶため空実装
    @Override
    public void onBoardChanged() {}

    // 手確定の通知。コンソール版では makeMove 内でログ済みのため空実装
    @Override
    public void onMoveMade(Move move) {}

    // ゲーム状態が変化したときに追加メッセージを出力する
    // IN_PROGRESS・WHITE_RESIGNED・BLACK_RESIGNED への遷移は他で処理済みのため出力不要
    @Override
    public void onGameStateChanged(GameState.GameStatus newStatus) {
        switch (newStatus) {
            case CHECK:
                System.out.println("⚠️  " + game.getCurrentPlayer().getColor() + " is in CHECK!");
                break;
            case CHECKMATE:
                System.out.println("♟ CHECKMATE!");
                break;
            case STALEMATE:
                System.out.println("♟ STALEMATE!");
                break;
            case FIFTY_MOVE_RULE:
                System.out.println("♟ DRAW! (Fifty-move rule)");
                break;
            case THREEFOLD_REPETITION:
                System.out.println("♟ DRAW! (Threefold repetition)");
                break;
            case INSUFFICIENT_MATERIAL:
                System.out.println("♟ DRAW! (Insufficient material)");
                break;
            case IN_PROGRESS:
            case WHITE_RESIGNED:
            case BLACK_RESIGNED:
                // これらの状態遷移は他のメソッドで処理済み
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
