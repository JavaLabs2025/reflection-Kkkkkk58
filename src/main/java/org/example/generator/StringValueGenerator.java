package org.example.generator;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class StringValueGenerator extends AbstractValueGenerator {
    private static final Integer MAX_STRING_LENGTH = 50;

    private static final Map<Class<?>, Function<Random, ?>> MAPPING_FUNCTION = Map.of(
            String.class, (random) -> StringValueGenerator.nextString(random, random.nextInt(MAX_STRING_LENGTH))
    );

    public StringValueGenerator(Random random) {
        super(random);
    }

    @Override
    protected Map<Class<?>, Function<Random, ?>> getMappingFunction() {
        return MAPPING_FUNCTION;
    }

    private static String nextString(Random random, int length) {
        return random.ints('a', 'z')
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
