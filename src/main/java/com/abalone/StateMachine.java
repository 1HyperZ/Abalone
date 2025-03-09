package com.abalone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


class StateMachine {
    private Map<State, List<Action>> stateTransitions;

    public StateMachine() {
        stateTransitions = new HashMap<>();
    }

    public State determineNextState(Board board, AIPlayer aiPlayer) {
        return State.NEUTRAL; // Placeholder
    }
}