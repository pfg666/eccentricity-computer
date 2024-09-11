package com.pfg666.eccompute;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.pfg666.eccompute.BFSTraverser.BFSStateVisitor;
import com.pfg666.eccompute.BFSTraverser.SearchState;

import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class EccentricyComputer {
	
	public EccentricyComputer() {
		
	}
	
	public static <I,O,S> Integer computeFromSequences(Collection<Word<I>> sequences, MealyMachine<S, I, ?, O> mealy, Collection<I> inputs) {
		Set<S> states = new LinkedHashSet<>();
		for (Word<I> sequence : sequences) {
			for (Word<I> prefix : sequence.prefixes(false)) {
				if (prefix.isEmpty() || inputs.contains(prefix.lastSymbol())) {
					@Nullable
					S state = mealy.getState(prefix);
					if (state != null) {
						states.add(state);
					} else {
						break;
					}
				}
			}
		}
		return compute(states, mealy, inputs);
	}
	
	public static <I,O,S> Integer compute(Collection<S> initialStates, MealyMachine<S, I, ?, O> mealy, Collection<I> inputs) {
		EccentricityStateVisitor<I,S> visitor = new EccentricityStateVisitor<>(mealy.getStates());
		BFSTraverser<I,S> traverser = new BFSTraverser<I,S>(mealy, initialStates, visitor);
		traverser.traverse(inputs);
		return visitor.getEccentricity();
	}
	
	private static class EccentricityStateVisitor<I,S> implements BFSStateVisitor<I,S> {
		private LinkedHashSet<S> targetStates;
		private Integer eccentricity;

		public EccentricityStateVisitor(Collection<S> targetStates) {
			this.targetStates = new LinkedHashSet<S>();
            this.targetStates.addAll(targetStates);
		}

		@Override
		public boolean visit(SearchState<I,S> state) {
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

}
