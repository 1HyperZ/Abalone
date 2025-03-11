package com.abalone.model;

import com.abalone.model.utils.Move;
import com.abalone.model.utils.Players.AIPlayer;
import com.abalone.model.utils.Players.Player;

public class GameManager {
    private Board board;
    private Player humanPlayer;
    private AIPlayer aiPlayer;
    private boolean isHumanTurn;
    private int humanScore;
    private int aiScore;

    public GameManager() {
        this.humanPlayer = new Player("Human");
        this.aiPlayer = new AIPlayer("AI");
        this.board = new Board(aiPlayer, humanPlayer);
        this.isHumanTurn = true;
        this.aiScore = 14;
        this.humanScore = 14;
    }

    /**
     * asks the AIPlayer to generate a move .
     * @return the chosen Move, or null if no moves are available
     */
    public Move getAIMove() {
       return aiPlayer.generateAIMove(board);
    }

    /**
     * @return the current Board instance
     */
    public Board getBoard() {
        return board;
    }

    /**
     * @return the AI Player name
     */
    public String getAIPlayerName() {
        return aiPlayer.getName();
    }

    /**
     * @return the human Player name
     */
    public String getHumanPlayerName() {
        return humanPlayer.getName();
    }
    
    /**
     * @return the human current score
     */
    public int getHumanScore() {
        return humanScore;
    }

    /**
     * @return the AI current score
     */
    public int getAIScore() {
        return aiScore;
    }

    /**
     * @return true if it is the human's turn, false otherwise
     */
    public boolean isHumanTurn() {
        return isHumanTurn;
    }

    /**
     * Switches the turn between human and AI.
     */
    public void switchTurn() {
        isHumanTurn = !isHumanTurn;
    }

    /**
     * Updates the scores for both players by counting their balls on the board.
     */
    public void updatePlayersScores() {
        int humanCount  = 0, aiCount  = 0;
        for (Player p : board.getPlayersOnBoard()) {
            if (p.getName().equals(humanPlayer.getName())) humanCount++;
            if (p.getName().equals(aiPlayer.getName())) aiCount++;
        }
        this.humanScore = humanCount;
        this.aiScore = aiCount;
    }

    /**
     * @return true if either player has 8 or fewer balls, which means game over
     */
    public boolean isGameOver() {
        return this.humanScore <= 8 || this.aiScore <= 8;
    }
    
     /**
     * @return the winner's name if the game is over, otherwise "No winner"
     */
    public String getWinner() {
        if (this.aiScore <= 8) return "Human";
        if (this.humanScore <= 8) return "AI";
        return "No winner";
    }

    
}
