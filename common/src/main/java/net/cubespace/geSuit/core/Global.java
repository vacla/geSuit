package net.cubespace.geSuit.core;

import java.util.UUID;

import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.core.remote.RemoteManager;
import net.cubespace.geSuit.core.storage.StorageSection;

import com.google.common.base.Preconditions;

/**
 * Static Access wrapper for {@link geCore}
 */
public final class Global {
    private Global() {}
    
    private static geCore instance;
    
    public static void setInstance(geCore instance) {
        Preconditions.checkState(Global.instance == null, "Instance is already set!");
        
        Global.instance = instance;
    }
    
    public static GlobalPlayer getPlayer(String name) {
        return instance.getPlayer(name, true);
    }
    
    public static GlobalPlayer getPlayer(String name, boolean useNickname) {
        return instance.getPlayer(name, useNickname);
    }
    
    public static GlobalPlayer getPlayerExact(String name) {
        return instance.getPlayerExact(name, true);
    }
    
    public static GlobalPlayer getPlayerExact(String name, boolean useNickname) {
        return instance.getPlayerExact(name, useNickname);
    }
    
    public static GlobalPlayer getPlayer(UUID id) {
        return instance.getPlayer(id);
    }
    
    public static GlobalPlayer getOfflinePlayer(UUID id) {
        return instance.getOfflinePlayer(id);
    }
    
    public static GlobalPlayer getOfflinePlayer(String name) {
        return instance.getOfflinePlayer(name, true);
    }
    
    public static GlobalPlayer getOfflinePlayer(String name, boolean useNickname) {
        return instance.getOfflinePlayer(name, useNickname);
    }
    
    public static ChannelManager getChannelManager() {
        return instance.getChannelManager();
    }
    
    public static RemoteManager getRemoteManager() {
        return instance.getRemoteManager();
    }
    
    public static Platform getPlatform() {
        return instance.getPlatform();
    }
    
    public static StorageSection getStorage() {
        return instance.getStorage();
    }
    
    public static CommandManager getCommandManager() {
        return instance.getCommandManager();
    }
}
