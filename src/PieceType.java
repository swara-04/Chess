public enum PieceType {
    KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN;

    public String getImageName(PieceColor color) {
        return color.name().toLowerCase() + "_" + this.name().toLowerCase() + ".png";
    }
}