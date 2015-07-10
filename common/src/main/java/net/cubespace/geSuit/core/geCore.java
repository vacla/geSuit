package net.cubespace.geSuit.core;

import java.util.Collection;
import java.util.UUID;

import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.remote.RemoteManager;
import net.cubespace.geSuit.core.storage.StorageProvider;

public class geCore {
    private GlobalManager globalManager;
    private ChannelManager channelManager;
    private RemoteManager remoteManager;
    private CommandManager commandManager;
    private StorageProvider storageProvider;
    
    private Platform platform;
    
    public geCore(Platform platform, GlobalManager globalManager, ChannelManager channelManager, CommandManager commandManager, StorageProvider storageProvider) {
        this.platform = platform;
        this.globalManager = globalManager;
        this.channelManager = channelManager;
        this.commandManager = commandManager;
        this.storageProvider = storageProvider;
        
        remoteManager = new RemoteManager(channelManager);
    }
    
    public GlobalPlayer getPlayer(String name) {
        return getPlayer(name, true);
    }
    
    public GlobalPlayer getPlayer(String name, boolean useNickname) {
        return globalManager.getPlayerManager().getPlayer(name, useNickname);
    }
    
    public GlobalPlayer getPlayerExact(String name) {
        return getPlayerExact(name, true);
    }
    
    public GlobalPlayer getPlayerExact(String name, boolean useNickname) {
        return globalManager.getPlayerManager().getPlayerExact(name, useNickname);
    }
    
    public GlobalPlayer getPlayer(UUID id) {
        return globalManager.getPlayerManager().getPlayer(id);
    }
    
    public GlobalPlayer getOfflinePlayer(UUID id) {
        return globalManager.getPlayerManager().getOfflinePlayer(id);
    }
    
    public GlobalPlayer getOfflinePlayer(String name) {
        return getOfflinePlayer(name, true);
    }
    
    public GlobalPlayer getOfflinePlayer(String name, boolean useNickname) {
        return globalManager.getPlayerManager().getOfflinePlayer(name, useNickname);
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
    
    public StorageProvider getStorageProvider() {
        return storageProvider;
    }
    
    public CommandManager getCommandManager() {
        return commandManager;
    }
    
    public GlobalServer getServer() {
        return globalManager.getServerManager().getCurrentServer();
    }
    
    public Collection<GlobalServer> getServers() {
        return globalManager.getServerManager().getServers();
    }
    
    public GlobalServer getServer(String name) {
        return globalManager.getServerManager().getServer(name);
    }
    
    public GlobalServer getServer(int id) {
        return globalManager.getServerManager().getServer(id);
    }
    
    public Messages getMessages() {
        return globalManager.getMessages();
    }
}
