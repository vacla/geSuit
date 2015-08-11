package net.cubespace.geSuit.core.commands;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.List;

import net.cubespace.geSuit.core.commands.ParseTreeBuilder.Variant;
import net.cubespace.geSuit.core.storage.DataConversion;

import org.junit.Test;

import com.google.common.collect.Lists;

public class TestParseTreeBuild {
    // methods for commands
    void command0(Object sender, String arg1, String arg2, int arg3) {}
    void command1(Object sender, String arg1, int arg2, String arg3) {}
    void command2(Object sender, int arg1, @Optional int arg2) {}
    void command3(Object sender, int arg1, @Varargs String arg2) {}
    void command4(Object sender, int arg1, @Optional @Varargs String arg2) {}
    void command5(Object sender, int arg1, @Optional int arg2, String arg3) {}
    void command6(Object sender, @Optional String arg1, String arg2) {}
    
    private Method getMethod(String name) {
        for (Method method : TestParseTreeBuild.class.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        fail("Error in test. Invalid method name " + name);
        return null;
    }
    
    private List<Variant> getVariants(String... methods) {
        List<Variant> variants = Lists.newArrayList();
        for (String method : methods) {
            variants.add(Variant.fromMethod(variants.size(), getMethod(method)));
        }
        
        return variants;
    }

    @Test
	public void testSinglePlain() {
        ParseTreeBuilder builder = new ParseTreeBuilder(getVariants("command0"));
        
        ParseNode node = builder.build();
        
        // Confirm the correct tree was generated
        assertEquals(1, node.getChildren().size());
        // arg1
        node = node.getChildren().get(0);
        assertEquals(DataConversion.getConverter(String.class), node.getTransformer());
        assertEquals(1, node.getChildren().size());
        // arg2
        node = node.getChildren().get(0);
        assertEquals(DataConversion.getConverter(String.class), node.getTransformer());
        assertEquals(1, node.getChildren().size());
        // arg3
        node = node.getChildren().get(0);
        assertEquals(DataConversion.getConverter(Integer.class), node.getTransformer());
        assertEquals(1, node.getChildren().size());
        // terminator
        node = node.getChildren().get(0);
        assertTrue(node.isTerminal());
	}
    
    @Test
    public void testDualPlain() {
        ParseTreeBuilder builder = new ParseTreeBuilder(getVariants("command0", "command1"));
        
        ParseNode left = builder.build();
        ParseNode right = null;
        
        // Confirm the correct tree was generated
        assertEquals(1, left.getChildren().size());
        // arg1 both
        left = right = left.getChildren().get(0);
        assertEquals(DataConversion.getConverter(String.class), left.getTransformer());
        assertEquals(2, left.getChildren().size());
        // arg2 left
        left = left.getChildren().get(0);
        assertEquals(DataConversion.getConverter(String.class), left.getTransformer());
        assertEquals(1, left.getChildren().size());
        // arg2 right
        right = right.getChildren().get(1);
        assertEquals(DataConversion.getConverter(Integer.class), right.getTransformer());
        assertEquals(1, right.getChildren().size());
        // arg3 left
        left = left.getChildren().get(0);
        assertEquals(DataConversion.getConverter(Integer.class), left.getTransformer());
        assertEquals(1, left.getChildren().size());
        // arg3 right
        right = right.getChildren().get(0);
        assertEquals(DataConversion.getConverter(String.class), right.getTransformer());
        assertEquals(1, right.getChildren().size());
        // left terminator
        left = left.getChildren().get(0);
        assertTrue(left.isTerminal());
        // right terminator
        right = right.getChildren().get(0);
        assertTrue(right.isTerminal());
    }

    @Test
    public void testOptional() {
        ParseTreeBuilder builder = new ParseTreeBuilder(getVariants("command2"));
        
        ParseNode parent = builder.build();
        
        // Confirm the correct tree was generated
        ParseNode node;
        assertEquals(1, parent.getChildren().size());
        // arg1
        node = parent = parent.getChildren().get(0);
        assertEquals(DataConversion.getConverter(Integer.class), node.getTransformer());
        assertEquals(2, node.getChildren().size());
        // arg2 (optional)
        node = parent.getChildren().get(0);
        assertEquals(DataConversion.getConverter(Integer.class), node.getTransformer());
        assertEquals(1, node.getChildren().size());
        assertTrue(node.getChildren().get(0).isTerminal());
        // alternate
        node = parent.getChildren().get(1);
        assertTrue(node.isTerminal());
    }
    
    @Test
    public void testVarargs() {
        ParseTreeBuilder builder = new ParseTreeBuilder(getVariants("command3"));
        
        ParseNode node = builder.build();
        
        // Confirm the correct tree was generated
        assertEquals(1, node.getChildren().size());
        // arg1
        node = node.getChildren().get(0);
        assertEquals(DataConversion.getConverter(Integer.class), node.getTransformer());
        assertEquals(1, node.getChildren().size());
        // arg2 (varargs)
        node = node.getChildren().get(0);
        assertEquals(DataConversion.getConverter(String.class), node.getTransformer());
        assertEquals(1, node.getChildren().size());
        assertTrue(node.isVarArgs());
        // terminal
        node = node.getChildren().get(0);
        assertTrue(node.isTerminal());
    }
    
    @Test
    public void testOptionalVarargs() {
        ParseTreeBuilder builder = new ParseTreeBuilder(getVariants("command4"));
        
        ParseNode parent = builder.build();
        
        // Confirm the correct tree was generated
        ParseNode node;
        assertEquals(1, parent.getChildren().size());
        // arg1
        node = parent = parent.getChildren().get(0);
        assertEquals(DataConversion.getConverter(Integer.class), node.getTransformer());
        assertEquals(2, node.getChildren().size());
        // arg2 (optional)
        node = parent.getChildren().get(0);
        assertEquals(DataConversion.getConverter(String.class), node.getTransformer());
        assertEquals(1, node.getChildren().size());
        assertTrue(node.isVarArgs());
        assertTrue(node.getChildren().get(0).isTerminal());
        // alternate
        node = parent.getChildren().get(1);
        assertTrue(node.isTerminal());
    }
    
    @Test
    public void testDualComplex() {
        ParseTreeBuilder builder = new ParseTreeBuilder(getVariants("command0", "command2", "command3"));
        
        ParseNode parent = builder.build();
        
        // Confirm the correct tree was generated
        ParseNode node;
        assertEquals(2, parent.getChildren().size());
        
        // arg1 command0 String
        node = parent.getChildren().get(0);
        assertEquals(DataConversion.getConverter(String.class), node.getTransformer());
        assertEquals(1, node.getChildren().size());
        // Trust that the rest of the nodes are correct as tested above
        
        // arg1 command2 and 3 Integer
        node = parent.getChildren().get(1);
        assertEquals(DataConversion.getConverter(Integer.class), node.getTransformer());
        assertEquals(3, node.getChildren().size());
        parent = node;
        
        // arg2 command2 present Highest
        node = parent.getChildren().get(0);
        assertEquals(DataConversion.getConverter(Integer.class), node.getTransformer());
        assertEquals(1, node.getChildren().size());
        
        // arg2 command3 varargs Normal
        node = parent.getChildren().get(1);
        assertEquals(DataConversion.getConverter(String.class), node.getTransformer());
        assertTrue(node.isVarArgs());
        assertEquals(1, node.getChildren().size());
        
        // arg2 command2 absent Lowest
        node = parent.getChildren().get(2);
        assertTrue(node.isTerminal());
    }

    @Test
    public void testMidOptional() {
        ParseTreeBuilder builder = new ParseTreeBuilder(getVariants("command5"));
        
        ParseNode parent = builder.build();
        
        // Confirm the correct tree was generated
        ParseNode node;
        assertEquals(1, parent.getChildren().size());
        // arg1
        node = parent = parent.getChildren().get(0);
        assertEquals(DataConversion.getConverter(Integer.class), node.getTransformer());
        assertEquals(0, node.getArgumentIndex());
        assertEquals(0, node.getInputIndex());
        assertEquals(2, node.getChildren().size());
        
        // arg2 (optional)
        node = parent.getChildren().get(0);
        assertEquals(DataConversion.getConverter(Integer.class), node.getTransformer());
        assertEquals(1, node.getArgumentIndex());
        assertEquals(1, node.getInputIndex());
        assertEquals(1, node.getChildren().size());
        ParseNode next = node.getChildren().get(0);
        assertFalse(next.isTerminal());
        
        // arg3 (after optional)
        node = next;
        assertEquals(DataConversion.getConverter(String.class), node.getTransformer());
        assertEquals(2, node.getArgumentIndex());
        assertEquals(2, node.getInputIndex());
        assertEquals(1, node.getChildren().size());
        assertTrue(node.getChildren().get(0).isTerminal());
        
        // arg3 (without optional)
        node = parent.getChildren().get(1);
        assertEquals(DataConversion.getConverter(String.class), node.getTransformer());
        assertEquals(2, node.getArgumentIndex());
        assertEquals(1, node.getInputIndex());
        assertEquals(1, node.getChildren().size());
        assertTrue(node.getChildren().get(0).isTerminal());
    }
    
    @Test
    public void testSameTypeOptional() {
        ParseTreeBuilder builder = new ParseTreeBuilder(getVariants("command6"));
        
        ParseNode parent = builder.build();
        
        // Confirm the correct tree was generated
        ParseNode node;
        assertEquals(2, parent.getChildren().size());
        // arg1 (optional)
        node = parent.getChildren().get(0);
        assertEquals(DataConversion.getConverter(String.class), node.getTransformer());
        assertEquals(0, node.getArgumentIndex());
        assertEquals(0, node.getInputIndex());
        assertEquals(1, node.getChildren().size());
        
        // arg2 (with optional)
        node = node.getChildren().get(0);
        assertEquals(DataConversion.getConverter(String.class), node.getTransformer());
        assertEquals(1, node.getArgumentIndex());
        assertEquals(1, node.getInputIndex());
        assertEquals(1, node.getChildren().size());
        ParseNode next = node.getChildren().get(0);
        assertTrue(next.isTerminal());
        
        // arg2 (without optional)
        node = parent.getChildren().get(1);
        assertEquals(DataConversion.getConverter(String.class), node.getTransformer());
        assertEquals(1, node.getArgumentIndex());
        assertEquals(0, node.getInputIndex());
        assertEquals(1, node.getChildren().size());
        next = node.getChildren().get(0);
        assertTrue(next.isTerminal());
    }
}
