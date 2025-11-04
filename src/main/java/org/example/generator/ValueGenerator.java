package org.example.generator;

import java.lang.reflect.Type;

public interface ValueGenerator {
    boolean supports(Class<?> clazz);
    Object generate(Class<?> clazz, Type genericType);
}
