// Which side a piece belongs to - also used to track whose turn it is
public enum PieceColor {
    WHITE, BLACK;

    // Convenience method so we don't have to write ternaries everywhere
    public PieceColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}