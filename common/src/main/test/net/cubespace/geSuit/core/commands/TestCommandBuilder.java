package net.cubespace.geSuit.core.commands;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

public class TestCommandBuilder {
    @Command(name="test1", permission="permission.name", usage="/<command> <string> <int>")
    private void test1a(CommandSender sender, String arg1, int arg2) {}
    
    @Command(name="test1", permission="permission.name", usage="/<command> <string>")
    private void test1b(CommandSender sender, String arg1) {}
    
    @Command(name="test2", aliases={"a", "b"}, permission="permission.name", usage="/<command> <string> <int>")
    private void test2a(CommandSender sender, String arg1, int arg2) {}
    
    @Command(name="test2", aliases={"c", "d"}, permission="permission.name", usage="/<command> <string>")
    private void test2b(CommandSender sender, String arg1) {}
    
    @Command(name="test3", aliases={"a", "b"}, permission="permission.name", usage="/<command> <string> <int>")
    private void test3a(CommandSender sender, String arg1, int arg2) {}
    
    @Command(name="test3", aliases={"a", "b"}, permission="permission.name", usage="/<command> <string>")
    private void test3b(CommandSender sender, String arg1) {}
    
    @Command(name="test4", aliases={"a", "b"}, permission="permission.name.special", usage="/<command> <string> <int>")
    private void test4a(CommandSender sender, String arg1, int arg2) {}
    
    @Command(name="test4", aliases={"a", "b"}, permission="permission.name", usage="/<command> <string>")
    private void test4b(CommandSender sender, String arg1) {}
    
    @Command(name="test5", usage="/<command> <string> <int>")
    private void test5(Player sender, String arg1, int arg2) {}
    
    @Command(name="test6", usage="/<command> <string> <int>")
    private void test6(Method sender, String arg1, int arg2) {}
    
    @Command(name="test7", usage="/<command> <string> <int>")
    private void test7(CommandContext<CommandSender> sender, String arg1, int arg2) {}
    @Command(name="test8", usage="/<command> <string> <int>")
    private void test8(CommandContext<Player> sender, String arg1, int arg2) {}
    @Command(name="test9", usage="/<command> <string> <int>")
    private void test9(CommandContext<Method> sender, String arg1, int arg2) {}
    
    @Command(name="test10")
    private void test10() {}
    
    @Command(name="test11", usage="/<command> <string> <int>")
    @CommandPriority(2)
    private void test11a(CommandSender sender, String arg1, int arg2) {}
    @Command(name="test11", usage="/<command> <string> <int>")
    @CommandPriority(1)
    private void test11b(CommandSender sender, String arg1, int arg2) {}
    @Command(name="test11", usage="/<command> <string> <int>")
    @CommandPriority(3)
    private void test11c(CommandSender sender, String arg1, int arg2) {}
    
    @CommandTabCompleter(name="test1")
    private Iterable<String> tabCompleter1a(CommandSender sender, int argument, String input, String arg1, int arg2) {return null;}
    @CommandTabCompleter(name="test1")
    private Iterable<String> tabCompleter1b(CommandSender sender, int argument, String input, String arg1) {return null;}
    @CommandTabCompleter(name="test1")
    private Iterable<String> tabCompleter1c(CommandSender sender, int argument, int input, String arg1, int arg2) {return null;}
    @CommandTabCompleter(name="test1")
    private Iterable<String> tabCompleter1d(String sender, int argument, String input, String arg1, int arg2) {return null;}
    @CommandTabCompleter(name="test1")
    private void tabCompleter1e(CommandSender sender, int argument, String input, String arg1, int arg2) {}
    @CommandTabCompleter(name="test1")
    private Iterable<String> tabCompleter1f(CommandSender sender, double argument, String input, String arg1, int arg2) {return null;}
    @CommandTabCompleter(name="test1")
    private List<String> tabCompleter1g(CommandSender sender, int argument, String input, String arg1, int arg2) {return null;}
    @CommandTabCompleter(name="test1")
    private Iterable<String> tabCompleter1h() {return null;}
    @CommandTabCompleter(name="test1")
    private Iterable<String> tabCompleter1i(CommandSender sender, Integer argument, String input, String arg1, int arg2) {return null;}
    @CommandTabCompleter(name="test1")
    private List<?> tabCompleter1j(CommandSender sender, int argument, String input, String arg1, int arg2) {return null;}
    @CommandTabCompleter(name="test1")
    private Iterable<Integer> tabCompleter1k(CommandSender sender, int argument, String input, String arg1, int arg2) {return null;}
    
    private Method getMethod(String name) {
        for (Method method : TestCommandBuilder.class.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        fail("Error in test. Invalid method name " + name);
        return null;
    }
    
    @Test
    public void testSingleNoAlias() {
        CommandBuilder builder = new FakeCommandBuilder();
        builder.addVariant(getMethod("test1a"));
        
        builder.build();
        
        assertEquals("test1", builder.getName());
        assertEquals("permission.name", builder.getPermission());
        assertNull(builder.getDescription());
        assertEquals("/<command> <string> <int>", builder.getUsage());
        assertNotNull(builder.getAliases());
        assertEquals(0, builder.getAliases().length);
        assertNotNull(builder.getParseTree());
        assertEquals(1, builder.getVariants().size());
    }
    
    @Test
    public void testDualNoAlias() {
        CommandBuilder builder = new FakeCommandBuilder();
        builder.addVariant(getMethod("test1a"));
        builder.addVariant(getMethod("test1b"));
        
        builder.build();
        
        assertEquals("test1", builder.getName());
        assertEquals("permission.name", builder.getPermission());
        assertNull(builder.getDescription());
        assertEquals("/<command> <string>\n/<command> <string> <int>", builder.getUsage());
        assertNotNull(builder.getAliases());
        assertEquals(0, builder.getAliases().length);
        assertNotNull(builder.getParseTree());
        assertEquals(2, builder.getVariants().size());
    }
    
    @Test
    public void testSingleAlias() {
        CommandBuilder builder = new FakeCommandBuilder();
        builder.addVariant(getMethod("test2a"));
        
        builder.build();
        
        assertEquals("test2", builder.getName());
        assertEquals("permission.name", builder.getPermission());
        assertNull(builder.getDescription());
        assertEquals("/<command> <string> <int>", builder.getUsage());
        assertArrayEquals(new String[] {"a", "b"}, builder.getAliases());
        assertNotNull(builder.getParseTree());
    }
    
    @Test
    public void testDualAlias() {
        CommandBuilder builder = new FakeCommandBuilder();
        builder.addVariant(getMethod("test2a"));
        builder.addVariant(getMethod("test2b"));
        
        builder.build();
        
        assertEquals("test2", builder.getName());
        assertEquals("permission.name", builder.getPermission());
        assertNull(builder.getDescription());
        assertEquals("/<command> <string>\n/<command> <string> <int>", builder.getUsage());
        assertArrayEquals(new String[] {"a", "b", "c", "d"}, builder.getAliases());
        assertNotNull(builder.getParseTree());
    }
    
    @Test
    public void testDualSameAlias() {
        CommandBuilder builder = new FakeCommandBuilder();
        builder.addVariant(getMethod("test3a"));
        builder.addVariant(getMethod("test3b"));
        
        builder.build();
        
        assertArrayEquals(new String[] {"a", "b"}, builder.getAliases());
    }
    
    @Test
    public void testDualMultiPerms() {
        CommandBuilder builder = new FakeCommandBuilder();
        builder.addVariant(getMethod("test4a"));
        builder.addVariant(getMethod("test4b"));
        
        builder.build();
        
        assertNull(builder.getPermission());
        
        assertEquals(2, builder.getVariants().size());
        assertEquals("permission.name.special", builder.getVariants().get(0).tag.permission());
        assertEquals("permission.name", builder.getVariants().get(1).tag.permission());
    }
    
    @Test
    public void testFailWrongName() {
        CommandBuilder builder = new FakeCommandBuilder();
        builder.addVariant(getMethod("test3a"));
        
        try {
            builder.addVariant(getMethod("test4b"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("but is declared as "));
        }
    }
    
    @Test
    public void testFailNotCommand() {
        CommandBuilder builder = new FakeCommandBuilder();
        
        try {
            builder.addVariant(getMethod("getMethod"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("requires the @Command annotation "));
        }
    }
    
    @Test
    public void testSinglePlayerSender() {
        CommandBuilder builder = new FakeCommandBuilder();
        
        builder.addVariant(getMethod("test5"));
        
        builder.build();
    }
    
    @Test
    public void testFailWrongSender() {
        CommandBuilder builder = new FakeCommandBuilder();
        
        try {
            builder.addVariant(getMethod("test6"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("first parameter be of type CommandSender, a subclass of CommandSender, "));
        }
    }
    
    @Test
    public void testSingleContextSender() {
        CommandBuilder builder = new FakeCommandBuilder();
        
        builder.addVariant(getMethod("test7"));
        
        builder.build();
    }
    
    @Test
    public void testSingleContextPlayerSender() {
        CommandBuilder builder = new FakeCommandBuilder();
        
        builder.addVariant(getMethod("test8"));
        
        builder.build();
    }
    
    @Test
    public void testFailContextWrongSender() {
        CommandBuilder builder = new FakeCommandBuilder();
        
        try {
            builder.addVariant(getMethod("test9"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("has invalid CommandContext type "));
        }
    }
    
    @Test
    public void testFailNoParams() {
        CommandBuilder builder = new FakeCommandBuilder();
        
        try {
            builder.addVariant(getMethod("test10"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("requires at least one parameter"));
        }
    }
    
    @Test
    public void testPriorityOrdering() {
        CommandBuilder builder = new FakeCommandBuilder();
        
        builder.addVariant(getMethod("test11a")); // Pri 2
        builder.addVariant(getMethod("test11b")); // Pri 1
        builder.addVariant(getMethod("test11c")); // Pri 3
        
        builder.build();
        
        assertEquals(3, builder.getVariants().size());
        assertEquals("test11c", builder.getVariants().get(0).method.getName());
        assertEquals("test11a", builder.getVariants().get(1).method.getName());
        assertEquals("test11b", builder.getVariants().get(2).method.getName());
    }
    
    @Test
    public void testAddTabCompleter() {
        CommandBuilder builder = new FakeCommandBuilder();
        
        builder.addVariant(getMethod("test1a"));
        builder.addTabCompleter(getMethod("tabCompleter1a"));
        
        builder.build();
        assertNotNull(builder.getVariants().get(0).tabCompleter);
    }
    
    @Test
    public void testTCIntegerParam() {
        CommandBuilder builder = new FakeCommandBuilder();
        
        builder.addVariant(getMethod("test1a"));
        builder.addTabCompleter(getMethod("tabCompleter1i"));
    }
    
    @Test
    public void testTCFailNoCommand() {
        CommandBuilder builder = new FakeCommandBuilder();
        try {
            builder.addTabCompleter(getMethod("tabCompleter1a"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("registered under command"));
        }
    }
    
    @Test
    public void testTCFailFirstArg() {
        CommandBuilder builder = new FakeCommandBuilder();
        try {
            builder.addVariant(getMethod("test1a"));
            builder.addTabCompleter(getMethod("tabCompleter1d"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("first parameter"));
        }
    }
    
    @Test
    public void testTCFailSecondArg() {
        CommandBuilder builder = new FakeCommandBuilder();
        try {
            builder.addVariant(getMethod("test1a"));
            builder.addTabCompleter(getMethod("tabCompleter1f"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("second parameter"));
        }
    }
    
    @Test
    public void testTCFailThirdArg() {
        CommandBuilder builder = new FakeCommandBuilder();
        try {
            builder.addVariant(getMethod("test1a"));
            builder.addTabCompleter(getMethod("tabCompleter1c"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("third parameter"));
        }
    }
    
    @Test
    public void testTCFailNoMatch() {
        CommandBuilder builder = new FakeCommandBuilder();
        try {
            builder.addVariant(getMethod("test1a"));
            builder.addTabCompleter(getMethod("tabCompleter1b"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("variants"));
        }
    }
    
    @Test
    public void testTCFailReturn() {
        CommandBuilder builder = new FakeCommandBuilder();
        try {
            builder.addVariant(getMethod("test1a"));
            builder.addTabCompleter(getMethod("tabCompleter1e"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("return"));
        }
    }
    
    @Test
    public void testTCFailNoTag() {
        CommandBuilder builder = new FakeCommandBuilder();
        try {
            builder.addVariant(getMethod("test1a"));
            builder.addTabCompleter(getMethod("test1a"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("@CommandTabCompleter"));
        }
    }
    
    @Test
    public void testTCFailParamCount() {
        CommandBuilder builder = new FakeCommandBuilder();
        try {
            builder.addVariant(getMethod("test1a"));
            builder.addTabCompleter(getMethod("tabCompleter1h"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("3 parameters"));
        }
    }
    
    @Test
    public void testTCSubclassReturn() {
        CommandBuilder builder = new FakeCommandBuilder();
        builder.addVariant(getMethod("test1a"));
        builder.addTabCompleter(getMethod("tabCompleter1g"));
    }
    
    @Test
    public void testTCWildcardReturn() {
        CommandBuilder builder = new FakeCommandBuilder();
        try {
            builder.addVariant(getMethod("test1a"));
            builder.addTabCompleter(getMethod("tabCompleter1j"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("return"));
        }
    }
    
    @Test
    public void testTCIntReturn() {
        CommandBuilder builder = new FakeCommandBuilder();
        try {
            builder.addVariant(getMethod("test1a"));
            builder.addTabCompleter(getMethod("tabCompleter1k"));
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("return"));
        }
    }
    
    private static class FakeCommandBuilder extends CommandBuilder {
        @Override
        protected boolean isCommandSender(Class<?> clazz) {
            return CommandSender.class.isAssignableFrom(clazz);
        }
    }
    
    // Fake interfaces as no CommandSenders/players are available in command
    private interface CommandSender {
    }
    
    private interface Player extends CommandSender {
    }
}
