package net.cubespace.geSuit;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import java.util.logging.Logger;

import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.config.ServerInfo;

public final class geSuit {
    private static geSuitPlugin plugin;
    
    static void setPlugin(geSuitPlugin plugin) {
        geSuit.plugin = plugin;
    }
    
    public static geSuitPlugin getPlugin() {
        return plugin;
    }
    
    public static Logger getLogger() {
        return plugin.getLogger();
    }
    
    public static File getFile(String path) {
        return new File(plugin.getDataFolder(), path);
    }
    
    public static File getFile() {
        return plugin.getDataFolder();
    }
    
    public static InputStream getResource(String path) {
        return plugin.getResourceAsStream(path);
    }
    
    public static ChannelManager getChannelManager() {
        return plugin.getChannelManager();
    }
    
    public static int getServerId(ServerInfo server) {
        return server.getAddress().getPort();
    }
    
    public static GSPlayer getPlayer(String name) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public static GSPlayer getPlayer(UUID id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public static GSPlayer getOfflinePlayer(String name) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public static GSPlayer getOfflinePlayer(UUID id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
