import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class NewGameDialog extends JDialog {

    private PieceColor playerColor = PieceColor.WHITE;
    private ChessAI.Difficulty difficulty = ChessAI.Difficulty.MEDIUM;

    private static final java.awt.Color BG           = new java.awt.Color(24, 24, 32);
    private static final java.awt.Color CARD_BG      = new java.awt.Color(35, 35, 46);
    private static final java.awt.Color ACCENT       = new java.awt.Color(99, 179, 120);
    private static final java.awt.Color TEXT         = new java.awt.Color(220, 220, 230);
    private static final java.awt.Color SUBTEXT      = new java.awt.Color(140, 140, 160);
    private static final java.awt.Color SEL_BG       = new java.awt.Color(99, 179, 120, 30);
    private static final java.awt.Color SEL_BORDER   = new java.awt.Color(99, 179, 120);
    private static final java.awt.Color UNSEL_BORDER = new java.awt.Color(60, 60, 75);

    public NewGameDialog(JFrame parent) {
        super(parent, "New Game", true);
        setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        root.setOpaque(true);
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(32, 36, 28, 36));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Title
        JLabel icon = new JLabel("♟", SwingConstants.CENTER);
        icon.setFont(new Font("Serif", Font.PLAIN, 42));
        icon.setForeground(ACCENT);
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("New Game", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Choose your settings to begin", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(SUBTEXT);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        body.add(icon);
        body.add(Box.createVerticalStrut(6));
        body.add(title);
        body.add(Box.createVerticalStrut(4));
        body.add(subtitle);
        body.add(Box.createVerticalStrut(24));

        // Play As
        body.add(makeSectionLabel("PLAY AS"));
        body.add(Box.createVerticalStrut(8));

        ToggleCard whiteCard = new ToggleCard("WHITE", "White", "You go first", true, 110);
        ToggleCard blackCard = new ToggleCard("BLACK", "Black", "Computer goes first", false, 110);
        whiteCard.addActionListener(e -> { playerColor = PieceColor.WHITE; whiteCard.setSelected(true); blackCard.setSelected(false); });
        blackCard.addActionListener(e -> { playerColor = PieceColor.BLACK; blackCard.setSelected(true); whiteCard.setSelected(false); });

        JPanel colorRow = new JPanel(new GridLayout(1, 2, 10, 0));
        colorRow.setOpaque(false);
        colorRow.setMaximumSize(new Dimension(400, 115));
        colorRow.setAlignmentX(CENTER_ALIGNMENT);
        colorRow.add(whiteCard);
        colorRow.add(blackCard);
        body.add(colorRow);
        body.add(Box.createVerticalStrut(18));

        // Difficulty
        body.add(makeSectionLabel("DIFFICULTY"));
        body.add(Box.createVerticalStrut(8));

        ToggleCard easyCard   = new ToggleCard("NONE", "Easy",   "Random moves",       false, 68);
        ToggleCard mediumCard = new ToggleCard("NONE", "Medium", "Looks 2 moves ahead", true,  68);
        ToggleCard hardCard   = new ToggleCard("NONE", "Hard",   "Looks 4 moves ahead", false, 68);
        easyCard.addActionListener(e   -> { difficulty = ChessAI.Difficulty.EASY;   easyCard.setSelected(true);   mediumCard.setSelected(false); hardCard.setSelected(false); });
        mediumCard.addActionListener(e -> { difficulty = ChessAI.Difficulty.MEDIUM; mediumCard.setSelected(true); easyCard.setSelected(false);   hardCard.setSelected(false); });
        hardCard.addActionListener(e   -> { difficulty = ChessAI.Difficulty.HARD;   hardCard.setSelected(true);   easyCard.setSelected(false);   mediumCard.setSelected(false); });

        JPanel diffRow = new JPanel(new GridLayout(1, 3, 10, 0));
        diffRow.setOpaque(false);
        diffRow.setMaximumSize(new Dimension(400, 72));
        diffRow.setAlignmentX(CENTER_ALIGNMENT);
        diffRow.add(easyCard);
        diffRow.add(mediumCard);
        diffRow.add(hardCard);
        body.add(diffRow);
        body.add(Box.createVerticalStrut(24));

        // Start button
        JButton startBtn = new JButton("Start Game") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT.brighter() : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(java.awt.Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        startBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        startBtn.setPreferredSize(new Dimension(200, 44));
        startBtn.setMaximumSize(new Dimension(200, 44));
        startBtn.setContentAreaFilled(false);
        startBtn.setBorderPainted(false);
        startBtn.setFocusPainted(false);
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.setAlignmentX(CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> dispose());
        body.add(startBtn);

        root.add(body, BorderLayout.CENTER);
        setContentPane(root);
        pack();
        setLocationRelativeTo(parent);
    }

    private JLabel makeSectionLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(SUBTEXT);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lbl;
    }

    private class ToggleCard extends JPanel {
        private boolean selected;
        private final java.util.List<ActionListener> listeners = new java.util.ArrayList<>();
        private final String topText;

        ToggleCard(String topText, String name, String desc, boolean selected, int height) {
            this.topText = topText;
            this.selected = selected;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setPreferredSize(new Dimension(130, height));

            boolean hasIcon = !topText.equals("NONE");

            if (hasIcon) {
                boolean isWhite = topText.equals("WHITE");
                JLabel topLbl = new JLabel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int size = 24;
                        int x = (getWidth() - size) / 2;
                        int y = (getHeight() - size) / 2;
                        if (isWhite) {
                            g2.setColor(new java.awt.Color(235, 235, 235));
                            g2.fillOval(x, y, size, size);
                            g2.setColor(new java.awt.Color(160, 160, 160));
                            g2.setStroke(new BasicStroke(1.5f));
                            g2.drawOval(x, y, size, size);
                        } else {
                            g2.setColor(new java.awt.Color(30, 30, 30));
                            g2.fillOval(x, y, size, size);
                            g2.setColor(new java.awt.Color(100, 100, 100));
                            g2.setStroke(new BasicStroke(1.5f));
                            g2.drawOval(x, y, size, size);
                        }
                        g2.dispose();
                    }
                };
                topLbl.setPreferredSize(new Dimension(130, 34));
                topLbl.setMinimumSize(new Dimension(130, 34));
                topLbl.setMaximumSize(new Dimension(130, 34));
                topLbl.setAlignmentX(CENTER_ALIGNMENT);
                add(topLbl);
                add(Box.createVerticalStrut(4));
            }

            JLabel nameLbl = new JLabel(name, SwingConstants.CENTER);
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            nameLbl.setForeground(TEXT);
            nameLbl.setAlignmentX(CENTER_ALIGNMENT);

            JLabel descLbl = new JLabel("<html><center>" + desc + "</center></html>", SwingConstants.CENTER);
            descLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
            descLbl.setForeground(SUBTEXT);
            descLbl.setAlignmentX(CENTER_ALIGNMENT);

            add(nameLbl);
            add(Box.createVerticalStrut(3));
            add(descLbl);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    ActionEvent ae = new ActionEvent(ToggleCard.this, ActionEvent.ACTION_PERFORMED, "");
                    listeners.forEach(l -> l.actionPerformed(ae));
                    repaint();
                }
            });
        }

        public void addActionListener(ActionListener l) { listeners.add(l); }
        public void setSelected(boolean sel) { this.selected = sel; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(selected ? SEL_BG : CARD_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(selected ? SEL_BORDER : UNSEL_BORDER);
            g2.setStroke(new BasicStroke(selected ? 2f : 1f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public PieceColor getPlayerColor() { return playerColor; }
    public ChessAI.Difficulty getDifficulty() { return difficulty; }
}