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
import com.chessgame.swing.ui.panel.MoveHistoryPanel;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;


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
    private final MoveHistoryPanel moveHistoryPanel;
    private boolean isAIGame = false;
    private Timer aiTimer;
    private SwingWorker<Move, Void> aiWorker;

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
        this.moveHistoryPanel = new MoveHistoryPanel(game);

        // Set up control panel callbacks
        controlPanel.setOnNewGame(this::showGameModeDialog);
        controlPanel.setOnUndo(this::undoMove);
        controlPanel.setOnResign(this::resign);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        mainPanel.add(controlPanel, BorderLayout.EAST);
        mainPanel.add(moveHistoryPanel, BorderLayout.WEST);

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


    // 盤面変化の通知: 盤面パネルと棋譜パネルを再描画する
    @Override
    public void onBoardChanged() {
        boardPanel.updateBoard();
        moveHistoryPanel.updateMoveHistory();
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
            case FIFTY_MOVE_RULE:
            case THREEFOLD_REPETITION:
            case INSUFFICIENT_MATERIAL:
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
            String msg = (winner != null) ? winner + " の勝ち！" : drawReasonMessage();
            JOptionPane.showMessageDialog(this, msg, "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * winner が null（引き分け）の場合に、具体的な引き分け理由を含むメッセージを返す。
     */
    private String drawReasonMessage() {
        return switch (game.getGameStatus()) {
            case FIFTY_MOVE_RULE -> "引き分け（50手ルール）！";
            case THREEFOLD_REPETITION -> "引き分け（同一局面3回）！";
            case INSUFFICIENT_MATERIAL -> "引き分け（戦力不足）！";
            default -> "引き分け（ステールメイト）！";
        };
    }

    /**
     * ゲームモード選択ダイアログ（Human vs Human / AI 難易度4段階）を表示し、選択結果に応じてゲームをセットアップする。
     */
    private void showGameModeDialog() {
        if (aiTimer != null) aiTimer.stop();
        cancelPendingAiWorker();

        game.removeObserver(this);
        game = GameModeDialog.showDialog(this);
        game.addObserver(this);
        boardPanel.setGame(game);
        statusPanel.setGame(game);
        moveHistoryPanel.setGame(game);

        isAIGame = GameModeDialog.isLastGameAI();

        game.startNewGame();
        statusPanel.updateStatus();
        updateControlButtonState(game.getGameStatus());
        scheduleAIMove();
    }

    /**
     * 800ms 後に AI の手の選択（バックグラウンド実行）を開始するタイマーをスケジュールする。
     * AI の手番でない場合やゲーム終了時は何もしない。
     */
    private void scheduleAIMove() {
        if (!isAIGame) return;
        if (game.isGameOver()) return;
        // 現在の手番が AI でない場合（プレイヤーの番）は何もしない
        if (!(game.getCurrentPlayer() instanceof AIPlayer)) return;

        // 直前のタイマー・思考中の worker が残っている場合は必ずキャンセルしてから再スケジュール
        if (aiTimer != null) aiTimer.stop();
        cancelPendingAiWorker();

        // AI の手番中は Undo を無効化する（バックグラウンド思考中に Undo されると
        // 選択結果が古い盤面向けになり不整合を起こすため、手番の間ずっと塞ぐ）
        controlPanel.setUndoEnabled(false);

        // AI_MOVE_DELAY_MS の遅延で AI の手の選択を開始する
        aiTimer = new Timer(AI_MOVE_DELAY_MS, e -> startAiWorker());
        aiTimer.setRepeats(false); // 1回だけ発火する（連続実行を防ぐ）
        aiTimer.start();
    }

    /**
     * AI の手をバックグラウンドスレッドで選択する。{@code selectMove} は Python
     * サブプロセスの起動・待機を伴うため（難易度4は最大20秒）、EDT 上で同期実行すると
     * ウィンドウ全体がフリーズしてしまう。SwingWorker でバックグラウンド実行し、
     * 結果の適用（{@code done()}）のみ EDT 上で行う。
     */
    private void startAiWorker() {
        if (game.isGameOver()) return;
        if (!(game.getCurrentPlayer() instanceof AIPlayer)) return;

        // New Game で this.game が差し替わった場合を検知するため、開始時点の参照を保持する
        final ChessGame gameAtStart = game;
        final AIPlayer ai = (AIPlayer) game.getCurrentPlayer();

        aiWorker = new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() {
                return ai.selectMove(gameAtStart);
            }

            @Override
            protected void done() {
                boolean cancelled = isCancelled();
                Move move = null;
                if (!cancelled) {
                    try {
                        move = get();
                    } catch (InterruptedException | ExecutionException ex) {
                        move = null;
                    }
                }

                if (AIPlayer.isMoveStillApplicable(move, gameAtStart, game, cancelled)) {
                    game.makeMove(move.getFrom(), move.getTo(), move.getPromotionPiece());
                } else if (game == gameAtStart) {
                    // 手を適用しない場合、思考開始時に無効化した Undo ボタンの状態を戻す
                    updateControlButtonState(game.getGameStatus());
                }
            }
        };
        aiWorker.execute();
    }

    /**
     * 思考中の AI worker が残っていればキャンセルする。New Game・破棄済みゲームに
     * 対して古い選択結果が適用されるのを防ぐために呼ぶ。
     */
    private void cancelPendingAiWorker() {
        if (aiWorker != null) {
            aiWorker.cancel(true);
            aiWorker = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingChessGameFrame frame = new SwingChessGameFrame();
            frame.setVisible(true);
        });
    }
}
