package com.github.myibu.regex.nfa;


import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class NFA {
    private NFAState start;
    private NFAState end;

    private NFA() {
    }

    private NFA(NFAState start, NFAState end) {
        this.start = start;
        this.end = end;
    }

    public static class NFAState {
        boolean isEnd;
        Map<Character, NFAState> transition;
        List<NFAState> epsilonTransitions;

        public NFAState(boolean isEnd) {
            this.isEnd = isEnd;
            this.transition = new HashMap<>();
            this.epsilonTransitions = new ArrayList<>();
        }
    }

    public static NFAState createState(boolean isEnd) {
        return new NFAState(isEnd);
    }

    public boolean search(String word) {
        List<NFA.NFAState> currentStates = new ArrayList<>();
        addNextState(start, currentStates, new ArrayList<>());

        for(char symbol : word.toCharArray()) {
            List<NFA.NFAState> nextStates = new ArrayList<>();
            for(NFA.NFAState state: currentStates) {
                NFA.NFAState nextState = state.transition.get(symbol);
                if (null != nextState) {
                    addNextState(nextState, nextStates, new ArrayList<>());
                }
            }
            currentStates = nextStates;
        }
        for (NFA.NFAState state: currentStates) {
            if (state.isEnd) {
                return true;
            }
        }
        return false;
    }

    private void addNextState(NFA.NFAState state, List<NFA.NFAState> nextStates, List<NFA.NFAState> visited) {
        if (state.epsilonTransitions.size() > 0) {
            for (NFA.NFAState st : state.epsilonTransitions) {
                if (!visited.contains(st)) {
                    visited.add(st);
                    addNextState(st, nextStates, visited);
                }
            }
        } else {
            nextStates.add(state);
        }
    }

    public static NFA concat(NFA first, NFA second) {
        addEpsilonTransition(first.end, second.start);
        first.end.isEnd = false;
        return new NFA(first.start, second.end);
    }

    public static NFA union(NFA first, NFA second) {
        NFA.NFAState start = NFA.createState(false);
        addEpsilonTransition(start, first.start);
        addEpsilonTransition(start, second.start);

        NFA.NFAState end = NFA.createState(true);
        addEpsilonTransition(first.end, end);
        first.end.isEnd = false;
        addEpsilonTransition(second.end, end);
        second.end.isEnd = false;

        return new NFA(start, end);
    }

    public static NFA closure(NFA nfa) {
        NFA.NFAState start = NFA.createState(false);
        NFA.NFAState end = NFA.createState(true);

        addEpsilonTransition(start, end);
        addEpsilonTransition(start, nfa.start);

        addEpsilonTransition(nfa.end, end);
        addEpsilonTransition(nfa.end, nfa.start);
        nfa.end.isEnd = false;

        return new NFA(start, end);
    }

    public static NFA fromEpsilon() {
        NFA.NFAState start = NFA.createState(false);
        NFA.NFAState end = NFA.createState(true);
        addEpsilonTransition(start, end);
        return new NFA(start, end);
    }

    public static NFA fromSymbol(Character symbol) {
        NFA.NFAState start = NFA.createState(false);
        NFA.NFAState end = NFA.createState(true);
        addTransition(start, end, symbol);
        return new NFA(start, end);
    }

    private static void addEpsilonTransition(NFA.NFAState from, NFA.NFAState to) {
        from.epsilonTransitions.add(to);
    }

    private static void addTransition(NFA.NFAState from, NFA.NFAState to, Character symbol) {
        from.transition.put(symbol, to);
    }
}
