package com.abalone.model.utils.Players;

import com.abalone.model.Board;
import com.abalone.model.utils.Move;

public class Player {
    protected String name;
    protected int score;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }

    public Move makeMove(Board board) {
        return null; // Human player moves are handled by UI
    }
}
