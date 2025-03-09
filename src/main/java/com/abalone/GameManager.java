package com.abalone;

public class GameManager {
    private Board board;
    private Player humanPlayer;
    private AIPlayer aiPlayer;
    private boolean isHumanTurn;

    public GameManager() {
        this.humanPlayer = new Player("Human");
        this.aiPlayer = new AIPlayer("AI");
        this.board = new Board(aiPlayer, humanPlayer);
        this.isHumanTurn = true;
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
    

    public boolean isHumanTurn() {
        return isHumanTurn;
    }

    public void switchTurn() {
        isHumanTurn = !isHumanTurn;
    }

    public boolean isGameOver() {
        int humanCount  = 0, aiCount  = 0;
    
        for (Player p : board.getPlayersOnBoard()) {
            if (p.getName().equals(humanPlayer.getName())) humanCount++;
            if (p.getName().equals(aiPlayer.getName())) aiCount ++;
        }
    
        return humanCount <= 8 || aiCount <= 8; // Only return true if a player lost
    }
    
    public String getWinner() {
        int humanCount  = 0, aiCount  = 0;
    
        for (Player p : board.getPlayersOnBoard()) {
            if (p.getName().equals(humanPlayer.getName())) humanCount++;
            if (p.getName().equals(aiPlayer.getName())) aiCount ++;
        }
    
        if (aiCount <= 8) return "Human";  // White lost, so Black wins
        if (humanCount <= 8) return "AI";  // Black lost, so White wins
    
        return "No winner";  // Shouldn't happen in a finished game
    }
    
}
