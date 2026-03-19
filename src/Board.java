// ArrayList for storing lists of positions/directions, List as the interface
import java.util.ArrayList;
import java.util.List;

// Stores the entire board state - the grid of pieces, whose turn it is,
// en passant availability, and move history
public class Board {
    private final Piece[][] grid;         // 8x8 array, [row][col], row 0 = black's back rank
    private PieceColor currentTurn;       // who moves next
    private Position enPassantTarget;     // the square a pawn can capture into via en passant (null if not available)
    private String moveHistory;           // running log of moves played

    public Board() {
        grid = new Piece[8][8];
        currentTurn = PieceColor.WHITE;
        enPassantTarget = null;
        moveHistory = "";
        setupStartingPosition();
    }

    // Places all 32 pieces in their standard starting squares
    private void setupStartingPosition() {
        placePiece(0, 0, PieceType.ROOK,   PieceColor.BLACK);
        placePiece(0, 1, PieceType.KNIGHT, PieceColor.BLACK);
        placePiece(0, 2, PieceType.BISHOP, PieceColor.BLACK);
        placePiece(0, 3, PieceType.QUEEN,  PieceColor.BLACK);
        placePiece(0, 4, PieceType.KING,   PieceColor.BLACK);
        placePiece(0, 5, PieceType.BISHOP, PieceColor.BLACK);
        placePiece(0, 6, PieceType.KNIGHT, PieceColor.BLACK);
        placePiece(0, 7, PieceType.ROOK,   PieceColor.BLACK);
        for (int c = 0; c < 8; c++) placePiece(1, c, PieceType.PAWN, PieceColor.BLACK);

        placePiece(7, 0, PieceType.ROOK,   PieceColor.WHITE);
        placePiece(7, 1, PieceType.KNIGHT, PieceColor.WHITE);
        placePiece(7, 2, PieceType.BISHOP, PieceColor.WHITE);
        placePiece(7, 3, PieceType.QUEEN,  PieceColor.WHITE);
        placePiece(7, 4, PieceType.KING,   PieceColor.WHITE);
        placePiece(7, 5, PieceType.BISHOP, PieceColor.WHITE);
        placePiece(7, 6, PieceType.KNIGHT, PieceColor.WHITE);
        placePiece(7, 7, PieceType.ROOK,   PieceColor.WHITE);
        for (int c = 0; c < 8; c++) placePiece(6, c, PieceType.PAWN, PieceColor.WHITE);
    }

    private void placePiece(int row, int col, PieceType type, PieceColor color) {
        grid[row][col] = new Piece(type, color);
    }

    // Returns null if position is off the board
    public Piece getPiece(Position pos) {
        if (!pos.isValid()) return null;
        return grid[pos.row][pos.col];
    }

    public Piece getPiece(int row, int col) {
        return grid[row][col];
    }

    public PieceColor getCurrentTurn() { return currentTurn; }
    public Position getEnPassantTarget() { return enPassantTarget; }
    public String getMoveHistory() { return moveHistory; }

    // Applies a move directly to the board - assumes the move is already validated
    public void applyMove(Move move) {
        Piece piece = getPiece(move.from);
        Position newEnPassant = null;

        switch (move.type) {
            case NORMAL:
            case PROMOTION:
                movePiece(move.from, move.to);
                if (move.type == Move.MoveType.PROMOTION) {
                    PieceType promo = move.promotionPiece != null ? move.promotionPiece : PieceType.QUEEN;
                    grid[move.to.row][move.to.col] = new Piece(promo, piece.getColor());
                }
                // if a pawn just moved two squares, mark the skipped square for en passant
                if (piece.getType() == PieceType.PAWN && Math.abs(move.to.row - move.from.row) == 2) {
                    int epRow = (move.from.row + move.to.row) / 2;
                    newEnPassant = new Position(epRow, move.from.col);
                }
                break;
            case EN_PASSANT:
                movePiece(move.from, move.to);
                // remove the captured pawn - it's on the same row as the capturing pawn's origin
                grid[move.from.row][move.to.col] = null;
                break;
            case CASTLE_KINGSIDE:
                movePiece(move.from, move.to);
                movePiece(new Position(move.from.row, 7), new Position(move.from.row, 5));
                break;
            case CASTLE_QUEENSIDE:
                movePiece(move.from, move.to);
                movePiece(new Position(move.from.row, 0), new Position(move.from.row, 3));
                break;
        }

        enPassantTarget = newEnPassant;
        moveHistory += move + "\n";
        currentTurn = currentTurn.opposite();
    }

    private void movePiece(Position from, Position to) {
        Piece p = grid[from.row][from.col];
        grid[to.row][to.col] = p;
        grid[from.row][from.col] = null;
        if (p != null) p.setMoved(true); // needed for castling rights
    }

    // Scans the board to find the king - used for check detection
    public Position findKing(PieceColor color) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (grid[r][c] != null &&
                    grid[r][c].getType() == PieceType.KING &&
                    grid[r][c].getColor() == color)
                    return new Position(r, c);
        return null;
    }

    // A king is in check if any opponent piece can attack its square
    public boolean isInCheck(PieceColor color) {
        Position kingPos = findKing(color);
        if (kingPos == null) return false;
        return isAttackedBy(kingPos, color.opposite());
    }

    // Checks if a given square is attacked by any piece of attackerColor
    // Used both for check detection and for validating castling paths
    public boolean isAttackedBy(Position pos, PieceColor attackerColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p == null || p.getColor() != attackerColor) continue;
                List<Position> attacks = getAttackSquares(new Position(r, c));
                if (attacks.contains(pos)) return true;
            }
        }
        return false;
    }

    // Returns all squares a piece can attack - ignores whether it leaves the king in check
    // (that's handled in MoveGenerator)
    public List<Position> getAttackSquares(Position from) {
        List<Position> squares = new ArrayList<>();
        Piece piece = getPiece(from);
        if (piece == null) return squares;

        switch (piece.getType()) {
            case PAWN:   addPawnAttacks(from, piece.getColor(), squares);                    break;
            case KNIGHT: addKnightSquares(from, piece.getColor(), squares);                  break;
            case BISHOP: addSlidingMoves(from, piece.getColor(), squares, true, false);      break;
            case ROOK:   addSlidingMoves(from, piece.getColor(), squares, false, true);      break;
            case QUEEN:  addSlidingMoves(from, piece.getColor(), squares, true, true);       break;
            case KING:   addKingSquares(from, piece.getColor(), squares);                    break;
        }
        return squares;
    }

    // Pawns only attack diagonally (not the squares they move to)
    private void addPawnAttacks(Position from, PieceColor color, List<Position> squares) {
        int dir = color == PieceColor.WHITE ? -1 : 1;
        Position left  = from.offset(dir, -1);
        Position right = from.offset(dir,  1);
        if (left.isValid())  squares.add(left);
        if (right.isValid()) squares.add(right);
    }

    private void addKnightSquares(Position from, PieceColor color, List<Position> squares) {
        int[][] deltas = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
        for (int[] d : deltas) {
            Position to = from.offset(d[0], d[1]);
            if (!to.isValid()) continue;
            Piece target = getPiece(to);
            if (target == null || target.getColor() != color) squares.add(to);
        }
    }

    // Handles bishops, rooks, and queens - slides in each direction until blocked
    private void addSlidingMoves(Position from, PieceColor color, List<Position> squares,
                                  boolean diagonal, boolean straight) {
        List<int[]> dirs = new ArrayList<>();
        if (diagonal) { dirs.add(new int[]{-1,-1}); dirs.add(new int[]{-1,1}); dirs.add(new int[]{1,-1}); dirs.add(new int[]{1,1}); }
        if (straight) { dirs.add(new int[]{-1,0});  dirs.add(new int[]{1,0});  dirs.add(new int[]{0,-1}); dirs.add(new int[]{0,1});  }

        for (int[] d : dirs) {
            Position cur = from.offset(d[0], d[1]);
            while (cur.isValid()) {
                Piece target = getPiece(cur);
                if (target == null) {
                    squares.add(cur);
                } else {
                    if (target.getColor() != color) squares.add(cur); // can capture
                    break; // blocked either way
                }
                cur = cur.offset(d[0], d[1]);
            }
        }
    }

    private void addKingSquares(Position from, PieceColor color, List<Position> squares) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                Position to = from.offset(dr, dc);
                if (!to.isValid()) continue;
                Piece target = getPiece(to);
                if (target == null || target.getColor() != color) squares.add(to);
            }
        }
    }

    // Deep copy of the board - used by MoveGenerator to test moves without
    // affecting the actual game state
    public Board copy() {
        Board copy = new Board();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null) {
                    Piece cp = new Piece(p.getType(), p.getColor());
                    if (p.hasMoved()) cp.setMoved(true);
                    copy.grid[r][c] = cp;
                } else {
                    copy.grid[r][c] = null;
                }
            }
        copy.currentTurn = this.currentTurn;
        copy.enPassantTarget = this.enPassantTarget;
        copy.moveHistory = this.moveHistory;
        return copy;
    }
}