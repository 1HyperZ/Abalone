package com.abalone.model.utils.Players;

import com.abalone.model.Board;
import com.abalone.model.StateMachine;
import com.abalone.model.utils.Move;

public class AIPlayer extends Player {
    private StateMachine stateMachine;

    public AIPlayer(String name) {
        super(name);
        stateMachine = new StateMachine();
    }

    public Move generateAIMove(Board board) {
        return stateMachine.determineAIMove(board, this);
    }

}
