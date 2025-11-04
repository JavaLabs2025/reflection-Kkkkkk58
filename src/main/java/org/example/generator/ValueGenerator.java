package org.example.generator;

public interface ValueGenerator {
    boolean supports(Class<?> clazz);
    Object generate(Class<?> clazz);
}
