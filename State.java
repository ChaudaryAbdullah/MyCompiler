package example;

import java.util.*;



public class State {
    int id;
    boolean isFinal;
    char transitionChar;
    Map<Character, Set<State>> transitions;

    public State(int id, boolean isFinal, char transitionChar) {
        this.id = id;
        this.isFinal = isFinal;
        this.transitionChar = transitionChar;
        this.transitions = new HashMap<>();
    }

    @SuppressWarnings("unused")
    public void addTransition(char input, State nextState) {
        transitions.computeIfAbsent(input, k -> new HashSet<>()).add(nextState);
    }

    @Override
    public String toString() {
        return "State " + id + " (" + transitionChar + ")" + (isFinal ? " (Final)" : "");
    }
}
