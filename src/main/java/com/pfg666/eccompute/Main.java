package com.pfg666.eccompute;

import java.io.FileInputStream;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.automaton.transducer.CompactMealy;
import net.automatalib.automaton.transducer.CompactMealy.Creator;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;

public class Main {
	public static void main(String[] args) throws Exception {
		MealyDOTParser parser = new MealyDOTParser();
		Creator<String, String> creator = new CompactMealy.Creator<String,String>();
		InputModelDeserializer<@Nullable String, CompactMealy<@Nullable String, @Nullable String>> mealyParser = DOTParsers.mealy();
		mealyParser.parse(new FileInputStream("specifications//dtls_server_all_cert_req.dot"));
	}
}
