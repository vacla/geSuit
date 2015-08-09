package net.cubespace.geSuit.core.commands;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.List;

import net.cubespace.geSuit.core.commands.ParseTree.ParseResult;
import net.cubespace.geSuit.core.commands.ParseTreeBuilder.Variant;

import org.junit.Test;

import com.google.common.collect.Lists;

public class TestParseTreeParse {
    // methods for commands
    void command0(Object sender, String arg1, String arg2, int arg3) {}
    void command1(Object sender, String arg1, int arg2, String arg3) {}
    void command2(Object sender, String arg1, int arg2, String arg3, int arg4) {}
    void command3(Object sender, int arg1, @Optional Integer arg2) {}
    void command4(Object sender, int arg1, @Varargs String arg2) {}
    
    private Method getMethod(String name) {
        for (Method method : TestParseTreeParse.class.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        fail("Error in test. Invalid method name " + name);
        return null;
    }
    
    private ParseTree makeParseTree(String... methodNames) {
        List<Variant> methods = Lists.newArrayListWithCapacity(methodNames.length);
        for (String name : methodNames) {
            methods.add(Variant.fromMethod(methods.size(), getMethod(name)));
        }
        
        ParseTreeBuilder builder = new ParseTreeBuilder(methods);
        ParseNode root = builder.build();
        return new ParseTree(root, methods);
    }
    
    private <T> void assertResultEquals(Class<T> clazz, T expected, Object actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            assertEquals(clazz, actual.getClass());
            assertEquals(expected, actual);
        }
    }
    
    @Test
    public void testSimpleOk() {
        ParseTree tree = makeParseTree("command0");
        
        ParseResult result = tree.parse(new String[] {"string", "112334", "4421"});
        assertEquals(0, result.variant);
        assertEquals(3, result.parameters.size());
        
        assertResultEquals(String.class, "string", result.parameters.get(0));
        assertResultEquals(String.class, "112334", result.parameters.get(1));
        assertResultEquals(Integer.class, 4421, result.parameters.get(2));
    }
    
    @Test
    public void testDualOk() {
        ParseTree tree = makeParseTree("command1", "command0");
        
        ParseResult result = tree.parse(new String[] {"string", "112334", "4421"});
        assertEquals(0, result.variant);
        assertEquals(3, result.parameters.size());
        
        assertResultEquals(String.class, "string", result.parameters.get(0));
        assertResultEquals(Integer.class, 112334, result.parameters.get(1));
        assertResultEquals(String.class, "4421", result.parameters.get(2));
    }
    
    @Test
    public void testSimpleFailSyntax1() {
        ParseTree tree = makeParseTree("command0");
        
        try {
            tree.parse(new String[] {"string", "112334", "4421", "extra"});
            fail();
        } catch (CommandSyntaxException e) {
            assertTrue(e.hasMoreInput());
        }
    }

    @Test
    public void testSimpleFailSyntax2() {
        ParseTree tree = makeParseTree("command0");
        
        try {
            tree.parse(new String[] {"string", "112334"});
            fail();
        } catch (CommandSyntaxException e) {
            assertFalse(e.hasMoreInput());
        }
    }
    
    @Test
    public void testSimpleFail() {
        ParseTree tree = makeParseTree("command0");

        try {
            tree.parse(new String[] {"string", "112334", "string"});
            fail();
        } catch (CommandInterpretException e) {
            assertEquals("string", e.getInput());
            assertEquals(2, e.getNode().getArgumentIndex());
            
            assertEquals(2, e.getPartialResult().parameters.size());
            assertResultEquals(String.class, "string", e.getPartialResult().parameters.get(0));
            assertResultEquals(String.class, "112334", e.getPartialResult().parameters.get(1));
        }
    }
    
    @Test
    public void testDualFail() {
        ParseTree tree = makeParseTree("command1", "command0");
        
        try {
            tree.parse(new String[] {"string", "string2", "string3"});
            fail();
        } catch (CommandInterpretException e) {
            // because of the order, command1 will be parsed, fail at arg 1, then try command0 which will fail at arg2 
            assertEquals(2, e.getNode().getArgumentIndex());
            assertEquals("string3", e.getInput());
        }
    }
    
    @Test
    public void testDualOkDifferentSize() {
        ParseTree tree = makeParseTree("command1", "command2");
        
        // Test short one
        ParseResult result = tree.parse(new String[] {"string", "123", "442"});
        
        assertEquals(0, result.variant);
        assertResultEquals(String.class, "string", result.parameters.get(0));
        assertResultEquals(Integer.class, 123, result.parameters.get(1));
        assertResultEquals(String.class, "442", result.parameters.get(2));
        
        // Test long one
        result = tree.parse(new String[] {"string", "123", "442", "555"});
        
        assertEquals(1, result.variant);
        assertResultEquals(String.class, "string", result.parameters.get(0));
        assertResultEquals(Integer.class, 123, result.parameters.get(1));
        assertResultEquals(String.class, "442", result.parameters.get(2));
        assertResultEquals(Integer.class, 555, result.parameters.get(3));
    }

    @Test
    public void testDualFailDifferentSize() {
        ParseTree tree = makeParseTree("command1", "command2");
        
        try {
            tree.parse(new String[] {"string", "123", "442", "value"});
            fail();
        } catch (CommandInterpretException e) {
            assertEquals(1, e.getNode().getVariant());
            assertEquals(3, e.getNode().getArgumentIndex());
            assertEquals("value", e.getInput());
        }
    }
    
    @Test
    public void testOptionalFallback() {
        ParseTree tree = makeParseTree("command3");
        
        // With optional specified
        ParseResult result = tree.parse(new String[] {"555", "123"});
        
        assertEquals(0, result.variant);
        assertResultEquals(Integer.class, 555, result.parameters.get(0));
        assertResultEquals(Integer.class, 123, result.parameters.get(1));
        
        // Without optional specified
        result = tree.parse(new String[] {"555"});
        
        assertEquals(0, result.variant);
        assertResultEquals(Integer.class, 555, result.parameters.get(0));
        assertResultEquals(Integer.class, null, result.parameters.get(1));
    }
    
    @Test
    public void testOptionalFail() {
        ParseTree tree = makeParseTree("command3");
        
        try {
            tree.parse(new String[] {"123", "not-int"});
            fail();
        } catch (CommandInterpretException e) {
            assertEquals(1, e.getNode().getArgumentIndex());
            assertEquals("not-int", e.getInput());
        }
    }
    
    @Test
    public void testVarargsOk() {
        ParseTree tree = makeParseTree("command4");
        
        // Just single argument
        ParseResult result = tree.parse(new String[] {"555", "value"});
        
        assertEquals(0, result.variant);
        assertEquals(2, result.parameters.size());
        assertResultEquals(Integer.class, 555, result.parameters.get(0));
        assertResultEquals(String.class, "value", result.parameters.get(1));
        
        // Multiple arguments
        result = tree.parse(new String[] {"555", "value1", "value2", "value3"});
        
        assertEquals(0, result.variant);
        assertEquals(2, result.parameters.size());
        assertResultEquals(Integer.class, 555, result.parameters.get(0));
        assertResultEquals(String.class, "value1 value2 value3", result.parameters.get(1));
    }
    
    @Test
    public void testVarargsFail() {
        ParseTree tree = makeParseTree("command4");
        
        try {
            tree.parse(new String[] {"123"});
            fail();
        } catch (CommandSyntaxException e) {
            assertEquals(1, e.getNode().getArgumentIndex());
            assertTrue(e.getNode().isVarArgs());
        }
    }
    
    
}
