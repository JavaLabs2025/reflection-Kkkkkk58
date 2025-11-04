package org.example.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

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
            return valueGenerator.generate(clazz);
        }

        if (depth > MAX_RECURSION_DEPTH) {
            return null;
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            return generateCollection(clazz, genericType, depth);
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

    private Collection<?> generateCollection(Class<?> clazz, Type genericType, int depth) throws Exception {
        if (List.class.isAssignableFrom(clazz)) {
            return generateCollectionInternal(genericType, depth, ArrayList::new);
        } else if (Set.class.isAssignableFrom(clazz)) {
            return generateCollectionInternal(genericType, depth, HashSet::new);
        } else {
            return generateCollectionInternal(genericType, depth, ArrayDeque::new);
        }
    }

    private Collection<?> generateCollectionInternal(
            Type genericType,
            int depth,
            Supplier<Collection<Object>> collectionSupplier
    ) throws Exception {
        Class<?> elementType = Object.class;
        Type elementGenericType = null;

        if (genericType instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                elementGenericType = typeArgs[0];
                elementType = getClassFromType(elementGenericType);
            }
        }

        int size = random.nextInt(MAX_COLLECTION_SIZE + 1);
        var collection = collectionSupplier.get();
        for (int i = 0; i < size; i++) {
            Object element = generateValueOfTypeInternal(elementType, elementGenericType, depth);
            collection.add(element);
        }
        return collection;
    }

    private Map<?, ?> generateMap(Type genericType, int depth) throws Exception {
        Class<?> keyType = Object.class;
        Type keyGenericType = null;
        Class<?> valueType = Object.class;
        Type valueGenericType = null;

        if (genericType instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                keyGenericType = typeArgs[0];
                keyType = getClassFromType(keyGenericType);
            }
            if (typeArgs.length > 1) {
                valueGenericType = typeArgs[1];
                valueType = getClassFromType(valueGenericType);
            }
        }

        int size = random.nextInt(MAX_COLLECTION_SIZE + 1);
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Object key = generateValueOfTypeInternal(keyType, keyGenericType, depth);
            Object value = generateValueOfTypeInternal(valueType, valueGenericType, depth);
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
