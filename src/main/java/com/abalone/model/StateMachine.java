package com.abalone.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.abalone.model.utils.Action;
import com.abalone.model.utils.State;
import com.abalone.model.utils.Players.AIPlayer;


class StateMachine {
    private Map<State, List<Action>> stateTransitions;

    public StateMachine() {
        stateTransitions = new HashMap<>();
    }

    public State determineNextState(Board board, AIPlayer aiPlayer) {
        return State.NEUTRAL; // Placeholder
    }
}