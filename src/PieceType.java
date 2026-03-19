// The six piece types in chess
public enum PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN;

    // Builds the expected filename for a piece's image.
    // e.g. WHITE KNIGHT -> "white_knight.png"
    public String getImageName(PieceColor color) {
        return color.name().toLowerCase() + "_" + this.name().toLowerCase() + ".png";
    }
}