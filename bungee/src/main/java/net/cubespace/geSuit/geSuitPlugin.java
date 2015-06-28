package net.cubespace.geSuit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import com.google.common.base.Strings;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.commands.AnnounceCommand;
import net.cubespace.geSuit.commands.BanCommands;
import net.cubespace.geSuit.commands.DebugCommand;
import net.cubespace.geSuit.commands.KickCommands;
import net.cubespace.geSuit.commands.MOTDCommand;
import net.cubespace.geSuit.commands.NamesCommand;
import net.cubespace.geSuit.commands.OnTimeCommand;
import net.cubespace.geSuit.commands.ReloadCommand;
import net.cubespace.geSuit.commands.SeenCommand;
import net.cubespace.geSuit.commands.WarnCommand;
import net.cubespace.geSuit.commands.WarnCommands;
import net.cubespace.geSuit.commands.WarnHistoryCommand;
import net.cubespace.geSuit.commands.WhereCommand;
import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.config.MainConfig.Redis;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.geCore;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.channel.ConnectionNotifier;
import net.cubespace.geSuit.core.channel.RedisChannelManager;
import net.cubespace.geSuit.core.commands.BungeeCommandManager;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.remote.RemoteManager;
import net.cubespace.geSuit.core.storage.RedisConnection;
import net.cubespace.geSuit.core.storage.StorageProvider;
import net.cubespace.geSuit.database.ConnectionPool;
import net.cubespace.geSuit.database.DatabaseManager;
import net.cubespace.geSuit.general.BroadcastManager;
import net.cubespace.geSuit.general.GeoIPLookup;
import net.cubespace.geSuit.moderation.BanManager;
import net.cubespace.geSuit.moderation.TrackingManager;
import net.cubespace.geSuit.moderation.WarningsManager;
import net.cubespace.geSuit.remote.moderation.BanActions;
import net.cubespace.geSuit.remote.moderation.TrackingActions;
import net.cubespace.geSuit.remote.moderation.WarnActions;
import net.cubespace.geSuit.remote.teleports.TeleportActions;
import net.cubespace.geSuit.teleports.SpawnManager;
import net.cubespace.geSuit.teleports.TeleportsManager;
import net.cubespace.geSuit.teleports.warps.WarpManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class geSuitPlugin extends Plugin implements ConnectionNotifier {

    public static ProxyServer proxy;
    private boolean DebugEnabled = false;
    private ConfigManager configManager;
    
    private RedisConnection redis;
    private RedisChannelManager channelManager;
    private BungeePlayerManager playerManager;
    private DatabaseManager databaseManager;
    private BungeeCommandManager commandManager;
    
    private GeoIPLookup geoIpLookup;
    private BroadcastManager broadcastManager;
    
    private BanManager bans;
    private TrackingManager tracking;
    private WarningsManager warnings;
    private TeleportsManager teleports;
    private SpawnManager spawns;
    private WarpManager warps;
    
    public void onEnable() {
        geSuit.setPlugin(this);
        getLogger().info(ChatColor.GREEN + "Starting geSuit");
        proxy = ProxyServer.getInstance();
        
        // Initialize config
        configManager = new ConfigManager(this, getDataFolder());
        try {
            configManager.initialize();
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, "Failed to load configuration files. Please fix the problem and restart BungeeCord.", e);
            return;
        }
        
        // Initialize database
        databaseManager = new DatabaseManager(new ConnectionPool(this), configManager.config().Database);
        if (!databaseManager.initialize()) {
            getLogger().severe("Database connection failed to initialize. Please fix the problem and restart BungeeCord.");
            return;
        }
        
        // Initialize backend
        redis = createRedis(configManager.config().Redis);
        if (redis == null) {
            getLogger().severe("Redis failed to initialize. Please fix the problem and restart BungeeCord.");
            return;
        }
        redis.setNotifier(this);

        channelManager = createChannelManager();
        
        // Create player manager
        Channel<BaseMessage> channel = channelManager.createChannel("players", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        
        playerManager = new BungeePlayerManager(channel, configManager, redis, this);
        
        // Initialize core
        commandManager = new BungeeCommandManager();
        
        geCore core = new geCore(new BungeePlatform(this), playerManager, channelManager, commandManager, new StorageProvider(redis));
        Global.setInstance(core);
        
        // Load addons
        initializeAddons();
        registerCommands();
        
        // Load language
        core.getMessages().loadDefaults();
        loadLanguage();
        
        // Begin all
        getProxy().getPluginManager().registerListener(this, playerManager);
        playerManager.initialize(bans, tracking, geoIpLookup, broadcastManager);
    }

    private void registerCommands() {
        PluginManager manager = getProxy().getPluginManager();
        if (configManager.config().MOTD_Enabled) {
            manager.registerCommand(this, new MOTDCommand(configManager));
        }
        if (configManager.config().Seen_Enabled) {
            manager.registerCommand(this, new SeenCommand(bans, geoIpLookup, configManager.config()));
        }
        if (configManager.moderation().TrackOnTime) {
            proxy.getPluginManager().registerCommand(this, new OnTimeCommand());
        }
        
        Global.getCommandManager().registerAll(new BanCommands(bans), this);
        Global.getCommandManager().registerAll(new KickCommands(bans), this);
        Global.getCommandManager().registerAll(new WarnCommands(warnings), this);
        
        manager.registerCommand(this, new WarnCommand());
        manager.registerCommand(this, new WhereCommand());
        manager.registerCommand(this, new ReloadCommand(this, configManager));
        manager.registerCommand(this, new DebugCommand());
        manager.registerCommand(this, new WarnHistoryCommand());
        manager.registerCommand(this, new NamesCommand());
        manager.registerCommand(this, new AnnounceCommand(broadcastManager));
    }

    private RedisConnection createRedis(final Redis config) {
        @SuppressWarnings("deprecation")
        Future<RedisConnection> future = getExecutorService().submit(new Callable<RedisConnection>() {
            @Override
            public RedisConnection call() throws Exception {
                try {
                    RedisConnection redis = new RedisConnection(config.host, config.port, config.password, 0);
                    redis.connect();
                    return redis;
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, "Unable to connect to Redis:", e);
                    return null;
                }
            }
        });
        
        try {
            return future.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            getLogger().log(Level.SEVERE, "An unhandled exception occurred while starting redis", e.getCause());
            return null;
        }
    }
    
    private RedisChannelManager createChannelManager() {
        final RedisChannelManager channelManager = new RedisChannelManager(redis, getLogger());

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
        
        return channelManager;
    }
    
    private void initializeAddons() {
        // Create channels
        Channel<BaseMessage> moderationChannel = channelManager.createChannel("moderation", BaseMessage.class);
        moderationChannel.setCodec(new BaseMessage.Codec());
        Channel<BaseMessage> warpsChannel = channelManager.createChannel("warps", BaseMessage.class);
        warpsChannel.setCodec(new BaseMessage.Codec());
        Channel<BaseMessage> teleportsChannel = Global.getChannelManager().createChannel("tp", BaseMessage.class);
        teleportsChannel.setCodec(new BaseMessage.Codec());
        
        // Create each remote
        bans = new BanManager(databaseManager.getBanHistory(), broadcastManager, moderationChannel, getLogger());
        warnings = new WarningsManager(databaseManager.getWarnHistory(), bans, broadcastManager, moderationChannel, getLogger());
        tracking = new TrackingManager(databaseManager.getTracking(), databaseManager.getOntime(), broadcastManager, getLogger());
        teleports = new TeleportsManager(teleportsChannel, this);
        
        // Register them
        RemoteManager manager = Global.getRemoteManager();
        manager.registerRemote("bans", BanActions.class, bans);
        manager.registerRemote("warns", WarnActions.class, warnings);
        manager.registerRemote("tracking", TrackingActions.class, tracking);
        
        manager.registerRemote("teleports", TeleportActions.class, teleports);
        
        // Create managers
        spawns = new SpawnManager();
        warps = new WarpManager(warpsChannel);
        geoIpLookup = new GeoIPLookup(getDataFolder(), configManager.moderation().GeoIP, broadcastManager, getLogger());
        configManager.addReloadListener(geoIpLookup);
        broadcastManager = new BroadcastManager(this, getProxy(), getLogger());
        
        // Load everything
        bans.loadConfig(configManager.moderation());
        tracking.loadConfig(configManager.moderation());
        warnings.loadConfig(configManager.moderation());
        teleports.loadConfig(configManager.teleports());
        broadcastManager.loadConfig(configManager.broadcasts());
        configManager.addReloadListener(bans);
        configManager.addReloadListener(tracking);
        configManager.addReloadListener(warnings);
        configManager.addReloadListener(teleports);
        configManager.addReloadListener(broadcastManager);
        
        spawns.loadSpawns();
        geoIpLookup.initialize();
    }
    
    public void loadLanguage() {
        if (!Strings.isNullOrEmpty(configManager.config().Lang)) {
            Messages messages = Global.getMessages();
            
            try {
                File langFile = new File(getDataFolder(), "lang/" + configManager.config().Lang + ".lang");
                if (langFile.exists()) {
                    messages.load(langFile);
                } else {
                    InputStream stream = getResourceAsStream("lang/" + configManager.config().Lang + ".lang");
                    if (stream != null) {
                        messages.load(stream);
                    } else {
                        getLogger().warning("Failed to load language " + configManager.config().Lang + ". Cannot find it either in the filesystem or in the jar.");
                    }
                }
            } catch (IOException e) {
                getLogger().log(Level.WARNING, "Failed to load language " + configManager.config().Lang + ":", e);
            }
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
    
    public BanManager getBanManager() {
        return bans;
    }
    
    public TrackingManager getTrackingManager() {
        return tracking;
    }
    
    public WarningsManager getWarningsManager() {
        return warnings;
    }
    
    public TeleportsManager getTeleportManager() {
        return teleports;
    }
    
    public SpawnManager getSpawnManager() {
        return spawns;
    }
    
    public WarpManager getWarpManager() {
        return warps;
    }
    
    public GeoIPLookup getGeoIPLookup() {
        return geoIpLookup;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
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