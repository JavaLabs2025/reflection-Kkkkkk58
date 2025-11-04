package org.example;


import java.util.List;
import java.util.Random;

import org.example.classes.Example;
import org.example.generator.Generator;
import org.example.generator.PrimitiveValueGenerator;
import org.example.generator.StringValueGenerator;

public class GenerateExample {
    public static void main(String[] args) {
        var random = new Random(42);
        var gen = new Generator(random, List.of(new PrimitiveValueGenerator(random), new StringValueGenerator(random)));

        try {
            Object generated = gen.generateValueOfType(Example.class);
            System.out.println(generated);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}