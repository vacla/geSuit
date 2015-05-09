package net.cubespace.geSuit.core.commands;

import java.util.Collection;

import net.md_5.bungee.api.ProxyServer;

public class BungeeCommandManager extends CommandManager {
    @Override
    protected void installCommands(Collection<WrapperCommand> commands) {
        for (WrapperCommand command : commands) {
            ProxyServer.getInstance().getPluginManager().registerCommand(command.getPlugin(), command);
        }
    }
}
