import java.util.List; // for legal move lists

// Sits between the UI and the board - validates moves, tracks game status,
// and rebuilds the move generator after each move
public class GameState {

    public enum Status {
        PLAYING,    // normal, no check
        CHECK,      // current player's king is under attack
        CHECKMATE,  // current player has no legal moves and is in check - they lose
        STALEMATE   // current player has no legal moves but isn't in check - draw
    }

    private Board board;
    private MoveGenerator moveGen;
    private Status status;

    public GameState() {
        board = new Board();
        moveGen = new MoveGenerator(board);
        status = Status.PLAYING;
    }

    public Board getBoard() { return board; }
    public Status getStatus() { return status; }
    public PieceColor getCurrentTurn() { return board.getCurrentTurn(); }

    public List<Move> getLegalMovesForPiece(Position pos) {
        return moveGen.getLegalMovesForPiece(pos);
    }

    // Validates and applies a move. Returns false if the move isn't legal.
    // The move coming in from the UI might be missing some info (e.g. castle type),
    // so we match it against the full legal move list to get the complete version
    public boolean makeMove(Move move) {
        List<Move> legal = moveGen.getLegalMovesForPiece(move.from);
        Move matched = null;
        for (Move m : legal) {
            if (m.to.equals(move.to) &&
                (move.type == Move.MoveType.NORMAL || m.type == move.type) &&
                (m.type != Move.MoveType.PROMOTION || m.promotionPiece == move.promotionPiece)) {
                matched = m;
                break;
            }
        }
        if (matched == null) return false; // illegal move, ignore it

        board.applyMove(matched);
        moveGen = new MoveGenerator(board); // regenerate for the new position
        updateStatus();
        return true;
    }

    // Called after every move to figure out the new game status.
    // No legal moves + in check = checkmate, no legal moves + not in check = stalemate
    private void updateStatus() {
        PieceColor current = board.getCurrentTurn();
        List<Move> legalMoves = moveGen.getLegalMoves(current);
        boolean inCheck = board.isInCheck(current);

        if (legalMoves.isEmpty())
            status = inCheck ? Status.CHECKMATE : Status.STALEMATE;
        else
            status = inCheck ? Status.CHECK : Status.PLAYING;
    }

    public boolean isGameOver() {
        return status == Status.CHECKMATE || status == Status.STALEMATE;
    }
}