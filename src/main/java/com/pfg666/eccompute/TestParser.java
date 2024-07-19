package com.pfg666.eccompute;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import net.automatalib.word.Word;

public class TestParser {
	
	/**
	 * Reads from a file reset-separated tests (test queries to be precise).
	 * It stops reading once it reaches the EOF, or an empty line.
	 * A non-empty line may contain:
	 * <ul>
	 * <li>reset - marking the end of the current test, and the beginning of a new test</li>
	 * <li>space-separated regular inputs and resets</li>
	 * <li>a single mutated input (starts with @)</li>
	 * <li>commented line (starts with # or !)</li>
	 * </ul>
	 */
	public List<Word<String>> readTests(String PATH) throws IOException {
		List<String> inputStrings = readTestStrings(PATH);
		List<String> flattenedInputStrings = inputStrings.stream()
				.map(i -> i.startsWith("@") ? new String[] {i} : i.split("\\s+")).flatMap(a -> Arrays.stream(a))
				.collect(Collectors.toList());
		
		List<Word<String>> tests = new LinkedList<>();
		LinkedList<String> currentTestStrings = new LinkedList<>();
		for (String inputString : flattenedInputStrings) {
			if (inputString.equals("reset")) {
				tests.add(Word.fromList(currentTestStrings));
				currentTestStrings.clear();
			}  else {
				currentTestStrings.add(inputString);
			}
		}
		if (!inputStrings.isEmpty()) {
			tests.add(Word.fromList(currentTestStrings));
		}
		return tests;
	}
	
	private List<String> readTestStrings(String PATH) throws IOException {
		List<String> trace;
		trace = Files.readAllLines(Paths.get(PATH), StandardCharsets.US_ASCII);
		ListIterator<String> it = trace.listIterator();
		while (it.hasNext()) {
			String line = it.next();
			if (line.startsWith("#") || line.startsWith("!")) {
				it.remove();
			} else {
				if (line.isEmpty()) {
					it.remove();
					while (it.hasNext()) {
						it.next();
						it.remove();
					}
				} else {
				}
			}
		}
		return trace;
	}
}
