package com.pfg666.eccompute;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class EccentricyComputer<I,O,S> {
	
	public EccentricyComputer() {
		
	}
	
	public Integer compute(Word<I> [] sequences, MealyMachine<S, I, ?, O> mealy, Collection<I> inputs) {
		Set<S> states = new LinkedHashSet<>();
		for (Word<I> sequence : sequences) {
			if (inputs.containsAll(sequence.asList()) && mealy.getState(sequence) != null) {
				states.add(mealy.getState(sequence));
			}
		}
		return compute(states, mealy, inputs);
	}
	
	public Integer compute(Collection<S> initialStates, MealyMachine<S, I, ?, O> mealy, Collection<I> inputs) {
		EccentricyComputer<I, O, S>.EccentricityStateVisitor visitor = new EccentricityStateVisitor(mealy.getStates());
		EccentricyComputer<I, O, S>.BFSTraverser traverser = new BFSTraverser(mealy, initialStates, visitor);
		traverser.traverse(inputs);
		return visitor.getEccentricity();
	}
	
	private class EccentricityStateVisitor implements com.pfg666.eccompute.EccentricyComputer.BFSTraverser.BFSStateVisitor {
		private LinkedHashSet<S> targetStates;
		private Integer eccentricity;

		public EccentricityStateVisitor(Collection<S> targetStates) {
			this.targetStates = new LinkedHashSet<S>();
            this.targetStates.addAll(targetStates);
		}

		@Override
		public boolean visit(EccentricyComputer<I, O, S>.BFSTraverser.SearchState state) {
			targetStates.remove(state.getState());
			if (targetStates.isEmpty()) {
				eccentricity = state.getDepth();
				return false;
			}
			return true;
		}
		
		public Integer getEccentricity() {
			return eccentricity;
		}
	}



    private class BFSTraverser {
        private Queue<SearchState> toVisit;
		private UniversalDeterministicAutomaton<S, I, ?, ?, ?> model;
		private BFSStateVisitor visitor;

        private BFSTraverser(UniversalDeterministicAutomaton<S, I, ?, ?, ?> model, Collection<S> initialStates, BFSStateVisitor stateVisitor) {
            for (S initialState : initialStates) {
                toVisit.add(new SearchState(initialState));
            }
            this.model = model;
            this.visitor = stateVisitor;
        }
        
        public void traverse(Collection<I> inputs) {
        	Set<S> visited = new HashSet<>();
        	while (!toVisit.isEmpty()) {
        		EccentricyComputer<I, O, S>.BFSTraverser.SearchState searchState = toVisit.poll();
        		boolean cont = visitor.visit(searchState);
        		visited.add(searchState.getState());
        		if (!cont) {
        			break;
        		} else {
        			S state = searchState.getState();
        			for (I input : inputs) {
						S nextState = model.getSuccessor(state, input);
						if (nextState != null && visited.contains(nextState)) {
							EccentricyComputer<I, O, S>.BFSTraverser.SearchState nextSearchState = new SearchState(searchState, input, nextState);
							toVisit.add(nextSearchState);
						}
        			}
        		}
        	}
        }
        
    
        private interface BFSStateVisitor {
        	boolean visit(SearchState state);
        }
        

        private class SearchState {


            private I input;
            private S state;
            private SearchState parent;

            public SearchState(S endState) {
                state = endState;
            }

            public SearchState(SearchState parent, I input, S state) {
                super();
                this.state = state;
                this.input = input;
                this.parent = parent;
            }

            public Word<I> getSuffix() {
                if (parent == null) {
                    return Word.epsilon();
                } else {
                    return getParent().getSuffix().append(input);
                }
            }
            
            public int getDepth() {
            	if (parent == null) {
                    return 0;
                } else {
                    return 1 + getDepth();
                }
            }

            public S getState() {
                return state;
            }

            public SearchState getParent() {
                return parent;
            }

        }
    }
}
