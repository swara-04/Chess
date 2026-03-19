import javax.swing.*;       // JFrame, JMenuBar, JMenu, JMenuItem, Timer
import java.awt.*;          // BorderLayout
import java.awt.event.ActionEvent; // for menu item callbacks

// The main window - owns the board, status bar, and menu.
// Also acts as the coordinator between dialogs (new game, game over) and the board panel
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
        add(statusBar, BorderLayout.NORTH); // status bar always visible at the top

        setupMenuBar();
        startNewGame(); // show the setup dialog immediately on launch

        pack();
        setLocationRelativeTo(null); // centre on screen
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

    // Shows the setup dialog, wires up a fresh game state + AI, and swaps in a new BoardPanel
    public void startNewGame() {
        NewGameDialog dialog = new NewGameDialog(this);
        dialog.setVisible(true); // blocks until the player hits Start

        PieceColor playerColor = dialog.getPlayerColor();
        ChessAI.Difficulty difficulty = dialog.getDifficulty();
        PieceColor aiColor = playerColor.opposite(); // AI always plays the other side

        ChessAI ai = new ChessAI(difficulty, aiColor);
        gameState = new GameState();

        // swap out the old board panel if one exists
        if (boardPanel != null) remove(boardPanel);
        boardPanel = new BoardPanel(gameState, statusBar, playerColor, ai, this);
        add(boardPanel, BorderLayout.CENTER);
        statusBar.update(gameState);
        revalidate();
        repaint();
        pack();
    }

    // Called by BoardPanel when the game ends.
    // Small delay so the player can see the final board state before the dialog pops up
    public void showGameOver() {
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