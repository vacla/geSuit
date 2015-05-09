package net.cubespace.geSuit.core.commands;

import java.util.Arrays;
import java.util.List;

import net.cubespace.geSuit.GSPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

abstract class GSCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {
    private Plugin plugin;
    
    public GSCommand(String name, Object plugin) {
        super(name);
        
        this.plugin = (Plugin)plugin;
    }
    
    public String getName() {
        return super.getName();
    }
    
    protected void setUsage0(String usage) {
        super.setUsage(usage);
    }
    
    protected String getUsage0() {
        return super.getUsage();
    }
    
    protected void setDescription0(String description) {
        super.setDescription(description);
    }
    
    protected void setPermission0(String permission) {
        super.setPermission(permission);
    }
    
    protected void setAliases0(String[] aliases) {
        super.setAliases(Arrays.asList(aliases));
    }
    
    protected boolean isCommandSender(Class<?> clazz) {
        return CommandSender.class.isAssignableFrom(clazz);
    }
    
    protected void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(GSPlugin.getPlugin(GSPlugin.class),runnable);
    }
    
    protected void displayWrongSenderError(Object sender) {
        if (sender instanceof Player) {
            ((CommandSender)sender).sendMessage(ChatColor.RED + "You are unable to run this command. You must run it from a console.");
        } else {
            ((CommandSender)sender).sendMessage(ChatColor.RED + "You are unable to run this command. You must run it from in game.");
        }
    }
    
    protected void sendMessage(Object sender, String message) {
        ((CommandSender)sender).sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    protected abstract boolean execute(Object sender, String label, String[] args);
    
    protected abstract List<String> tabComplete(Object sender, String label, String[] args);

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        return execute((Object)sender, alias, args);
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return tabComplete((Object)sender, alias, args);
    }
}