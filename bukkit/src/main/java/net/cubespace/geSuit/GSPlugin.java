package net.cubespace.geSuit;

import java.io.IOException;
import java.util.logging.Level;

import net.cubespace.geSuit.core.ConnectionNotifier;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.channel.RedisChannelManager;
import net.cubespace.geSuit.core.channel.RedisConnection;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class GSPlugin extends JavaPlugin implements ConnectionNotifier {
    
    private RedisConnection redis;
    private RedisChannelManager channelManager;
    private ModuleManager moduleManager;
    
    @Override
    public void onEnable() {
        getLogger().info("Starting geSuit");
        
        if (!initializeRedis()) {
            getLogger().severe("Unable to connect to redis");
            return;
        }
        
        channelManager = new RedisChannelManager(redis, getLogger());
        
        getLogger().info("Initializing modules:");
        moduleManager = new ModuleManager(this);
        moduleManager.loadAll();
        moduleManager.enableAll();
    }
    
    @Override
    public void onDisable() {
        moduleManager.disableAll();
        
        redis.shutdown();
    }
    
    private boolean initializeRedis() {
        FileConfiguration config = getConfig();
        
        try {
            redis = new RedisConnection(config.getString("redis.host", "localhost"), config.getInt("redis.port", 6379), config.getString("redis.password", ""));
            redis.setNotifier(this);
            return true;
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Unable to connect to redis:", e);
            return false;
        }
    }
    
    public ChannelManager getChannelManager() {
        return channelManager;
    }
    
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    @Override
    public void onConnectionRestored() {
        getLogger().info("Connection to redis has been restored.");
    }

    @Override
    public void onConnectionLost(Throwable e) {
        getLogger().log(Level.WARNING, "Connection to redis has been lost. Most geSuit functions will be unavailable until it is restored.", e);
    }
}
