package org.example.generator;

import java.util.List;
import java.util.Random;

import org.example.classes.BinaryTreeNode;
import org.example.classes.Cart;
import org.example.classes.Example;
import org.example.classes.Product;
import org.example.classes.Rectangle;
import org.example.classes.Shape;
import org.example.classes.Triangle;
import org.junit.jupiter.api.Test;

import static org.example.generator.Generator.MAX_COLLECTION_SIZE;
import static org.example.generator.Generator.MAX_RECURSION_DEPTH;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorTest {
    private final Random random = new Random(42);
    private final Generator generator = new Generator(random, List.of(
            new PrimitiveValueGenerator(random),
            new StringValueGenerator(random)
    ));
    
    @Test
    void generateExample() {
        Object result = generator.generateValueOfType(Example.class);
        
        assertNotNull(result);
        assertInstanceOf(Example.class, result);
        
        Example example = (Example) result;
        System.out.println("Generated Example: " + example);
    }
    
    @Test
    void generateProduct() {
        Object result = generator.generateValueOfType(Product.class);
        
        assertNotNull(result);
        assertInstanceOf(Product.class, result);
        
        Product product = (Product) result;
        assertNotNull(product.getName(), "Product name should not be null");

        System.out.println("Generated Product: " + product);
    }
    
    @Test
    void generateRectangle() {
        Object result = generator.generateValueOfType(Rectangle.class);
        
        assertNotNull(result);
        assertInstanceOf(Rectangle.class, result);
        
        Rectangle rectangle = (Rectangle) result;
        
        System.out.println("Generated Rectangle: " + rectangle);
    }
    
    @Test
    void generateTriangle() {
        Object result = generator.generateValueOfType(Triangle.class);
        
        assertNotNull(result);
        assertInstanceOf(Triangle.class, result);
        
        Triangle triangle = (Triangle) result;
        System.out.println("Generated Triangle: " + triangle);
    }

    @Test
    void generateCart() {
        Object result = generator.generateValueOfType(Cart.class);

        assertNotNull(result);
        assertInstanceOf(Cart.class, result);

        Cart cart = (Cart) result;
        assertNotNull(cart.getItems(), "Cart items should not be null");
        assertTrue(
                cart.getItems().size() <= MAX_COLLECTION_SIZE,
                "Cart should have at most" + MAX_COLLECTION_SIZE + " items"
        );

        cart.getItems().forEach(item -> {
            assertInstanceOf(Product.class, item, "Cart item should be Product");
            assertNotNull(item.getName());
        });

        System.out.println("Generated Cart with " + cart.getItems().size() + " items:");
        cart.getItems().forEach(p -> System.out.println("  - " + p));
    }
    
    @Test
    void generateShapeImplementation() {
        Object result = generator.generateValueOfType(Shape.class);
        
        assertNotNull(result);
        assertInstanceOf(Shape.class, result);
        assertTrue(result instanceof Rectangle || result instanceof Triangle,
            "Shape should be either Rectangle or Triangle");
        
        Shape shape = (Shape) result;
        assertTrue(shape.getArea() >= 0);
        assertTrue(shape.getPerimeter() >= 0);
        
        System.out.println("Generated Shape: " + shape.getClass().getSimpleName() + " = " + shape);
    }
    
    @Test
    void generateBinaryTreeNode() {
        Object result = generator.generateValueOfType(BinaryTreeNode.class);
        
        assertNotNull(result);
        assertInstanceOf(BinaryTreeNode.class, result);
        
        BinaryTreeNode tree = (BinaryTreeNode) result;
        assertNotNull(tree.getData(), "Root data should not be null");
        
        int depth = calculateTreeDepth(tree);
        assertTrue(depth <= MAX_RECURSION_DEPTH + 1, "Tree depth should be limited");
        
        System.out.println("Generated BinaryTreeNode with depth " + depth + ":");
        printTree(tree, "", true);
    }
    
    @Test
    void generateNotGeneratableThrows() {
        class NotGeneratable {
            public NotGeneratable(int x) {}
        }
        
        assertThrows(RuntimeException.class, () -> 
            generator.generateValueOfType(NotGeneratable.class)
        );
    }

    private int calculateTreeDepth(BinaryTreeNode node) {
        if (node == null) {
            return 0;
        }
        return Math.max(
            calculateTreeDepth(node.getLeft()),
            calculateTreeDepth(node.getRight())
        ) + 1;
    }

    private void printTree(BinaryTreeNode node, String prefix, boolean isTail) {
        if (node == null) {
            return;
        }

        System.out.println(prefix + (isTail ? "└── " : "├── ") + node.getData());

        if (node.getLeft() != null || node.getRight() != null) {
            if (node.getLeft() != null) {
                printTree(node.getLeft(), prefix + (isTail ? "    " : "│   "), node.getRight() == null);
            }
            if (node.getRight() != null) {
                printTree(node.getRight(), prefix + (isTail ? "    " : "│   "), true);
            }
        }
    }
}

