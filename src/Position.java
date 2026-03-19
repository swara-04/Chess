import java.util.Objects; // for Objects.hash in hashCode

// A board coordinate. Row 0 is the top of the board (black's back rank), row 7 is the bottom.
// Col 0 is the a-file, col 7 is the h-file.
public class Position {
    public final int row;
    public final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    // Returns a new position offset by dRow/dCol - used when generating moves
    public Position offset(int dRow, int dCol) {
        return new Position(row + dRow, col + dCol);
    }

    // Needed so positions can be compared in lists (e.g. checking if a square is attacked)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return row == p.row && col == p.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    // Converts to standard chess notation e.g. (6, 4) -> "e2"
    @Override
    public String toString() {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }
}