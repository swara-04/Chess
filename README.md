# Chess

A two-player chess game built in Java with a Swing GUI. Supports playing against a computer opponent.

## Requirements

- Java JDK 11 or higher

## Installation

1. Clone the repository
2. Add your piece images to the `images/` folder (see [Images](#images) below)

## Usage

Compile and run from the `chess` directory:

```bash
javac -d out src/*.java
java -cp out Main
```

## Images

The game loads piece images from the `images/` folder. Files must be named using the format `color_piece.png`, for example:

```
white_king.png
white_queen.png
white_rook.png
white_bishop.png
white_knight.png
white_pawn.png
black_king.png
black_queen.png
black_rook.png
black_bishop.png
black_knight.png
black_pawn.png
```

If no images are found, the game falls back to Unicode chess symbols.

## Features

- Full chess rule implementation including castling, en passant, and pawn promotion
- Check, checkmate, and stalemate detection
- Computer opponent using Minimax with Alpha-Beta pruning
- Three difficulty levels: Easy, Medium, Hard
- Choose your side at the start of each game

## Project Structure

```
chess/
├── src/
│   ├── Main.java
│   ├── Board.java
│   ├── BoardPanel.java
│   ├── ChessAI.java
│   ├── GameOverDialog.java
│   ├── GameState.java
│   ├── GameWindow.java
│   ├── ImageLoader.java
│   ├── Move.java
│   ├── MoveGenerator.java
│   ├── NewGameDialog.java
│   ├── Piece.java
│   ├── PieceColor.java
│   ├── PieceType.java
│   ├── Position.java
│   └── StatusBar.java
├── images/
└── out/
```
