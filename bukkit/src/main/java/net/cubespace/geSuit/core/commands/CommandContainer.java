package net.cubespace.geSuit.core.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Strings;

class CommandContainer extends Command implements PluginIdentifiableCommand {
    private CommandWrapper proxy;
    protected CommandContainer(CommandWrapper proxy) {
        super(proxy.getName(), Strings.nullToEmpty(proxy.getDescription()), proxy.getUsage(), Arrays.asList(proxy.getAliases()));
        
        setPermission(proxy.getPermission());
        this.proxy = proxy;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        proxy.execute(new BukkitSenderProxy(sender), label, args);
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException {
        return proxy.tabComplete(new BukkitSenderProxy(sender), label, args);
    }
    
    @Override
    public Plugin getPlugin() {
        return (Plugin)proxy.getPlugin();
    }
    
    private static class BukkitSenderProxy implements CommandSenderProxy {
        private CommandSender sender;
        public BukkitSenderProxy(CommandSender sender) {
            this.sender = sender;
        }
        
        @Override
        public Object getSender() {
            return sender;
        }
        
        @Override
        public boolean isPlayer() {
            return sender instanceof Player;
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
            return new BukkitCommandContext<CommandSender>(sender, tag, label);
        }

        @Override
        public CommandContext<?> asErrorContext(net.cubespace.geSuit.core.commands.Command tag, String label, Throwable cause, int argumentIndex, String input) {
            return new BukkitCommandContext<CommandSender>(sender, tag, label, cause, argumentIndex, input);
        } 
    }
    
    private static class BukkitCommandContext<T extends CommandSender> extends CommandContext<T> {
        BukkitCommandContext(T sender, net.cubespace.geSuit.core.commands.Command command, String label) {
            super(sender, command, label);
        }
        
        BukkitCommandContext(T sender, net.cubespace.geSuit.core.commands.Command command, String label, Throwable error, int argument, String argumentValue) {
            super(sender, command, label, error, argument, argumentValue);
        }
        
        @Override
        public void sendMessage(String message) {
            getSender().sendMessage(message);
        }
    }
}
