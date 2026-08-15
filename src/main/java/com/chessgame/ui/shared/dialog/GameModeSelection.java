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

package com.chessgame.ui.shared.dialog;

import com.chessgame.game.core.ChessGame;
import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.player.Player;
import com.chessgame.gamestate.model.TimeControl;
import com.chessgame.gamestate.model.TimeControlPreset;
import com.chessgame.model.Color;

/**
 * ゲームモード選択ダイアログ・持ち時間選択ダイアログの選択結果から {@link ChessGame} を
 * 生成する（UIツールキット非依存）。Swing版・JavaFX版の両 {@code GameModeDialog} で
 * 重複していたロジックを共通化した（Issue #187）。
 *
 * <p>結果は生成した {@link ChessGame} と、それがAI対戦かどうかのフラグを保持する
 * {@link Result} として返す。従来 {@code isAIGame} という可変static状態で呼び出し側へ
 * 別途伝えていたが、戻り値に含めることでその状態を不要にしている。</p>
 */
public final class GameModeSelection {

    private GameModeSelection() {
    }

    /**
     * 選択結果から生成した {@link ChessGame} と、それがAI対戦かどうかを保持する不変の結果。
     *
     * @param game   選択結果に応じて生成されたゲーム
     * @param aiGame AI対戦であれば true
     */
    public record Result(ChessGame game, boolean aiGame) {
    }

    /**
     * ゲームモード・持ち時間の選択インデックスからゲームを生成する。
     * ダイアログ表示を伴わないため単体テストから直接検証できる。
     *
     * @param modeChoice ゲームモードの選択インデックス（0=Human vs Human, 1〜4=AI難易度）
     * @param timeChoice 持ち時間の選択インデックス（0=無制限, 1=Blitz, 2=Rapid, 3=Classical）
     * @return 選択結果
     */
    public static Result resolve(int modeChoice, int timeChoice) {
        boolean aiGame = modeChoice != 0;
        TimeControl timeControl = resolveTimeControl(timeChoice);
        ChessGame game = (modeChoice == 0)
            ? createHumanVsHumanGame(timeControl)
            : createAIGame(modeChoice, timeControl);
        return new Result(game, aiGame);
    }

    private static ChessGame createHumanVsHumanGame(TimeControl timeControl) {
        return (timeControl != null)
            ? new ChessGame(Player.human(Color.WHITE, "White"), Player.human(Color.BLACK, "Black"), timeControl)
            : ChessGame.createTwoPlayerGame("White", "Black");
    }

    /**
     * 持ち時間選択の選択肢インデックスから対応する {@link TimeControl} を返す。
     * 「無制限」（未知の値を含む）の場合は null を返す。
     *
     * @param timeChoice 持ち時間の選択インデックス
     * @return 対応する {@link TimeControl}、無制限の場合は null
     */
    private static TimeControl resolveTimeControl(int timeChoice) {
        switch (timeChoice) {
            case 1: return TimeControlPreset.BLITZ.toTimeControl();
            case 2: return TimeControlPreset.RAPID.toTimeControl();
            case 3: return TimeControlPreset.CLASSICAL.toTimeControl();
            default: return null;
        }
    }

    /**
     * AI 対戦ゲームを生成する。
     *
     * @param difficulty  AI の難易度（1=Easy, 2=Medium, 3=Hard, 4=Expert）
     * @param timeControl 持ち時間ルール。無制限なら null
     * @return AI 対戦ゲーム
     */
    private static ChessGame createAIGame(int difficulty, TimeControl timeControl) {
        Player whitePlayer = Player.human(Color.WHITE, "You");
        Player blackPlayer = new AIPlayer("AI", Color.BLACK, difficulty);
        return (timeControl != null)
            ? new ChessGame(whitePlayer, blackPlayer, timeControl)
            : new ChessGame(whitePlayer, blackPlayer);
    }
}
