import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GameWindow extends JFrame {

    private GameState gameState;
    private BoardPanel boardPanel;
    private StatusBar statusBar;

    public GameWindow() {
        setTitle("Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        statusBar = new StatusBar();
        setLayout(new BorderLayout());
        add(statusBar, BorderLayout.NORTH);

        setupMenuBar();
        startNewGame();

        pack();
        setLocationRelativeTo(null);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New Game");
        newGame.addActionListener(this::onNewGame);
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        gameMenu.add(newGame);
        gameMenu.addSeparator();
        gameMenu.add(exit);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    public void startNewGame() {
        NewGameDialog dialog = new NewGameDialog(this);
        dialog.setVisible(true);

        PieceColor playerColor = dialog.getPlayerColor();
        ChessAI.Difficulty difficulty = dialog.getDifficulty();
        PieceColor aiColor = playerColor.opposite();

        ChessAI ai = new ChessAI(difficulty, aiColor);
        gameState = new GameState();

        if (boardPanel != null) remove(boardPanel);
        boardPanel = new BoardPanel(gameState, statusBar, playerColor, ai, this);
        add(boardPanel, BorderLayout.CENTER);
        statusBar.update(gameState);
        revalidate();
        repaint();
        pack();
    }

    public void showGameOver() {
        // Small delay so the final board state renders before the dialog pops
        Timer t = new Timer(400, e -> {
            GameOverDialog dlg = new GameOverDialog(this, gameState, boardPanel.getPlayerColor());
            dlg.setVisible(true);
            if (dlg.getResult() == GameOverDialog.Result.PLAY_AGAIN) {
                startNewGame();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void onNewGame(ActionEvent e) {
        startNewGame();
    }
}