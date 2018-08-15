package net.cubespace.geSuit;

import net.cubespace.geSuit.commands.ActiveKicksCommand;
import net.cubespace.geSuit.commands.AdminCommands;
import net.cubespace.geSuit.commands.BanCommand;
import net.cubespace.geSuit.commands.DebugCommand;
import net.cubespace.geSuit.commands.ForceBatchNameHistoryUpdateCommand;
import net.cubespace.geSuit.commands.ForceNameHistoryCommand;
import net.cubespace.geSuit.commands.KickHistoryCommand;
import net.cubespace.geSuit.commands.LastLoginsCommand;
import net.cubespace.geSuit.commands.LockdownCommand;
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
import net.cubespace.geSuit.database.convert.Converter;
import net.cubespace.geSuit.listeners.APIMessageListener;
import net.cubespace.geSuit.listeners.AdminMessageListener;
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
import net.cubespace.geSuit.managers.APIManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.GeoIPManager;
import net.cubespace.geSuit.managers.LockDownManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import org.bstats.bungeecord.Metrics;

import java.util.HashMap;
import java.util.Map;


public class geSuit extends Plugin
{

    public static geSuit instance;
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
        for (CHANNEL_NAMES name : CHANNEL_NAMES.values()) {
            getProxy().registerChannel(name.toString());
        }
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
        proxy.getPluginManager().registerListener(this, new AdminMessageListener());
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
            geSuit.instance.getLogger().info("DEBUG: " + msg);
		}
	}
    
    public enum CHANNEL_NAMES {
        
        TELEPORT_CHANNEL("bungeecord:gesuitteleport"),
        SPAWN_CHANNEL("bungeecord:gesuitspawns"),
        BAN_CHANNEL("bungeecord:gesuitbans"),
        PORTAL_CHANNEL("bungeecord:gesuitportals"),
        WARP_CHANNEL("bungeecord:gesuitwarps"),
        HOME_CHANNEL("bungeecord:gesuithomes"),
        API_CHANNEL("bungeecord:gesuitapi"),
        ADMIN_CHANNEL("bungeecord:gesuitadmin");
        
        private final String channelName;
        
        CHANNEL_NAMES(String string) {
            channelName = string;
        }
        
        @Override
        public String toString() {
            return channelName;
        }
    }
}