package com.abalone;

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

    public Board getBoard() {
        return board;
    }

    public AIPlayer getAIPlayer() {
        return aiPlayer;
    }

    public Player getHumanPlayer() {
        return humanPlayer;
    }
    
    public int getHumanScore() {
        return humanScore;
    }

    public int getAIScore() {
        return aiScore;
    }

    public boolean isHumanTurn() {
        return isHumanTurn;
    }

    public void switchTurn() {
        isHumanTurn = !isHumanTurn;
    }

    public void updatePlayersScores() {
        int humanCount  = 0, aiCount  = 0;
    
        for (Player p : board.getPlayersOnBoard()) {
            if (p.getName().equals(humanPlayer.getName())) humanCount++;
            if (p.getName().equals(aiPlayer.getName())) aiCount ++;
        }
        
        this.humanScore = humanCount;
        this.aiScore = aiCount;
    }

    public boolean isGameOver() {
        return this.humanScore <= 8 || this.aiScore <= 8; // Only return true if a player lost
    }
    
    public String getWinner() {
        
        if (this.aiScore <= 8) return "Human";  // White lost, so Black wins
        if (this.humanScore <= 8) return "AI";  // Black lost, so White wins
    
        return "No winner";  // Shouldn't happen in a finished game
    }

    
}
