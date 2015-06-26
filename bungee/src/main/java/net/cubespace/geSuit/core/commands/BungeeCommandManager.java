package net.cubespace.geSuit.core.commands;

import java.util.Collection;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeCommandManager extends CommandManager {
    @Override
    protected void installCommands(Collection<CommandWrapper> commands) {
        for (CommandWrapper command : commands) {
            ProxyServer.getInstance().getPluginManager().registerCommand((Plugin)command.getPlugin(), new CommandContainer(command));
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
                ProxyServer.getInstance().getScheduler().runAsync((Plugin)plugin, block);
            }
        };
    }
}
