// Collections for shuffling moves, Map/EnumMap for piece value lookup
import java.util.*;

// Chess AI using Minimax with Alpha-Beta pruning.
// Difficulty controls how many moves ahead it looks:
//   EASY   = picks a random legal move
//   MEDIUM = looks 2 moves ahead
//   HARD   = looks 4 moves ahead
public class ChessAI {

    public enum Difficulty { EASY, MEDIUM, HARD }

    private final Difficulty difficulty;
    private final PieceColor aiColor;
    private final Random random = new Random();

    // Standard piece values in centipawns (100 = 1 pawn)
    // These are well-established values used in most chess engines
    private static final Map<PieceType, Integer> PIECE_VALUE = new EnumMap<>(PieceType.class);
    static {
        PIECE_VALUE.put(PieceType.PAWN,   100);
        PIECE_VALUE.put(PieceType.KNIGHT, 320);
        PIECE_VALUE.put(PieceType.BISHOP, 330);
        PIECE_VALUE.put(PieceType.ROOK,   500);
        PIECE_VALUE.put(PieceType.QUEEN,  900);
        PIECE_VALUE.put(PieceType.KING,  20000); // arbitrarily high so the AI never sacrifices it
    }

    // Piece-square tables give a positional bonus/penalty depending on where a piece stands.
    // Stored from white's perspective (row 0 = rank 8, row 7 = rank 1).
    // For black pieces we just flip the row index when looking up.
    private static final int[] PAWN_TABLE = {
         0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50, // advanced pawns are strong
        10, 10, 20, 30, 30, 20, 10, 10,
         5,  5, 10, 25, 25, 10,  5,  5,
         0,  0,  0, 20, 20,  0,  0,  0, // center control bonus
         5, -5,-10,  0,  0,-10, -5,  5,
         5, 10, 10,-20,-20, 10, 10,  5, // penalise blocking centre pawns
         0,  0,  0,  0,  0,  0,  0,  0
    };
    private static final int[] KNIGHT_TABLE = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30, // knights are best in the centre
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50  // knights on the rim are dim
    };
    private static final int[] BISHOP_TABLE = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };
    private static final int[] ROOK_TABLE = {
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10, 10, 10, 10, 10,  5, // rooks are great on the 7th rank
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
         0,  0,  0,  5,  5,  0,  0,  0
    };
    private static final int[] QUEEN_TABLE = {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
          0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };
    // King wants to stay safe (castled) in the middlegame - heavily penalised in the centre
    private static final int[] KING_MID_TABLE = {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20, // behind pawns after castling
         20, 30, 10,  0,  0, 10, 30, 20
    };

    public ChessAI(Difficulty difficulty, PieceColor aiColor) {
        this.difficulty = difficulty;
        this.aiColor = aiColor;
    }

    public Move getBestMove(GameState gameState) {
        MoveGenerator gen = new MoveGenerator(gameState.getBoard());
        List<Move> moves = gen.getLegalMoves(aiColor);
        if (moves.isEmpty()) return null;

        switch (difficulty) {
            case EASY:   return moves.get(random.nextInt(moves.size())); // just pick randomly
            case MEDIUM: return minimax(gameState, 2);
            case HARD:
            default:     return minimax(gameState, 4);
        }
    }

    // Entry point for the search - finds the best move at the given depth
    private Move minimax(GameState gameState, int depth) {
        MoveGenerator gen = new MoveGenerator(gameState.getBoard());
        List<Move> moves = gen.getLegalMoves(aiColor);
        if (moves.isEmpty()) return null;

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // shuffle so equal-scoring moves don't always play the same way
        Collections.shuffle(moves, random);

        for (Move move : moves) {
            Board copy = gameState.getBoard().copy();
            copy.applyMove(move);
            int score = alphaBeta(copy, depth - 1, alpha, beta, false);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
        }
        return bestMove;
    }

    // Minimax with alpha-beta pruning.
    // alpha = best score the maximiser can guarantee so far
    // beta  = best score the minimiser can guarantee so far
    // When beta <= alpha we can stop searching this branch (it won't be chosen)
    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean maximizing) {
        MoveGenerator gen = new MoveGenerator(board);
        PieceColor current = board.getCurrentTurn();
        List<Move> moves = gen.getLegalMoves(current);

        // base case: depth reached or no moves left (checkmate/stalemate)
        if (depth == 0 || moves.isEmpty()) {
            return evaluate(board, moves.isEmpty(), current);
        }

        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                Board copy = board.copy();
                copy.applyMove(move);
                int eval = alphaBeta(copy, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // beta cutoff - minimiser won't allow this
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                Board copy = board.copy();
                copy.applyMove(move);
                int eval = alphaBeta(copy, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // alpha cutoff - maximiser won't allow this
            }
            return minEval;
        }
    }

    // Static board evaluation - positive = good for AI, negative = bad
    // Adds up material value + positional bonuses for every piece on the board
    private int evaluate(Board board, boolean noMoves, PieceColor sideToMove) {
        if (noMoves) {
            if (board.isInCheck(sideToMove)) {
                // checkmate - whoever is to move has lost
                return sideToMove == aiColor ? -100000 : 100000;
            }
            return 0; // stalemate is a draw
        }

        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p == null) continue;
                int val = PIECE_VALUE.get(p.getType()) + getPieceSquareBonus(p, r, c);
                score += (p.getColor() == aiColor) ? val : -val;
            }
        }
        return score;
    }

    // Looks up positional bonus from the piece-square table
    // Black's table is mirrored vertically since the tables are written from white's POV
    private int getPieceSquareBonus(Piece piece, int row, int col) {
        int tableRow = (piece.getColor() == PieceColor.WHITE) ? row : (7 - row);
        int idx = tableRow * 8 + col;

        switch (piece.getType()) {
            case PAWN:   return PAWN_TABLE[idx];
            case KNIGHT: return KNIGHT_TABLE[idx];
            case BISHOP: return BISHOP_TABLE[idx];
            case ROOK:   return ROOK_TABLE[idx];
            case QUEEN:  return QUEEN_TABLE[idx];
            case KING:   return KING_MID_TABLE[idx];
            default:     return 0;
        }
    }
}