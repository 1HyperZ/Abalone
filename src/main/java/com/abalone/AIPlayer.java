package com.abalone;

import java.util.List;
import java.util.Random;

public class AIPlayer extends Player {
    private Random random;

    public AIPlayer(String name) {
        super(name);
        this.random = new Random();
    }

    @Override
    public Move makeMove(Board board) {
        List<Move> possibleMoves = board.getPossibleMoves(this);
        if (possibleMoves.isEmpty()) return null; // No moves available

        Move selectedMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
        board.applyMove(selectedMove); // Apply AI move

        return selectedMove; // Return move for logging/debugging
    }

}
