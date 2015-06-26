package net.cubespace.geSuit.core.commands;

import java.lang.reflect.Method;
import java.util.Collection;

import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.core.commands.CommandWrapper;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

public class BukkitCommandManager extends CommandManager {
    private SimpleCommandMap commandMap;
    
    public BukkitCommandManager() {
        try
        {
            Method method = Bukkit.getServer().getClass().getMethod("getCommandMap");
            commandMap = (SimpleCommandMap)method.invoke(Bukkit.getServer());
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void installCommands(Collection<CommandWrapper> commands) {
        for (CommandWrapper command : commands) {
            try {
                commandMap.register(((Plugin)command.getPlugin()).getName().toLowerCase(), new CommandContainer(command));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    protected CommandBuilder createBuilder() {
        return new CommandBuilder() {
            @Override
            protected boolean isCommandSender(Class<?> clazz) {
                return CommandSender.class.isAssignableFrom(clazz);
            }
        };
    }

    @Override
    protected CommandWrapper createCommand(final Object plugin, Object holder, CommandBuilder builder) {
        return new CommandWrapper(plugin, holder, builder) {
            @Override
            protected void runAsync(Runnable block) {
                Bukkit.getScheduler().runTaskAsynchronously((Plugin)plugin, block);
            }
        };
    }
}
