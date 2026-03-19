public class Move {
    public final Position from;
    public final Position to;
    public final MoveType type;
    public final PieceType promotionPiece;

    public enum MoveType {
        NORMAL, CASTLE_KINGSIDE, CASTLE_QUEENSIDE, EN_PASSANT, PROMOTION
    }

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