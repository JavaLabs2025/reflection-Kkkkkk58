package org.example.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.example.annotation.Generatable;

public class Generator {
    public static final int MAX_COLLECTION_SIZE = 5;
    public static final int MAX_RECURSION_DEPTH = 3;
    private final Random random;
    private final List<ValueGenerator> valueGenerators;

    public Generator(
            Random random,
            List<ValueGenerator> valueGenerators
    ) {
        this.random = random;
        this.valueGenerators = valueGenerators;
    }

    public Object generateValueOfType(Class<?> clazz) {
        try {
            return generateValueOfTypeInternal(clazz, null, 0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate value for type: " + clazz.getName(), e);
        }
    }

    private Object generateValueOfTypeInternal(Class<?> clazz, Type genericType, int depth) throws Exception {
        var valueGenerator = findValueGeneratorForClass(clazz);
        if (valueGenerator != null) {
            return valueGenerator.generate(clazz, genericType);
        }

        if (depth > MAX_RECURSION_DEPTH) {
            return null;
        }

        if (List.class.isAssignableFrom(clazz)) {
            return generateList(genericType, depth);
        }
        if (Set.class.isAssignableFrom(clazz)) {
            return generateSet(genericType, depth);
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return generateMap(genericType, depth);
        }

        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return generateInterfaceImplementation(clazz, depth);
        }

        if (!clazz.isAnnotationPresent(Generatable.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @Generatable");
        }

        Constructor<?> constructor = selectConstructor(clazz);
        Object[] params = generateParameters(constructor, depth + 1);
        return constructor.newInstance(params);
    }

    private ValueGenerator findValueGeneratorForClass(Class<?> clazz) {
        return valueGenerators.stream()
                .filter(it -> it.supports(clazz))
                .findFirst()
                .orElse(null);
    }

    private List<?> generateList(Type genericType, int depth) throws Exception {
        Class<?> elementType = getCollectionElementType(genericType);

        int size = random.nextInt(MAX_COLLECTION_SIZE + 1);
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Object element = generateValueOfTypeInternal(elementType, null, depth);
            list.add(element);
        }
        return list;
    }

    private Set<?> generateSet(Type genericType, int depth) throws Exception {
        Class<?> elementType = getCollectionElementType(genericType);

        int size = random.nextInt(MAX_COLLECTION_SIZE + 1);
        Set<Object> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            Object element = generateValueOfTypeInternal(elementType, null, depth);
            set.add(element);
        }
        return set;
    }

    private Class<?> getCollectionElementType(Type genericType) {
        Class<?> elementType = Object.class;

        if (genericType instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                elementType = getClassFromType(typeArgs[0]);
            }
        }
        return elementType;
    }

    private Map<?, ?> generateMap(Type genericType, int depth) throws Exception {
        Class<?> keyType = Object.class;
        Class<?> valueType = Object.class;

        if (genericType instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                keyType = getClassFromType(typeArgs[0]);
            }
            if (typeArgs.length > 1) {
                valueType = getClassFromType(typeArgs[1]);
            }
        }

        int size = random.nextInt(MAX_COLLECTION_SIZE + 1);
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Object key = generateValueOfTypeInternal(keyType, null, depth);
            Object value = generateValueOfTypeInternal(valueType, null, depth);
            map.put(key, value);
        }
        return map;
    }

    private Class<?> getClassFromType(Type type) {
        if (type instanceof Class<?> cls) {
            return cls;
        } else if (type instanceof ParameterizedType paramType) {
            return (Class<?>) paramType.getRawType();
        }
        return Object.class;
    }

    private Object generateInterfaceImplementation(Class<?> interfaceClass, int depth) throws Exception {
        List<Class<?>> implementations = findGeneratableImplementations(interfaceClass);

        if (implementations.isEmpty()) {
            throw new IllegalArgumentException(
                    "No @Generatable implementations found for interface/abstract class: " + interfaceClass.getName()
            );
        }

        Class<?> selectedClass = implementations.get(random.nextInt(implementations.size()));
        return generateValueOfTypeInternal(selectedClass, null, depth);
    }

    private List<Class<?>> findGeneratableImplementations(Class<?> interfaceClass) {
        return GeneratableRegistry.getAllGeneratableClasses().stream()
                .filter(it -> isGeneratableClass(it, interfaceClass))
                .toList();
    }

    private boolean isGeneratableClass(Class<?> clazz, Class<?> interfaceClass) {
        return interfaceClass.isAssignableFrom(clazz) &&
                !clazz.isInterface() &&
                !Modifier.isAbstract(clazz.getModifiers());
    }

    private Constructor<?> selectConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        if (constructors.length == 0) {
            throw new IllegalArgumentException("No constructors found for class: " + clazz.getName());
        }

        int selectedIndex = random.nextInt(constructors.length);

        return constructors[selectedIndex];
    }

    private Object[] generateParameters(Constructor<?> constructor, int depth) throws Exception {
        Parameter[] parameters = constructor.getParameters();
        Object[] result = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Type genericType = parameters[i].getParameterizedType();
            Class<?> paramType = parameters[i].getType();

            result[i] = generateValueOfTypeInternal(paramType, genericType, depth);
        }

        return result;
    }
}
