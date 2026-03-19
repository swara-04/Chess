# ♟ Java Chess

A fully-featured Java Swing chess game with proper move generation, check/checkmate/stalemate detection, castling, en passant, and pawn promotion.

---

## Project Structure

```
chess/
├── images/              ← DROP YOUR PNGs HERE
│   ├── white_king.png
│   ├── white_queen.png
│   ├── white_rook.png
│   ├── white_bishop.png
│   ├── white_knight.png
│   ├── white_pawn.png
│   ├── black_king.png
│   ├── black_queen.png
│   ├── black_rook.png
│   ├── black_bishop.png
│   ├── black_knight.png
│   └── black_pawn.png
└── src/
    └── chess/
        ├── Main.java          - Entry point
        ├── Color.java         - WHITE / BLACK enum
        ├── PieceType.java     - KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
        ├── Piece.java         - A single chess piece
        ├── Position.java      - Board coordinate (row, col)
        ├── Move.java          - A move (from, to, type)
        ├── Board.java         - Board state + check detection
        ├── MoveGenerator.java - Legal move generation (all rules)
        ├── GameState.java     - Overall game logic + status
        ├── ImageLoader.java   - Loads/scales your PNGs
        ├── BoardPanel.java    - Swing board renderer + click handler
        ├── StatusBar.java     - Turn/check/checkmate display
        └── GameWindow.java    - Main JFrame window
```

---

## Image Naming Convention

Name your PNG files **exactly** like this:

| File name           | Description         |
|---------------------|---------------------|
| `white_king.png`    | White king          |
| `white_queen.png`   | White queen         |
| `white_rook.png`    | White rook          |
| `white_bishop.png`  | White bishop        |
| `white_knight.png`  | White knight        |
| `white_pawn.png`    | White pawn          |
| `black_king.png`    | Black king          |
| `black_queen.png`   | Black queen         |
| `black_rook.png`    | Black rook          |
| `black_bishop.png`  | Black bishop        |
| `black_knight.png`  | Black knight        |
| `black_pawn.png`    | Black pawn          |

All 12 files go in the `images/` folder.  
Images are **auto-scaled** to fit the tile size — any resolution works.

> **No images?** No problem — the game falls back to Unicode chess symbols automatically, so it's playable even without images.

---

## How to Compile & Run

### From the command line (from inside the `chess/` folder):

```bash
# Compile
javac -d out src/chess/*.java

# Run (run from the chess/ folder so images/ is found)
java -cp out chess.Main
```

### From an IDE (IntelliJ / Eclipse / VS Code):

1. Open the `chess/` folder as a project.
2. Mark `src/` as the sources root.
3. Run `chess.Main`.
4. Make sure the **working directory** is set to the `chess/` folder (not `src/`) so the `images/` folder is found.

---

## Features

- ✅ Full legal move generation (no illegal moves allowed)
- ✅ Check / Checkmate / Stalemate detection
- ✅ Castling (kingside & queenside)
- ✅ En passant
- ✅ Pawn promotion (dialog to choose piece)
- ✅ Visual move highlights (selected piece, legal moves, last move, king in check)
- ✅ Coordinate labels (a-h, 1-8) on the board
- ✅ New Game via menu
- ✅ Fallback Unicode pieces if images are missing
