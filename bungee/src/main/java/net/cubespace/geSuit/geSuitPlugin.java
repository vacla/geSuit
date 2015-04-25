package net.cubespace.geSuit;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import net.cubespace.geSuit.commands.BanCommand;
import net.cubespace.geSuit.commands.DebugCommand;
import net.cubespace.geSuit.commands.MOTDCommand;
import net.cubespace.geSuit.commands.NamesCommand;
import net.cubespace.geSuit.commands.OnTimeCommand;
import net.cubespace.geSuit.commands.ReloadCommand;
import net.cubespace.geSuit.commands.SeenCommand;
import net.cubespace.geSuit.commands.TempBanCommand;
import net.cubespace.geSuit.commands.UnbanCommand;
import net.cubespace.geSuit.commands.WarnCommand;
import net.cubespace.geSuit.commands.WarnHistoryCommand;
import net.cubespace.geSuit.commands.WhereCommand;
import net.cubespace.geSuit.configs.SubConfig.Redis;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.geCore;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.channel.ConnectionNotifier;
import net.cubespace.geSuit.core.channel.RedisChannelManager;
import net.cubespace.geSuit.core.storage.RedisConnection;
import net.cubespace.geSuit.database.DatabaseManager;
import net.cubespace.geSuit.listeners.APIMessageListener;
import net.cubespace.geSuit.listeners.BansMessageListener;
import net.cubespace.geSuit.listeners.BungeeChatListener;
import net.cubespace.geSuit.listeners.HomesMessageListener;
import net.cubespace.geSuit.listeners.PlayerListener;
import net.cubespace.geSuit.listeners.PortalsMessageListener;
import net.cubespace.geSuit.listeners.SpawnListener;
import net.cubespace.geSuit.listeners.SpawnMessageListener;
import net.cubespace.geSuit.listeners.TeleportsListener;
import net.cubespace.geSuit.listeners.TeleportsMessageListener;
import net.cubespace.geSuit.listeners.WarpsMessageListener;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.GeoIPManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class geSuitPlugin extends Plugin implements ConnectionNotifier {

    public static ProxyServer proxy;
    private boolean DebugEnabled = false;
    private RedisConnection redis;
    private RedisChannelManager channelManager;
    private BungeePlayerManager playerManager;
    private DatabaseManager databaseManager;

    public void onEnable() {
        geSuit.setPlugin(this);
        LoggingManager.log(ChatColor.GREEN + "Starting geSuit");
        proxy = ProxyServer.getInstance();
        LoggingManager.log(ChatColor.GREEN + "Initialising Managers");
        
        databaseManager = new DatabaseManager(ConfigManager.main.Database);
        if (!databaseManager.initialize()) {
            return;
        }

        if (!initializeRedis()) {
            LoggingManager.log("Unable to connect to redis");
            return;
        }

        initializeChannelManager();
        playerManager = new BungeePlayerManager(channelManager);
        getProxy().getPluginManager().registerListener(this, playerManager);
        geCore core = new geCore(new BungeePlatform(this), playerManager, channelManager);
        Global.setInstance(core);

        registerListeners();
        registerCommands();
        GeoIPManager.initialize();
    }

    private void registerCommands() {
        // A little hardcore. Prevent updating without a restart. But command
        // squatting = bad!
        if (ConfigManager.main.MOTD_Enabled) {
            proxy.getPluginManager().registerCommand(this, new MOTDCommand());
        }
        if (ConfigManager.main.Seen_Enabled) {
            proxy.getPluginManager().registerCommand(this, new SeenCommand());
        }
        proxy.getPluginManager().registerCommand(this, new UnbanCommand());
        proxy.getPluginManager().registerCommand(this, new BanCommand());
        proxy.getPluginManager().registerCommand(this, new TempBanCommand());
        proxy.getPluginManager().registerCommand(this, new WarnCommand());
        proxy.getPluginManager().registerCommand(this, new WhereCommand());
        proxy.getPluginManager().registerCommand(this, new ReloadCommand());
        proxy.getPluginManager().registerCommand(this, new DebugCommand());
        proxy.getPluginManager().registerCommand(this, new WarnHistoryCommand());
        proxy.getPluginManager().registerCommand(this, new NamesCommand());
        if (ConfigManager.bans.TrackOnTime) {
            proxy.getPluginManager().registerCommand(this, new OnTimeCommand());
        }
    }

    private void registerListeners() {
        getProxy().registerChannel("geSuitTeleport"); // Teleport out/in
        getProxy().registerChannel("geSuitSpawns"); // Spawns out/in
        getProxy().registerChannel("geSuitBans"); // Bans in
        getProxy().registerChannel("geSuitPortals"); // Portals out/in
        getProxy().registerChannel("geSuitWarps"); // Warps in
        getProxy().registerChannel("geSuitHomes"); // Homes in
        getProxy().registerChannel("geSuitAPI"); // API messages in

        proxy.getPluginManager().registerListener(this, new PlayerListener());
        proxy.getPluginManager().registerListener(this, new BansMessageListener());
        proxy.getPluginManager().registerListener(this, new TeleportsListener());
        proxy.getPluginManager().registerListener(this, new TeleportsMessageListener());
        proxy.getPluginManager().registerListener(this, new WarpsMessageListener());
        proxy.getPluginManager().registerListener(this, new HomesMessageListener());
        proxy.getPluginManager().registerListener(this, new PortalsMessageListener());
        proxy.getPluginManager().registerListener(this, new SpawnListener());
        proxy.getPluginManager().registerListener(this, new SpawnMessageListener());
        proxy.getPluginManager().registerListener(this, new APIMessageListener());
        if (ConfigManager.main.BungeeChatIntegration) {
            proxy.getPluginManager().registerListener(this, new BungeeChatListener());
        }
    }

    private boolean initializeRedis() {
        final CountDownLatch latch = new CountDownLatch(1);

        getProxy().getScheduler().runAsync(this, new Runnable() {
            @Override
            public void run() {
                try {
                    Redis config = ConfigManager.main.Redis;
                    redis = new RedisConnection(config.host, config.port, config.password);
                    redis.setNotifier(geSuitPlugin.this);
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, "Unable to connect to Redis:", e);
                } finally {
                    latch.countDown();
                }
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        return redis != null;
    }
    
    private void initializeChannelManager() {
        channelManager = new RedisChannelManager(redis, getLogger());

        final CountDownLatch latch = new CountDownLatch(1);

        getProxy().getScheduler().runAsync(this, new Runnable() {
            @Override
            public void run() {
                channelManager.initialize(latch);
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
        }
    }

    public void onDisable() {
        channelManager.shutdown();
        databaseManager.shutdown();
        redis.shutdown();
    }

    public boolean isDebugEnabled() {
        return DebugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        DebugEnabled = debugEnabled;
    }

    public void DebugMsg(String msg) {
        if (isDebugEnabled()) {
            getLogger().info("DEBUG: " + msg);
        }
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    @Override
    public void onConnectionLost(Throwable e) {
        getLogger().log(Level.WARNING, "Connection to redis has been lost. Most geSuit functions will be unavailable until it is restored.", e);
    }

    @Override
    public void onConnectionRestored() {
        getLogger().info("Connection to redis has been restored.");
    }
}