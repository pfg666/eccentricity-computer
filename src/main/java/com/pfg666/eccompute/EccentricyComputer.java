package com.pfg666.eccompute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.pfg666.eccompute.BFSTraverser.BFSStateVisitor;
import com.pfg666.eccompute.BFSTraverser.SearchState;

import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public class EccentricyComputer {
	
	public EccentricyComputer() {
		
	}

	public static <I,O,S> EccentricityComputerResult computeFromSequences(Collection<Word<I>> sequences, MealyMachine<S, I, ?, O> mealy, Collection<I> inputs) {
		Map<S,Word<I>> states = new LinkedHashMap<>();
		for (Word<I> sequence : sequences) {
			WordBuilder<I> builder = new WordBuilder<>();
			for (I input : sequence) {
				builder.toWord();
				if (inputs.contains(input)) {
					builder.append(input);
				}
			}
			for (Word<I> prefix : builder.toWord().prefixes(false)) {
				@Nullable
				S state = mealy.getState(prefix);
				if (state != null) {
					states.put(state, prefix);
				} else {
					break;
				}
			}
		}
		EccentricityComputerResult result = compute(states, mealy, inputs);
		Collection<Word<?>> seq = new ArrayList<>();
		seq.addAll(sequences);
		result.setSpecificationSequences(seq);
		return result;
	}
	
	public static <I,O,S> EccentricityComputerResult compute(Map<S,Word<I>> initialStates, MealyMachine<S, I, ?, O> mealy, Collection<I> inputs) {
		EccentricityStateVisitor<I,S> visitor = new EccentricityStateVisitor<>(mealy.getStates());
		BFSTraverser<I,S> traverser = new BFSTraverser<I,S>(mealy, initialStates.keySet(), visitor);
		traverser.traverse(inputs);
		S closestSpecState = visitor.getSearchState().getRoot().getState();
		return new EccentricityComputerResult( visitor.getEccentricity(), 
				inputs,
				initialStates.keySet(),
				mealy,
				initialStates.get(closestSpecState).concat(visitor.getSearchState().getSuffix()), initialStates.get(closestSpecState));
	}
	
	private static class EccentricityStateVisitor<I,S> implements BFSStateVisitor<I,S> {
		private LinkedHashSet<S> targetStates;
		private SearchState<I,S> searchState;
		private Integer eccentricity;
		private S furthestState;

		public EccentricityStateVisitor(Collection<S> targetStates) {
			this.targetStates = new LinkedHashSet<S>();
            this.targetStates.addAll(targetStates);
		}

		@Override
		public boolean visit(SearchState<I,S> state) {
			targetStates.remove(state.getState());
			if (targetStates.isEmpty()) {
				this.searchState = state;
				eccentricity = state.getDepth();
				furthestState = state.getState();
				return false;
			}
			return true;
		}
		
		public SearchState<I,S> getSearchState() {
			return searchState;
		}
		
		public S getFurthestState() {
			return furthestState;
		}
		
		public Integer getEccentricity() {
			return eccentricity;
		}
	}
	
//	static record EccentricityComputerResult(Integer eccentricity, Word<?> furthestSutStateASeq, Word<?> closestSpecStateASeq) {
//	}
}
