import java.util.ArrayList;
import java.util.List;

// Generates all legal moves for a given position.
// "Legal" means the move doesn't leave the moving side's king in check.
// Pseudo-legal moves are generated first, then filtered by simulating each one
public class MoveGenerator {

    private final Board board;

    public MoveGenerator(Board board) {
        this.board = board;
    }

    // All legal moves for every piece of the given color
    public List<Move> getLegalMoves(PieceColor color) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.getColor() == color)
                    moves.addAll(getLegalMovesForPiece(new Position(r, c)));
            }
        return moves;
    }

    // Legal moves for a single piece - filters out anything that leaves the king in check
    public List<Move> getLegalMovesForPiece(Position from) {
        List<Move> pseudo = getPseudoLegalMoves(from);
        List<Move> legal = new ArrayList<>();
        for (Move m : pseudo)
            if (!leavesKingInCheck(m)) legal.add(m);
        return legal;
    }

    // Generates all moves a piece can physically make, ignoring check
    private List<Move> getPseudoLegalMoves(Position from) {
        List<Move> moves = new ArrayList<>();
        Piece piece = board.getPiece(from);
        if (piece == null) return moves;

        switch (piece.getType()) {
            case PAWN:   generatePawnMoves(from, piece.getColor(), moves);                   break;
            case KNIGHT: generateKnightMoves(from, piece.getColor(), moves);                 break;
            case BISHOP: generateSlidingMoves(from, piece.getColor(), moves, true, false);   break;
            case ROOK:   generateSlidingMoves(from, piece.getColor(), moves, false, true);   break;
            case QUEEN:  generateSlidingMoves(from, piece.getColor(), moves, true, true);    break;
            case KING:   generateKingMoves(from, piece.getColor(), moves);                   break;
        }
        return moves;
    }

    private void generatePawnMoves(Position from, PieceColor color, List<Move> moves) {
        int dir = color == PieceColor.WHITE ? -1 : 1; // white moves up (negative row), black moves down
        int startRow = color == PieceColor.WHITE ? 6 : 1;
        int promRow  = color == PieceColor.WHITE ? 0 : 7; // promotion rank

        // one square forward
        Position one = from.offset(dir, 0);
        if (one.isValid() && board.getPiece(one) == null) {
            if (one.row == promRow) addPromotionMoves(from, one, moves);
            else moves.add(new Move(from, one));

            // two squares from starting rank - only if the one-square path is clear
            if (from.row == startRow) {
                Position two = from.offset(2 * dir, 0);
                if (board.getPiece(two) == null)
                    moves.add(new Move(from, two));
            }
        }

        // diagonal captures (including en passant)
        for (int dc : new int[]{-1, 1}) {
            Position cap = from.offset(dir, dc);
            if (!cap.isValid()) continue;
            Piece target = board.getPiece(cap);
            if (target != null && target.getColor() != color) {
                if (cap.row == promRow) addPromotionMoves(from, cap, moves);
                else moves.add(new Move(from, cap));
            }
            // en passant - capture into the square behind the enemy pawn
            if (cap.equals(board.getEnPassantTarget()))
                moves.add(new Move(from, cap, Move.MoveType.EN_PASSANT));
        }
    }

    // Always generate all four promotion options - the player picks in the UI
    private void addPromotionMoves(Position from, Position to, List<Move> moves) {
        for (PieceType pt : new PieceType[]{PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT})
            moves.add(new Move(from, to, Move.MoveType.PROMOTION, pt));
    }

    private void generateKnightMoves(Position from, PieceColor color, List<Move> moves) {
        int[][] deltas = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
        for (int[] d : deltas) {
            Position to = from.offset(d[0], d[1]);
            if (!to.isValid()) continue;
            Piece target = board.getPiece(to);
            if (target == null || target.getColor() != color)
                moves.add(new Move(from, to));
        }
    }

    // Shared logic for bishop, rook, queen - slides until it hits something
    private void generateSlidingMoves(Position from, PieceColor color, List<Move> moves,
                                       boolean diagonal, boolean straight) {
        List<int[]> dirs = new ArrayList<>();
        if (diagonal) { dirs.add(new int[]{-1,-1}); dirs.add(new int[]{-1,1}); dirs.add(new int[]{1,-1}); dirs.add(new int[]{1,1}); }
        if (straight) { dirs.add(new int[]{-1,0});  dirs.add(new int[]{1,0});  dirs.add(new int[]{0,-1}); dirs.add(new int[]{0,1});  }

        for (int[] d : dirs) {
            Position cur = from.offset(d[0], d[1]);
            while (cur.isValid()) {
                Piece target = board.getPiece(cur);
                if (target == null) {
                    moves.add(new Move(from, cur));
                } else {
                    if (target.getColor() != color) moves.add(new Move(from, cur)); // can capture
                    break; // blocked either way
                }
                cur = cur.offset(d[0], d[1]);
            }
        }
    }

    private void generateKingMoves(Position from, PieceColor color, List<Move> moves) {
        // normal one-square moves
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                Position to = from.offset(dr, dc);
                if (!to.isValid()) continue;
                Piece target = board.getPiece(to);
                if (target == null || target.getColor() != color)
                    moves.add(new Move(from, to));
            }
        }

        // castling - only if king hasn't moved and isn't currently in check
        Piece king = board.getPiece(from);
        if (king != null && !king.hasMoved() && !board.isInCheck(color)) {
            tryCastle(from, color, true, moves);  // kingside
            tryCastle(from, color, false, moves); // queenside
        }
    }

    // Validates castling conditions for one side and adds the move if everything checks out
    private void tryCastle(Position kingPos, PieceColor color, boolean kingside, List<Move> moves) {
        int row = kingPos.row;
        int rookCol = kingside ? 7 : 0;
        Piece rook = board.getPiece(new Position(row, rookCol));
        if (rook == null || rook.getType() != PieceType.ROOK || rook.hasMoved()) return;

        // squares between king and rook must all be empty
        int startCol = kingside ? 5 : 1;
        int endCol   = kingside ? 6 : 3;
        for (int c = startCol; c <= endCol; c++)
            if (board.getPiece(row, c) != null) return;

        // king can't pass through or land on an attacked square
        int[] passCols = kingside ? new int[]{5, 6} : new int[]{3, 2};
        for (int c : passCols)
            if (board.isAttackedBy(new Position(row, c), color.opposite())) return;

        Move.MoveType type = kingside ? Move.MoveType.CASTLE_KINGSIDE : Move.MoveType.CASTLE_QUEENSIDE;
        moves.add(new Move(kingPos, new Position(row, kingside ? 6 : 2), type));
    }

    // Simulates the move on a copy of the board and checks if the king ends up in check
    private boolean leavesKingInCheck(Move move) {
        Board testBoard = board.copy();
        testBoard.applyMove(move);
        PieceColor movedColor = board.getCurrentTurn();
        return testBoard.isInCheck(movedColor);
    }
}