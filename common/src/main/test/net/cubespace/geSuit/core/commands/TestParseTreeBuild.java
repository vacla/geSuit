package net.cubespace.geSuit.core.commands;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.List;

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
    
    private Method getMethod(String name) {
        for (Method method : TestParseTreeBuild.class.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        fail("Error in test. Invalid method name " + name);
        return null;
    }

    @Test
	public void testSinglePlain() {
        List<Method> variants = Lists.newArrayList(getMethod("command0"));
        ParseTree tree = new ParseTree(variants);
        
        tree.build();
        
        // Confirm the correct tree was generated
        ParseNode node = tree.root;
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
        List<Method> variants = Lists.newArrayList(getMethod("command0"), getMethod("command1"));
        ParseTree tree = new ParseTree(variants);
        
        tree.build();
        
        // Confirm the correct tree was generated
        ParseNode left = tree.root;
        ParseNode right = null;
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
        List<Method> variants = Lists.newArrayList(getMethod("command2"));
        ParseTree tree = new ParseTree(variants);
        
        tree.build();
        
        // Confirm the correct tree was generated
        ParseNode node;
        ParseNode parent = tree.root;
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
        List<Method> variants = Lists.newArrayList(getMethod("command3"));
        ParseTree tree = new ParseTree(variants);
        
        tree.build();
        
        // Confirm the correct tree was generated
        ParseNode node = tree.root;
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
        List<Method> variants = Lists.newArrayList(getMethod("command4"));
        ParseTree tree = new ParseTree(variants);
        
        tree.build();
        
        // Confirm the correct tree was generated
        ParseNode node;
        ParseNode parent = tree.root;
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
        List<Method> variants = Lists.newArrayList(getMethod("command0"), getMethod("command2"), getMethod("command3"));
        ParseTree tree = new ParseTree(variants);
        
        tree.build();
        
        // Confirm the correct tree was generated
        ParseNode parent = tree.root;
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

}
