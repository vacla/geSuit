package net.cubespace.geSuit.core.commands;

import java.util.List;

// THIS CLASS SHOULD BE REPLACED ON BUNGEE AND BUKKIT SIDES
abstract class GSCommand {
    public GSCommand(String name, Object plugin) {
        throw new UnsupportedOperationException("Should have been overridden");
    }
    
    public String getName() {
        throw new UnsupportedOperationException("Should have been overridden");
    }
    
    protected void setUsage0(String usage) {
        throw new UnsupportedOperationException("Should have been overridden");
    }
    
    protected String getUsage0() {
        throw new UnsupportedOperationException("Should have been overridden");
    }
    
    protected void setDescription0(String description) {
        throw new UnsupportedOperationException("Should have been overridden");
    }
    
    protected void setPermission0(String permission) {
        throw new UnsupportedOperationException("Should have been overridden");
    }
    
    protected void setAliases0(String[] aliases) {
        throw new UnsupportedOperationException("Should have been overridden");
    }
    
    protected boolean isCommandSender(Class<?> clazz) {
        throw new UnsupportedOperationException("Should have been overridden");
    }
    
    protected void runAsync(Runnable runnable) {
        throw new UnsupportedOperationException("Should have been overridden");
    }
    
    protected void displayWrongSenderError(Object sender) {
        throw new UnsupportedOperationException("Should have been overridden");
    }
    
    protected void sendMessage(Object sender, String message) {
        throw new UnsupportedOperationException("Should have been overridden");
    }
    
    protected abstract boolean execute(Object sender, String label, String[] args);
    
    protected abstract List<String> tabComplete(Object sender, String label, String[] args);
}
