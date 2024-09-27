package com.pfg666.eccompute;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.CompactMealy;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.serialization.dot.DOTInputModelData;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;

public class EccentricyComputerTest extends TestCase {
	private static String MBEDTLS_SERVER_DOT = "/models/dtls/server/mbedtls-2.26.0_all_cert_req.dot";
	
	public void testComputeFromSequences() throws IOException{
		StateMachine stateMachine = parseStateMachine(MBEDTLS_SERVER_DOT);
		List<Word<String>> stateCover = Automata.stateCover(stateMachine.mealyMachine, stateMachine.alphabet);
		int ecc = EccentricyComputer.computeFromSequences(stateCover, stateMachine.mealyMachine, stateMachine.alphabet).getEccentricity();
		Assert.assertEquals(0, ecc);
	}
	
	private StateMachine parseStateMachine(String path) throws IOException {
		URL modelUrl = getResource(path);
		DOTInputModelData<Integer, @Nullable String, CompactMealy<@Nullable String, @Nullable String>> modelData = DOTParsers.mealy().readModel(modelUrl);
		return new StateMachine(modelData.model, modelData.alphabet);
	}
	
	private URL getResource(String path) throws IOException {
		return EccentricyComputerTest.class.getResource(path);
	}
	
	public static class StateMachine {
		public final MealyMachine<?, String, ?, String> mealyMachine;
		public final Alphabet<String> alphabet;
		public StateMachine(MealyMachine<?, String, ?, String> mealyMachine, Alphabet<String> alphabet) {
			this.mealyMachine = mealyMachine;
			this.alphabet = alphabet;
		}
	} 
}
