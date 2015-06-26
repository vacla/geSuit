package net.cubespace.geSuit.core.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.cubespace.geSuit.core.commands.CommandBuilder.CommandDefinition;
import net.cubespace.geSuit.core.commands.ParseTreeBuilder.Variant;

import org.junit.Test;

import com.google.common.collect.Lists;

public class TestCommandContainer {
    @Test
    public void testConstruct() {
        Object instance = new Object();
        
        // Prepare the values
        CommandBuilder builder = mock(CommandBuilder.class);
        when(builder.getName()).thenReturn("name");
        when(builder.getAliases()).thenReturn(new String[] {"alias"});
        when(builder.getPermission()).thenReturn("permission");
        when(builder.getUsage()).thenReturn("usage");
        when(builder.getParseTree()).thenReturn(new ParseTree(new ParseNode(-1, -1), Lists.<Variant>newArrayList()));
        when(builder.getDescription()).thenReturn("description");
        when(builder.getVariants()).thenReturn(Lists.<CommandDefinition>newArrayList());
        
        CommandWrapper wrapper = new CommandWrapper(null, instance, builder) {
            @Override
            protected void runAsync(Runnable block) {
            }
        };
        
        // Test the container
        CommandContainer container = new CommandContainer(wrapper);
        assertEquals("name", container.getName());
        assertEquals("permission", container.getPermission());
        assertArrayEquals(new String[] {"alias"}, container.getAliases());
    }
    
    @Test
    public void testMinimalConstruct() {
        Object instance = new Object();
        
        // Prepare the values
        CommandBuilder builder = mock(CommandBuilder.class);
        when(builder.getName()).thenReturn("name");
        when(builder.getAliases()).thenReturn(new String[] {});
        when(builder.getPermission()).thenReturn(null);
        when(builder.getUsage()).thenReturn("usage");
        when(builder.getParseTree()).thenReturn(new ParseTree(new ParseNode(-1, -1), Lists.<Variant>newArrayList()));
        when(builder.getDescription()).thenReturn(null);
        when(builder.getVariants()).thenReturn(Lists.<CommandDefinition>newArrayList());
        
        CommandWrapper wrapper = new CommandWrapper(null, instance, builder) {
            @Override
            protected void runAsync(Runnable block) {
            }
        };
        
        // Test the container
        CommandContainer container = new CommandContainer(wrapper);
        assertEquals("name", container.getName());
        assertNull(container.getPermission());
        assertTrue(container.getAliases().length == 0);
    }
}
