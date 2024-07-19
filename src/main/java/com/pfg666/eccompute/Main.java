package com.pfg666.eccompute;

import java.io.FileInputStream;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.automaton.transducer.CompactMealy;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.word.Word;

public class Main {
	public static void main(String[] args) throws Exception {
		InputModelDeserializer<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> mealyParser = DOTParsers.mealy();
		TestParser testParser = new TestParser();
		if (args.length != 2) {
			System.out.println("Usage: ecccompute sut_model happy_flows");
		}
		List<Word<String>> happyFlows = testParser.readTests(args[1]);
		InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> sutData = mealyParser.readModel(new FileInputStream(args[0]));
		Integer eccentricity = EccentricyComputer.computeFromSequences(happyFlows, sutData.model, sutData.alphabet);
		System.out.println("Computed eccentricity: " + eccentricity);
	}
}
