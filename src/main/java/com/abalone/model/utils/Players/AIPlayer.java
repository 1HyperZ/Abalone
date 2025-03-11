package com.abalone.model.utils.Players;

import java.util.List;
import java.util.Random;

import com.abalone.model.Board;
import com.abalone.model.utils.Move;

public class AIPlayer extends Player {
    private Random random;

    public AIPlayer(String name) {
        super(name);
        this.random = new Random();
    }

    public Move generateAIMove(Board board) {
        List<Move> possibleMoves = board.getPossibleMoves(this);
        if (possibleMoves.isEmpty()) {
            System.out.println("No valid moves for AI!");
            return null;
        }
        Move selectedMove = possibleMoves.get(new Random().nextInt(possibleMoves.size()));
        return selectedMove;
    }

}
