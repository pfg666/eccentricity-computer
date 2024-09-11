package com.pfg666.eccompute;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.word.Word;

public class BFSTraverser<I,S> {
    private Queue<SearchState<I,S>> toVisit;
	private UniversalDeterministicAutomaton<S, I, ?, ?, ?> model;
	private BFSStateVisitor<I,S> visitor;

    public BFSTraverser(UniversalDeterministicAutomaton<S, I, ?, ?, ?> model, Collection<S> initialStates, BFSStateVisitor<I,S> stateVisitor) {
    	toVisit = new ArrayDeque<>();
        for (S initialState : initialStates) {
            toVisit.add(new SearchState<>(initialState));
        }
        this.model = model;
        this.visitor = stateVisitor;
    }
    
    public void traverse(Collection<I> inputs) {
    	Set<S> visited = new HashSet<>();
    	while (!toVisit.isEmpty()) {
    		SearchState<I,S> searchState = toVisit.poll();
    		boolean cont = visitor.visit(searchState);
    		visited.add(searchState.getState());
    		if (!cont) {
    			break;
    		} else {
    			S state = searchState.getState();
    			for (I input : inputs) {
					S nextState = model.getSuccessor(state, input);
					if (nextState != null && !visited.contains(nextState)) {
						SearchState<I,S> nextSearchState = new SearchState<>(searchState, input, nextState);
						toVisit.add(nextSearchState);
					}
    			}
    		}
    	}
    }
    

    public static interface BFSStateVisitor<I,S> {
    	boolean visit(SearchState<I,S> state);
    }

    public static class SearchState<I,S> {


        private I input;
        private S state;
        private SearchState<I,S> parent;

        public SearchState(S endState) {
            state = endState;
        }

        public SearchState(SearchState<I,S> parent, I input, S state) {
            super();
            this.state = state;
            this.input = input;
            this.parent = parent;
        }

        public Word<I> getSuffix() {
            if (parent == null) {
                return Word.<I>epsilon();
            } else {
                return getParent().getSuffix().append(input);
            }
        }
        
        public int getDepth() {
        	if (parent == null) {
                return 0;
            } else {
                return 1 + parent.getDepth();
            }
        }

        public S getState() {
            return state;
        }

        public SearchState<I,S> getParent() {
            return parent;
        }
        
        public SearchState<I,S> getRoot() {
        	if (parent == null) {
        		return this;
        	} else {
        		return getParent().getRoot();
        	}
        }

    }
}