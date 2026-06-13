package com.chessgame.javafx;

import com.chessgame.game.AIPlayer;
import com.chessgame.game.ChessGame;
import com.chessgame.game.GameObserver;
import com.chessgame.game.Player;
import com.chessgame.model.Color;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.move.model.Move;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;
import java.util.Optional;

/**
 * JavaFX チェスゲームアプリケーションのメインクラス。
 * {@link Application} を継承して JavaFX ウィンドウを構築し、
 * {@link GameObserver} を実装してゲームイベントをUIに反映する。
 */
public class ChessGameApp extends Application implements GameObserver {
    /** AI が手を指すまでの遅延（ミリ秒）。即時実行だと UI 更新が追いつかないため遅延させる。 */
    private static final int AI_MOVE_DELAY_MS = 800;

    private ChessGame game;
    private ChessBoardView boardView;
    private StatusBar statusBar;
    private ControlPanel controlPanel;
    private boolean isAIGame = false;
    private PauseTransition aiDelay;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        game = ChessGame.createTwoPlayerGame("White Player", "Black Player");
        game.addObserver(this);

        boardView = new ChessBoardView(game);
        statusBar = new StatusBar();
        controlPanel = new ControlPanel();

        boardView.setOnMoveCallback(this::updateStatusBar);
        controlPanel.setOnNewGame(this::showGameModeDialog);
        controlPanel.setOnUndo(this::undoMove);
        controlPanel.setOnResign(this::resign);
        controlPanel.setOnQuit(Platform::exit);

        BorderPane root = new BorderPane();
        root.setCenter(boardView);
        root.setBottom(statusBar);
        root.setRight(controlPanel);

        Scene scene = new Scene(root, 840, 550);
        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        showGameModeDialog();
    }

    /**
     * ゲームモード選択ダイアログ（Human vs Human / AI 難易度4段階）を表示し、
     * 選択結果に応じてゲームをセットアップする。
     */
    private void showGameModeDialog() {
        if (aiDelay != null) aiDelay.stop();

        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.setTitle("New Game");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setResizable(false);

        javafx.scene.control.Button btnHumanVsHuman = new javafx.scene.control.Button("Human vs Human");
        javafx.scene.control.Button btnEasy = new javafx.scene.control.Button("Human vs AI (Easy)");
        javafx.scene.control.Button btnMedium = new javafx.scene.control.Button("Human vs AI (Medium)");
        javafx.scene.control.Button btnHard = new javafx.scene.control.Button("Human vs AI (Hard)");
        javafx.scene.control.Button btnExpert = new javafx.scene.control.Button("Human vs AI (Expert)");

        btnHumanVsHuman.setPrefWidth(120);
        btnHumanVsHuman.setPrefHeight(40);
        btnEasy.setPrefWidth(120);
        btnEasy.setPrefHeight(40);
        btnMedium.setPrefWidth(120);
        btnMedium.setPrefHeight(40);
        btnHard.setPrefWidth(120);
        btnHard.setPrefHeight(40);
        btnExpert.setPrefWidth(120);
        btnExpert.setPrefHeight(40);

        btnHumanVsHuman.setOnAction(e -> {
            setupTwoPlayerGame();
            dialog.close();
        });
        btnEasy.setOnAction(e -> {
            setupAIGame(1);
            dialog.close();
        });
        btnMedium.setOnAction(e -> {
            setupAIGame(2);
            dialog.close();
        });
        btnHard.setOnAction(e -> {
            setupAIGame(3);
            dialog.close();
        });
        btnExpert.setOnAction(e -> {
            setupAIGame(4);
            dialog.close();
        });

        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        vbox.setPadding(new javafx.geometry.Insets(20));

        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(10);
        hbox.setAlignment(javafx.geometry.Pos.CENTER);
        hbox.getChildren().addAll(btnHumanVsHuman, btnEasy, btnMedium, btnHard, btnExpert);

        javafx.scene.control.Label label = new javafx.scene.control.Label("ゲームモードを選択してください");
        label.setStyle("-fx-font-size: 14;");

        vbox.getChildren().addAll(label, hbox);

        javafx.scene.Scene scene = new javafx.scene.Scene(vbox, 700, 120);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void setupTwoPlayerGame() {
        game.removeObserver(this);
        game = ChessGame.createTwoPlayerGame("White", "Black");
        game.addObserver(this);
        boardView.setGame(game);
        isAIGame = false;
        game.startNewGame();
        boardView.resetView();
        statusBar.resetStatus();
        updateStatusBar();
        controlPanel.setUndoDisabled(true);
    }

    private void setupAIGame(int difficulty) {
        game.removeObserver(this);
        Player whitePlayer = Player.human(Color.WHITE, "You");
        Player blackPlayer = new AIPlayer("AI", Color.BLACK, difficulty);
        game = new ChessGame(whitePlayer, blackPlayer);
        game.addObserver(this);
        boardView.setGame(game);
        isAIGame = true;
        game.startNewGame();
        boardView.resetView();
        statusBar.resetStatus();
        updateStatusBar();
        controlPanel.setUndoDisabled(true);
    }

    /**
     * AI_MOVE_DELAY_MS 後に AI の手を実行する。AI の番でない場合は何もしない。
     */
    private void scheduleAIMove() {
        if (game.isGameOver()) return;
        if (!game.getCurrentPlayer().isAI()) return;

        if (aiDelay != null) aiDelay.stop();
        aiDelay = new PauseTransition(Duration.millis(AI_MOVE_DELAY_MS));
        aiDelay.setOnFinished(e -> {
            if (game.isGameOver()) return;
            if (!game.getCurrentPlayer().isAI()) return;
            AIPlayer ai = (AIPlayer) game.getCurrentPlayer();
            Move aiMove = ai.selectMove(game);
            if (aiMove != null) {
                game.makeMove(aiMove.getFrom(), aiMove.getTo());
                boardView.updateBoardDisplay();
            }
        });
        aiDelay.play();
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
     * 確認ダイアログを表示し、承認された場合に現在のプレイヤーを投了させる。
     */
    private void resign() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resign");
        alert.setHeaderText("Resign Game?");
        alert.setContentText("Are you sure you want to resign?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                game.resign(game.getCurrentPlayer().getColor());
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

    @Override public void onBoardChanged() {}

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
            case WHITE_RESIGNED:
                statusBar.setCheckmateStatus("Black");
                controlPanel.setUndoDisabled(true);
                break;
            case BLACK_RESIGNED:
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
        showGameOverDialog(winner != null ? winner + " wins!" : "Draw! Stalemate!");
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
