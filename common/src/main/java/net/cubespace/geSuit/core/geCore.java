package net.cubespace.geSuit.core;

import java.util.Collection;
import java.util.UUID;

import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.channel.RedisChannelManager;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.core.remote.RemoteManager;
import net.cubespace.geSuit.core.storage.RedisSection;
import net.cubespace.geSuit.core.storage.StorageSection;

public class geCore {
    private PlayerManager playerManager;
    private ChannelManager channelManager;
    private RemoteManager remoteManager;
    private CommandManager commandManager;
    
    private Platform platform;
    
    public geCore(Platform platform, PlayerManager playerManager, ChannelManager channelManager, CommandManager commandManager) {
        this.platform = platform;
        this.playerManager = playerManager;
        this.channelManager = channelManager;
        this.commandManager = commandManager;
        
        remoteManager = new RemoteManager(channelManager);
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
    
    public RemoteManager getRemoteManager() {
        return remoteManager;
    }
    
    public Platform getPlatform() {
        return platform;
    }
    
    public StorageSection getStorage() {
        return new RedisSection(((RedisChannelManager)channelManager).getRedis());
    }
    
    public CommandManager getCommandManager() {
        return commandManager;
    }
    
    public GlobalServer getServer() {
        return playerManager.getCurrentServer();
    }
    
    public Collection<GlobalServer> getServers() {
        return playerManager.getServers();
    }
    
    public GlobalServer getServer(String name) {
        return playerManager.getServer(name);
    }
    
    public GlobalServer getServer(int id) {
        return playerManager.getServer(id);
    }
}
