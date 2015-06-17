package net.cubespace.geSuit.core;

import java.util.Collection;
import java.util.UUID;

import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.remote.RemoteManager;
import net.cubespace.geSuit.core.storage.StorageSection;

import com.google.common.base.Preconditions;

/**
 * Static Access wrapper for {@link geCore}
 */
public final class Global {
    private Global() {}
    
    private static geCore instance;
    
    /**
     * Sets the backend class. This can not be used by plugins
     */
    public static void setInstance(geCore instance) {
        Preconditions.checkState(Global.instance == null, "Instance is already set!");
        
        Global.instance = instance;
    }
    
    /**
     * Gets an online player by a partial name or nickname
     * @param name The name fragment to find by
     * @return The best matching GlobalPlayer or null
     */
    public static GlobalPlayer getPlayer(String name) {
        return instance.getPlayer(name, true);
    }
    
    /**
     * Gets an online player by a partial name or nickname
     * @param name The name fragment to find by
     * @param useNickname When true, nicknames will be considered as well as names
     * @return The best matching GlobalPlayer or null
     */
    public static GlobalPlayer getPlayer(String name, boolean useNickname) {
        return instance.getPlayer(name, useNickname);
    }
    
    /**
     * Gets an online player by name or nickname.
     * This matches by the exact name or nickname ignoring case. 
     * @param name The whole name to find by
     * @return The best matching GlobalPlayer or null
     */
    public static GlobalPlayer getPlayerExact(String name) {
        return instance.getPlayerExact(name, true);
    }
    
    /**
     * Gets an online player by name or nickname.
     * This matches by the exact name or nickname ignoring case. 
     * @param name The whole name to find by
     * @param useNickname When true, nicknames will be considered as well as names
     * @return The matching GlobalPlayer or null
     */
    public static GlobalPlayer getPlayerExact(String name, boolean useNickname) {
        return instance.getPlayerExact(name, useNickname);
    }
    
    /**
     * Gets an online player by their UUID 
     * @param id The UUID of the player to retrieve
     * @return The matching GlobalPlayer or null
     */
    public static GlobalPlayer getPlayer(UUID id) {
        return instance.getPlayer(id);
    }
    
    /**
     * Gets an online or offline player by their UUID
     * @param id The UUID of the player to retrieve
     * @return The matching GlobalPlayer (either online or offline) or null 
     */
    public static GlobalPlayer getOfflinePlayer(UUID id) {
        return instance.getOfflinePlayer(id);
    }
    
    /**
     * Gets an online or offline player by name or nickname.
     * This matches by the exact name or nickname ignoring case. 
     * @param name The whole name to find by
     * @return The matching GlobalPlayer (either online or offline) or null
     */
    public static GlobalPlayer getOfflinePlayer(String name) {
        return instance.getOfflinePlayer(name, true);
    }
    
    /**
     * Gets an online or offline player by name or nickname.
     * This matches by the exact name or nickname ignoring case. 
     * @param name The whole name to find by
     * @param useNickname When true, nicknames will be considered as well as names
     * @return The matching GlobalPlayer (either online or offline) or null
     */
    public static GlobalPlayer getOfflinePlayer(String name, boolean useNickname) {
        return instance.getOfflinePlayer(name, useNickname);
    }
    
    /**
     * Gets the current ChannelManager for this session.
     * Use this to create communication channels between servers
     * @return The ChannelManager
     */
    public static ChannelManager getChannelManager() {
        return instance.getChannelManager();
    }
    
    /**
     * Gets the current RemoteManager for this session.
     * Use this to define and use remotes.
     * @return The RemoteManager
     * @see RemoteManager
     */
    public static RemoteManager getRemoteManager() {
        return instance.getRemoteManager();
    }
    
    /**
     * Gets some platform specific information
     * @return An instance of Platform
     */
    public static Platform getPlatform() {
        return instance.getPlatform();
    }
    
    /**
     * Provides access to read and write into redis.
     * @return A StorageSection you can manipulate
     */
    public static StorageSection getStorage() {
        return instance.getStorage();
    }
    
    /**
     * Gets the current CommandManager for this session.
     * Use this to use the custom command system
     * @return The CommandManager
     * @see CommandManager
     */
    public static CommandManager getCommandManager() {
        return instance.getCommandManager();
    }
    
    /**
     * @return Returns the current server information.
     */
    public static GlobalServer getServer() {
        return instance.getServer();
    }
    
    /**
     * Gets all servers the proxy knows about
     * @return A collection of servers
     */
    public static Collection<GlobalServer> getServers() {
        return instance.getServers();
    }
    
    /**
     * Gets a servers information by its name
     * @param name The name of the server
     * @return The server information or null
     */
    public static GlobalServer getServer(String name) {
        return instance.getServer(name);
    }
    
    /**
     * Gets a servers information by its id
     * @param id The id of the server
     * @return The server information or null
     */
    public static GlobalServer getServer(int id) {
        return instance.getServer(id);
    }
    
    public static Messages getMessages() {
        return instance.getMessages();
    }
}
