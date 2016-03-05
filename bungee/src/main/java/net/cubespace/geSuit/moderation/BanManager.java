package net.cubespace.geSuit.moderation;

import java.net.InetAddress;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.config.ConfigReloadListener;
import net.cubespace.geSuit.config.ModerationConfig;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.Platform;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.moderation.GlobalBanEvent;
import net.cubespace.geSuit.core.events.moderation.GlobalUnbanEvent;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.FireBanEventMessage;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.core.objects.DateDiff;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.objects.Result.Type;
import net.cubespace.geSuit.core.storage.StorageException;
import net.cubespace.geSuit.core.storage.StorageInterface;
import net.cubespace.geSuit.core.storage.StorageProvider;
import net.cubespace.geSuit.database.repositories.BanHistory;
import net.cubespace.geSuit.general.BroadcastManager;
import net.cubespace.geSuit.remote.moderation.BanActions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BanManager implements BanActions, ConfigReloadListener {
    private final BanHistory banRepo;
    private final BroadcastManager broadcasts;
    private final Logger logger;
    private final Channel<BaseMessage> channel;
    private final Messages messages;
    private final Platform platform;
    private final ProxyServer proxy;
    private final StorageInterface ipBans;
    
    private ModerationConfig config;
    
    public BanManager(BanHistory banRepo, BroadcastManager broadcasts, Channel<BaseMessage> channel, Messages messages, StorageProvider provider, ProxyServer proxy, Platform platform) {
        this.banRepo = banRepo;
        this.broadcasts = broadcasts;
        this.logger = platform.getLogger();
        this.channel = channel;
        this.messages = messages;
        this.proxy = proxy;
        this.platform = platform;

        boolean storageLogging = true;
        ipBans = provider.create("geSuit.ipbans", storageLogging);
    }
    
    public void loadConfig(ModerationConfig config) {
        this.config = config;
    }
    
    @Override
    public void onConfigReloaded(ConfigManager manager) {
        loadConfig(manager.moderation());
    }
    
    @Override
    public Result ban(GlobalPlayer player, String reason, String by, UUID byId) {
        return ban0(player, reason, by, byId, 0, false);
    }
    
    @Override
    public Result ban(GlobalPlayer player, String reason, String by, UUID byId, boolean isAuto) {
        return ban0(player, reason, by, byId, 0, isAuto);
    }
    
    @Override
    public Result ban(InetAddress ip, String reason, String by, UUID byId) {
        return ban0(ip, reason, by, byId, 0, false);
    }
    
    @Override
    public Result ban(InetAddress ip, String reason, String by, UUID byId, boolean isAuto) {
        return ban0(ip, reason, by, byId, 0, isAuto);
    }
    
    
    @Override
    public Result banUntil(GlobalPlayer player, String reason, long until, String by, UUID byId) {
        return ban0(player, reason, by, byId, until, false);
    }
    
    @Override
    public Result banUntil(GlobalPlayer player, String reason, long until, String by, UUID byId, boolean isAuto) {
        return ban0(player, reason, by, byId, until, isAuto);
    }

    @Override
    public Result banUntil(InetAddress ip, String reason, long until, String by, UUID byId) {
        return ban0(ip, reason, by, byId, until, false);
    }
    
    @Override
    public Result banUntil(InetAddress ip, String reason, long until, String by, UUID byId, boolean isAuto) {
        return ban0(ip, reason, by, byId, until, isAuto);
    }
    
    @Override
    public Result unban(GlobalPlayer player, String reason, String by, UUID byId) {
        return unban0(player, reason, by, byId);
    }
    
    @Override
    public Result unban(InetAddress ip, String reason, String by, UUID byId) {
        return unban0(ip, reason, by, byId);
    }
    

    @Override
    public Result ipbanUntil(GlobalPlayer player, String reason, long until, String by, UUID byId) {
        return ipban0(player, reason, by, byId, until, false);
    }
    
    @Override
    public Result ipbanUntil(GlobalPlayer player, String reason, long until, String by, UUID byId, boolean isAuto) {
        return ipban0(player, reason, by, byId, until, isAuto);
    }
    
    @Override
    public Result ipban(GlobalPlayer player, String reason, String by, UUID byId) {
        return ipban0(player, reason, by, byId, 0, false);
    }
    
    @Override
    public Result ipban(GlobalPlayer player, String reason, String by, UUID byId, boolean isAuto) {
        return ipban0(player, reason, by, byId, 0, isAuto);
    }
    
    @Override
    public Result ipunban(GlobalPlayer player, String reason, String by, UUID byId) {
        try {
            BanInfo<GlobalPlayer> playerBan = getBan(player);
            BanInfo<InetAddress> ipBan = getBan(player.getAddress());
            
            if (playerBan == null && ipBan == null) {
                return new Result(Type.Fail, messages.get("ban.not-banned"));
            }
            
            // Record the unban
            if (playerBan != null) {
                banRepo.recordUnban(playerBan, reason, by, byId);
                setBan(player, null);
            }
            
            if (ipBan != null) {
                banRepo.recordUnban(ipBan, reason, by, byId);
                setBan(player.getAddress(), null);
            }
            
            // Fire Event
            if (playerBan != null) {
                platform.callEvent(new GlobalUnbanEvent(playerBan, player.getAddress()));
                channel.broadcast(FireBanEventMessage.createFromIPUnban(playerBan, player.getAddress()));
            } else {
                platform.callEvent(new GlobalUnbanEvent(ipBan));
                channel.broadcast(FireBanEventMessage.createFromUnban(ipBan));
            }
            
            // Finally broadcast message
            String message = messages.get("unban.broadcast", "player", player.getDisplayName(), "sender", by);
            if (config.BroadcastUnbans) {
                broadcasts.broadcastGlobal(message);
                return new Result(Type.Success, (byId == null ? message : null));
            } else {
                return new Result(Type.Success, message);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "A database exception occured while attempting to ipunban " + player.getDisplayName(), e);
            return new Result(Type.Fail, ChatColor.RED + "An internal error occured");
        }
    }
    
    private <T> Result ban0(T who, String reason, String by, UUID byId, long until, boolean isAuto) {
        try {
            BanInfo<T> current = getBan(who);
            
            if (current != null && !current.isTemporary()) {
                return new Result(Type.Fail, messages.get("ban.already-banned"));
            }
            
            if (Strings.isNullOrEmpty(reason)) {
                reason = config.DefaultBanReason;
            }
            
            BanInfo<?> ban = new BanInfo<Object>(who);
            
            ban.setReason(reason);
            ban.setBannedBy(by, byId);
            ban.setDate(System.currentTimeMillis());
            ban.setUntil(until);
            
            // Record in db
            if (current != null) {
                banRepo.recordUnban(current, "Time Adjustment", "Auto", null);
            }
            banRepo.recordBan(ban);
            
            // Store in redis
            setBan(who, (BanInfo<T>)ban);
            
            // Fire event
            platform.callEvent(new GlobalBanEvent(ban, isAuto));
            channel.broadcast(FireBanEventMessage.createFromBan(ban, isAuto));
            
            // Kick if online
            kickRelevant(ban, getBanKickReason(ban));
            
            // Finally broadcast message
            String message = getBanBroadcast(ban, isAuto);
            if (config.BroadcastBans) {
                broadcasts.broadcastGlobal(message);
                return new Result(Type.Success, (byId == null ? message : null));
            } else {
                return new Result(Type.Success, message);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "A database exception occured while attempting to ban " + who, e);
            return new Result(Type.Fail, ChatColor.RED + "An internal error occured");
        }
    }
    
    private Result ipban0(GlobalPlayer player, String reason, String by, UUID byId, long until, boolean isAuto) {
        try {
            BanInfo<GlobalPlayer> playerCurrent = getBan(player);
            BanInfo<InetAddress> ipCurrent = getBan(player.getAddress());
            
            boolean playerBanned = false;
            boolean ipBanned = false;
            
            if (playerCurrent != null && !playerCurrent.isTemporary()) {
                playerBanned = true;
            }
            
            if (ipCurrent != null && !ipCurrent.isTemporary()) {
                ipBanned = true;
            }
            
            if (ipBanned && playerBanned) {
                return new Result(Type.Fail, messages.get("ban.already-banned"));
            }
            
            if (Strings.isNullOrEmpty(reason)) {
                reason = config.DefaultBanReason;
            }
            
            // Un-temp ban if needed
            if (playerCurrent != null && !playerBanned) {
                banRepo.recordUnban(playerCurrent, "Time Adjustment", "Auto", null);
                playerCurrent = null;
            }
            if (ipCurrent != null && !ipBanned) {
                banRepo.recordUnban(ipCurrent, "Time Adjustment", "Auto", null);
                ipCurrent = null;
            }
            
            playerCurrent = new BanInfo<GlobalPlayer>(player);
            
            playerCurrent.setReason(reason);
            playerCurrent.setBannedBy(by, byId);
            playerCurrent.setDate(System.currentTimeMillis());
            playerCurrent.setUntil(until);
            
            if (!playerBanned) {
                banRepo.recordBan(playerCurrent);
                setBan(player, playerCurrent);
            }
            
            ipCurrent = new BanInfo<InetAddress>(player.getAddress());
            ipCurrent.setReason(reason);
            ipCurrent.setBannedBy(by, byId);
            ipCurrent.setDate(System.currentTimeMillis());
            ipCurrent.setUntil(until);
            
            if (!ipBanned) {
                banRepo.recordBan(ipCurrent);
                setBan(player.getAddress(), ipCurrent);
            }
            
            // Fire event
            platform.callEvent(new GlobalBanEvent(playerCurrent, player.getAddress(), isAuto));
            channel.broadcast(FireBanEventMessage.createFromIPBan(playerCurrent, player.getAddress(), isAuto));
            
            // Kick if online
            kickRelevant(ipCurrent, getBanKickReason(ipCurrent));
            
            // Finally broadcast message
            String message = getBanBroadcast(ipCurrent, isAuto);
            if (config.BroadcastBans) {
                broadcasts.broadcastGlobal(message);
                return new Result(Type.Success, (byId == null ? message : null));
            } else {
                return new Result(Type.Success, message);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "A database exception occured while attempting to ipban " + player.getName(), e);
            return new Result(Type.Fail, ChatColor.RED + "An internal error occured");
        }
    }
    
    private <T> Result unban0(T who, String reason, String by, UUID byId) {
        try {
            BanInfo<T> ban = getBan(who);
            
            if (ban == null) {
                return new Result(Type.Fail, messages.get("ban.not-banned"));
            }
            
            // Record the unban
            banRepo.recordUnban(ban, reason, by, byId);
            
            setBan(who, null);
            
            // Fire Event
            platform.callEvent(new GlobalUnbanEvent(ban));
            channel.broadcast(FireBanEventMessage.createFromUnban(ban));
            
            // Finally broadcast message
            String message = messages.get(
                    "unban.broadcast", 
                    "player", (who instanceof GlobalPlayer ? ((GlobalPlayer)who).getDisplayName() : ((InetAddress)who).getHostAddress()),
                    "sender", by);
            if (config.BroadcastUnbans) {
                broadcasts.broadcastGlobal(message);
                return new Result(Type.Success, (byId == null ? message : null));
            } else {
                return new Result(Type.Success, message);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "A database exception occured while attempting to unban " + who, e);
            return new Result(Type.Fail, ChatColor.RED + "An internal error occured");
        }
    }
    
    public String getBanKickReason(BanInfo<?> ban) {
        if (ban.isTemporary()) {
            long remaining = ban.getUntil() - ban.getDate();
            
            DateDiff diff = new DateDiff(remaining);
            String remainingShort = diff.toString();
            String remainingLong = diff.toLongString(2);
            String time = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG).format(ban.getUntil());
            
            if (ban.getWho() instanceof GlobalPlayer) {
                return messages.get(
                        "tempban.display.personal",
                        "player", ((GlobalPlayer)ban.getWho()).getDisplayName(),
                        "time", time,
                        "left", remainingLong,
                        "shortleft", remainingShort,
                        "message", ban.getReason(),
                        "sender", ban.getBannedBy()
                        );
            } else {
                return messages.get(
                        "iptempban.display.personal",
                        "player", ((InetAddress)ban.getWho()).getHostAddress(), // TODO: Display a player name if possible
                        "time", time,
                        "left", remainingLong,
                        "shortleft", remainingShort,
                        "message", ban.getReason(),
                        "sender", ban.getBannedBy()
                        );
            }
        } else {
            if (ban.getWho() instanceof GlobalPlayer) {
                return messages.get(
                        "ban.display.personal",
                        "player", ((GlobalPlayer)ban.getWho()).getDisplayName(),
                        "message", ban.getReason(),
                        "sender", ban.getBannedBy()
                        );
            } else {
                return messages.get(
                        "ipban.display.personal",
                        "player", ((InetAddress)ban.getWho()).getHostAddress(), // TODO: Display a player name if possible
                        "message", ban.getReason(),
                        "sender", ban.getBannedBy()
                        );
            }
        }
    }
    
    public String getBanBroadcast(BanInfo<?> ban, boolean isAuto) {
        String message;
        if (ban.isTemporary()) {
            long remaining = ban.getUntil() - ban.getDate();
            
            DateDiff diff = new DateDiff(remaining);
            String remainingShort = diff.toString();
            String remainingLong = diff.toLongString(2);
            
            String id;
            if (ban.getWho() instanceof GlobalPlayer) {
                id = (isAuto ? "tempban.display.broadcast.auto" : "tempban.display.broadcast");
            } else {
                id = (isAuto ? "iptempban.display.broadcast.auto" : "iptempban.display.broadcast");
            }
            
         // TODO: Display a player name if possible
            return messages.get(
                    id,
                    "player", (ban.getWho() instanceof GlobalPlayer ? ((GlobalPlayer)ban.getWho()).getDisplayName() : ((InetAddress)ban.getWho()).getHostAddress()),
                    "time", DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG).format(ban.getUntil()),
                    "left", remainingLong,
                    "shortleft", remainingShort,
                    "message", ban.getReason(),
                    "sender", ban.getBannedBy()
                    );
        } else {
            if (ban.getWho() instanceof GlobalPlayer) {
                return messages.get(
                        (isAuto ? "ban.display.broadcast.auto" : "ban.display.broadcast"),
                        "player", ((GlobalPlayer)ban.getWho()).getDisplayName(),
                        "message", ban.getReason(),
                        "sender", ban.getBannedBy()
                        );
            } else {
                return messages.get(
                        (isAuto ? "ipban.display.broadcast.auto" : "ipban.display.broadcast"),
                        "player", ((InetAddress)ban.getWho()).getHostAddress(), // TODO: Display a player name if possible
                        "message", ban.getReason(),
                        "sender", ban.getBannedBy()
                        );
            }
        }
    }
    
    private void kickRelevant(BanInfo<?> ban, String reason) {
        BaseComponent[] message = TextComponent.fromLegacyText(reason);
        
        if (ban.getWho() instanceof GlobalPlayer) {
            ProxiedPlayer player = proxy.getPlayer(((GlobalPlayer)ban.getWho()).getUniqueId());
            if (player != null) {
                player.disconnect(message);
            }
        } else if (ban.getWho() instanceof InetAddress) {
            for (ProxiedPlayer player : proxy.getPlayers()) {
                if (player.getAddress().getAddress().equals(ban.getWho())) {
                    player.disconnect(message);
                }
            }
        }
    }
    
    @Override
    public Result kick(GlobalPlayer player, String reason) {
        return kick(player, reason, false);
    }
    
    @Override
    public Result kick(GlobalPlayer player, String reason, boolean isAuto) {
        ProxiedPlayer proxied = proxy.getPlayer(player.getUniqueId());
        if (proxied == null) {
            return new Result(Type.Fail, messages.get("player.not-online", "player", player.getDisplayName()));
        }
        
        if (Strings.isNullOrEmpty(reason)) {
            reason = config.DefaultKickReason;
        }

        proxied.disconnect(TextComponent.fromLegacyText(messages.get("kick.display.personal", "message", reason)));
        
        if (config.BroadcastKicks) {
            if (isAuto) {
                broadcasts.broadcastGlobal(messages.get("kick.display.broadcast.auto", "player", player.getDisplayName(), "message", reason));
            } else {
                broadcasts.broadcastGlobal(messages.get("kick.display.broadcast", "player", player.getDisplayName(), "message", reason));
            }
        }
        
        return new Result(Type.Success, ChatColor.GREEN + String.format("%s has been kicked", player.getDisplayName()));
    }

    @Override
    public Result kickAll(String reason) {
        if (Strings.isNullOrEmpty(reason)) {
            reason = config.DefaultKickReason;
        }

        reason = messages.get("kick.display.personal", "message", reason);

        BaseComponent[] message = TextComponent.fromLegacyText(reason);
        for (ProxiedPlayer p : proxy.getPlayers()) {
            if (!p.hasPermission("gesuit.bypass.kickall")) {
                p.disconnect(message);
            }
        }
        
        return new Result(Type.Success, ChatColor.GREEN + "All players kicked");
    }
    
    @Override
    public BanInfo<InetAddress> getIPBan(InetAddress ip) {
        String name = ip.getHostAddress();
        if (!ipBans.contains(name)) {
            return null;
        }
        
        BanInfo<InetAddress> ban = new BanInfo<InetAddress>(ip);
        ban = ipBans.getStorable(name, ban);
        
        return ban;
    }
    
    @Override
    public void setIPBan(InetAddress ip, BanInfo<InetAddress> ban) {
        Preconditions.checkArgument(ban == null || ban.getWho().equals(ip));
        
        String name = ip.getHostAddress();
        
        if (ban == null) {
            ipBans.remove(name);
        } else {
            ipBans.set(name, ban);
        }
        
        ipBans.update();
    }
    
    @Override
    public boolean isIPBanned(InetAddress ip) {
        return getIPBan(ip) != null;
    }
    
    @Override
    public List<BanInfo<GlobalPlayer>> getHistory(GlobalPlayer player) throws StorageException {
        try {
            return banRepo.getBanHistory(player);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to get banhistory for " + player.getDisplayName(), e);
            throw new StorageException("Unable to retrieve banhistory");
        }
    }
    
    @Override
    public List<BanInfo<InetAddress>> getHistory(InetAddress ip) throws StorageException {
        try {
            return banRepo.getBanHistory(ip);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to get banhistory for " + ip.getHostAddress(), e);
            throw new StorageException("Unable to retrieve banhistory");
        }
    }
    
    public BanInfo<?> getAnyBan(GlobalPlayer player) {
        BanInfo<GlobalPlayer> playerBan = getBan(player);
        if (playerBan != null) {
            return playerBan;
        }
        
        // Check IP ban state
        BanInfo<InetAddress> ipBan = getBan(player.getAddress());
        if (ipBan != null) {
            return ipBan;
        }
        
        return null;
    }
    
    /**
     * Retrieves the currently active ban on the target.
     * This will clear any expired bans in the process
     * @param who The target to get the ban of
     * @return The ban or null
     */
    public <T> BanInfo<T> getBan(T who) {
        BanInfo<T> ban;
        if (who instanceof GlobalPlayer) {
            ban = (BanInfo<T>)((GlobalPlayer)who).getBanInfo();
        } else if (who instanceof InetAddress) {
            ban = (BanInfo<T>)getIPBan((InetAddress)who);
        } else {
            throw new AssertionError("Invalid ban target type");
        }
        
        if (ban != null && ban.isTemporary()) {
            // Has ban expired?
            if (System.currentTimeMillis() >= ban.getUntil()) {
                setBan(who, null);
                return null;
            } else {
                return ban;
            }
        } else {
            return ban;
        }
    }
    
    /**
     * Sets the currently active ban for a target
     * @param who The target of the ban
     * @param ban The ban or null to clear it
     */
    public <T> void setBan(T who, BanInfo<T> ban) {
        if (who instanceof GlobalPlayer) {
            ((GlobalPlayer)who).setBan((BanInfo<GlobalPlayer>)ban);
            ((GlobalPlayer)who).saveIfModified();
        } else if (who instanceof InetAddress) {
            setIPBan((InetAddress)who, (BanInfo<InetAddress>)ban);
        } else {
            throw new AssertionError("Invalid ban target type");
        }
    }
}
