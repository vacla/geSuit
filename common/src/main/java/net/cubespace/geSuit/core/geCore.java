package net.cubespace.geSuit.core;

import java.util.UUID;

import net.cubespace.geSuit.core.channel.ChannelManager;

public class geCore {
    private PlayerManager playerManager;
    private ChannelManager channelManager;
    private Platform platform;
    
    public geCore(Platform platform, PlayerManager playerManager, ChannelManager channelManager) {
        this.platform = platform;
        this.playerManager = playerManager;
        this.channelManager = channelManager;
    }
    
    public GlobalPlayer getPlayer(String name) {
        return getPlayer(name, true);
    }
    
    public GlobalPlayer getPlayer(String name, boolean useNickname) {
        return playerManager.getPlayer(name, useNickname);
    }
    
    public GlobalPlayer getPlayerExact(String name) {
        return getPlayerExact(name, true);
    }
    
    public GlobalPlayer getPlayerExact(String name, boolean useNickname) {
        return playerManager.getPlayerExact(name, useNickname);
    }
    
    public GlobalPlayer getPlayer(UUID id) {
        return playerManager.getPlayer(id);
    }
    
    public GlobalPlayer getOfflinePlayer(UUID id) {
        return playerManager.getOfflinePlayer(id);
    }
    
    public GlobalPlayer getOfflinePlayer(String name) {
        return getOfflinePlayer(name, true);
    }
    
    public GlobalPlayer getOfflinePlayer(String name, boolean useNickname) {
        return playerManager.getOfflinePlayer(name, useNickname);
    }
    
    public ChannelManager getChannelManager() {
        return channelManager;
    }
    
    public Platform getPlatform() {
        return platform;
    }
}
