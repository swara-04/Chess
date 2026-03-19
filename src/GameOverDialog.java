import javax.swing.*;   // JDialog, JPanel, JLabel, JButton, BoxLayout, Box, SwingConstants, BorderFactory
import java.awt.*;      // Graphics, Graphics2D, Color, Font, FontMetrics, Dimension, GridLayout, BorderLayout
import java.awt.event.*; // ActionListener (via lambda)

// Shown at the end of every game - tells the player what happened and lets them play again or quit
public class GameOverDialog extends JDialog {

    public enum Result { PLAY_AGAIN, QUIT }
    private Result result = Result.QUIT; // default to quit if they close the dialog

    // same dark theme as NewGameDialog
    private static final java.awt.Color BG      = new java.awt.Color(24, 24, 32);
    private static final java.awt.Color ACCENT  = new java.awt.Color(99, 179, 120);
    private static final java.awt.Color TEXT    = new java.awt.Color(220, 220, 230);
    private static final java.awt.Color SUBTEXT = new java.awt.Color(140, 140, 160);
    private static final java.awt.Color BTN_SEC = new java.awt.Color(55, 55, 70); // secondary button (quit)

    public GameOverDialog(JFrame parent, GameState gameState, PieceColor playerColor) {
        super(parent, "Game Over", true);
        setUndecorated(true);
        setBackground(new java.awt.Color(0, 0, 0, 0));

        GameState.Status status = gameState.getStatus();
        PieceColor turn = gameState.getCurrentTurn(); // whoever's turn it is has just lost (or stalemated)

        // work out what to show based on the game result
        String emoji, headline, subline;
        if (status == GameState.Status.STALEMATE) {
            emoji = "🤝";
            headline = "Stalemate!";
            subline = "It's a draw — no legal moves.";
        } else {
            // in checkmate the side to move is the loser, so the winner is the opposite
            PieceColor winner = turn.opposite();
            boolean playerWon = winner == playerColor;
            if (playerWon) {
                emoji = "🏆";
                headline = "You Win!";
                subline = (winner == PieceColor.WHITE ? "White" : "Black") + " wins by Checkmate";
            } else {
                emoji = "💀";
                headline = "You Lose!";
                subline = (winner == PieceColor.WHITE ? "White" : "Black") + " wins by Checkmate";
            }
        }

        // root panel draws its own rounded background since we're undecorated
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(36, 44, 28, 44));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JLabel emojiLbl = new JLabel(emoji, SwingConstants.CENTER);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        emojiLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel headlineLbl = new JLabel(headline, SwingConstants.CENTER);
        headlineLbl.setFont(new Font("SansSerif", Font.BOLD, 26));
        headlineLbl.setForeground(TEXT);
        headlineLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel(subline, SwingConstants.CENTER);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subLbl.setForeground(SUBTEXT);
        subLbl.setAlignmentX(CENTER_ALIGNMENT);

        body.add(emojiLbl);
        body.add(Box.createVerticalStrut(10));
        body.add(headlineLbl);
        body.add(Box.createVerticalStrut(6));
        body.add(subLbl);
        body.add(Box.createVerticalStrut(28));

        // play again is the primary action (green), quit is secondary (dark)
        JButton playAgainBtn = makeButton("Play Again", ACCENT, java.awt.Color.WHITE);
        JButton quitBtn      = makeButton("Quit",       BTN_SEC, SUBTEXT);

        playAgainBtn.addActionListener(e -> { result = Result.PLAY_AGAIN; dispose(); });
        quitBtn.addActionListener(e      -> { result = Result.QUIT;       dispose(); });

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(280, 44));
        btnPanel.setAlignmentX(CENTER_ALIGNMENT);
        btnPanel.add(playAgainBtn);
        btnPanel.add(quitBtn);

        body.add(btnPanel);
        root.add(body, BorderLayout.CENTER);
        setContentPane(root);
        pack();
        setLocationRelativeTo(parent);
    }

    // Reusable styled button - draws its own rounded background so we can skip the default L&F
    private JButton makeButton(String text, java.awt.Color bg, java.awt.Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg); // brighten on hover
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(fg);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(120, 44));
        btn.setContentAreaFilled(false); // we're drawing the background ourselves
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public Result getResult() { return result; }
}