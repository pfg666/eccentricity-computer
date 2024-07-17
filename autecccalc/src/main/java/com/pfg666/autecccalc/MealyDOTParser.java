package com.pfg666.autecccalc;

import java.io.IOException;
import java.io.InputStream;

import net.automatalib.automaton.AutomatonCreator;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.dot.DOTInputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;

/**
 * Generates a Mealy machine from a DOT specification.
 */
public class MealyDOTParser {
    public static <S, I, O, A extends MutableMealyMachine<S, I, ?, O>> InputModelData<I, A> parse(AutomatonCreator<A, I> creator, InputStream inputStream, MealyInputOutputProcessor <I, O> processor) throws IOException {
        DOTInputModelDeserializer<S, I, A> parser = DOTParsers.mealy(creator, (map)
                -> {
                    Pair<String, String> ioStringPair = DOTParsers.DEFAULT_MEALY_EDGE_PARSER.apply(map);
                    Pair<I, O> ioPair = processor.processMealyInputOutput(ioStringPair.getFirst(), ioStringPair.getSecond());
                    return ioPair;
                });
        return parser.readModel(inputStream);
    }

    public static interface MealyInputOutputProcessor <I, O> {
        Pair<I, O> processMealyInputOutput(String inputName, String outputName);
    }
}
