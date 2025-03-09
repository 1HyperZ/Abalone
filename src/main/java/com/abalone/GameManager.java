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
        int whiteCount = 0, blackCount = 0;
    
        for (Player p : board.getPlayersOnBoard()) {
            if (p.getName().equals("White")) whiteCount++;
            if (p.getName().equals("Black")) blackCount++;
        }
    
        return whiteCount < 6 || blackCount < 6; // Only return true if a player lost
    }
    
    public String getWinner() {
        int whiteCount = 0, blackCount = 0;
    
        for (Player p : board.getPlayersOnBoard()) {
            if (p.getName().equals("White")) whiteCount++;
            if (p.getName().equals("Black")) blackCount++;
        }
    
        if (whiteCount < 6) return "Black";  // White lost, so Black wins
        if (blackCount < 6) return "White";  // Black lost, so White wins
    
        return "No winner";  // Shouldn't happen in a finished game
    }
    
}
