package com.abalone.controller;

import java.util.List;
import java.util.Random;
import com.abalone.model.GameManager;
import com.abalone.model.utils.Move;
import com.abalone.model.utils.Players.AIPlayer;
import com.abalone.model.utils.Players.Player;
import com.abalone.view.GameView;

/**
 * GameController manages user moves and AI moves during the game.
 */
public class GameController {
    private GameManager gameManager;
    private GameView gameView;
    private boolean isHumanTurn;
    private int selectedPosition = -1; // No piece selected initially so -1

    public GameController(GameManager gameManager, GameView gameView) {
        this.gameManager = gameManager;
        this.gameView = gameView;
        this.isHumanTurn = true;
    }

    /**
     * Processes a click on a board cell.
     * If no piece is selected, selects a human piece.
     * If a piece is already selected, attempts to move it.
     * @param toPosition the index of the clicked cell
     */
    public void handleMove(int toPosition) {
        System.out.println("Clicked position: " + toPosition);
        if (selectedPosition == -1) {
            // Select a human piece.
            Player pieceOwner = gameManager.getBoard().getPlayerAt(toPosition);
            if (pieceOwner != null && pieceOwner.getName().equals(gameManager.getHumanPlayer().getName())) {
                selectedPosition = toPosition;
                System.out.println("Selected piece at: " + selectedPosition);
                gameView.highlightPiece(toPosition);
            } else {
                System.out.println("Selection failed! Not a human piece.");
            }
        } else {
            // Attempt to move the selected piece.
            Move move = new Move(selectedPosition, toPosition);
            if (gameManager.getBoard().isValidMove(move)) {
                System.out.println("Valid move from " + selectedPosition + " to " + toPosition);
                gameManager.getBoard().applyMove(move);
                gameView.renderBoard(gameManager.getBoard());
                switchTurn();
                gameManager.updatePlayersScores();
                gameView.updateScores(gameManager.getHumanScore(), gameManager.getAIScore());
                if (gameManager.isGameOver()) {
                    gameView.showGameOver(gameManager.getWinner() + " wins!");
                }
            } else {
                System.out.println("Invalid move from " + selectedPosition + " to " + toPosition);
            }
            selectedPosition = -1;
            gameView.clearHighlight();
        }
    }

    /**
     * Switches the turn between the human and the AI.
     */
    private void switchTurn() {
        isHumanTurn = !isHumanTurn;
        gameView.updateTurnLabel(isHumanTurn ? "Human" : "AI");
        if (!isHumanTurn) {
            applyAIMove(gameManager.getAIMove());
        }
    }

    /**
     * Makes a random valid move for the AI.
        * @param selectedMove the selected move to apply 
    */
    private void applyAIMove(Move selectedMove) {
        System.out.println("AI moves: " + selectedMove);
        gameManager.getBoard().applyMove(selectedMove);
        gameView.renderBoard(gameManager.getBoard());
        switchTurn();
    }

    /**
     * Returns true if it is currently the human's turn.
     * @return true if human turn, false otherwise
     */
    public boolean isHumanTurn() {
        return isHumanTurn;
    }

    /**
     * Starts the game by rendering the board and updating the turn label.
     */
    public void startGame() {
        gameView.renderBoard(gameManager.getBoard());
        gameView.updateTurnLabel("Human");
    }

    /**
     * Resets the game state and starts a new game.
     */
    public void startNewGame() {
        gameManager = new GameManager();
        gameView.renderBoard(gameManager.getBoard());
        gameView.updateTurnLabel("Human");
        gameManager.updatePlayersScores();
        gameView.updateScores(gameManager.getHumanScore(), gameManager.getAIScore());
        isHumanTurn = true;
    }
}
