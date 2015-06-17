package net.cubespace.geSuit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import com.google.common.base.Strings;

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
import net.cubespace.geSuit.configs.SubConfig.Redis;
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
import net.cubespace.geSuit.database.DatabaseManager;
import net.cubespace.geSuit.general.GeoIPLookup;
import net.cubespace.geSuit.listeners.APIMessageListener;
import net.cubespace.geSuit.listeners.BungeeChatListener;
import net.cubespace.geSuit.listeners.HomesMessageListener;
import net.cubespace.geSuit.listeners.PortalsMessageListener;
import net.cubespace.geSuit.listeners.SpawnListener;
import net.cubespace.geSuit.listeners.SpawnMessageListener;
import net.cubespace.geSuit.listeners.TeleportsListener;
import net.cubespace.geSuit.listeners.TeleportsMessageListener;
import net.cubespace.geSuit.listeners.WarpsMessageListener;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.LoggingManager;
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

public class geSuitPlugin extends Plugin implements ConnectionNotifier {

    public static ProxyServer proxy;
    private boolean DebugEnabled = false;
    private RedisConnection redis;
    private RedisChannelManager channelManager;
    private BungeePlayerManager playerManager;
    private DatabaseManager databaseManager;
    private BungeeCommandManager commandManager;
    
    private GeoIPLookup geoIpLookup;
    
    private BanManager bans;
    private TrackingManager tracking;
    private WarningsManager warnings;
    private TeleportsManager teleports;
    private SpawnManager spawns;
    private WarpManager warps;
    
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
        commandManager = new BungeeCommandManager();
        getProxy().getPluginManager().registerListener(this, playerManager);
        geCore core = new geCore(new BungeePlatform(this), playerManager, channelManager, commandManager);
        Global.setInstance(core);
        
        core.getMessages().loadDefaults();
        loadLanguage();
        
        initializeRemotes();
        
        playerManager.initialize(bans);

        registerListeners();
        registerCommands();
        
        registerGenerals();
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
        
        Global.getCommandManager().registerAll(new BanCommands(bans), this);
        Global.getCommandManager().registerAll(new KickCommands(bans), this);
        Global.getCommandManager().registerAll(new WarnCommands(warnings), this);
        
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
        getProxy().registerChannel("geSuitPortals"); // Portals out/in
        getProxy().registerChannel("geSuitWarps"); // Warps in
        getProxy().registerChannel("geSuitHomes"); // Homes in
        getProxy().registerChannel("geSuitAPI"); // API messages in

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
                    redis = new RedisConnection(config.host, config.port, config.password, 0);
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
    
    private void initializeRemotes() {
        Channel<BaseMessage> moderationChannel = channelManager.createChannel("moderation", BaseMessage.class);
        moderationChannel.setCodec(new BaseMessage.Codec());
        
        RemoteManager manager = Global.getRemoteManager();
        manager.registerRemote("bans", BanActions.class, bans = new BanManager(databaseManager.getBanHistory(), moderationChannel));
        manager.registerRemote("warns", WarnActions.class, warnings = new WarningsManager(databaseManager.getWarnHistory(), (BanManager)manager.getRemote(BanActions.class), moderationChannel));
        manager.registerRemote("tracking", TrackingActions.class, tracking = new TrackingManager(databaseManager));
        
        manager.registerRemote("teleports", TeleportActions.class, teleports = new TeleportsManager());
        
        spawns = new SpawnManager();
        warps = new WarpManager();
    }
    
    private void registerGenerals() {
        geoIpLookup = new GeoIPLookup();
        geoIpLookup.initialize();
    }
    
    public void loadLanguage() {
        if (!Strings.isNullOrEmpty(ConfigManager.main.Lang)) {
            Messages messages = Global.getMessages();
            
            try {
                File langFile = new File(getDataFolder(), "lang/" + ConfigManager.main.Lang + ".lang");
                if (langFile.exists()) {
                    messages.load(langFile);
                } else {
                    InputStream stream = getResourceAsStream("lang/" + ConfigManager.main.Lang + ".lang");
                    if (stream != null) {
                        messages.load(stream);
                    } else {
                        getLogger().warning("Failed to load language " + ConfigManager.main.Lang + ". Cannot find it either in the filesystem or in the jar.");
                    }
                }
            } catch (IOException e) {
                getLogger().log(Level.WARNING, "Failed to load language " + ConfigManager.main.Lang + ":", e);
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

    @Override
    public void onConnectionLost(Throwable e) {
        getLogger().log(Level.WARNING, "Connection to redis has been lost. Most geSuit functions will be unavailable until it is restored.", e);
    }

    @Override
    public void onConnectionRestored() {
        getLogger().info("Connection to redis has been restored.");
    }
}