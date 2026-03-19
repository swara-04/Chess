// Represents a single move from one square to another, plus any special move type
public class Move {
    public final Position from;
    public final Position to;
    public final MoveType type;
    public final PieceType promotionPiece; // only set when type == PROMOTION

    public enum MoveType {
        NORMAL,
        CASTLE_KINGSIDE,
        CASTLE_QUEENSIDE,
        EN_PASSANT,
        PROMOTION
    }

    // Most moves are just NORMAL
    public Move(Position from, Position to) {
        this(from, to, MoveType.NORMAL, null);
    }

    public Move(Position from, Position to, MoveType type) {
        this(from, to, type, null);
    }

    public Move(Position from, Position to, MoveType type, PieceType promotionPiece) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.promotionPiece = promotionPiece;
    }

    @Override
    public String toString() {
        return from + "->" + to + (type != MoveType.NORMAL ? " (" + type + ")" : "");
    }
}