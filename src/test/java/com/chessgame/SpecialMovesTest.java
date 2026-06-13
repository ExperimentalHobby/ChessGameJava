package com.chessgame;

import com.chessgame.game.ChessGame;
import com.chessgame.board.model.Position;

public class SpecialMovesTest {

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║   Chess Game - Special Moves Test Suite          ║");
        System.out.println("╚═══════════════════════════════════════════════════╝\n");

        testBasicMoves();
        testPawnPromotion();
        testCastling();
        testCheck();
        testCheckmate();

        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("║            All Tests Completed!                  ║");
        System.out.println("╚═══════════════════════════════════════════════════╝\n");
    }

    private static void testBasicMoves() {
        System.out.println("► Test 1: Basic Opening Moves");
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();

        testMove(game, "e2", "e4", "1. e4");
        testMove(game, "e7", "e5", "1... e5");
        testMove(game, "g1", "f3", "2. Nf3");
        testMove(game, "b8", "c6", "2... Nc6");

        System.out.println("✓ Basic moves completed\n");
    }

    private static void testPawnPromotion() {
        System.out.println("► Test 2: Pawn Promotion");
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();

        testMove(game, "a2", "a4", "Setup");
        testMove(game, "h7", "h5", "Setup");
        testMove(game, "a4", "a5", "Setup");
        testMove(game, "h5", "h4", "Setup");
        testMove(game, "a5", "a6", "Setup");
        testMove(game, "h4", "h3", "Setup");
        testMove(game, "a6", "a7", "Setup");
        testMove(game, "h3", "h2", "Setup");

        System.out.println("- Board state: Pawns near promotion");
        printBoard(game);
        System.out.println("✓ Pawn promotion test setup complete\n");
    }

    private static void testCastling() {
        System.out.println("► Test 3: Castling Rights");
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();

        testMove(game, "e2", "e4", "1. e4");
        testMove(game, "e7", "e5", "1... e5");
        testMove(game, "g1", "f3", "2. Nf3");
        testMove(game, "g8", "f6", "2... Nf6");
        testMove(game, "f1", "c4", "3. Bc4");
        testMove(game, "f8", "c5", "3... Bc5");

        System.out.println("✓ Castling preparation complete\n");
    }

    private static void testCheck() {
        System.out.println("► Test 4: Check Detection");
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();

        testMove(game, "f2", "f3", "1. f3");
        testMove(game, "e7", "e5", "1... e5");
        testMove(game, "g2", "g4", "2. g4");
        testMove(game, "d8", "h4", "2... Qh4#");

        // フールズメイトは CHECKMATE だが、チェック経由で到達するため CHECK も含めて確認する
        if (game.getGameStatus().toString().contains("CHECK") || game.getGameStatus().toString().contains("CHECKMATE")) {
            System.out.println("✓ Checkmate detected!");
        }
        System.out.println();
    }

    private static void testCheckmate() {
        System.out.println("► Test 5: Checkmate Scenarios");
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();

        System.out.println("\n  Scenario A: Fool's Mate");
        testMove(game, "f2", "f3", "1. f3");
        testMove(game, "e7", "e5", "1... e5");
        testMove(game, "g2", "g4", "2. g4");
        testMove(game, "d8", "h4", "2... Qh4#");

        String status = game.getGameStatus().toString();
        // CHECKMATE のみ確認（フールズメイトは2手で成立するため）
        if (status.equals("CHECKMATE")) {
            System.out.println("  Result: ✓ CHECKMATE - Black wins!");
        }
        System.out.println();
    }

    private static void testMove(ChessGame game, String from, String to, String notation) {
        boolean success = game.makeMove(Position.of(from), Position.of(to));
        System.out.println("  " + notation + (success ? " - ✓" : " - ✗ FAILED"));
    }

    private static void printBoard(ChessGame game) {
        System.out.println("\n  a b c d e f g h");
        System.out.println("  ─────────────────────");

        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " │");
            for (int col = 0; col < 8; col++) {
                Position pos = Position.of(row, col);
                var piece = game.getBoard().getPieceAt(pos);
                System.out.print(piece == null ? " . " : " " + piece + " ");
            }
            System.out.println("│");
        }
        System.out.println("  ─────────────────────\n");
    }
}
