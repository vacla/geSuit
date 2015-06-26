package net.cubespace.geSuit.core.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.TabExecutor;

class CommandContainer extends net.md_5.bungee.api.plugin.Command implements TabExecutor {
    private CommandWrapper proxy;
    protected CommandContainer(CommandWrapper proxy) {
        super(proxy.getName(), proxy.getPermission(), proxy.getAliases());
        
        this.proxy = proxy;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        proxy.execute(new BungeeSenderProxy(sender), proxy.getName(), args);
    }
    
    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return proxy.tabComplete(new BungeeSenderProxy(sender), proxy.getName(), args);
    }
    
    private static class BungeeSenderProxy implements CommandSenderProxy {
        private CommandSender sender;
        public BungeeSenderProxy(CommandSender sender) {
            this.sender = sender;
        }
        
        @Override
        public Object getSender() {
            return sender;
        }
        
        @Override
        public boolean isPlayer() {
            return sender instanceof ProxiedPlayer;
        }

        @Override
        public void sendMessage(String message) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }

        @Override
        public boolean hasPermission(String permission) {
            return sender.hasPermission(permission);
        }

        @Override
        public CommandContext<?> asContext(net.cubespace.geSuit.core.commands.Command tag, String label) {
            return new BungeeCommandContext<CommandSender>(sender, tag, label);
        }

        @Override
        public CommandContext<?> asErrorContext(net.cubespace.geSuit.core.commands.Command tag, String label, Throwable cause, int argumentIndex, String input) {
            return new BungeeCommandContext<CommandSender>(sender, tag, label, cause, argumentIndex, input);
        } 
    }
    
    private static class BungeeCommandContext<T extends CommandSender> extends CommandContext<T> {
        BungeeCommandContext(T sender, net.cubespace.geSuit.core.commands.Command command, String label) {
            super(sender, command, label);
        }
        
        BungeeCommandContext(T sender, net.cubespace.geSuit.core.commands.Command command, String label, Throwable error, int argument, String argumentValue) {
            super(sender, command, label, error, argument, argumentValue);
        }
        
        @Override
        public void sendMessage(String message) {
            getSender().sendMessage(message);
        }
    }
}
