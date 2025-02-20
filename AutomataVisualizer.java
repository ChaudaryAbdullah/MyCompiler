package example;

import java.util.*;

public class AutomataVisualizer {
    private static final String ARROW = "→";
    private static final String VERTICAL = "│";
    private static final String HORIZONTAL = "──";
    private static final String CORNER = "└";
    private static final String EPSILON = "ε";

    static class DFAState {
        Set<State> nfaStates;
        int id;
        Map<Character, DFAState> transitions;
        boolean isFinal;

        DFAState(Set<State> nfaStates, int id) {
            this.nfaStates = nfaStates;
            this.id = id;
            this.transitions = new HashMap<>();
            this.isFinal = nfaStates.stream().anyMatch(s -> s.isFinal);
        }
    }

    public static void visualizeAutomata(Token token) {
        if (token.getStatePath().isEmpty()) return;

        System.out.println("\n=== Token: " + token.getValue() + " (" + getTokenTypeName(token.getType()) + ") ===\n");

        Map<Integer, State> nfaStates = new HashMap<>();
        Map<Integer, Map<Character, Set<Integer>>> nfaTransitions = new HashMap<>();
        Map<Integer, String> stateValues = new HashMap<>();

        for (int i = 0; i < token.getStatePath().size(); i++) {
            State state = token.getStatePath().get(i);
            nfaStates.put(state.id, state);

            if (i < token.getValue().length()) {
                stateValues.put(state.id, String.valueOf(token.getValue().charAt(i)));
            } else {
                stateValues.put(state.id, "");
            }
        }

        for (int i = 0; i < token.getStatePath().size(); i++) {
            State state = token.getStatePath().get(i);
            Map<Character, Set<Integer>> stateTransitions = new HashMap<>();
            nfaTransitions.put(state.id, stateTransitions);

            for (Map.Entry<Character, Set<State>> transition : state.transitions.entrySet()) {
                Set<Integer> targetStates = new HashSet<>();
                for (State target : transition.getValue()) {
                    targetStates.add(target.id);
                }
                stateTransitions.put(transition.getKey(), targetStates);
            }

            if (i < token.getStatePath().size() - 1) {
                Set<Integer> epsilonTargets = stateTransitions.computeIfAbsent(' ', k -> new HashSet<>());
                epsilonTargets.add(token.getStatePath().get(i + 1).id);
            }
        }

        System.out.println("NFA States and Transitions:");
        System.out.println("─".repeat(50));
        
        List<Integer> sortedStateIds = new ArrayList<>(nfaStates.keySet());
        Collections.sort(sortedStateIds);
        
        for (Integer stateId : sortedStateIds) {
            State state = nfaStates.get(stateId);
            String stateValue = stateValues.get(stateId);
            System.out.printf("q%d [%s]%s\n", 
                stateId, 
                stateValue.isEmpty() ? "-" : stateValue,
                state.isFinal ? " (Final)" : "");
            
            Map<Character, Set<Integer>> transitions = nfaTransitions.get(stateId);
            if (transitions != null && !transitions.isEmpty()) {
                List<Map.Entry<Character, Set<Integer>>> sortedTransitions = 
                    new ArrayList<>(transitions.entrySet());
                sortedTransitions.sort(Map.Entry.comparingByKey());
                
                for (Map.Entry<Character, Set<Integer>> transition : sortedTransitions) {
                    String symbol = transition.getKey() == ' ' ? EPSILON : 
                                  transition.getKey() == '\n' ? "\\n" : 
                                  String.valueOf(transition.getKey());
                    
                    List<Integer> sortedTargets = new ArrayList<>(transition.getValue());
                    Collections.sort(sortedTargets);
                    
                    for (Integer targetId : sortedTargets) {
                        System.out.printf("%s%s -%s→ q%d\n", 
                            VERTICAL, HORIZONTAL, symbol, targetId);
                    }
                }
            }
        }

        DFAState initial = constructDFA(token.getStatePath());
        
        System.out.println("\nDFA States and Transitions:");
        System.out.println("─".repeat(50));
        
        Set<DFAState> visited = new HashSet<>();
        Queue<DFAState> queue = new LinkedList<>();
        queue.offer(initial);
        
        while (!queue.isEmpty()) {
            DFAState current = queue.poll();
            
            if (!visited.add(current)) continue;
            
            System.out.printf("D%d%s\n", current.id, current.isFinal ? " (Final)" : "");
            
            List<Map.Entry<Character, DFAState>> sortedTransitions = 
                new ArrayList<>(current.transitions.entrySet());
            sortedTransitions.sort(Map.Entry.comparingByKey());
            
            for (Map.Entry<Character, DFAState> transition : sortedTransitions) {
                String symbol = transition.getKey() == ' ' ? EPSILON : 
                              transition.getKey() == '\n' ? "\\n" : 
                              String.valueOf(transition.getKey());
                              
                System.out.printf("%s%s -%s→ D%d\n", 
                    VERTICAL, HORIZONTAL, symbol, transition.getValue().id);
                    
                queue.offer(transition.getValue());
            }
        }
    }

    private static DFAState constructDFA(List<State> nfaPath) {
        Map<Set<State>, DFAState> dfaStates = new HashMap<>();
        int dfaStateCounter = 0;

        Set<State> currentStates = new HashSet<>();
        // currentStates.add(nfaPath.get(0));

        DFAState initialDFA = new DFAState(currentStates, dfaStateCounter++);
        dfaStates.put(currentStates, initialDFA);

        for (int i = 0; i < nfaPath.size(); i++) {
            State current = nfaPath.get(i);

            char transitionChar = current.transitionChar;
            if (transitionChar != 'ε' && transitionChar != '?' && transitionChar != ' ') {
                Set<State> nextStates = new HashSet<>();
                nextStates.add(current);

                DFAState currentDFA = dfaStates.get(currentStates);
                DFAState nextDFA = dfaStates.get(nextStates);
                
                if (nextDFA == null) {
                    nextDFA = new DFAState(nextStates, dfaStateCounter++);
                    dfaStates.put(nextStates, nextDFA);
                }

                currentDFA.transitions.put(transitionChar, nextDFA);
                currentStates = nextStates;
            }
        }
        
        return initialDFA;
    }
    

    public static void displayAllAutomata(List<Token> tokens) {
        System.out.println("\n=== Complete Lexical Analysis Automata ===\n");

        Map<Integer, Set<String>> processedPatterns = new HashMap<>();
        
        for (Token token : tokens) {
            if (token.getType() == Token.EOF) continue;
            
            String pattern = getTokenPattern(token);
            Set<String> patterns = processedPatterns.computeIfAbsent(
                token.getType(), k -> new HashSet<>());
                
            if (patterns.add(pattern)) {
                visualizeAutomata(token);
            }
        }
    }

    private static String getTokenPattern(Token token) {
        return token.getStatePath().stream()
            .map(s -> String.valueOf(s.transitionChar))
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
    }

    private static String getTokenTypeName(int type) {
        return switch (type) {
            case Token.KEYWORD -> "KEYWORD";
            case Token.DATATYPE -> "DATATYPE";
            case Token.IDENTIFIER -> "IDENTIFIER";
            case Token.INTEGER -> "INTEGER";
            case Token.DECIMAL -> "DECIMAL";
            case Token.CHARACTER -> "CHARACTER";
            case Token.BOOLEAN -> "BOOLEAN";
            case Token.STRING -> "STRING";
            case Token.OPERATOR -> "OPERATOR";
            case Token.PUNCTUATOR -> "PUNCTUATOR";
            case Token.INPUT -> "INPUT";
            case Token.OUTPUT -> "OUTPUT";
            default -> "UNKNOWN";
        };
    }
}