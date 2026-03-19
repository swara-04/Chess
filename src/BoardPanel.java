import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class BoardPanel extends JPanel {

    private static final int TILE_SIZE = 80;
    private static final java.awt.Color LIGHT     = new java.awt.Color(240, 217, 181);
    private static final java.awt.Color DARK      = new java.awt.Color(181, 136,  99);
    private static final java.awt.Color SELECTED  = new java.awt.Color( 20,  85,  30, 180);
    private static final java.awt.Color LEGAL     = new java.awt.Color( 20,  85,  30,  60);
    private static final java.awt.Color CHECK_CLR = new java.awt.Color(220,  50,  50, 160);
    private static final java.awt.Color LAST_MOVE = new java.awt.Color(205, 210,  50, 120);

    private final GameState gameState;
    private final ImageLoader imageLoader;
    private final StatusBar statusBar;
    private final GameWindow gameWindow;

    private final PieceColor playerColor;
    private final ChessAI ai;

    private Position selectedPos;
    private List<Move> legalMoves;
    private Move lastMove;

    public BoardPanel(GameState gameState, StatusBar statusBar, PieceColor playerColor, ChessAI ai, GameWindow gameWindow) {
        this.gameState = gameState;
        this.statusBar = statusBar;
        this.playerColor = playerColor;
        this.ai = ai;
        this.gameWindow = gameWindow;
        this.imageLoader = new ImageLoader(TILE_SIZE);
        this.legalMoves = new ArrayList<>();

        setPreferredSize(new Dimension(TILE_SIZE * 8, TILE_SIZE * 8));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX() / TILE_SIZE, e.getY() / TILE_SIZE);
            }
        });

        if (playerColor == PieceColor.BLACK) {
            SwingUtilities.invokeLater(this::triggerAIMove);
        }
    }

    public PieceColor getPlayerColor() { return playerColor; }

    private void handleClick(int col, int row) {
        if (gameState.isGameOver()) return;
        if (gameState.getCurrentTurn() != playerColor) return;

        Position clicked = new Position(row, col);
        Piece clickedPiece = gameState.getBoard().getPiece(clicked);

        if (selectedPos == null) {
            if (clickedPiece != null && clickedPiece.getColor() == playerColor) {
                selectedPos = clicked;
                legalMoves = gameState.getLegalMovesForPiece(clicked);
            }
        } else {
            Move attemptedMove = buildMove(selectedPos, clicked);
            boolean moved = gameState.makeMove(attemptedMove);

            if (moved) {
                lastMove = new Move(selectedPos, clicked);
                selectedPos = null;
                legalMoves.clear();
                statusBar.update(gameState);
                repaint();

                if (gameState.isGameOver()) {
                    gameWindow.showGameOver();
                } else {
                    Timer timer = new Timer(300, e -> triggerAIMove());
                    timer.setRepeats(false);
                    timer.start();
                }
                return;
            } else {
                if (clickedPiece != null && clickedPiece.getColor() == playerColor) {
                    selectedPos = clicked;
                    legalMoves = gameState.getLegalMovesForPiece(clicked);
                } else {
                    selectedPos = null;
                    legalMoves.clear();
                }
            }
        }
        repaint();
    }

    private void triggerAIMove() {
        if (gameState.isGameOver()) return;
        statusBar.setThinking(true);

        SwingWorker<Move, Void> worker = new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() {
                return ai.getBestMove(gameState);
            }

            @Override
            protected void done() {
                try {
                    Move aiMove = get();
                    if (aiMove != null) {
                        lastMove = aiMove;
                        gameState.makeMove(aiMove);
                        statusBar.setThinking(false);
                        statusBar.update(gameState);
                        repaint();

                        if (gameState.isGameOver()) {
                            gameWindow.showGameOver();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private Move buildMove(Position from, Position to) {
        Piece piece = gameState.getBoard().getPiece(from);
        if (piece != null && piece.getType() == PieceType.PAWN) {
            int promRow = piece.getColor() == PieceColor.WHITE ? 0 : 7;
            if (to.row == promRow) {
                PieceType promo = askPromotion();
                return new Move(from, to, Move.MoveType.PROMOTION, promo);
            }
        }
        return new Move(from, to);
    }

    private PieceType askPromotion() {
        Object[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int result = JOptionPane.showOptionDialog(this, "Promote pawn to:", "Promotion",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        switch (result) {
            case 1: return PieceType.ROOK;
            case 2: return PieceType.BISHOP;
            case 3: return PieceType.KNIGHT;
            default: return PieceType.QUEEN;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawTiles(g2);
        drawLastMove(g2);
        drawLegalMoves(g2);
        drawSelection(g2);
        drawCheck(g2);
        drawPieces(g2);
        drawCoordinates(g2);
    }

    private void drawTiles(Graphics2D g) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                g.setColor((r + c) % 2 == 0 ? LIGHT : DARK);
                g.fillRect(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
    }

    private void drawLastMove(Graphics2D g) {
        if (lastMove == null) return;
        g.setColor(LAST_MOVE);
        fillTile(g, lastMove.from);
        fillTile(g, lastMove.to);
    }

    private void drawSelection(Graphics2D g) {
        if (selectedPos == null) return;
        g.setColor(SELECTED);
        fillTile(g, selectedPos);
    }

    private void drawLegalMoves(Graphics2D g) {
        g.setColor(LEGAL);
        for (Move m : legalMoves) {
            Piece target = gameState.getBoard().getPiece(m.to);
            int x = m.to.col * TILE_SIZE;
            int y = m.to.row * TILE_SIZE;
            if (target != null) {
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            } else {
                int pad = TILE_SIZE / 3;
                g.fillOval(x + pad, y + pad, TILE_SIZE - 2 * pad, TILE_SIZE - 2 * pad);
            }
        }
    }

    private void drawCheck(Graphics2D g) {
        Board board = gameState.getBoard();
        if (board.isInCheck(board.getCurrentTurn())) {
            Position kingPos = board.findKing(board.getCurrentTurn());
            if (kingPos != null) {
                g.setColor(CHECK_CLR);
                fillTile(g, kingPos);
            }
        }
    }

    private void drawPieces(Graphics2D g) {
        Board board = gameState.getBoard();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece == null) continue;
                Image img = imageLoader.getImage(piece);
                if (img != null) {
                    g.drawImage(img, c * TILE_SIZE + 5, r * TILE_SIZE + 5,
                        TILE_SIZE - 10, TILE_SIZE - 10, this);
                } else {
                    drawFallbackPiece(g, piece, r, c);
                }
            }
        }
    }

    private void drawFallbackPiece(Graphics2D g, Piece piece, int row, int col) {
        String symbol;
        switch (piece.getType()) {
            case KING:   symbol = "♔"; break;
            case QUEEN:  symbol = "♕"; break;
            case ROOK:   symbol = "♖"; break;
            case BISHOP: symbol = "♗"; break;
            case KNIGHT: symbol = "♘"; break;
            default:     symbol = "♙"; break;
        }
        g.setFont(new Font("Serif", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        int x = col * TILE_SIZE + (TILE_SIZE - fm.stringWidth(symbol)) / 2;
        int y = row * TILE_SIZE + (TILE_SIZE + fm.getAscent()) / 2 - 4;
        g.setColor(piece.getColor() == PieceColor.WHITE ? java.awt.Color.DARK_GRAY : java.awt.Color.LIGHT_GRAY);
        g.drawString(symbol, x + 1, y + 1);
        g.setColor(piece.getColor() == PieceColor.WHITE ? java.awt.Color.WHITE : java.awt.Color.BLACK);
        g.drawString(symbol, x, y);
    }

    private void drawCoordinates(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        for (int i = 0; i < 8; i++) {
            g.setColor(i % 2 == 0 ? DARK : LIGHT);
            g.drawString(String.valueOf(8 - i), 3, i * TILE_SIZE + 14);
            g.setColor(i % 2 == 0 ? LIGHT : DARK);
            g.drawString(String.valueOf((char)('a' + i)), i * TILE_SIZE + TILE_SIZE - 12, 8 * TILE_SIZE - 3);
        }
    }

    private void fillTile(Graphics2D g, Position pos) {
        g.fillRect(pos.col * TILE_SIZE, pos.row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    public void resetGame() {
        selectedPos = null;
        legalMoves.clear();
        lastMove = null;
        repaint();
    }
}