import java.util.List;

public class GameState {

    public enum Status {
        PLAYING, CHECK, CHECKMATE, STALEMATE
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
        if (matched == null) return false;

        board.applyMove(matched);
        moveGen = new MoveGenerator(board);
        updateStatus();
        return true;
    }

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