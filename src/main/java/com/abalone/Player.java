package com.abalone;

public class Player {
    protected String name;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Move makeMove(Board board) {
        return null; // Human player moves are handled by UI
    }
}
