package net.cubespace.geSuit;

import net.cubespace.geSuit.core.BukkitPlayerManager;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.ServerManager;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.channel.ConnectionNotifier;
import net.cubespace.geSuit.core.channel.RedisChannelManager;
import net.cubespace.geSuit.core.commands.BukkitCommandManager;
import net.cubespace.geSuit.core.geCore;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.storage.RedisConnection;
import net.cubespace.geSuit.core.storage.StorageProvider;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

public class GSPlugin extends JavaPlugin implements ConnectionNotifier {
    
    private RedisConnection redis;
    private RedisChannelManager channelManager;
    
    private ModuleManager moduleManager;
    private BukkitCommandManager commandManager;
    
    @Override
    public void onEnable() {
        getLogger().info("Starting geSuitBukkit");

        // Create the Config.yml file if missing
        saveDefaultConfig();

        redis = createRedis();
        if (redis == null) {
            getLogger().severe("Redis failed to initialize. Please fix the problem and restart the server.");
            return;
        }
        redis.setNotifier(this);
        
        channelManager = initializeChannelManager();
        
        BukkitPlatform platform = new BukkitPlatform(this);
        StorageProvider storageProvider = new StorageProvider(redis, platform);
        
        // Create global manager
        Channel<BaseMessage> channel = channelManager.createChannel("players", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        BukkitPlayerManager playerManager = new BukkitPlayerManager(channel, channelManager.getRedis(), storageProvider, platform);
        playerManager.initRedis();
        ServerManager serverManager = new ServerManager(platform);
        
        GlobalBukkitManager globalManager = new GlobalBukkitManager(channel, playerManager, serverManager, new Messages());
        
        // Initialize core
        commandManager = new BukkitCommandManager();
        geCore core = new geCore(platform, globalManager, channelManager, commandManager, storageProvider);
        Global.setInstance(core);
        
        // Load player manager
        globalManager.requestNetworkUpdate();
        
        getLogger().info("Initializing modules:");
        moduleManager = new ModuleManager(this);
        moduleManager.loadAll();
        moduleManager.enableAll();
    }
    
    @Override
    public void onDisable() {
        moduleManager.disableAll();
        
        channelManager.shutdown();
        redis.shutdown();
    }
    
    private RedisConnection createRedis() {
        FileConfiguration config = getConfig();

        String redisHost = config.getString("redis.host", "localhost");
        int redisPort = config.getInt("redis.port", 6379);
        String password = config.getString("redis.password", "");

        try {
            RedisConnection redis = new RedisConnection(redisHost, redisPort, password, Bukkit.getPort());
            redis.connect();
            return redis;
        } catch (IOException e) {
            String msg = "Unable to connect to redis, host " + redisHost + ", port " + redisPort;
            if (password.isEmpty())
                getLogger().log(Level.SEVERE, msg + " (no password):", e);
            else
                getLogger().log(Level.SEVERE, msg + " (password length " + password.length() + "):", e);
            return null;
        }
    }
    
    private RedisChannelManager initializeChannelManager() {
        final RedisChannelManager channelManager = new RedisChannelManager(redis, getLogger());
        
        final CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(() -> channelManager.initialize(latch), "GSSubscriptionThread");

        thread.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        
        return channelManager;
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
