package net.cubespace.geSuit.core.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import net.cubespace.geSuit.core.commands.CommandBuilder.CommandDefinition;
import net.cubespace.geSuit.core.commands.ParseTreeBuilder.Variant;

import org.junit.Test;

import com.google.common.collect.Lists;

public class TestCommandWrapper {
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
}
