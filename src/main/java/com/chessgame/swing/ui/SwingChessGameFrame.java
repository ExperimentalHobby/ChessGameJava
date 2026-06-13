package com.chessgame.swing.ui;

import com.chessgame.game.player.AIPlayer;
import com.chessgame.game.core.ChessGame;
import com.chessgame.game.observer.GameObserver;
import com.chessgame.game.player.Player;
import com.chessgame.model.Color;
import com.chessgame.gamestate.model.GameState;
import com.chessgame.move.model.Move;
import com.chessgame.swing.board.SwingChessBoardPanel;

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

    /** 勝敗確定時のステータス表示に使う緑色。 */
    private static final java.awt.Color WIN_COLOR = new java.awt.Color(0, 140, 0);

    private ChessGame game;
    private final SwingChessBoardPanel boardPanel;
    private JLabel statusLabel;
    private JLabel moveCountLabel;
    private JButton undoButton;
    private JButton resignButton;
    private boolean isAIGame = false;
    private Timer aiTimer;

    /**
     * フレームを生成し、デフォルトの2人対戦ゲームを初期化する。
     * 起動時にゲームモード選択ダイアログを表示する。
     */
    public SwingChessGameFrame() {
        setTitle("Java Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Create default game first so boardPanel has a valid reference
        this.game = ChessGame.createTwoPlayerGame("White", "Black");
        this.game.addObserver(this);
        this.boardPanel = new SwingChessBoardPanel(this.game);

        JPanel statusPanel = createStatusPanel();
        JPanel controlPanel = createControlPanel();

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        mainPanel.add(controlPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);

        showGameModeDialog();
    }

    /**
     * ゲームステータス（手番・手数）を表示するパネルを生成して返す。
     *
     * @return ステータスパネル
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Status"));

        statusLabel = new JLabel("White to move");
        statusLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 14));

        moveCountLabel = new JLabel("Moves: 0");
        moveCountLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));

        panel.add(statusLabel);
        panel.add(moveCountLabel);

        return panel;
    }

    /**
     * 新ゲーム・undo・投了・終了ボタンを含むコントロールパネルを生成して返す。
     *
     * @return コントロールパネル
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Controls"));

        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> showGameModeDialog());

        undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> undoMove());

        resignButton = new JButton("Resign");
        resignButton.addActionListener(e -> resign());

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> System.exit(0));

        panel.add(newGameButton);
        panel.add(undoButton);
        panel.add(resignButton);
        panel.add(quitButton);

        return panel;
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

    /**
     * ゲーム状態に応じてステータスラベルのテキストと色を更新する。
     */
    private void updateStatus() {
        GameState.GameStatus status = game.getGameStatus();
        String playerName = game.getCurrentPlayer().getName();
        String statusText;

        switch (status) {
            // 王手: undo・投了は引き続き有効
            case CHECK:
                statusText = playerName + " は王手です！";
                statusLabel.setForeground(java.awt.Color.RED);
                undoButton.setEnabled(true);
                resignButton.setEnabled(true);
                break;
            // チェックメイト: 終局のため undo・投了を無効化
            // getCurrentPlayer() は敗者なので、勝者は opposite()
            case CHECKMATE:
                String winner = game.getCurrentPlayer().getColor().opposite().toString();
                statusText = "チェックメイト！ " + winner + " の勝ち！";
                statusLabel.setForeground(WIN_COLOR);
                undoButton.setEnabled(false);
                resignButton.setEnabled(false);
                break;
            // ステールメイト: 合法手なし・王手なし → 引き分け
            case STALEMATE:
                statusText = "ステールメイト！ 引き分け！";
                statusLabel.setForeground(java.awt.Color.BLUE);
                undoButton.setEnabled(false);
                resignButton.setEnabled(false);
                break;
            // 白投了 → 黒の勝ち
            case WHITE_RESIGNED:
                statusText = "白が投了！ BLACK の勝ち！";
                statusLabel.setForeground(WIN_COLOR);
                undoButton.setEnabled(false);
                resignButton.setEnabled(false);
                break;
            // 黒投了 → 白の勝ち
            case BLACK_RESIGNED:
                statusText = "黒が投了！ WHITE の勝ち！";
                statusLabel.setForeground(WIN_COLOR);
                undoButton.setEnabled(false);
                resignButton.setEnabled(false);
                break;
            // IN_PROGRESS: 通常の手番表示。履歴があるときだけ undo を有効化
            default:
                statusText = playerName + " の番";
                statusLabel.setForeground(java.awt.Color.BLACK);
                undoButton.setEnabled(!game.getMoveHistory().isEmpty());
                resignButton.setEnabled(true);
                break;
        }

        statusLabel.setText(statusText);
        moveCountLabel.setText("Moves: " + game.getMoveHistory().size());
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
        updateStatus();
        // AI 対戦かつ IN_PROGRESS への遷移はプレイヤーの手が完了して AI の番になった状態
        if (isAIGame && newStatus == GameState.GameStatus.IN_PROGRESS) {
            scheduleAIMove();
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

        Object[] options = {"Human vs Human", "Human vs AI（Easy）", "Human vs AI（Medium）",
            "Human vs AI（Hard）", "Human vs AI（Expert）"};
        int choice = JOptionPane.showOptionDialog(this,
            "ゲームモードを選択してください",
            "ゲームモード選択",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == JOptionPane.CLOSED_OPTION) choice = 0;

        if (choice == 0) {
            setupTwoPlayerGame();
        } else {
            setupAIGame(choice);
        }
    }

    /**
     * 2人対戦ゲームをセットアップし、新ゲームを開始する。
     */
    private void setupTwoPlayerGame() {
        game.removeObserver(this);
        game = ChessGame.createTwoPlayerGame("White", "Black");
        game.addObserver(this);
        boardPanel.setGame(game);
        isAIGame = false;
        game.startNewGame();
        updateStatus();
    }

    /**
     * AI 対戦ゲームをセットアップし、新ゲームを開始する。先手番（白）が AI の場合は即座に AI の手を実行する。
     *
     * @param difficulty AI の難易度（1=ランダム、2=駒取り優先、3=最善手優先、4=minimax）
     */
    private void setupAIGame(int difficulty) {
        game.removeObserver(this);
        Player whitePlayer = Player.human(Color.WHITE, "You");
        Player blackPlayer = new AIPlayer("AI", Color.BLACK, difficulty);
        game = new ChessGame(whitePlayer, blackPlayer);
        game.addObserver(this);
        boardPanel.setGame(game);
        isAIGame = true;
        game.startNewGame();
        updateStatus();
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
                game.makeMove(aiMove.getFrom(), aiMove.getTo());
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
