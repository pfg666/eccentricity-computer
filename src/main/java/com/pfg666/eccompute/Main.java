package com.pfg666.eccompute;

import java.io.FileInputStream;
import java.util.LinkedHashSet;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.beust.jcommander.JCommander;

import net.automatalib.automaton.fsa.CompactDFA;
import net.automatalib.automaton.transducer.CompactMealy;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public class Main {
	public static void main(String[] args) throws Exception {
		InputModelDeserializer<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> mealyParser = DOTParsers.mealy();
		Config config = new Config();
		JCommander commander = JCommander.newBuilder()
                .allowParameterOverwriting(true)
                .programName("eccompute")
                .addObject(config)
                .build();
		if (args.length == 0) {
			commander.usage();
		} else {
			commander.parse(args);
			String specification = config.getSpecification();
			InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> sutData = mealyParser.readModel(new FileInputStream(config.getModel()));
			if (config.getSpecificationType() == SpecificationType.HAPPY_FLOWS) {
				TestParser testParser = new TestParser();
				List<Word<String>> happyFlows = testParser.readTests(specification);
				Integer eccentricity = EccentricyComputer.computeFromSequences(happyFlows, sutData.model, sutData.alphabet);
				System.out.println("Computed eccentricity with happy flows: " + eccentricity);
			} else if (config.getSpecificationType() == SpecificationType.MEALY_MACHINE){
				InputModelData<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> specData = mealyParser.readModel(new FileInputStream(config.getSpecification()));
				List<Word<@Nullable String>> specAccessSequences = Automata.stateCover(specData.model, specData.alphabet);
				Integer eccentricity = EccentricyComputer.computeFromSequences(specAccessSequences, sutData.model, sutData.alphabet);
				System.out.println("Computed eccentricity with Mealy machine specification: " + eccentricity);
			} else if (config.getSpecificationType() == SpecificationType.GENERAL_BUG_PATTERN){
				InputModelDeserializer<@Nullable String, CompactDFA<@Nullable String>> dfaParser = DOTParsers.dfa();
				InputModelData<@Nullable String, CompactDFA<@Nullable String>> specData = dfaParser.readModel(new FileInputStream(config.getSpecification()));
				List<Word<@Nullable String>> specAccessSequences = Automata.stateCover(specData.model, specData.alphabet);
				LinkedHashSet<Word<@Nullable String>> inputAccessSequences = new LinkedHashSet<Word<String>>();
				for (Word<@Nullable String> aSeq : specAccessSequences) {
					WordBuilder<String> builder = new WordBuilder<>();
					for (String sym : aSeq) {
						if (sym.startsWith("I_")) {
							builder.add(sym.substring(2));
						}
					}
					inputAccessSequences.add(builder.toWord());
				}
				Integer eccentricity = EccentricyComputer.computeFromSequences(specAccessSequences, sutData.model, sutData.alphabet);
				System.out.println("Computed eccentricity with general bug pattern: " + eccentricity);
			} else {
				throw new RuntimeException("Not supported specification type " + config.getSpecificationType());
			}
		}
	}
}
