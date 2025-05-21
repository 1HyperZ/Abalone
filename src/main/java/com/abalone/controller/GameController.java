package com.abalone.controller;

import com.abalone.model.GameManager;
import com.abalone.model.utils.Move;
import com.abalone.model.utils.Players.Player;
import com.abalone.view.GameView;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;

/**
 * GameController class manages user moves and AI moves during the game.
 */
public class GameController {
    private GameManager gameManager;
    private final GameView gameView;
    private boolean isHumanTurn;
    private int selectedPosition;
    private AnimationTimer gameLoop;


    public GameController(GameManager gameManager, GameView gameView) {
        this.gameManager = gameManager;
        this.gameView = gameView;
        this.isHumanTurn = true;
        selectedPosition = -1; // No piece selected at start so -1
        initGameLoop();
    }

    /**
     * Called when there is a click on a board cell.
     * If no piece is selected, highlights a human piece.
     * If a piece is already selected, try to move the previously selected piece to the new clicked piece.
     * O(n)
     * 
     * @param clickedPosition the index of the clicked cell
     */
    public void clickedBoardCell(int clickedPosition) {
        if (gameManager.isGameOver()) {
            gameView.showGameOver(gameManager.getWinner() + " wins!");
            return;
        }
        if (!isHumanTurn) {
            return;
        }
        System.out.println("Clicked position: " + clickedPosition);
        if (selectedPosition == -1) {
            // Select a human piece.
            Player pieceOwner = gameManager.getBoard().getPlayerAt(clickedPosition);
            if (pieceOwner != null && pieceOwner.getName().equals(gameManager.getHumanPlayerName())) {
                selectedPosition = clickedPosition;
                System.out.println("Selected piece at: " + selectedPosition);
                gameView.highlightPiece(clickedPosition);
            } else {
                System.out.println("Selection failed! Not a human piece.");
            }
        } else {
            // try to move the previously selected piece to the new clicked piece.
            Move move = new Move(selectedPosition, clickedPosition);
            if (gameManager.getBoard().isValidMove(move)) {
                System.out.println("Valid move from " + selectedPosition + " to " + clickedPosition);
                gameManager.getBoard().applyMove(move);
                gameView.renderBoard(gameManager.getBoard());
                switchTurn();
            } else {
                System.out.println("Invalid move from " + selectedPosition + " to " + clickedPosition);
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
        gameLoop.start();
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
        gameLoop.start();
    }

    /**
     * Initializes the game loop for the animation timer.
     * O(n^3)
     * 
     * The game loop checks if the game is over and updates the UI accordingly.
     */
    private void initGameLoop() {
        gameLoop = new AnimationTimer() { // O()
            @Override
            public void handle(long now) {
                // Check if game is over.
                if (gameManager.isGameOver()) {
                    gameLoop.stop();
                    // Ensure UI updates happens.
                    Platform.runLater(() -> {
                        gameView.showGameOver(gameManager.getWinner() + " wins!");
                    });
                    return;
                }

                // If it's the AI's turn, apply AI move.
                if (!isHumanTurn) {
                    Move aiMove = gameManager.getAIMove(); // O(n^3)
                    System.out.println("AI moves: " + aiMove);
                    gameManager.getBoard().applyMove(aiMove);
                    gameManager.updatePlayersScores();

                    // Ensure UI updates happens.
                    Platform.runLater(() -> {
                        gameView.renderBoard(gameManager.getBoard());
                        gameManager.updatePlayersScores();
                        gameView.updateScores(gameManager.getHumanScore(), gameManager.getAIScore());
                        gameView.updateTurnLabel("Human");
                    });
                    isHumanTurn = true;
                }
                // Otherwise, wait for human move through clickedBoardCell function.
            }
        };
    }
}
