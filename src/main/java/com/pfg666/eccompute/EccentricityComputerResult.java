package com.pfg666.eccompute;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class EccentricityComputerResult {
	private Integer eccentricity;
	private Collection<?> inputs;
	private Collection<?> basisStates;
	private MealyMachine<?, ?, ?, ?> sutMealy;
	private Word<?> furthestSutStateASeq;
	private Object closestSpecStateAseq;

	public EccentricityComputerResult(
			Integer eccentricity,
			Collection<?> inputs,
			Collection<?> basisStates,
			MealyMachine<?, ?, ?, ?> sutMealy,
			Word<?> furthestSutStateASeq, 
			Word<?> closestSpecStateASeq) {
		this.eccentricity = eccentricity;
		this.inputs = inputs;
		this.basisStates = basisStates;
		this.sutMealy = sutMealy;
		this.furthestSutStateASeq = furthestSutStateASeq;
		this.closestSpecStateAseq = closestSpecStateASeq;
	}
	
	public Integer getEccentricity() {
		return eccentricity;
	}
	
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("ECCENTRICITY RESULT AND STATISTICS");
//		pw.println();
		pw.println("=".repeat(30));
//		pw.println();
		pw.format("Eccentricity: %d", eccentricity);
		pw.println();
		pw.format("Alphabet Size (inputs): %d", inputs.size());
		pw.println();
		pw.format("Basis Size (states): %d", basisStates.size());
		pw.println();
		pw.format("SUT Model Size (states): %s", sutMealy.size());
		pw.println();
		pw.format("ASeq For Furthest SUT State From Basis: %s", furthestSutStateASeq.toString());
		pw.println();
		pw.format("ASeq For Closest Basis State to Furthest SUT State: %s", closestSpecStateAseq.toString());
		return sw.toString();
	}
}
