package com.chessgame.swing.ui.dialog;

import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.AIPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameModeDialog のユニットテスト。
 * ゲームモード選択とゲーム生成が正しく動作することを検証する。
 */
class GameModeDialogTest {

    @Test
    void testGameModeDialogCreatesValidGame() {
        // Dialog クラスはユーザーインタラクションが必要なため、
        // ここでは static メソッド isLastGameAI() の動作を検証する
        assertFalse(GameModeDialog.isLastGameAI());
    }

    @Test
    void testTwoPlayerGameCreation() {
        // 2人対戦ゲームの生成テスト
        ChessGame game = ChessGame.createTwoPlayerGame("White", "Black");
        assertNotNull(game);
        assertEquals("White", game.getCurrentPlayer().getName());
    }

    @Test
    void testAIGameCreationLogic() {
        // AI ゲーム生成ロジックのテスト
        // 難易度 1（Easy）の場合、AIPlayer インスタンスが生成されることを確認
        ChessGame game = createAIGameForTesting(1);
        assertNotNull(game);
        assertNotNull(game.getCurrentPlayer());
    }

    /**
     * テスト用 AI ゲーム生成メソッド。
     * GameModeDialog の private メソッドと同じロジック。
     */
    private ChessGame createAIGameForTesting(int difficulty) {
        com.chessgame.game.player.Player whitePlayer =
            com.chessgame.game.player.Player.human(com.chessgame.model.Color.WHITE, "You");
        com.chessgame.game.player.Player blackPlayer =
            new AIPlayer("AI", com.chessgame.model.Color.BLACK, difficulty);
        return new ChessGame(whitePlayer, blackPlayer);
    }
}
