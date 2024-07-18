package com.pfg666.eccompute;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

import java.util.ArrayList;

public class EccentricyComputer<I,O,S> {
	private List<S> states;
	
	public EccentricyComputer() {
		
	}
	
	public void initialize(Collection<S> states) {
		this.states = new ArrayList<S>(states);
	}
	
	public void initialize(Collection<Word<I>> sequences, MealyMachine<S, I, ?, O> mealy) {
		states = new ArrayList<>(sequences.size());
		Set<S> uniqueStates = new LinkedHashSet<>();
		sequences.forEach(seq -> uniqueStates.add(mealy.getState(seq)));
		states.addAll(uniqueStates);
		if (sequences.size() != states.size()) {
			System.out.println("Number of states reached by sequences is smaller than a number of sequences");
		}
	}
	
	public void calculate(MealyMachine<S, I, ?, O> mealy) {
		
	}
}
