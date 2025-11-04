package org.example.generator;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public abstract class AbstractValueGenerator implements ValueGenerator {
    private final Random random;

    protected AbstractValueGenerator(Random random) {
        this.random = random;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return getMappingFunction().containsKey(clazz);
    }

    @Override
    public Object generate(Class<?> clazz, Type genericType) {
        return getMappingFunction().get(clazz).apply(random);
    }

    protected abstract Map<Class<?>, Function<Random, ?>> getMappingFunction();
}
