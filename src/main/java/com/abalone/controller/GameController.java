package com.abalone.controller;

import java.util.List;
import java.util.Random;

import com.abalone.model.GameManager;
import com.abalone.model.utils.Move;
import com.abalone.model.utils.Players.Player;
import com.abalone.view.GameView;

public class GameController {
    private GameManager gameManager;
    private GameView gameView;
    private boolean isHumanTurn;
    private int selectedPosition = -1; // No piece selected initially

    public GameController(GameManager gameManager, GameView gameView) {
        this.gameManager = gameManager;
        this.gameView = gameView;
        this.isHumanTurn = true;
    }

    /**
     * Called when a board cell is clicked.
     * For the human turn, handles piece selection and movement.
     */
    public void handleMove(int toPosition) {
        System.out.println("Clicked position: " + toPosition);
        // Human turn: either select a piece or attempt to move the selected piece.
        if (selectedPosition == -1) {
            // No piece is selected. Attempt to select if the clicked cell contains a human piece.
            Player pieceOwner = gameManager.getBoard().getPlayerAt(toPosition);
            if (pieceOwner != null && pieceOwner.getName().equals(gameManager.getHumanPlayer().getName())) {
                selectedPosition = toPosition;
                System.out.println("Selected piece at: " + selectedPosition);
                gameView.highlightPiece(toPosition);
            } else {
                System.out.println("Selection failed! Not a human piece.");
            }
        } else {
            // A piece is already selected. Attempt to move it to the clicked cell.
            Move move = new Move(selectedPosition, toPosition);
            if (gameManager.getBoard().isValidMove(move)) {
                System.out.println("Valid move from " + selectedPosition + " to " + toPosition);
                gameManager.getBoard().applyMove(move);
                gameView.renderBoard(gameManager.getBoard());
                switchTurn(); // Switch to AI turn after a valid move
                gameManager.updatePlayersScores();
                gameView.updateScores(gameManager.getHumanScore(), gameManager.getAIScore());
                if (gameManager.isGameOver()) {
                    gameView.showGameOver(gameManager.getWinner() + " wins!");
                }
            } else {
                System.out.println("Invalid move from " + selectedPosition + " to " + toPosition);
            }
            // Reset selection regardless of move success.
            selectedPosition = -1;
            gameView.clearHighlight();
        }
    }

    /**
     * Switches the turn between Human and AI.
     */
    private void switchTurn() {
        isHumanTurn = !isHumanTurn;
        gameView.updateTurnLabel(isHumanTurn ? "Human" : "AI");

        if (!isHumanTurn) {
            aiMove();
        }
    }

    /**
     * Makes a random valid move for the AI.
     */
    private void aiMove() {
        List<Move> possibleMoves = gameManager.getBoard().getPossibleMoves(gameManager.getAIPlayer());
        if (possibleMoves.isEmpty()) {
            System.out.println("No valid moves for AI!");
            return;
        }
        Move selectedMove = possibleMoves.get(new Random().nextInt(possibleMoves.size()));
        System.out.println("AI moves: " + selectedMove);
        gameManager.getBoard().applyMove(selectedMove);
        gameView.renderBoard(gameManager.getBoard());
        // Switch back to human turn.
        switchTurn();
    }

    public boolean isHumanTurn() {
        return isHumanTurn;
    }

    public void startGame() {
        gameView.renderBoard(gameManager.getBoard());
        gameView.updateTurnLabel("Human");
    }

    public void startNewGame() {
        gameManager = new GameManager(); // Reset game state
        gameView.renderBoard(gameManager.getBoard());
        gameView.updateTurnLabel("Human");
        gameManager.updatePlayersScores();
        gameView.updateScores(gameManager.getHumanScore(), gameManager.getAIScore());
        isHumanTurn = true;
    }
}
