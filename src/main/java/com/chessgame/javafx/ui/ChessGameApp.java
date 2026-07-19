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

package com.chessgame.javafx.ui;

import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.observer.GameObserver;
import com.chessgame.model.Color;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.move.model.Move;
import com.chessgame.javafx.board.ChessBoardView;
import com.chessgame.javafx.ui.dialog.GameModeDialog;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * JavaFX チェスゲームアプリケーションのメインクラス。
 * {@link Application} を継承して JavaFX ウィンドウを構築し、
 * {@link GameObserver} を実装してゲームイベントをUIに反映する。
 */
public class ChessGameApp extends Application implements GameObserver {
    /** AI が手を指すまでの遅延（ミリ秒）。即時実行だと UI 更新が追いつかないため遅延させる。 */
    private static final int AI_MOVE_DELAY_MS = 800;
    /** 持ち時間の残り時間表示・時間切れ検出をポーリングする間隔（ミリ秒）。 */
    private static final int CLOCK_TICK_MS = 200;

    private ChessGame game;
    private ChessBoardView boardView;
    private StatusBar statusBar;
    private ControlPanel controlPanel;
    private MoveHistoryPanel moveHistoryPanel;
    private ClockPanel clockPanel;
    private boolean isAIGame = false;
    private PauseTransition aiDelay;
    private Timeline clockTimeline;
    private Task<Move> aiTask;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        game = ChessGame.createTwoPlayerGame("White Player", "Black Player");
        game.addObserver(this);

        boardView = new ChessBoardView(game);
        statusBar = new StatusBar();
        controlPanel = new ControlPanel();
        moveHistoryPanel = new MoveHistoryPanel(game);
        clockPanel = new ClockPanel(game);

        boardView.setOnMoveCallback(this::updateStatusBar);
        controlPanel.setOnNewGame(this::showGameModeDialog);
        controlPanel.setOnUndo(this::undoMove);
        controlPanel.setOnResign(this::resign);
        controlPanel.setOnQuit(Platform::exit);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setTop(clockPanel);
        root.setCenter(boardView);
        root.setBottom(statusBar);
        root.setRight(controlPanel);
        root.setLeft(moveHistoryPanel);

        Scene scene = new Scene(root, 640, 550);
        primaryStage.setTitle("Chess Game [JavaFX]");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        clockPanel.updateClocks();
        showGameModeDialog();
    }

    /**
     * ゲームモード選択ダイアログを表示し、選択結果に応じてゲームをセットアップする。
     */
    private void showGameModeDialog() {
        if (aiDelay != null) aiDelay.stop();
        if (clockTimeline != null) clockTimeline.stop();
        cancelPendingAiTask();

        game.removeObserver(this);
        game = GameModeDialog.showDialog(primaryStage);
        game.addObserver(this);
        boardView.setGame(game);
        moveHistoryPanel.setGame(game);
        clockPanel.setGame(game);
        isAIGame = GameModeDialog.isLastGameAI();

        game.startNewGame();
        boardView.resetView();
        statusBar.resetStatus();
        updateStatusBar();
        clockPanel.updateClocks();
        controlPanel.setUndoDisabled(true);
        startClockTimelineIfNeeded();
    }

    /**
     * 持ち時間ルールが設定されている場合、残り時間表示・時間切れ検出を定期的にポーリングする
     * タイムラインを開始する。ルール無しの対局では何もしない。
     */
    private void startClockTimelineIfNeeded() {
        if (!game.hasTimeControl()) return;

        clockTimeline = new Timeline(new KeyFrame(Duration.millis(CLOCK_TICK_MS), e -> {
            if (game.checkTimeout()) {
                clockTimeline.stop();
            }
            clockPanel.updateClocks();
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    /**
     * AI_MOVE_DELAY_MS 後に AI の手の選択（バックグラウンド実行）を開始する。
     * AI の番でない場合は何もしない。
     */
    private void scheduleAIMove() {
        if (game.isGameOver()) return;
        if (!game.getCurrentPlayer().isAI()) return;

        if (aiDelay != null) aiDelay.stop();
        cancelPendingAiTask();

        // AI の手番中は Undo を無効化する（バックグラウンド思考中に Undo されると
        // 選択結果が古い盤面向けになり不整合を起こすため、手番の間ずっと塞ぐ）
        controlPanel.setUndoDisabled(true);

        aiDelay = new PauseTransition(Duration.millis(AI_MOVE_DELAY_MS));
        aiDelay.setOnFinished(e -> startAiTask());
        aiDelay.play();
    }

    /**
     * AI の手をバックグラウンドスレッドで選択する。{@code selectMove} は Python
     * サブプロセスの起動・待機を伴うため（難易度4は最大20秒）、JavaFX Application
     * Thread 上で同期実行するとウィンドウ全体がフリーズしてしまう。{@link Task} を
     * 別スレッドで実行し、結果の適用は完了コールバック（FX スレッド上で実行される）で行う。
     */
    private void startAiTask() {
        if (game.isGameOver()) return;
        if (!game.getCurrentPlayer().isAI()) return;

        // New Game で this.game が差し替わった場合を検知するため、開始時点の参照を保持する
        final ChessGame gameAtStart = game;
        final AIPlayer ai = (AIPlayer) game.getCurrentPlayer();

        Task<Move> task = new Task<>() {
            @Override
            protected Move call() {
                return ai.selectMove(gameAtStart);
            }
        };
        task.setOnSucceeded(e -> applyAiTaskResult(task.getValue(), gameAtStart, false));
        task.setOnCancelled(e -> applyAiTaskResult(null, gameAtStart, true));
        task.setOnFailed(e -> applyAiTaskResult(null, gameAtStart, true));

        aiTask = task;
        Thread thread = new Thread(task, "ai-move-selector");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * バックグラウンドで選択された AI の手を、適用可能であれば盤面に反映する。
     * 適用しない場合（キャンセル・New Game によるゲーム差し替え等）は、
     * 同じゲームのままであれば無効化した Undo ボタンの状態を戻す。
     */
    private void applyAiTaskResult(Move move, ChessGame gameAtStart, boolean cancelled) {
        boolean applied = applyAiMoveIfStillValid(gameAtStart, game, move, cancelled);
        if (applied) {
            boardView.updateBoardDisplay();
        } else if (game == gameAtStart) {
            controlPanel.setUndoDisabled(game.getMoveHistory().isEmpty());
        }
    }

    /**
     * AI 思考完了後、選択された手を適用すべきかを判定し、適用可能なら盤面に反映する。
     * {@code Task}/JavaFX Application Thread に依存しない static メソッドとして
     * 切り出すことで、New Game・Undo による {@code game} インスタンス差し替えや
     * キャンセル時の競合防止ロジックを、JavaFX Node を介さず単体で結合テストできる
     * ようにしている（{@code com.chessgame.swing.ui.SwingChessGameFrame} の
     * 同名メソッドと対をなす）。
     *
     * @param gameAtStart 思考開始時点の {@link ChessGame} インスタンス
     * @param currentGame 現在（思考完了時点）の {@link ChessGame} インスタンス
     * @param move        AI が選択した手（取得失敗時は null）
     * @param cancelled   task がキャンセルされていたか
     * @return 手を適用した場合 true
     */
    static boolean applyAiMoveIfStillValid(ChessGame gameAtStart, ChessGame currentGame, Move move, boolean cancelled) {
        if (!AIPlayer.isMoveStillApplicable(move, gameAtStart, currentGame, cancelled)) {
            return false;
        }
        return currentGame.makeMove(move.getFrom(), move.getTo(), move.getPromotionPiece());
    }

    /**
     * 思考中の AI task が残っていればキャンセルする。New Game・破棄済みゲームに
     * 対して古い選択結果が適用されるのを防ぐために呼ぶ。
     */
    private void cancelPendingAiTask() {
        if (aiTask != null) {
            aiTask.cancel();
            aiTask = null;
        }
    }

    /**
     * 直前の手を取り消す。AI 対戦中は AI の手も合わせて2手分戻す。
     */
    private void undoMove() {
        if (game.getMoveHistory().isEmpty()) return;
        if (aiDelay != null) aiDelay.stop();
        game.undo();
        // AI 対戦中で undo 後の手番が AI なら、もう1手戻してプレイヤーの番に戻す
        if (isAIGame && !game.getMoveHistory().isEmpty() && !game.getCurrentPlayer().isHuman()) {
            game.undo();
        }
        boardView.resetView();
        updateStatusBar();
    }

    /**
     * 確認ダイアログを表示し、承認された場合に人間側のプレイヤーを投了させる。
     * Human vs AI では AI の手番中でも常に人間側を投了させる（2人対戦では現在の手番のまま）。
     */
    private void resign() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resign");
        alert.setHeaderText("Resign Game?");
        alert.setContentText("Are you sure you want to resign?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                game.resign(game.getResigningColor());
                updateStatusBar();
            }
        });
    }

    /**
     * 現在の手番プレイヤー名と手数をステータスバーに反映し、Undo ボタン状態を更新する。
     */
    private void updateStatusBar() {
        statusBar.updateStatus(game.getCurrentPlayer().getName() + " to move");
        statusBar.updateMoveCount(game.getMoveHistory().size());
        controlPanel.setUndoDisabled(game.getMoveHistory().isEmpty());
    }

    // 盤面変化の通知: 着手・undo・New Game のいずれでも発火するため、棋譜パネルの更新に使う
    @Override
    public void onBoardChanged() {
        moveHistoryPanel.updateMoveHistory();
    }

    // 手確定後、AI 対戦中なら AI の手をスケジュールする
    @Override
    public void onMoveMade(Move move) {
        if (isAIGame) scheduleAIMove();
    }

    @Override
    public void onGameStateChanged(GameState.GameStatus newStatus) {
        switch (newStatus) {
            // 王手: Undo は履歴があれば有効のまま
            case CHECK:
                statusBar.setCheckStatus(game.getCurrentPlayer().getColor().toString());
                controlPanel.setUndoDisabled(game.getMoveHistory().isEmpty());
                break;
            // チェックメイト: 勝者は getCurrentPlayer() の反対側
            case CHECKMATE:
                statusBar.setCheckmateStatus(game.getCurrentPlayer().getColor().opposite().toString());
                controlPanel.setUndoDisabled(true);
                break;
            case STALEMATE:
                statusBar.setStalemateStatus();
                controlPanel.setUndoDisabled(true);
                break;
            case FIFTY_MOVE_RULE:
                statusBar.setDrawStatus("Fifty-move rule");
                controlPanel.setUndoDisabled(true);
                break;
            case THREEFOLD_REPETITION:
                statusBar.setDrawStatus("Threefold repetition");
                controlPanel.setUndoDisabled(true);
                break;
            case INSUFFICIENT_MATERIAL:
                statusBar.setDrawStatus("Insufficient material");
                controlPanel.setUndoDisabled(true);
                break;
            case WHITE_RESIGNED:
                statusBar.setCheckmateStatus("Black");
                controlPanel.setUndoDisabled(true);
                break;
            case BLACK_RESIGNED:
                statusBar.setCheckmateStatus("White");
                controlPanel.setUndoDisabled(true);
                break;
            case WHITE_TIMEOUT:
                statusBar.setCheckmateStatus("Black");
                controlPanel.setUndoDisabled(true);
                break;
            case BLACK_TIMEOUT:
                statusBar.setCheckmateStatus("White");
                controlPanel.setUndoDisabled(true);
                break;
            default:
                updateStatusBar();
                break;
        }
    }

    @Override
    public void onCheckDetected(Color kingColor) {
        statusBar.setCheckStatus(kingColor.toString());
    }

    @Override
    public void onGameOver(Color winner) {
        if (clockTimeline != null) clockTimeline.stop();
        showGameOverDialog(winner != null ? winner + " wins!" : drawReasonMessage());
    }

    /**
     * winner が null（引き分け）の場合に、具体的な引き分け理由を含むメッセージを返す。
     */
    private String drawReasonMessage() {
        return switch (game.getGameStatus()) {
            case FIFTY_MOVE_RULE -> "Draw! Fifty-move rule!";
            case THREEFOLD_REPETITION -> "Draw! Threefold repetition!";
            case INSUFFICIENT_MATERIAL -> "Draw! Insufficient material!";
            default -> "Draw! Stalemate!";
        };
    }

    private void showGameOverDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Over");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
