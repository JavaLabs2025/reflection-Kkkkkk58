package org.example.generator;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static java.util.Map.entry;

public class PrimitiveValueGenerator extends AbstractValueGenerator {
    private static final Integer ALPHABET_SIZE = 26;

    private static final Map<Class<?>, Function<Random, ?>> MAPPING_FUNCTION = Map.ofEntries(
            entry(int.class, Random::nextInt),
            entry(Integer.class, Random::nextInt),
            entry(long.class, Random::nextLong),
            entry(Long.class, Random::nextLong),
            entry(double.class, (random) -> random.nextDouble() * 1000),
            entry(Double.class, (random) -> random.nextDouble() * 1000),
            entry(float.class, (random) -> random.nextFloat() * 1000),
            entry(Float.class, (random) -> random.nextFloat() * 1000),
            entry(short.class, (random) -> (short) random.nextInt(Short.MAX_VALUE)),
            entry(Short.class, (random) -> (short) random.nextInt(Short.MAX_VALUE)),
            entry(byte.class, (random) -> (byte) random.nextInt(Byte.MAX_VALUE)),
            entry(Byte.class, (random) -> (byte) random.nextInt(Byte.MAX_VALUE)),
            entry(boolean.class, Random::nextBoolean),
            entry(Boolean.class, Random::nextBoolean),
            entry(char.class, PrimitiveValueGenerator::nextChar),
            entry(Character.class, PrimitiveValueGenerator::nextChar)
    );

    public PrimitiveValueGenerator(Random random) {
        super(random);
    }

    @Override
    protected Map<Class<?>, Function<Random, ?>> getMappingFunction() {
        return MAPPING_FUNCTION;
    }

    private static char nextChar(Random random) {
        return (char) ('a' + random.nextInt(ALPHABET_SIZE));
    }
}
