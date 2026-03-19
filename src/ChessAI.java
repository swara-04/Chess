import java.util.*;

/**
 * Chess AI using Minimax with Alpha-Beta pruning.
 * Difficulty controls search depth:
 *   EASY   = random legal move
 *   MEDIUM = depth 2
 *   HARD   = depth 4
 */
public class ChessAI {

    public enum Difficulty { EASY, MEDIUM, HARD }

    private final Difficulty difficulty;
    private final PieceColor aiColor;
    private final Random random = new Random();

    // Piece base values (centipawns)
    private static final Map<PieceType, Integer> PIECE_VALUE = new EnumMap<>(PieceType.class);
    static {
        PIECE_VALUE.put(PieceType.PAWN,   100);
        PIECE_VALUE.put(PieceType.KNIGHT, 320);
        PIECE_VALUE.put(PieceType.BISHOP, 330);
        PIECE_VALUE.put(PieceType.ROOK,   500);
        PIECE_VALUE.put(PieceType.QUEEN,  900);
        PIECE_VALUE.put(PieceType.KING,  20000);
    }

    // Piece-square tables (from white's perspective, row 0 = rank 8)
    private static final int[] PAWN_TABLE = {
         0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
         5,  5, 10, 25, 25, 10,  5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5, -5,-10,  0,  0,-10, -5,  5,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    };
    private static final int[] KNIGHT_TABLE = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
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
         5, 10, 10, 10, 10, 10, 10,  5,
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
    private static final int[] KING_MID_TABLE = {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20,
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
            case EASY:
                return moves.get(random.nextInt(moves.size()));
            case MEDIUM:
                return minimax(gameState, 2);
            case HARD:
            default:
                return minimax(gameState, 4);
        }
    }

    private Move minimax(GameState gameState, int depth) {
        MoveGenerator gen = new MoveGenerator(gameState.getBoard());
        List<Move> moves = gen.getLegalMoves(aiColor);
        if (moves.isEmpty()) return null;

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // Shuffle for variety at equal scores
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

    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean maximizing) {
        MoveGenerator gen = new MoveGenerator(board);
        PieceColor current = board.getCurrentTurn();
        List<Move> moves = gen.getLegalMoves(current);

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
                if (beta <= alpha) break;
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
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int evaluate(Board board, boolean noMoves, PieceColor sideToMove) {
        if (noMoves) {
            if (board.isInCheck(sideToMove)) {
                // Checkmate — very bad for the side to move
                return sideToMove == aiColor ? -100000 : 100000;
            }
            return 0; // stalemate
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

    private int getPieceSquareBonus(Piece piece, int row, int col) {
        // For black pieces, flip the table vertically
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