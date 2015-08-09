package net.cubespace.geSuit.core.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Arrays;

import net.cubespace.geSuit.core.commands.CommandBuilder.CommandDefinition;
import net.cubespace.geSuit.core.commands.ParseTreeBuilder.Variant;

import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TestCommandWrapper {
    
    private Method getMethod(String name) {
        for (Method method : TestCommandWrapper.class.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        fail("Error in test. Invalid method name " + name);
        return null;
    }
    
    @Test
    public void testConstruct() {
        Object instance = new Object();
        String plugin = "plugin";
        
        // Prepare the values
        CommandBuilder builder = mock(CommandBuilder.class);
        when(builder.getName()).thenReturn("name");
        when(builder.getAliases()).thenReturn(new String[] {"alias"});
        when(builder.getPermission()).thenReturn("permission");
        when(builder.getUsage()).thenReturn("usage");
        when(builder.getParseTree()).thenReturn(new ParseTree(new ParseNode(-1, -1), Lists.<Variant>newArrayList()));
        when(builder.getDescription()).thenReturn("description");
        when(builder.getVariants()).thenReturn(Lists.<CommandDefinition>newArrayList());
        
        // Create the wrapper
        CommandWrapper wrapper = new CommandWrapper(plugin, instance, builder) {
            @Override
            protected void runAsync(Runnable block) {
            }
        };
        
        assertEquals("name", wrapper.getName());
        assertEquals("permission", wrapper.getPermission());
        assertEquals("usage", wrapper.getUsage());
        assertEquals("description", wrapper.getDescription());
        assertEquals("plugin", wrapper.getPlugin());
        assertArrayEquals(new String[] {"alias"}, wrapper.getAliases());
    }
    
    private CommandWrapper createCommand(String method) {
        CommandBuilder builder = new CommandBuilder() {
            @Override
            protected boolean isCommandSender(Class<?> clazz) {
                return clazz.equals(String.class);
            }
        };
        
        builder.addVariant(getMethod(method));
        builder.build();
        
        return new CommandWrapper("plugin", this, builder) {
            @Override
            protected void runAsync(Runnable block) {
            }
        };
    }
    
    @Test
    public void testExecuteBasic() {
        // Prepare
        CommandWrapper wrapper = createCommand("test1");
        
        // Run the test
        wrapper.execute(new CommandSenderProxyImpl("sender", false), "test1", new String[] {"string", "123"});
        
        // Tests are in test1()
    }
    
    @Command(name="test1", permission="permission.name", usage="/<command> <string> <int>")
    private void test1(String sender, String arg1, int arg2) {
        assertEquals("sender", sender);
        assertEquals("string", arg1);
        assertEquals(123, arg2);
    }
    
    @Test
    public void testExecuteCommandContext() {
        // Prepare
        CommandWrapper wrapper = createCommand("test2a");
        
        // Run the test
        wrapper.execute(new CommandSenderProxyImpl("sender", false), wrapper.getName(), new String[] {"string", "123"});
        
        // Tests are in test2a()
    }
    
    @Command(name="test2", permission="permission.name", usage="/<command> <string> <int>")
    private void test2a(CommandContext<String> context, String arg1, int arg2) {
        assertEquals("sender", context.getSender());
        assertEquals("string", arg1);
        assertEquals(123, arg2);
    }
    
    @Test
    public void testExecuteCommandErrorContext() {
        // Prepare
        CommandWrapper wrapper = createCommand("test2b");
        
        // Run the test
        wrapper.execute(new CommandSenderProxyImpl("sender", false), wrapper.getName(), new String[] {"string", "error"});
        
        // Tests are in test2b()
    }
    
    @Command(name="test2", permission="permission.name", usage="/<command> <string> <int>")
    private void test2b(CommandContext<String> context, String arg1, int arg2) {
        assertTrue(context.isErrored());
        assertEquals("error", context.getErrorInput());
        assertEquals("sender", context.getSender());
    }
    
    private CommandWrapper createCommand(String method, String tabComplete) {
        CommandBuilder builder = new CommandBuilder() {
            @Override
            protected boolean isCommandSender(Class<?> clazz) {
                return clazz.equals(String.class);
            }
        };
        
        builder.addVariant(getMethod(method));
        builder.addTabCompleter(getMethod(tabComplete));
        builder.build();
        
        return new CommandWrapper("plugin", this, builder) {
            @Override
            protected void runAsync(Runnable block) {
            }
        };
    }
    
    @Test
    public void testTCInputArg1() {
        // Prepare
        CommandWrapper wrapper = createCommand("test1", "test1TabComplete");
        
        // Run the test
        wrapper.tabComplete(new CommandSenderProxyImpl("sender", false), wrapper.getName(), new String[] {"test"});
        
        // Tests are in test1TabComplete()
    }
    
    @CommandTabCompleter(name="test1")
    private Iterable<String> test1TabComplete(String sender, int argument, String input, String arg1, int arg2) {
        assertEquals(0, argument);
        assertEquals("test", input);
        return null;
    }
    
    @Test
    public void testTCInputArg2() {
        // Prepare
        CommandWrapper wrapper = createCommand("test1", "test2TabComplete");
        
        // Run the test
        wrapper.tabComplete(new CommandSenderProxyImpl("sender", false), wrapper.getName(), new String[] {"test", "5"});
        
        // Tests are in test2TabComplete()
    }
    
    @CommandTabCompleter(name="test1")
    private Iterable<String> test2TabComplete(String sender, int argument, String input, String arg1, int arg2) {
        assertEquals(1, argument);
        assertEquals("5", input);
        assertEquals("test", arg1);
        
        return null;
    }
    
    @Test
    public void testTCReturn() {
        // Prepare
        CommandWrapper wrapper = createCommand("test1", "test3TabComplete");
        
        // Run the test
        Iterable<String> results = wrapper.tabComplete(new CommandSenderProxyImpl("sender", false), wrapper.getName(), new String[] {"t"});
        
        assertTrue(Iterables.contains(results, "test"));
        assertTrue(Iterables.contains(results, "thingie"));
    }
    
    @CommandTabCompleter(name="test1")
    private Iterable<String> test3TabComplete(String sender, int argument, String input, String arg1, int arg2) {
        return Arrays.asList("test", "thingie");
    }
    
    @Test
    public void testTCTooManyArgs() {
        // Prepare
        CommandWrapper wrapper = createCommand("test1", "test4TabComplete");
        
        // Run the test
        wrapper.tabComplete(new CommandSenderProxyImpl("sender", false), wrapper.getName(), new String[] {"test", "123", "extra"});
        
        // Should never call test4TabComplete()
    }
    
    @CommandTabCompleter(name="test1")
    private Iterable<String> test4TabComplete(String sender, int argument, String input, String arg1, int arg2) {
        fail();
        return null;
    }
    
    @Test
    public void testTCExactArgsInvalid() {
        // Prepare
        CommandWrapper wrapper = createCommand("test1", "test5TabComplete");
        
        // Run the test
        wrapper.tabComplete(new CommandSenderProxyImpl("sender", false), wrapper.getName(), new String[] {"test", "asd"});
        
        // Tests are in test5TabComplete()
    }
    
    @CommandTabCompleter(name="test1")
    private Iterable<String> test5TabComplete(String sender, int argument, String input, String arg1, int arg2) {
        assertEquals(1, argument);
        assertEquals("asd", input);
        
        return null;
    }
    
    @Test
    public void testTCEmptyArgs() {
        // Prepare
        CommandWrapper wrapper = createCommand("test1", "test6TabComplete");
        
        // Run the test
        wrapper.tabComplete(new CommandSenderProxyImpl("sender", false), wrapper.getName(), new String[] {});
        
        // Tests are in test6TabComplete
    }
    
    @CommandTabCompleter(name="test1")
    private Iterable<String> test6TabComplete(String sender, int argument, String input, String arg1, int arg2) {
        assertEquals(0, argument);
        assertEquals("", input);
        
        return null;
    }
    
    @Test
    public void testTCNextEmpty() {
        // Prepare
        CommandWrapper wrapper = createCommand("test1", "test7TabComplete");
        
        // Run the test
        wrapper.tabComplete(new CommandSenderProxyImpl("sender", false), wrapper.getName(), new String[] {"test", ""});
        
        // Tests are in test7TabComplete
    }
    
    @CommandTabCompleter(name="test1")
    private Iterable<String> test7TabComplete(String sender, int argument, String input, String arg1, int arg2) {
        assertEquals(1, argument);
        assertEquals("", input);
        return null;
    }
    
    private static class CommandSenderProxyImpl implements CommandSenderProxy {
        private final String sender;
        private final boolean isPlayer;
        
        public CommandSenderProxyImpl(String sender, boolean isPlayer) {
            this.sender = sender;
            this.isPlayer = isPlayer;
        }
        
        @Override
        public Object getSender() {
            return sender;
        }

        @Override
        public boolean isPlayer() {
            return isPlayer;
        }

        @Override
        public void sendMessage(String message) {
        }

        @Override
        public boolean hasPermission(String permission) {
            return true;
        }

        @Override
        public CommandContext<?> asContext(Command tag, String label) {
            return new CommandContext<String>(sender, tag, label) {
                @Override
                public void sendMessage(String message) {
                }
            };
        }

        @Override
        public CommandContext<?> asErrorContext(Command tag, String label, Throwable cause, int argumentIndex, String input) {
            return new CommandContext<String>(sender, tag, label, cause, argumentIndex, input) {
                @Override
                public void sendMessage(String message) {
                }
            };
        }
        
    }
}
