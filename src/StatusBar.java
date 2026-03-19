import javax.swing.*;  // JPanel, JLabel, BorderFactory, FlowLayout, Box
import java.awt.*;     // Color, Font, BorderLayout, FlowLayout

// The dark bar at the top of the window showing whose turn it is and any important game status
public class StatusBar extends JPanel {

    private final JLabel statusLabel;
    private final JLabel turnIndicator; // coloured dot showing current player

    public StatusBar() {
        setLayout(new BorderLayout(10, 5));
        setBackground(new java.awt.Color(40, 40, 40));
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        turnIndicator = new JLabel("⬤");
        turnIndicator.setFont(new Font("Serif", Font.PLAIN, 22));
        turnIndicator.setForeground(java.awt.Color.WHITE);

        statusLabel = new JLabel("White's turn");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusLabel.setForeground(java.awt.Color.WHITE);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(turnIndicator);
        left.add(statusLabel);
        add(left, BorderLayout.CENTER);
    }

    // Called after every move to reflect the new game state
    public void update(GameState gameState) {
        PieceColor turn = gameState.getCurrentTurn();
        GameState.Status status = gameState.getStatus();

        // dot is white for white's turn, near-black for black's turn
        turnIndicator.setForeground(turn == PieceColor.WHITE
            ? java.awt.Color.WHITE
            : new java.awt.Color(30, 30, 30));

        String text;
        switch (status) {
            case CHECK:     text = (turn == PieceColor.WHITE ? "White" : "Black") + " is in CHECK!"; break;
            case CHECKMATE: text = (turn == PieceColor.WHITE ? "Black" : "White") + " wins by Checkmate!"; break;
            case STALEMATE: text = "Stalemate — It's a Draw!"; break;
            default:        text = (turn == PieceColor.WHITE ? "White" : "Black") + "'s turn"; break;
        }
        statusLabel.setText(text);
    }

    // Shown while the AI is computing its move on the background thread
    public void setThinking(boolean thinking) {
        if (thinking) statusLabel.setText("Computer is thinking...");
    }
}