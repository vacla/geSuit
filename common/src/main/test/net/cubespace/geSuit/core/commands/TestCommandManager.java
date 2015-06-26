package net.cubespace.geSuit.core.commands;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.Test;

public class TestCommandManager {
    @Command(name="test1", permission="permission.name", usage="/<command> <string> <int>")
    private void test1a(CommandSender sender, String arg1, int arg2) {}
    
    @Command(name="test2", permission="permission.name", usage="/<command> <string> <int>")
    private void test2a(CommandSender sender, String arg1, int arg2) {}
    
    @Command(name="test2", permission="permission.name", usage="/<command> <string>")
    private void test2b(CommandSender sender, String arg1) {}
    
    private Method getMethod(String name) {
        for (Method method : TestCommandManager.class.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        fail("Error in test. Invalid method name " + name);
        return null;
    }
    
    @Test
    public void testRegisterAll() {
        final String plugin = "test";
        
        CommandManager manager = new FakeCommandManager() {
            @Override
            protected void installCommands(Collection<CommandWrapper> commands) {
                assertEquals(2, commands.size());
                for (CommandWrapper command : commands) {
                    assertNotNull(command.getPlugin());
                }
            }
        };
        
        manager.registerAll(this, plugin);
    }
    
    @Test
    public void testRegisterOne() {
        final String plugin = "test";
        
        CommandManager manager = new FakeCommandManager() {
            @Override
            protected void installCommands(Collection<CommandWrapper> commands) {
                assertEquals(1, commands.size());
                for (CommandWrapper command : commands) {
                    assertNotNull(command.getPlugin());
                }
            }
        };
        
        manager.registerCommand(this, getMethod("test2a"), plugin);
    }
    
    private static class FakeCommandBuilder extends CommandBuilder {
        @Override
        protected boolean isCommandSender(Class<?> clazz) {
            return CommandSender.class.isAssignableFrom(clazz);
        }
    }
    
    private static class FakeCommandWrapper extends CommandWrapper {
        public FakeCommandWrapper(Object plugin, Object holder, CommandBuilder builder) {
            super(plugin, holder, builder);
        }

        @Override
        protected void runAsync(Runnable block) {
        }
    };
    
    private static abstract class FakeCommandManager extends CommandManager {
        @Override
        protected CommandWrapper createCommand(Object plugin, Object holder, CommandBuilder builder) {
            return new FakeCommandWrapper(plugin, holder, builder);
        }
        
        @Override
        protected CommandBuilder createBuilder() {
            return new FakeCommandBuilder();
        }
    }
    
    // Fake interfaces as no CommandSenders/players are available in command
    private interface CommandSender {
    }
}
