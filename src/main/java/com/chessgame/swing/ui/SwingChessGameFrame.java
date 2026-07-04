package com.chessgame.swing.ui;

import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.observer.GameObserver;
import com.chessgame.model.Color;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.move.model.Move;
import com.chessgame.swing.board.SwingChessBoardPanel;
import com.chessgame.swing.ui.dialog.GameModeDialog;
import com.chessgame.swing.ui.panel.StatusPanel;
import com.chessgame.swing.ui.panel.ControlPanel;

import javax.swing.*;
import java.awt.*;


/**
 * Swing 版チェスゲームのメインウィンドウ。
 * 盤面パネル・ステータス表示・コントロールパネルを組み合わせた UI を提供する。
 * Human vs Human および Human vs AI（難易度3段階）モードを切り替えられる。
 * {@link GameObserver} を実装してゲームイベントを UI に反映する。
 */
public class SwingChessGameFrame extends JFrame implements GameObserver {
    /** AI が手を指すまでの遅延（ミリ秒）。即時実行だと UI 更新が追いつかないため遅延させる。 */
    private static final int AI_MOVE_DELAY_MS = 800;

    private ChessGame game;
    private final SwingChessBoardPanel boardPanel;
    private final StatusPanel statusPanel;
    private final ControlPanel controlPanel;
    private boolean isAIGame = false;
    private Timer aiTimer;

    /**
     * フレームを生成し、デフォルトの2人対戦ゲームを初期化する。
     * 起動時にゲームモード選択ダイアログを表示する。
     */
    public SwingChessGameFrame() {
        setTitle("Chess Game [Swing]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Create default game first so boardPanel has a valid reference
        this.game = ChessGame.createTwoPlayerGame("White", "Black");
        this.game.addObserver(this);
        this.boardPanel = new SwingChessBoardPanel(this.game);

        this.statusPanel = new StatusPanel(game);
        this.controlPanel = new ControlPanel();

        // Set up control panel callbacks
        controlPanel.setOnNewGame(this::showGameModeDialog);
        controlPanel.setOnUndo(this::undoMove);
        controlPanel.setOnResign(this::resign);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        mainPanel.add(controlPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
        game.startNewGame(); // pack() 前にステータスラベルを確定させてから preferred size を計算させる
        pack();
        setLocationRelativeTo(null);
        // フレームが可視になってからダイアログを表示する。
        // 不可視フレームからモーダルダイアログを開くと OS がフレームを強制リサイズするため、
        // invokeLater で setVisible(true) の後に実行されるよう遅延させる。
        SwingUtilities.invokeLater(this::showGameModeDialog);
    }


    /**
     * 直前の手を取り消す。AI 対戦時はAIの手も合わせて2手戻す。
     */
    private void undoMove() {
        if (!game.getMoveHistory().isEmpty()) {
            game.undo();
            // AI 対戦時は AI の手も合わせて取り消す（プレイヤーが2手分戻るのを防ぐ）
            if (isAIGame && !game.getMoveHistory().isEmpty()) {
                game.undo();
            }
        }
    }

    /**
     * 確認ダイアログを表示し、承認された場合に現在のプレイヤーを投了させる。
     */
    private void resign() {
        int result = JOptionPane.showConfirmDialog(this,
            "投了しますか？", "Resign", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            Color current = game.getCurrentPlayer().getColor();
            game.resign(current);
        }
    }


    // 盤面変化の通知: 盤面パネルを再描画する
    @Override
    public void onBoardChanged() {
        boardPanel.updateBoard();
    }

    // 手確定の通知: 盤面の更新は onBoardChanged が行うため空実装
    @Override
    public void onMoveMade(Move move) {}

    // ゲーム状態変化の通知: ステータスラベルを更新し、AI 対戦時は次の手をスケジュールする
    @Override
    public void onGameStateChanged(GameState.GameStatus newStatus) {
        statusPanel.updateStatus();
        updateControlButtonState(newStatus);
        // AI 対戦かつ IN_PROGRESS への遷移はプレイヤーの手が完了して AI の番になった状態
        if (isAIGame && newStatus == GameState.GameStatus.IN_PROGRESS) {
            scheduleAIMove();
        }
    }

    /**
     * ゲーム状態に応じてコントロールボタンの有効・無効を更新する。
     */
    private void updateControlButtonState(GameState.GameStatus status) {
        switch (status) {
            case CHECK:
                controlPanel.setUndoEnabled(true);
                controlPanel.setResignEnabled(true);
                break;
            case CHECKMATE:
            case STALEMATE:
            case WHITE_RESIGNED:
            case BLACK_RESIGNED:
                controlPanel.setUndoEnabled(false);
                controlPanel.setResignEnabled(false);
                break;
            default: // IN_PROGRESS
                controlPanel.setUndoEnabled(!game.getMoveHistory().isEmpty());
                controlPanel.setResignEnabled(true);
                break;
        }
    }

    // 王手の通知: updateStatus 内で onGameStateChanged 経由の CHECK 処理に統合済みのため空実装
    @Override
    public void onCheckDetected(Color kingColor) {}

    // ゲーム終了の通知: AI タイマーを止めてからダイアログを表示する
    @Override
    public void onGameOver(Color winner) {
        // AI タイマーが残っている場合は停止して誤動作を防ぐ
        if (aiTimer != null) aiTimer.stop();
        // ゲーム終了ダイアログは EDT 上で表示する
        SwingUtilities.invokeLater(() -> {
            // winner が null の場合はステールメイトによる引き分け
            String msg = (winner != null)
                ? winner + " の勝ち！"
                : "引き分け（ステールメイト）！";
            JOptionPane.showMessageDialog(this, msg, "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * ゲームモード選択ダイアログ（Human vs Human / AI 難易度4段階）を表示し、選択結果に応じてゲームをセットアップする。
     */
    private void showGameModeDialog() {
        if (aiTimer != null) aiTimer.stop();

        game.removeObserver(this);
        game = GameModeDialog.showDialog(this);
        game.addObserver(this);
        boardPanel.setGame(game);

        isAIGame = GameModeDialog.isLastGameAI();

        game.startNewGame();
        statusPanel.updateStatus();
        updateControlButtonState(game.getGameStatus());
        scheduleAIMove();
    }

    /**
     * 800ms 後に AI の手を1手実行するタイマーをスケジュールする。
     * AI の手番でない場合やゲーム終了時は何もしない。
     */
    private void scheduleAIMove() {
        if (!isAIGame) return;
        if (game.isGameOver()) return;
        // 現在の手番が AI でない場合（プレイヤーの番）は何もしない
        if (!(game.getCurrentPlayer() instanceof AIPlayer)) return;

        // 直前のタイマーが残っている場合は必ずキャンセルしてから再スケジュール
        if (aiTimer != null) aiTimer.stop();

        // AI_MOVE_DELAY_MS の遅延で AI の手を実行する
        aiTimer = new Timer(AI_MOVE_DELAY_MS, e -> {
            // タイマー発火時にゲームが終了している可能性があるため再チェック
            if (game.isGameOver()) return;
            if (!(game.getCurrentPlayer() instanceof AIPlayer)) return;

            AIPlayer ai = (AIPlayer) game.getCurrentPlayer();
            Move aiMove = ai.selectMove(game);
            if (aiMove != null) {
                game.makeMove(aiMove);
            }
        });
        aiTimer.setRepeats(false); // 1回だけ発火する（連続実行を防ぐ）
        aiTimer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingChessGameFrame frame = new SwingChessGameFrame();
            frame.setVisible(true);
        });
    }
}
