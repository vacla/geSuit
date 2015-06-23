package net.cubespace.geSuit.core.commands;

import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

abstract class GSCommand extends Command implements TabExecutor {
    private Plugin plugin;
    
    private String[] aliases;
    private String permission;
    private String usage;
    
    public GSCommand(String name, Object plugin) {
        super(name);
        
        this.plugin = (Plugin)plugin;
        aliases = new String[0];
        usage = "";
    }
    
    @Override
    public String getPermission() {
        return permission;
    }
    
    @Override
    public String[] getAliases() {
        return aliases;
    }
    
    public String getName() {
        return super.getName();
    }
    
    protected void setUsage0(String usage) {
        this.usage = usage;
    }
    
    protected String getUsage0() {
        return usage;
    }
    
    public Plugin getPlugin() {
        return plugin;
    }
    
    protected void setDescription0(String description) {
    }
    
    protected void setPermission0(String permission) {
        this.permission = permission;
    }
    
    protected void setAliases0(String[] aliases) {
        this.aliases = aliases;
    }
    
    protected boolean isCommandSender(Class<?> clazz) {
        return CommandSender.class.isAssignableFrom(clazz);
    }
    
    protected void runAsync(Runnable runnable) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, runnable);
    }
    
    protected void displayWrongSenderError(Object sender) {
        if (sender instanceof ProxiedPlayer) {
            ((CommandSender)sender).sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You are unable to run this command. You must run it from a console."));
        } else {
            ((CommandSender)sender).sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You are unable to run this command. You must run it from in game."));
        }
    }
    
    protected void sendMessage(Object sender, String message) {
        ((CommandSender)sender).sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
    }
    
    protected abstract boolean execute(Object sender, String label, String[] args);
    
    protected abstract List<String> tabComplete(Object sender, String label, String[] args);
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        execute((Object)sender, getName(), args);
    }
    
    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return tabComplete((Object)sender, getName(), args);
    }
    
    protected CommandContext<?> createContext(final Object sender, net.cubespace.geSuit.core.commands.Command command, String label) {
        return new BungeeCommandContext<CommandSender>((CommandSender)sender, command, label);
    }
    
    protected CommandContext<?> createErrorContext(Object sender, net.cubespace.geSuit.core.commands.Command command, String label, Throwable error, int argument, String input) {
        return new BungeeCommandContext<CommandSender>((CommandSender)sender, command, label, error, argument, input);
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
            getSender().sendMessage(TextComponent.fromLegacyText(message));
        }
    }
}