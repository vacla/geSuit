package net.cubespace.geSuit;

import net.cubespace.geSuit.commands.*;
import net.cubespace.geSuit.database.convert.Converter;
import net.cubespace.geSuit.listeners.*;
import net.cubespace.geSuit.managers.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import java.util.HashMap;
import java.util.Map;


public class geSuit extends Plugin
{
    
    public static geSuit getInstance() {
        return instance;
    }
    
    private static geSuit instance;
    public static ProxyServer proxy;
    private boolean DebugEnabled = false;
    public static APIManager api;

    public void onEnable()
    {
        instance = this;
        LoggingManager.log(ChatColor.GREEN + "Starting geSuit");
        proxy = ProxyServer.getInstance();
        LoggingManager.log(ChatColor.GREEN + "Initialising Managers");
        new DatabaseManager();
        if (ConfigManager.main.ConvertFromBungeeSuite) {
            Converter converter = new Converter();
            converter.convert();
        }

        registerListeners();
        registerCommands();
        GeoIPManager.initialize();
        LockDownManager.initialize();
        api = new APIManager();
        Metrics metrics = new Metrics(this);
        Metrics.SimpleBarChart chart = new Metrics.SimpleBarChart("Servers", () -> {
            Map<String, Integer> map = new HashMap<>();
            map.put("Server Count", getProxy().getServers().size());
            return map;
        });
        metrics.addCustomChart(chart);
    
    
    }

    private void registerCommands()
    {
        // A little hardcore. Prevent updating without a restart. But command squatting = bad!
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
        proxy.getPluginManager().registerCommand(this, new KickHistoryCommand());
        proxy.getPluginManager().registerCommand(this, new NamesCommand());
        proxy.getPluginManager().registerCommand(this, new LockdownCommand());
        proxy.getPluginManager().registerCommand(this, new ForceNameHistoryCommand());
        proxy.getPluginManager().registerCommand(this, new ForceBatchNameHistoryUpdateCommand());
        proxy.getPluginManager().registerCommand(this, new ActiveKicksCommand());
        proxy.getPluginManager().registerCommand(this, new AdminCommands());
        if (ConfigManager.bans.TrackOnTime) {
        	proxy.getPluginManager().registerCommand(this, new OnTimeCommand());
            proxy.getPluginManager().registerCommand(this, new LastLoginsCommand());
        }
    }
    private void registerListeners()
    {
        boolean legacy = ConfigManager.main.enableLegacy;
        for (CHANNEL_NAMES name : CHANNEL_NAMES.values()) {
            getProxy().registerChannel(name.toString());
        }
        if (legacy) {
            for (CHANNEL_NAMES name : CHANNEL_NAMES.values()) {
                getProxy().registerChannel(name.getLegacy());                 // For new bukkit plugins on 1.12 bungee
                getProxy().registerChannel(name.getLegacy().toLowerCase());   // For old legacy bukkit plugins
            }
        }

        proxy.getPluginManager().registerListener(this, new PlayerListener());
        proxy.getPluginManager().registerListener(this, new BansMessageListener(legacy));
        proxy.getPluginManager().registerListener(this, new TeleportsListener());
        proxy.getPluginManager().registerListener(this, new TeleportsMessageListener(legacy));
        proxy.getPluginManager().registerListener(this, new WarpsMessageListener(legacy));
        proxy.getPluginManager().registerListener(this, new HomesMessageListener(legacy));
        proxy.getPluginManager().registerListener(this, new PortalsMessageListener(legacy));
        proxy.getPluginManager().registerListener(this, new SpawnListener());
        proxy.getPluginManager().registerListener(this, new SpawnMessageListener(legacy));
        proxy.getPluginManager().registerListener(this, new APIMessageListener(legacy));
        proxy.getPluginManager().registerListener(this, new AdminMessageListener(legacy));
        if (ConfigManager.main.BungeeChatIntegration) {
            proxy.getPluginManager().registerListener(this, new BungeeChatListener());
        }
    }

	public boolean isDebugEnabled() {
		return DebugEnabled;
	}

	public void setDebugEnabled(boolean debugEnabled) {
		DebugEnabled = debugEnabled;
	}

    public void DebugMsg(String msg) {
        if (isDebugEnabled()) {
            geSuit.getInstance().getLogger().info("DEBUG: " + msg);
		}
	}
    
    public enum CHANNEL_NAMES {

        TELEPORT_CHANNEL("bungeecord:gesuitteleport", "geSuitTeleport"),
        SPAWN_CHANNEL("bungeecord:gesuitspawns", "geSuitSpawns"),
        BAN_CHANNEL("bungeecord:gesuitbans", "geSuitBans"),
        PORTAL_CHANNEL("bungeecord:gesuitportals", "geSuitPortals"),
        WARP_CHANNEL("bungeecord:gesuitwarps", "geSuitWarps"),
        HOME_CHANNEL("bungeecord:gesuithomes", "geSuitHomes"),
        API_CHANNEL("bungeecord:gesuitapi", "geSuitAPI"),
        ADMIN_CHANNEL("bungeecord:gesuitadmin", "geSuitAdmin");

        private final String channelName;
        private final String legacy_channelName;

        CHANNEL_NAMES(String string) {
            this(string, null);
        }

        CHANNEL_NAMES(String string, String legacy) {

            channelName = string;
            legacy_channelName = legacy;
        }
        
        @Override
        public String toString() {
            return channelName;
        }

        public String getLegacy() {
            return legacy_channelName;
        }
    }
}