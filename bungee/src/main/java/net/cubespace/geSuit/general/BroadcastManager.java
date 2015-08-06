package net.cubespace.geSuit.general;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.config.BroadcastsConfig;
import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.config.ConfigReloadListener;
import net.cubespace.geSuit.config.BroadcastsConfig.BroadcastEntry;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.DateDiff;
import net.cubespace.geSuit.core.util.Utilities;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * BroadcastManager handles automated broadcasts as well as provides interfaces
 * for broadcasting messages in different configurations
 */
public class BroadcastManager implements ConfigReloadListener {
    private ProxyServer proxy;
    private Logger logger;
    private Random random;
    private Plugin plugin;
    
    private ScheduledTask broadcastTask;
    private List<ScheduledBroadcast> broadcasts;
    private Set<ServerInfo> excludedServers;
    
    private BroadcastHandler broadcastHandler;
    
    private long manualBroadcastCooldown;
    
    private Map<String, BaseComponent[]> definedBroadcasts;
    
    public BroadcastManager(Plugin plugin, ProxyServer proxy, Logger logger) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.logger = logger;
        
        random = new Random();
        broadcasts = Lists.newArrayList();
        excludedServers = Sets.newHashSet();
        definedBroadcasts = Maps.newHashMap();
        broadcastHandler = new InternalBroadcastHandler();
    }
    
    /**
     * Starts up the broadcast scheduler if not running. 
     * Does not need to be explicitly run.
     */
    public void startupScheduler() {
        if (broadcastTask != null) {
            return;
        }
        
        broadcastTask = proxy.getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                broadcastNext();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    /**
     * Shuts down the broadcast scheduler if running
     */
    public void shutdownScheduler() {
        if (broadcastTask == null) {
            return;
        }
        
        broadcastTask.cancel();
        broadcastTask = null;
    }
    
    /**
     * Loads or reloads the broadcast settings from the config
     * @param config The config containing all settings
     */
    public void loadConfig(BroadcastsConfig config) {
        generateDefaults(config);
        
        broadcasts.clear();
        definedBroadcasts.clear();
        excludedServers.clear();
        
        // Load defined broadcasts
        if (config.Named != null) {
            for (Entry<String, String> entry : config.Named.entrySet()) {
                BaseComponent[] message = TextComponent.fromLegacyText(Utilities.colorize(entry.getValue()).trim());
                definedBroadcasts.put(entry.getKey().toLowerCase(), message);
            }
        }
        
        try {
            DateDiff diff = DateDiff.valueOf(config.ManualCooldown);
            manualBroadcastCooldown = diff.toMillis();
        } catch (IllegalArgumentException e) {
            logger.severe("[Broadcast] Invalid cooldown value " + config.ManualCooldown + ". Expected a format like 30s, 5m, or 3m30s. Using default");
            manualBroadcastCooldown = TimeUnit.SECONDS.toMillis(30);
            return;
        }
        
        if (!config.Enabled) {
            shutdownScheduler();
            return;
        }
        
        // Load global first
        if (config.Global != null) {
            ScheduledBroadcast announcement = loadBroadcast(config.Global, null);
            if (announcement != null) {
                broadcasts.add(announcement);
            }
        }
        
        // Load server announcements
        for (Entry<String, BroadcastEntry> entry : config.Servers.entrySet()) {
            // Determine the server
            ServerInfo server = proxy.getServerInfo(entry.getKey());
            if (server == null) {
                logger.severe("[Broadcast] Error in broadcasts. Unknown server " + entry.getKey());
                continue;
            }
            
            ScheduledBroadcast announcement = loadBroadcast(entry.getValue(), server);
            if (announcement != null) {
                broadcasts.add(announcement);
            }
        }
        
        // Get the task running if its not
        startupScheduler();
    }
    
    private ScheduledBroadcast loadBroadcast(BroadcastEntry entry, ServerInfo server) {
        ScheduledBroadcast announcement = new ScheduledBroadcast(server);
        
        // Parse the interval
        try {
            DateDiff diff = DateDiff.valueOf(entry.Interval);
            announcement.setInterval(diff.toMillis());
        } catch (IllegalArgumentException e) {
            if (server == null) {
                logger.severe("[Broadcast] Error in broadcasts. Cannot parse interval " + entry.Interval + " for Global broadcasts");
            } else {
                logger.severe("[Broadcast] Error in broadcasts. Cannot parse interval " + entry.Interval + " for " + server.getName() + " server broadcasts");
            }
            return null;
        }
        
        // Do messages
        List<BaseComponent[]> messages = Lists.newArrayListWithCapacity(entry.Messages.size());
        for (String rawMessage : entry.Messages) {
            if (Strings.isNullOrEmpty(rawMessage)) {
                continue;
            }
            
            BaseComponent[] message;
            if (rawMessage.startsWith("@")) {
                message = definedBroadcasts.get(rawMessage.substring(1).toLowerCase());
                if (message == null) {
                    if (server == null) {
                        logger.warning("[Broadcast] Unknown defined broadcast " + rawMessage.substring(1) + " in Global broadcasts");
                    } else {
                        logger.warning("[Broadcast] Unknown defined broadcast " + rawMessage.substring(1) + " in " + server.getName() + " server broadcasts");
                    }
                    continue;
                }
            } else {
                rawMessage = Utilities.colorize(rawMessage).trim();
                message = TextComponent.fromLegacyText(rawMessage);
            }
            messages.add(message);
        }
        announcement.setMessages(messages);
        
        // Random start?
        if (entry.RandomStart != null && entry.RandomStart) {
            announcement.setStart(random.nextInt(messages.size()));
        }
        
        if (entry.Isolated != null && entry.Isolated && server != null) {
            excludedServers.add(server);
        }
        
        return announcement;
    }
    
    @Override
    public void onConfigReloaded(ConfigManager manager) {
        loadConfig(manager.broadcasts());
    }
    
    /**
     * Gets a defined broadcast added either through the config
     * or through {@link #addDefinedBroadcast(String, BaseComponent[])}
     * @param name The name of the broadcast case insensitive
     * @return The message or null
     */
    public BaseComponent[] getDefinedBroadcast(String name) {
        return definedBroadcasts.get(name.toLowerCase());
    }
    
    /**
     * Adds or updates a defined broadcast. 
     * @param name The name of the broadcast
     * @param message The message to set
     */
    public void addDefinedBroadcast(String name, BaseComponent[] message) {
        definedBroadcasts.put(name.toLowerCase(), message);
    }
    
    /**
     * @return  Returns the time in ms that users will have to wait after 
     *          using /!announce before it can be used again
     */
    public long getManualBroadcastCooldown() {
        return manualBroadcastCooldown;
    }
    
    @SuppressWarnings("deprecation")
    private void broadcastNext() {
        for (ScheduledBroadcast announcement : broadcasts) {
            if (System.currentTimeMillis() >= announcement.getNextExecuteTime()) {
                BaseComponent[] message = announcement.nextMessage();
                ServerInfo targetServer = announcement.getTargetServer();
                
                // Broadcast
                if (targetServer == null) {
                    proxy.getConsole().sendMessage(ChatColor.YELLOW + "Automatic global broadcast:");
                    broadcastGlobal(message, excludedServers);
                } else {
                    broadcastServer(targetServer, message);
                    
                    // Show on the console
                    proxy.getConsole().sendMessage(ChatColor.YELLOW + "Automatic broadcast on server " + targetServer.getName() + ":");
                    proxy.getConsole().sendMessage(message);
                }
            }
        }
    }
    
    /**
     * Broadcasts a message to a server
     * @param server The server to broadcast on
     * @param message The message to broadcast
     */
    public void broadcastServer(ServerInfo server, BaseComponent[] message) {
        for (ProxiedPlayer player : server.getPlayers()) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Broadcasts a message to a server
     * @param server The server to broadcast on
     * @param message The message to broadcast
     */
    public void broadcastServer(ServerInfo server, String message) {
        broadcastServer(server, TextComponent.fromLegacyText(message));
    }
    
    /**
     * Broadcast a message globally
     * @param message The message to broadcast
     */
    public void broadcastGlobal(BaseComponent[] message) {
        broadcastGlobal(message, Collections.<ServerInfo>emptySet());
    }
    
    /**
     * Broadcast a message globally
     * @param message The message to broadcast
     */
    public void broadcastGlobal(String message) {
        broadcastGlobal(TextComponent.fromLegacyText(message));
    }
    
    /**
     * Broadcast a message globally except on servers specified
     * @param message The message to broadcast
     * @param excludedServers A set of all servers to exclude
     */
    public void broadcastGlobal(BaseComponent[] message, Set<ServerInfo> excludedServers) {
        if (excludedServers.isEmpty()) {
            proxy.broadcast(message);
        } else {
            for (ProxiedPlayer player : proxy.getPlayers()) {
                if (player.getServer() != null && excludedServers.contains(player.getServer().getInfo())) {
                    continue;
                }
                player.sendMessage(message);
            }
            // Show on console
            proxy.getConsole().sendMessage(message);
        }
    }
    
    /**
     * Broadcast a message globally except on servers specified
     * @param message The message to broadcast
     * @param excludedServers A set of all servers to exclude
     */
    public void broadcastGlobal(String message, Set<ServerInfo> excludedServers) {
        broadcastGlobal(TextComponent.fromLegacyText(message), excludedServers);
    }
    
    /**
     * Broadcast a message globally to the group specified.
     * The interpretation of this group depends on whether
     * {@link #setGroupBroadcastHandler(BroadcastHandler)}
     * has been set. 
     * @param group The group to broadcast to
     * @param message The message to broadcast
     */
    public void broadcastGroup(String group, BaseComponent[] message) {
        broadcastHandler.broadcastOn(group, message);
    }
    
    /**
     * Broadcast a message globally to the group specified.
     * The interpretation of this group depends on whether
     * {@link #setGroupBroadcastHandler(BroadcastHandler)}
     * has been set. 
     * @param group The group to broadcast to
     * @param message The message to broadcast
     */
    public void broadcastGroup(String group, String message) {
        broadcastGroup(group, TextComponent.fromLegacyText(message));
    }
    
    /**
     * Broadcast a message globally to anyone that has the specified permission
     * on BungeeCord
     * @param permission The permission receivers need to have
     * @param message The message to broadcast
     */
    public void broadcastPermission(String permission, BaseComponent[] message) {
        for (ProxiedPlayer player : proxy.getPlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(message);
            }
        }
        
        proxy.getConsole().sendMessage(message);
    }
    
    /**
     * Broadcast a message globally to anyone that has the specified permission
     * on BungeeCord
     * @param permission The permission receivers need to have
     * @param message The message to broadcast
     */
    public void broadcastPermission(String permission, String message) {
        broadcastPermission(permission, TextComponent.fromLegacyText(message));
    }
    
    /**
     * Sends a message specifically to the target player.
     * @param target The target player
     * @param message The message to send
     * @deprecated This is deprecated because this is only temporary. This will be within GlobalPlayer
     */
    @Deprecated
    public void sendMessage(GlobalPlayer target, String message) {
        for (ProxiedPlayer player : proxy.getPlayers()) {
            if (player.getUniqueId().equals(target.getUniqueId())) {
                player.sendMessage(TextComponent.fromLegacyText(message));
                break;
            }
        }
    }
    
    /**
     * Sets the BroadcastHandler used for group broadcasts.
     * @param handler The handler to use, or null to use the default
     */
    public void setGroupBroadcastHandler(BroadcastHandler handler) {
        if (handler == null) {
            broadcastHandler = new InternalBroadcastHandler();
        } else {
            broadcastHandler = handler;
        }
    }
    
    private void generateDefaults(BroadcastsConfig config) {
        boolean modified = false;
        
        if (config.Global == null) {
            BroadcastEntry entry = new BroadcastEntry();
            entry.Interval = "5m";
            config.Global = entry;
            modified = true;
        }
        
        if (config.Servers == null) {
            config.Servers = new HashMap<>();
            modified = true;
        }
        
        if (modified) {
            try {
                config.save();
            } catch (InvalidConfigurationException e) {
                logger.log(Level.SEVERE, "[Broadcast] Failed to save default broadcasts.", e);
            }
        }
    }
    
    private static class ScheduledBroadcast implements Comparable<ScheduledBroadcast> { 
        private ServerInfo targetServer;
        private long interval;
        private long nextExecuteTime;
        
        private List<BaseComponent[]> messages;
        private int nextMessage;
        
        public ScheduledBroadcast(ServerInfo targetServer) {
            this.targetServer = targetServer;
        }
        
        public ServerInfo getTargetServer() {
            return targetServer;
        }
        
        public void setInterval(long interval) {
            this.interval = interval;
            nextExecuteTime = System.currentTimeMillis() + interval;
        }
        
        public void setMessages(List<BaseComponent[]> messages) {
            this.messages = messages;
        }

        public void setStart(int start) {
            Preconditions.checkArgument(start >= 0 && start < messages.size());
            nextMessage = start;
        }
        
        public BaseComponent[] nextMessage() {
            BaseComponent[] message = messages.get(nextMessage++);
            if (nextMessage >= messages.size()) {
                nextMessage = 0;
            }
            
            nextExecuteTime = System.currentTimeMillis() + interval;
            return message;
        }
        
        public long getNextExecuteTime() {
            return nextExecuteTime;
        }

        @Override
        public int compareTo(ScheduledBroadcast other) {
            return Long.compare(nextExecuteTime, other.nextExecuteTime);
        }
    }
    
    private class InternalBroadcastHandler implements BroadcastHandler {
        @Override
        public void broadcastOn(String group, BaseComponent[] message) {
            String permission = "gesuit.broadcast." + group;
            broadcastPermission(permission, message);
        }
    }
}
