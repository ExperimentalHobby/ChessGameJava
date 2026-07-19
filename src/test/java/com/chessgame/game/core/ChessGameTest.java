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

package com.chessgame.game.core;

import com.chessgame.model.Color;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.gamestate.model.TimeControl;
import com.chessgame.gamestate.model.TimeControlPreset;
import com.chessgame.board.model.Board;
import com.chessgame.board.model.Position;
import com.chessgame.move.model.Move;
import com.chessgame.piece.model.PieceType;
import com.chessgame.game.observer.GameObserver;
import com.chessgame.game.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class ChessGameTest {
    private ChessGame game;

    @BeforeEach
    public void setUp() {
        game = ChessGame.createTwoPlayerGame("White", "Black");
        game.startNewGame();
    }

    @Test
    public void testGameInitialization() {
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.IN_PROGRESS);
        assertThat(game.isGameOver()).isFalse();
    }

    @Test
    public void testGetAllAvailableMovesReturnsAllOwnLegalMoves() {
        // 初期局面の白は合法手20（ポーン2種×8 + ナイト2種×2）
        assertThat(game.getAllAvailableMoves()).hasSize(20);
    }

    // ===================== FEN =====================

    @Test
    public void testToFenOnFreshGame() {
        assertThat(game.toFen()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    @Test
    public void testToFenAfterAMove() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.toFen()).isEqualTo("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
    }

    @Test
    public void testFromFenToFenRoundTrip() {
        String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
        ChessGame loaded = ChessGame.fromFen(fen,
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));

        assertThat(loaded.toFen()).isEqualTo(fen);
    }

    @Test
    public void testFromFenRespectsLimitedCastlingRights() {
        // 白のキングサイドのみ権利あり
        String fen = "r3k2r/8/8/8/8/8/8/R3K2R w K - 0 1";
        ChessGame loaded = ChessGame.fromFen(fen,
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));

        assertThat(loaded.hasCastlingRight(Color.WHITE, true)).isTrue();
        assertThat(loaded.hasCastlingRight(Color.WHITE, false)).isFalse();
    }

    @Test
    public void testFromFenComputesCheckStatusImmediately() {
        // 黒ルークがe1のキングに王手をかけている局面。makeMove を呼ばずとも
        // 読み込み直後にCHECKであることが分かるはず
        String fen = "4r3/8/8/8/8/8/8/R3K2R w KQ - 0 1";
        ChessGame loaded = ChessGame.fromFen(fen,
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));

        assertThat(loaded.getGameStatus()).isEqualTo(GameState.GameStatus.CHECK);
    }

    @Test
    public void testFromFenComputesCheckmateStatusImmediately() {
        // 白ルークによるバックランクメイト。黒番で読み込んだ直後にCHECKMATEであるはず
        String fen = "R5k1/5ppp/8/8/8/8/8/4K3 b - - 0 1";
        ChessGame loaded = ChessGame.fromFen(fen,
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));

        assertThat(loaded.getGameStatus()).isEqualTo(GameState.GameStatus.CHECKMATE);
        assertThat(loaded.isGameOver()).isTrue();
    }

    @Test
    public void testFromFenThenMoveWorksNormally() {
        String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
        ChessGame loaded = ChessGame.fromFen(fen,
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));

        assertThat(loaded.makeMove(Position.of("e1"), Position.of("g1"))).isTrue(); // キングサイドキャスリング
        assertThat(loaded.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
    }

    // ===================== PGN =====================

    @Test
    public void testToPgnContainsExpectedMovetext() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("e7"), Position.of("e5"))).isTrue();
        assertThat(game.makeMove(Position.of("g1"), Position.of("f3"))).isTrue();
        assertThat(game.makeMove(Position.of("b8"), Position.of("c6"))).isTrue();

        String pgn = game.toPgn();

        assertThat(pgn).contains("1. e4 e5 2. Nf3 Nc6");
        assertThat(pgn).contains("[White \"White\"]");
        assertThat(pgn).contains("[Black \"Black\"]");
        assertThat(pgn).contains("[Result \"*\"]");
    }

    @Test
    public void testToPgnResultTagForWhiteAndBlackTimeout() {
        long[] fakeNow = {1_000_000L};
        ChessGame whiteTimedOut = new ChessGame(
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"),
            new TimeControl(180_000L, 0L),
            () -> fakeNow[0]);
        fakeNow[0] += 200_000L;
        assertThat(whiteTimedOut.checkTimeout()).isTrue();
        assertThat(whiteTimedOut.toPgn()).contains("[Result \"0-1\"]");

        long[] fakeNow2 = {1_000_000L};
        ChessGame blackTimedOut = new ChessGame(
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"),
            new TimeControl(180_000L, 0L),
            () -> fakeNow2[0]);
        assertThat(blackTimedOut.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        fakeNow2[0] += 200_000L;
        assertThat(blackTimedOut.checkTimeout()).isTrue();
        assertThat(blackTimedOut.toPgn()).contains("[Result \"1-0\"]");
    }

    @Test
    public void testFromPgnReproducesFinalPosition() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("e7"), Position.of("e5"))).isTrue();
        assertThat(game.makeMove(Position.of("g1"), Position.of("f3"))).isTrue();
        assertThat(game.makeMove(Position.of("b8"), Position.of("c6"))).isTrue();
        String pgn = game.toPgn();

        ChessGame reloaded = ChessGame.fromPgn(pgn,
            Player.human(Color.WHITE, "W2"), Player.human(Color.BLACK, "B2"));

        assertThat(reloaded.toFen()).isEqualTo(game.toFen());
    }

    @Test
    public void testFromFenToPgnToFenRoundTrip() {
        String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
        ChessGame loaded = ChessGame.fromFen(fen,
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));
        assertThat(loaded.makeMove(Position.of("e1"), Position.of("g1"))).isTrue(); // O-O
        assertThat(loaded.makeMove(Position.of("e8"), Position.of("g8"))).isTrue(); // O-O

        String pgn = loaded.toPgn();
        ChessGame reloaded = ChessGame.fromPgn(pgn,
            Player.human(Color.WHITE, "W3"), Player.human(Color.BLACK, "B3"));

        assertThat(reloaded.toFen()).isEqualTo(loaded.toFen());
    }

    @Test
    public void testFromPgnSkipsComments() {
        String pgn = "1. e4 {good move} e5 2. Nf3 Nc6";

        ChessGame reloaded = ChessGame.fromPgn(pgn,
            Player.human(Color.WHITE, "W4"), Player.human(Color.BLACK, "B4"));

        assertThat(reloaded.toFen())
            .isEqualTo("r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3");
    }

    @Test
    public void testFromPgnSkipsNag() {
        String pgn = "1. e4 $1 e5 2. Nf3 Nc6";

        ChessGame reloaded = ChessGame.fromPgn(pgn,
            Player.human(Color.WHITE, "W6"), Player.human(Color.BLACK, "B6"));

        assertThat(reloaded.toFen())
            .isEqualTo("r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3");
    }

    @Test
    public void testFromPgnSkipsVariations() {
        String pgn = "1. e4 e5 (1... c5 2. Nf3 d6) 2. Nf3 Nc6";

        ChessGame reloaded = ChessGame.fromPgn(pgn,
            Player.human(Color.WHITE, "W5"), Player.human(Color.BLACK, "B5"));

        assertThat(reloaded.toFen())
            .isEqualTo("r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3");
    }

    @Test
    public void testFromPgnSkipsCommentsVariationsAndNagTogether() {
        String pgn = "1. e4 $1 {good move} e5 (1... c5 2. Nf3 (2. Nc3 d6) d6) 2. Nf3 Nc6";

        ChessGame reloaded = ChessGame.fromPgn(pgn,
            Player.human(Color.WHITE, "W7"), Player.human(Color.BLACK, "B7"));

        assertThat(reloaded.toFen())
            .isEqualTo("r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3");
    }

    // ===================== アンパッサン（既存バグ修正の回帰テスト） =====================

    @Test
    public void testEnPassantCaptureIsAvailableImmediatelyAfterTwoSquarePawnMove() {
        // 1. e4 a6 2. e5 d5 の直後、白は exd6（アンパッサン）が指せるはず
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("e5"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();

        assertThat(game.getEnPassantTarget()).isEqualTo(Position.of("d6"));
        assertThat(game.getAvailableMoves(Position.of("e5")))
            .anyMatch(m -> m.getTo().equals(Position.of("d6")) && m.isEnPassant());
    }

    @Test
    public void testEnPassantTargetExpiresAfterOneMove() {
        // アンパッサン対象は1手限り。直後にそれを使わず別の手を指したら消える
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("e5"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("b1"), Position.of("c3"))).isTrue();

        assertThat(game.getEnPassantTarget()).isNull();
    }

    @Test
    public void testWhitePawnMove() {
        boolean moved = game.makeMove(Position.of("e2"), Position.of("e4"));
        assertThat(moved).isTrue();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
    }

    @Test
    public void testInvalidMove() {
        boolean moved = game.makeMove(Position.of("e4"), Position.of("e5"));
        assertThat(moved).isFalse();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    public void testSimpleGame() {
        // 1. e4
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        // 1... e5
        assertThat(game.makeMove(Position.of("e7"), Position.of("e5"))).isTrue();
        // 2. Nf3
        assertThat(game.makeMove(Position.of("g1"), Position.of("f3"))).isTrue();
        // 2... Nc6
        assertThat(game.makeMove(Position.of("b8"), Position.of("c6"))).isTrue();

        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.IN_PROGRESS);
        assertThat(game.isGameOver()).isFalse();
    }

    @Test
    public void testGetAvailableMovesDelegatesToMoveValidator() {
        // 駒種ごとの詳細な合法手生成ロジックはMoveValidatorTestで検証済み。
        // ここではMoveValidatorへの委譲が行われることのみを確認する
        var moves = game.getAvailableMoves(Position.of("e2"));

        assertThat(moves).isNotEmpty();
        assertThat(moves).allMatch(m -> m.getFrom().equals(Position.of("e2")));
    }

    @Test
    public void testPlayerSwitching() {
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
        game.makeMove(Position.of("e2"), Position.of("e4"));
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
        game.makeMove(Position.of("e7"), Position.of("e5"));
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    public void testGameObserver() {
        TestGameObserver observer = new TestGameObserver();
        game.addObserver(observer);

        game.makeMove(Position.of("e2"), Position.of("e4"));

        assertThat(observer.moveMadeCount).isEqualTo(1);
        assertThat(observer.boardChangedCount).isEqualTo(1);
    }

    @Test
    public void testPawnAutoPromotesToQueen() {
        // 昇格先を指定しない makeMove(from, to) はクイーンへ自動昇格する。
        // 連続した捕獲でポーンを a8 まで進めて昇格させる。
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("d5"))).isTrue(); // exd5
        assertThat(game.makeMove(Position.of("c7"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("d5"), Position.of("c6"))).isTrue(); // dxc6
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("c6"), Position.of("b7"))).isTrue(); // cxb7
        assertThat(game.makeMove(Position.of("a6"), Position.of("a5"))).isTrue();
        assertThat(game.makeMove(Position.of("b7"), Position.of("a8"))).isTrue(); // bxa8=Q

        var promoted = game.getBoard().getPieceAt(Position.of("a8"));
        assertThat(promoted).isNotNull();
        assertThat(promoted.getType()).isEqualTo(PieceType.QUEEN);
        assertThat(promoted.getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    public void testPawnPromotesToSpecifiedPiece() {
        // 昇格先を明示した場合はその駒種に昇格する。
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("c7"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("d5"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("c6"), Position.of("b7"))).isTrue();
        assertThat(game.makeMove(Position.of("a6"), Position.of("a5"))).isTrue();
        assertThat(game.makeMove(Position.of("b7"), Position.of("a8"), PieceType.KNIGHT)).isTrue();

        var promoted = game.getBoard().getPieceAt(Position.of("a8"));
        assertThat(promoted).isNotNull();
        assertThat(promoted.getType()).isEqualTo(PieceType.KNIGHT);
    }

    @Test
    public void testMakeMoveWithMoveObjectAppliesItsOwnPromotionChoice() {
        // makeMove(Move) は Move 自身が保持する昇格先をそのまま適用する（呼び出し側でクイーンに落ちないこと）。
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("c7"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("d5"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("c6"), Position.of("b7"))).isTrue();
        assertThat(game.makeMove(Position.of("a6"), Position.of("a5"))).isTrue();

        Move knightPromotion = game.getAvailableMoves(Position.of("b7")).stream()
            .filter(m -> m.getTo().equals(Position.of("a8")) && m.getPromotionPiece() == PieceType.KNIGHT)
            .findFirst()
            .orElseThrow();

        assertThat(game.makeMove(knightPromotion)).isTrue();

        var promoted = game.getBoard().getPieceAt(Position.of("a8"));
        assertThat(promoted).isNotNull();
        assertThat(promoted.getType()).isEqualTo(PieceType.KNIGHT);
    }

    @Test
    public void testUndoAfterBothPlayersMoved() {
        // White と Black が1手ずつ指した後 undo すると Black の番に戻る
        game.makeMove(Position.of("e2"), Position.of("e4"));
        game.makeMove(Position.of("e7"), Position.of("e5"));
        game.undo();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
    }

    @Test
    public void testUndoTwiceRestoresWhiteTurn() {
        // 2手進めて2回 undo すると White の番に戻る
        game.makeMove(Position.of("e2"), Position.of("e4"));
        game.makeMove(Position.of("e7"), Position.of("e5"));
        game.undo();
        game.undo();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    public void testUndoAfterOnlyWhiteMoved() {
        // White だけが指した後 undo すると White の番に戻る
        game.makeMove(Position.of("e2"), Position.of("e4"));
        game.undo();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }

    @Test
    public void testUndoReturnsFalseWhenHistoryIsEmpty() {
        // 対局開始直後（1手も指していない）に undo すると false が返り、状態も変化しない
        assertThat(game.undo()).isFalse();
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.WHITE);
        assertThat(game.getMoveHistory().isEmpty()).isTrue();
    }

    @Test
    public void testUndoAfterFromFenRestoresOriginalFen() {
        // 黒番開始・キャスリング権制限あり・ハーフムーブクロック非ゼロの非標準局面
        String fen = "r3k2r/8/8/8/8/8/8/R3K2R b Kq - 3 5";
        ChessGame loaded = ChessGame.fromFen(fen,
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));

        assertThat(loaded.makeMove(Position.of("a8"), Position.of("b8"))).isTrue();
        assertThat(loaded.undo()).isTrue();

        // undo は標準初期配置ではなく、fromFen で読み込んだ元の局面に戻るべき
        assertThat(loaded.toFen()).isEqualTo(fen);
    }

    @Test
    public void testUndoTwiceAfterFromFenRestoresBlackTurn() {
        // 黒番開始FENで2手指して2回 undo すると Black の番に戻る（White 直書きが直っていることの確認）
        String fen = "r3k2r/8/8/8/8/8/8/R3K2R b Kq - 3 5";
        ChessGame loaded = ChessGame.fromFen(fen,
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));

        assertThat(loaded.makeMove(Position.of("a8"), Position.of("b8"))).isTrue();
        assertThat(loaded.makeMove(Position.of("a1"), Position.of("b1"))).isTrue();
        loaded.undo();
        loaded.undo();

        assertThat(loaded.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);
        assertThat(loaded.getMoveHistory().isEmpty()).isTrue();
        assertThat(loaded.toFen()).isEqualTo(fen);
    }

    @Test
    public void testResign() {
        assertThat(game.isGameOver()).isFalse();
        game.resign(Color.WHITE);
        assertThat(game.isGameOver()).isTrue();
        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.WHITE_RESIGNED);
    }

    // ===================== 投了する側の色の判定（Issue #70） =====================

    @Test
    public void testGetHumanColorReturnsWhiteWhenWhiteIsHumanAndBlackIsAi() {
        ChessGame aiGame = new ChessGame(
            Player.human(Color.WHITE, "You"), new Player(Color.BLACK, "AI", false));

        assertThat(aiGame.getHumanColor()).isEqualTo(Color.WHITE);
    }

    @Test
    public void testGetHumanColorReturnsBlackWhenBlackIsHumanAndWhiteIsAi() {
        ChessGame aiGame = new ChessGame(
            new Player(Color.WHITE, "AI", false), Player.human(Color.BLACK, "You"));

        assertThat(aiGame.getHumanColor()).isEqualTo(Color.BLACK);
    }

    @Test
    public void testGetHumanColorReturnsNullWhenBothPlayersAreHuman() {
        assertThat(game.getHumanColor()).isNull();
    }

    @Test
    public void testGetResigningColorReturnsHumanColorEvenDuringAiTurn() {
        // Issue #70: AI（黒）の手番中でも Resign は人間（白）を投了させるべき
        ChessGame aiGame = new ChessGame(
            Player.human(Color.WHITE, "You"), new Player(Color.BLACK, "AI", false));
        aiGame.startNewGame();
        assertThat(aiGame.makeMove(Position.of("e2"), Position.of("e4"))).isTrue(); // 手番をAI(黒)に渡す
        assertThat(aiGame.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);

        assertThat(aiGame.getResigningColor()).isEqualTo(Color.WHITE);
    }

    @Test
    public void testGetResigningColorReturnsCurrentPlayerColorInTwoPlayerGame() {
        // 2人対戦（両者human）では従来通り現在の手番側を投了させる
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue(); // 手番をBlackに渡す
        assertThat(game.getCurrentPlayer().getColor()).isEqualTo(Color.BLACK);

        assertThat(game.getResigningColor()).isEqualTo(Color.BLACK);
    }

    // ===================== 引き分けルール =====================

    @Test
    public void testHalfmoveClockResetsOnPawnMove() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.getHalfmoveClock()).isEqualTo(0);
    }

    @Test
    public void testHalfmoveClockResetsOnCapture() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("d5"))).isTrue(); // exd5 (capture)
        assertThat(game.getHalfmoveClock()).isEqualTo(0);
    }

    @Test
    public void testHalfmoveClockIncrementsOnQuietMove() {
        assertThat(game.makeMove(Position.of("b1"), Position.of("c3"))).isTrue(); // ナイト移動（駒取り無し）
        assertThat(game.getHalfmoveClock()).isEqualTo(1);
        assertThat(game.makeMove(Position.of("b8"), Position.of("c6"))).isTrue();
        assertThat(game.getHalfmoveClock()).isEqualTo(2);
    }

    @Test
    public void testThreefoldRepetitionDraw() {
        // ナイトの往復を2往復（計8手）させて同一局面を3回出現させる
        for (int i = 0; i < 2; i++) {
            assertThat(game.makeMove(Position.of("b1"), Position.of("c3"))).isTrue();
            assertThat(game.makeMove(Position.of("b8"), Position.of("c6"))).isTrue();
            assertThat(game.makeMove(Position.of("c3"), Position.of("b1"))).isTrue();
            assertThat(game.makeMove(Position.of("c6"), Position.of("b8"))).isTrue();
        }
        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.THREEFOLD_REPETITION);
        assertThat(game.isGameOver()).isTrue();
    }

    @Test
    public void testInsufficientMaterialDraw() {
        // 盤面を両キングのみに操作してから1手指し、computeGameState を走らせる
        Board board = game.getBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = Position.of(row, col);
                var piece = board.getPieceAt(pos);
                if (piece != null && piece.getType() != PieceType.KING) {
                    board.removePiece(pos);
                }
            }
        }

        assertThat(game.makeMove(Position.of("e1"), Position.of("e2"))).isTrue();
        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.INSUFFICIENT_MATERIAL);
        assertThat(game.isGameOver()).isTrue();
    }

    @Test
    public void testNoMoveAllowedAfterDraw() {
        game.resign(Color.WHITE); // 任意の終局状態を作る（投了）
        assertThat(game.makeMove(Position.of("e7"), Position.of("e5"))).isFalse();
    }

    @Test
    public void testUndoRestoresHalfmoveClock() {
        assertThat(game.makeMove(Position.of("b1"), Position.of("c3"))).isTrue(); // clock=1
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue(); // clock=0（ポーン移動）
        assertThat(game.getHalfmoveClock()).isEqualTo(0);

        game.undo(); // d7d5 を取り消す -> clock は c3 の直後の状態（1）に戻るはず
        assertThat(game.getHalfmoveClock()).isEqualTo(1);
    }

    // ===================== 特殊手の実行結果・undo(結合テスト) =====================

    @Test
    public void testKingsideCastlingMovesKingAndRook() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("e7"), Position.of("e5"))).isTrue();
        assertThat(game.makeMove(Position.of("g1"), Position.of("f3"))).isTrue();
        assertThat(game.makeMove(Position.of("b8"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("f1"), Position.of("c4"))).isTrue();
        assertThat(game.makeMove(Position.of("g8"), Position.of("f6"))).isTrue();

        assertThat(game.makeMove(Position.of("e1"), Position.of("g1"))).isTrue();

        assertThat(game.getBoard().getPieceAt(Position.of("g1")).getType()).isEqualTo(PieceType.KING);
        assertThat(game.getBoard().getPieceAt(Position.of("f1")).getType()).isEqualTo(PieceType.ROOK);
        assertThat(game.getBoard().getPieceAt(Position.of("e1"))).isNull();
        assertThat(game.getBoard().getPieceAt(Position.of("h1"))).isNull();
    }

    @Test
    public void testQueensideCastlingMovesKingAndRook() {
        assertThat(game.makeMove(Position.of("d2"), Position.of("d4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("b1"), Position.of("c3"))).isTrue();
        assertThat(game.makeMove(Position.of("b8"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("c1"), Position.of("f4"))).isTrue();
        assertThat(game.makeMove(Position.of("c8"), Position.of("f5"))).isTrue();
        assertThat(game.makeMove(Position.of("d1"), Position.of("d3"))).isTrue();
        assertThat(game.makeMove(Position.of("d8"), Position.of("d7"))).isTrue();

        assertThat(game.makeMove(Position.of("e1"), Position.of("c1"))).isTrue();

        assertThat(game.getBoard().getPieceAt(Position.of("c1")).getType()).isEqualTo(PieceType.KING);
        assertThat(game.getBoard().getPieceAt(Position.of("d1")).getType()).isEqualTo(PieceType.ROOK);
        assertThat(game.getBoard().getPieceAt(Position.of("e1"))).isNull();
        assertThat(game.getBoard().getPieceAt(Position.of("a1"))).isNull();
    }

    @Test
    public void testEnPassantCaptureRemovesCapturedPawn() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("e5"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue(); // 2マス進み、e5にアンパッサン対象を提供

        assertThat(game.makeMove(Position.of("e5"), Position.of("d6"))).isTrue(); // exd6 en passant

        assertThat(game.getBoard().getPieceAt(Position.of("d6")).getType()).isEqualTo(PieceType.PAWN);
        assertThat(game.getBoard().getPieceAt(Position.of("d5"))).isNull(); // 取られた黒ポーンが除去されている
    }

    @Test
    public void testUndoAfterCastlingRestoresKingAndRookPosition() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("e7"), Position.of("e5"))).isTrue();
        assertThat(game.makeMove(Position.of("g1"), Position.of("f3"))).isTrue();
        assertThat(game.makeMove(Position.of("b8"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("f1"), Position.of("c4"))).isTrue();
        assertThat(game.makeMove(Position.of("g8"), Position.of("f6"))).isTrue();
        assertThat(game.makeMove(Position.of("e1"), Position.of("g1"))).isTrue();

        game.undo(); // キャスリングを取り消す

        assertThat(game.getBoard().getPieceAt(Position.of("e1")).getType()).isEqualTo(PieceType.KING);
        assertThat(game.getBoard().getPieceAt(Position.of("h1")).getType()).isEqualTo(PieceType.ROOK);
        assertThat(game.getBoard().getPieceAt(Position.of("f1"))).isNull();
        assertThat(game.getBoard().getPieceAt(Position.of("g1"))).isNull();
        // moveCount も復元され、再度キャスリング権があること
        assertThat(game.hasCastlingRight(Color.WHITE, true)).isTrue();
    }

    @Test
    public void testUndoAfterEnPassantRestoresCapturedPawn() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("e5"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e5"), Position.of("d6"))).isTrue(); // exd6 en passant

        game.undo();

        var restoredPawn = game.getBoard().getPieceAt(Position.of("d5"));
        assertThat(restoredPawn).isNotNull();
        assertThat(restoredPawn.getType()).isEqualTo(PieceType.PAWN);
        assertThat(restoredPawn.getColor()).isEqualTo(Color.BLACK);
        assertThat(game.getBoard().getPieceAt(Position.of("e5")).getType()).isEqualTo(PieceType.PAWN); // 白ポーンもe5に戻る
        assertThat(game.getBoard().getPieceAt(Position.of("d6"))).isNull();
    }

    @Test
    public void testUndoAfterPromotionRestoresPawn() {
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("c7"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("d5"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("c6"), Position.of("b7"))).isTrue();
        assertThat(game.makeMove(Position.of("a6"), Position.of("a5"))).isTrue();
        assertThat(game.makeMove(Position.of("b7"), Position.of("a8"))).isTrue(); // bxa8=Q

        game.undo();

        var restoredPawn = game.getBoard().getPieceAt(Position.of("b7"));
        assertThat(restoredPawn).isNotNull();
        assertThat(restoredPawn.getType()).isEqualTo(PieceType.PAWN);
        assertThat(restoredPawn.getColor()).isEqualTo(Color.WHITE);
        assertThat(game.getBoard().getPieceAt(Position.of("a8")).getType()).isEqualTo(PieceType.ROOK); // 取られた黒ルークが復元
    }

    @Test
    public void testCastlingRejectedWhenPassingSquareIsAttacked() {
        // 黒の a6 ビショップが f1(キングの経由マス)を直接睨む局面を作る。
        // キング・ルークは未移動、f1/g1 は空、キング自身も王手されていないが、
        // 経由マス f1 が攻撃されているためキャスリングは不可のはず。
        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("b7"), Position.of("b6"))).isTrue();
        assertThat(game.makeMove(Position.of("g2"), Position.of("g3"))).isTrue();
        assertThat(game.makeMove(Position.of("c8"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("g1"), Position.of("f3"))).isTrue();
        assertThat(game.makeMove(Position.of("h7"), Position.of("h6"))).isTrue();
        assertThat(game.makeMove(Position.of("f1"), Position.of("g2"))).isTrue();
        assertThat(game.makeMove(Position.of("h6"), Position.of("h5"))).isTrue();

        assertThat(game.getAvailableMoves(Position.of("e1")))
            .noneMatch(m -> m.getTo().equals(Position.of("g1")));
        assertThat(game.makeMove(Position.of("e1"), Position.of("g1"))).isFalse();
    }

    // ===================== GameObserverコールバック(結合テスト) =====================

    @Test
    public void testOnMoveMadeNotifiesCastlingMove() {
        TestGameObserver observer = new TestGameObserver();
        game.addObserver(observer);

        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("e7"), Position.of("e5"))).isTrue();
        assertThat(game.makeMove(Position.of("g1"), Position.of("f3"))).isTrue();
        assertThat(game.makeMove(Position.of("b8"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("f1"), Position.of("c4"))).isTrue();
        assertThat(game.makeMove(Position.of("g8"), Position.of("f6"))).isTrue();

        assertThat(game.makeMove(Position.of("e1"), Position.of("g1"))).isTrue(); // キャスリング

        assertThat(observer.lastMove).isNotNull();
        assertThat(observer.lastMove.isCastling()).isTrue();
        assertThat(observer.lastMove.getFrom()).isEqualTo(Position.of("e1"));
        assertThat(observer.lastMove.getTo()).isEqualTo(Position.of("g1"));
    }

    @Test
    public void testOnMoveMadeNotifiesEnPassantMove() {
        TestGameObserver observer = new TestGameObserver();
        game.addObserver(observer);

        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("e5"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();

        assertThat(game.makeMove(Position.of("e5"), Position.of("d6"))).isTrue(); // exd6 en passant

        assertThat(observer.lastMove).isNotNull();
        assertThat(observer.lastMove.isEnPassant()).isTrue();
        assertThat(observer.lastMove.getFrom()).isEqualTo(Position.of("e5"));
        assertThat(observer.lastMove.getTo()).isEqualTo(Position.of("d6"));
    }

    @Test
    public void testOnMoveMadeNotifiesPromotionMove() {
        TestGameObserver observer = new TestGameObserver();
        game.addObserver(observer);

        assertThat(game.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();
        assertThat(game.makeMove(Position.of("d7"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("e4"), Position.of("d5"))).isTrue();
        assertThat(game.makeMove(Position.of("c7"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("d5"), Position.of("c6"))).isTrue();
        assertThat(game.makeMove(Position.of("a7"), Position.of("a6"))).isTrue();
        assertThat(game.makeMove(Position.of("c6"), Position.of("b7"))).isTrue();
        assertThat(game.makeMove(Position.of("a6"), Position.of("a5"))).isTrue();

        assertThat(game.makeMove(Position.of("b7"), Position.of("a8"), PieceType.KNIGHT)).isTrue(); // bxa8=N

        assertThat(observer.lastMove).isNotNull();
        assertThat(observer.lastMove.isPromotion()).isTrue();
        assertThat(observer.lastMove.getPromotionPiece()).isEqualTo(PieceType.KNIGHT);
        assertThat(observer.lastMove.getFrom()).isEqualTo(Position.of("b7"));
        assertThat(observer.lastMove.getTo()).isEqualTo(Position.of("a8"));
    }

    @Test
    public void testOnCheckDetectedCalledWithAttackedKingColor() {
        ChessGame fenGame = ChessGame.fromFen("4k3/8/8/8/8/8/4Q3/4K3 w - - 0 1",
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));
        TestGameObserver observer = new TestGameObserver();
        fenGame.addObserver(observer);

        assertThat(fenGame.makeMove(Position.of("e2"), Position.of("e7"))).isTrue(); // Qe7+

        assertThat(fenGame.getGameStatus()).isEqualTo(GameState.GameStatus.CHECK);
        assertThat(observer.checkDetectedCount).isEqualTo(1);
        assertThat(observer.lastCheckedKingColor).isEqualTo(Color.BLACK);
        assertThat(observer.lastGameStatus).isEqualTo(GameState.GameStatus.CHECK);
    }

    @Test
    public void testOnGameOverCalledWithWinnerOnCheckmate() {
        TestGameObserver observer = new TestGameObserver();
        game.addObserver(observer);

        assertThat(game.makeMove(Position.of("f2"), Position.of("f3"))).isTrue();
        assertThat(game.makeMove(Position.of("e7"), Position.of("e5"))).isTrue();
        assertThat(game.makeMove(Position.of("g2"), Position.of("g4"))).isTrue();
        assertThat(game.makeMove(Position.of("d8"), Position.of("h4"))).isTrue(); // Qh4# フールズメイト

        assertThat(game.getGameStatus()).isEqualTo(GameState.GameStatus.CHECKMATE);
        assertThat(observer.gameOverCount).isEqualTo(1);
        assertThat(observer.lastGameOverWinner).isEqualTo(Color.BLACK);
    }

    @Test
    public void testOnGameOverCalledWithNullWinnerOnStalemate() {
        ChessGame fenGame = ChessGame.fromFen("7k/5K2/8/7Q/8/8/8/8 w - - 0 1",
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));
        TestGameObserver observer = new TestGameObserver();
        fenGame.addObserver(observer);

        assertThat(fenGame.makeMove(Position.of("h5"), Position.of("g6"))).isTrue(); // Qg6 ステールメイト

        assertThat(fenGame.getGameStatus()).isEqualTo(GameState.GameStatus.STALEMATE);
        assertThat(observer.gameOverCount).isEqualTo(1);
        assertThat(observer.lastGameOverWinner).isNull();
    }

    @Test
    public void testOnGameOverCalledWithOpponentColorOnResign() {
        TestGameObserver observer = new TestGameObserver();
        game.addObserver(observer);

        assertThat(game.resign(Color.WHITE)).isTrue();

        assertThat(observer.gameOverCount).isEqualTo(1);
        assertThat(observer.lastGameOverWinner).isEqualTo(Color.BLACK);
    }

    // ===================== 複合シナリオ(チェック・キャスリング・アンパッサン・昇格の結合テスト) =====================

    @Test
    public void testCastlingRejectedWhileInCheck() {
        // 黒ルークがe1のキングに王手をかけている。パスするマス自体は攻撃されていなくても
        // 王手中はキャスリングできない
        ChessGame fenGame = ChessGame.fromFen("4r3/8/8/8/8/8/8/R3K2R w KQ - 0 1",
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));

        assertThat(fenGame.getGameStatus()).isEqualTo(GameState.GameStatus.CHECK);
        assertThat(fenGame.getAvailableMoves(Position.of("e1")))
            .noneMatch(m -> m.isCastling());
        assertThat(fenGame.makeMove(Position.of("e1"), Position.of("g1"))).isFalse();
    }

    @Test
    public void testEnPassantCaptureResolvesCheck() {
        // 黒ポーンd5がe4の白キングに王手をかけている。白はe5ポーンでのアンパッサン捕獲
        // (exd6 e.p.)により王手を解消できる
        ChessGame fenGame = ChessGame.fromFen("k7/8/8/3pP3/4K3/8/8/8 w - d6 0 1",
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));

        assertThat(fenGame.getGameStatus()).isEqualTo(GameState.GameStatus.CHECK);

        assertThat(fenGame.makeMove(Position.of("e5"), Position.of("d6"))).isTrue();

        assertThat(fenGame.getGameStatus()).isEqualTo(GameState.GameStatus.IN_PROGRESS);
        assertThat(fenGame.getBoard().getPieceAt(Position.of("d5"))).isNull();
    }

    @Test
    public void testPromotionDeliversCheckmateAndNotifiesGameOver() {
        // 黒キングh8はg7/h7/f7の自駒に囲まれている。白ポーンb7がb8でクイーンに昇格すると
        // 8段目に遮る駒がなくバックランクメイトになる
        ChessGame fenGame = ChessGame.fromFen("7k/1P3ppp/8/8/8/8/8/4K3 w - - 0 1",
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"));
        TestGameObserver observer = new TestGameObserver();
        fenGame.addObserver(observer);

        assertThat(fenGame.makeMove(Position.of("b7"), Position.of("b8"))).isTrue(); // b8=Q

        assertThat(fenGame.getGameStatus()).isEqualTo(GameState.GameStatus.CHECKMATE);
        assertThat(fenGame.isGameOver()).isTrue();
        assertThat(observer.gameOverCount).isEqualTo(1);
        assertThat(observer.lastGameOverWinner).isEqualTo(Color.WHITE);
        assertThat(observer.lastMove.isPromotion()).isTrue();
    }

    // ===================== 持ち時間管理(Issue #34) =====================

    @Test
    public void testDefaultFactoryHasNoTimeControl() {
        assertThat(game.hasTimeControl()).isFalse();
    }

    @Test
    public void testTimeControlPresetGivesBothPlayersInitialRemainingTime() {
        ChessGame timedGame = ChessGame.createTwoPlayerGame("White", "Black", TimeControlPreset.BLITZ);

        assertThat(timedGame.hasTimeControl()).isTrue();
        assertThat(timedGame.getRemainingMillis(Color.WHITE)).isEqualTo(3 * 60_000L);
        assertThat(timedGame.getRemainingMillis(Color.BLACK)).isEqualTo(3 * 60_000L);
    }

    @Test
    public void testMakeMoveConsumesElapsedTimeFromMoverClock() {
        // increment=0のTimeControlを使い、加算(increment)の影響を受けずに消費のみを検証する
        long[] fakeNow = {1_000_000L};
        ChessGame timedGame = new ChessGame(
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"),
            new TimeControl(180_000L, 0L),
            () -> fakeNow[0]);

        fakeNow[0] += 5_000L; // 白が5秒思考してから着手

        assertThat(timedGame.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();

        assertThat(timedGame.getRemainingMillis(Color.WHITE)).isEqualTo(180_000L - 5_000L);
        assertThat(timedGame.getRemainingMillis(Color.BLACK)).isEqualTo(180_000L);
    }

    @Test
    public void testMakeMoveAddsIncrementAfterSuccessfulMove() {
        long[] fakeNow = {1_000_000L};
        ChessGame timedGame = new ChessGame(
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"),
            TimeControlPreset.BLITZ.toTimeControl(), // 2秒加算
            () -> fakeNow[0]);

        fakeNow[0] += 5_000L;

        assertThat(timedGame.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();

        assertThat(timedGame.getRemainingMillis(Color.WHITE)).isEqualTo(3 * 60_000L - 5_000L + 2_000L);
    }

    @Test
    public void testMoveRejectedAndWhiteTimeoutDeclaredWhenTimeExpired() {
        long[] fakeNow = {1_000_000L};
        ChessGame timedGame = new ChessGame(
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"),
            new TimeControl(180_000L, 0L),
            () -> fakeNow[0]);

        fakeNow[0] += 200_000L; // 残り時間(180秒)を超えて経過

        assertThat(timedGame.makeMove(Position.of("e2"), Position.of("e4"))).isFalse();
        assertThat(timedGame.getGameStatus()).isEqualTo(GameState.GameStatus.WHITE_TIMEOUT);
        assertThat(timedGame.isGameOver()).isTrue();
    }

    @Test
    public void testCheckTimeoutDeclaresTimeoutWithoutMakeMoveCall() {
        // makeMove を一度も呼ばなくても、UIのタイマーがcheckTimeout()をポーリングするだけで
        // 時間切れ（指し手を放置したまま思考時間を使い切るケース）を検出できることを確認する
        long[] fakeNow = {1_000_000L};
        ChessGame timedGame = new ChessGame(
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"),
            new TimeControl(180_000L, 0L),
            () -> fakeNow[0]);

        fakeNow[0] += 200_000L;

        assertThat(timedGame.checkTimeout()).isTrue();
        assertThat(timedGame.getGameStatus()).isEqualTo(GameState.GameStatus.WHITE_TIMEOUT);
    }

    @Test
    public void testCheckTimeoutReturnsFalseWhenTimeRemainsOrUnlimited() {
        long[] fakeNow = {1_000_000L};
        ChessGame timedGame = new ChessGame(
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"),
            new TimeControl(180_000L, 0L),
            () -> fakeNow[0]);

        fakeNow[0] += 5_000L; // 残り時間はまだ十分ある
        assertThat(timedGame.checkTimeout()).isFalse();
        assertThat(timedGame.getGameStatus()).isEqualTo(GameState.GameStatus.IN_PROGRESS);

        // 時間管理無しの対局では、いくら経過しても常に false
        assertThat(game.checkTimeout()).isFalse();
    }

    @Test
    public void testUndoResetsTurnStartTimeToAvoidUnfairCharge() {
        // undo()は残り時間の巻き戻しまでは行わないが（既知の制限）、計測開始時刻だけは
        // リセットする。しないと、undo判断にかかった実時間が次の一手に不当に課金されてしまう
        long[] fakeNow = {0L};
        ChessGame timedGame = new ChessGame(
            Player.human(Color.WHITE, "W"), Player.human(Color.BLACK, "B"),
            new TimeControl(180_000L, 0L),
            () -> fakeNow[0]);

        fakeNow[0] += 5_000L;
        assertThat(timedGame.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();

        fakeNow[0] += 50_000L; // undoするか迷っている間に実時間が経過する想定
        assertThat(timedGame.undo()).isTrue();

        fakeNow[0] += 3_000L;
        assertThat(timedGame.makeMove(Position.of("e2"), Position.of("e4"))).isTrue();

        // 最初の一手で消費した5秒は巻き戻らない（既知の制限）。undo判断中の50秒だけが
        // 課金されず、undo後に新たに消費した3秒のみが上乗せされることを確認する
        assertThat(timedGame.getRemainingMillis(Color.WHITE)).isEqualTo(180_000L - 5_000L - 3_000L);
    }

    // Test observer implementation
    private static class TestGameObserver implements GameObserver {
        int moveMadeCount = 0;
        int boardChangedCount = 0;
        int checkDetectedCount = 0;
        Color lastCheckedKingColor = null;
        int gameOverCount = 0;
        Color lastGameOverWinner = null;
        Move lastMove = null;
        GameState.GameStatus lastGameStatus = null;

        @Override
        public void onBoardChanged() {
            boardChangedCount++;
        }

        @Override
        public void onMoveMade(Move move) {
            moveMadeCount++;
            lastMove = move;
        }

        @Override
        public void onGameStateChanged(GameState.GameStatus newStatus) {
            lastGameStatus = newStatus;
        }

        @Override
        public void onCheckDetected(Color kingColor) {
            checkDetectedCount++;
            lastCheckedKingColor = kingColor;
        }

        @Override
        public void onGameOver(Color winner) {
            gameOverCount++;
            lastGameOverWinner = winner;
        }
    }
}
