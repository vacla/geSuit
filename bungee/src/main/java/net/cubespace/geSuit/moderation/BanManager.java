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

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.config.ConfigReloadListener;
import net.cubespace.geSuit.config.ModerationConfig;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.moderation.GlobalBanEvent;
import net.cubespace.geSuit.core.events.moderation.GlobalUnbanEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.FireBanEventMessage;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.objects.Result.Type;
import net.cubespace.geSuit.core.storage.StorageException;
import net.cubespace.geSuit.core.storage.StorageSection;
import net.cubespace.geSuit.database.repositories.BanHistory;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.remote.moderation.BanActions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BanManager implements BanActions, ConfigReloadListener {
    private BanHistory banRepo;
    private Logger logger;
    private Channel<BaseMessage> channel;
    
    private ModerationConfig config;
    
    public BanManager(BanHistory banRepo, Channel<BaseMessage> channel, Logger logger) {
        this.banRepo = banRepo;
        this.logger = logger;
        this.channel = channel;
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
            BanInfo<GlobalPlayer> playerBan = player.getBanInfo();
            BanInfo<InetAddress> ipBan = getBan(player.getAddress());
            
            if (playerBan == null && ipBan == null) {
                return new Result(Type.Fail, Global.getMessages().get("ban.not-banned"));
            }
            
            // Record the unban
            if (playerBan != null) {
                banRepo.recordUnban(playerBan, reason, by, byId);
                player.removeBan();
                player.save();
            }
            
            if (ipBan != null) {
                banRepo.recordUnban(ipBan, reason, by, byId);
                setBan(player.getAddress(), null);
            }
            
            // Fire Event
            if (playerBan != null) {
                Global.getPlatform().callEvent(new GlobalUnbanEvent(playerBan, player.getAddress()));
                channel.broadcast(FireBanEventMessage.createFromIPUnban(playerBan, player.getAddress()));
            } else {
                Global.getPlatform().callEvent(new GlobalUnbanEvent(ipBan));
                channel.broadcast(FireBanEventMessage.createFromUnban(ipBan));
            }
            
            // Finally broadcast message
            String message = Global.getMessages().get("unban.broadcast", "player", player.getDisplayName(), "sender", by);
            if (config.BroadcastUnbans) {
                PlayerManager.sendBroadcast(message, null);
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
            BanInfo<T> current;
            if (who instanceof GlobalPlayer) {
                current = (BanInfo<T>)((GlobalPlayer)who).getBanInfo();
            } else if (who instanceof InetAddress) {
                current = (BanInfo<T>)getBan((InetAddress)who);
            } else {
                throw new AssertionError("Unknown ban target " + who.getClass());
            }
            
            if (current != null) {
                if (!current.isTemporary()) {
                    return new Result(Type.Fail, Global.getMessages().get("ban.already-banned"));
                } else if (System.currentTimeMillis() > current.getUntil()) {
                    // Not active anymore
                    current = null;
                }
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
            if (ban.getWho() instanceof GlobalPlayer) {
                GlobalPlayer player = (GlobalPlayer)ban.getWho();
                player.setBan((BanInfo<GlobalPlayer>)ban);
                player.save();
            } else if (ban.getWho() instanceof InetAddress) {
                setBan((InetAddress)ban.getWho(), (BanInfo<InetAddress>)ban);
            }
            
            // Fire event
            Global.getPlatform().callEvent(new GlobalBanEvent(ban, isAuto));
            channel.broadcast(FireBanEventMessage.createFromBan(ban, isAuto));
            
            // Kick if online
            kickRelevant(ban, getBanKickReason(ban));
            
            // Finally broadcast message
            String message = getBanBroadcast(ban, isAuto);
            if (config.BroadcastBans) {
                PlayerManager.sendBroadcast(message, null);
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
            BanInfo<GlobalPlayer> playerCurrent = player.getBanInfo();
            BanInfo<InetAddress> ipCurrent = getBan(player.getAddress());
            
            boolean playerBanned = false;
            boolean ipBanned = false;
            
            if (playerCurrent != null) {
                if (!playerCurrent.isTemporary()) {
                    playerBanned = true;
                } else if (System.currentTimeMillis() > playerCurrent.getUntil()) {
                    // Not active anymore
                    playerCurrent = null;
                }
            }
            
            if (ipCurrent != null) {
                if (!ipCurrent.isTemporary()) {
                    ipBanned = true;
                } else if (System.currentTimeMillis() > ipCurrent.getUntil()) {
                    // Not active anymore
                    ipCurrent = null;
                }
            }
            
            if (ipBanned && playerBanned) {
                return new Result(Type.Fail, Global.getMessages().get("ban.already-banned"));
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
                player.setBan(playerCurrent);
                player.save();
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
            Global.getPlatform().callEvent(new GlobalBanEvent(playerCurrent, player.getAddress(), isAuto));
            channel.broadcast(FireBanEventMessage.createFromIPBan(playerCurrent, player.getAddress(), isAuto));
            
            // Kick if online
            kickRelevant(ipCurrent, getBanKickReason(ipCurrent));
            
            // Finally broadcast message
            String message = getBanBroadcast(ipCurrent, isAuto);
            if (config.BroadcastBans) {
                PlayerManager.sendBroadcast(message, null);
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
            BanInfo<T> ban;
            if (who instanceof GlobalPlayer) {
                ban = (BanInfo<T>) ((GlobalPlayer)who).getBanInfo();
            } else if (who instanceof InetAddress) {
                ban = (BanInfo<T>) getBan((InetAddress)who);
            } else {
                throw new AssertionError("Unknown ban target type");
            }
            
            if (ban == null) {
                return new Result(Type.Fail, Global.getMessages().get("ban.not-banned"));
            }
            
            // Record the unban
            banRepo.recordUnban(ban, reason, by, byId);
            
            if (who instanceof GlobalPlayer) {
                ((GlobalPlayer)who).removeBan();
                ((GlobalPlayer)who).save();
            } else if (who instanceof InetAddress) {
                setBan((InetAddress)who, null);
            }
            
            // Fire Event
            Global.getPlatform().callEvent(new GlobalUnbanEvent(ban));
            channel.broadcast(FireBanEventMessage.createFromUnban(ban));
            
            // Finally broadcast message
            String message = Global.getMessages().get(
                    "unban.broadcast", 
                    "player", (who instanceof GlobalPlayer ? ((GlobalPlayer)who).getDisplayName() : ((InetAddress)who).getHostAddress()),
                    "sender", by);
            if (config.BroadcastUnbans) {
                PlayerManager.sendBroadcast(message, null);
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
            
            String remainingShort = Utilities.buildShortTimeDiffString(remaining, 10);
            String remainingLong = Utilities.buildTimeDiffString(remaining, 2);
            String time = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG).format(ban.getUntil());
            
            if (ban.getWho() instanceof GlobalPlayer) {
                return Global.getMessages().get(
                        "tempban.display.personal",
                        "player", ((GlobalPlayer)ban.getWho()).getDisplayName(),
                        "time", time,
                        "left", remainingLong,
                        "shortleft", remainingShort,
                        "message", ban.getReason(),
                        "sender", ban.getBannedBy()
                        );
            } else {
                return Global.getMessages().get(
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
                return Global.getMessages().get(
                        "ban.display.personal",
                        "player", ((GlobalPlayer)ban.getWho()).getDisplayName(),
                        "message", ban.getReason(),
                        "sender", ban.getBannedBy()
                        );
            } else {
                return Global.getMessages().get(
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
            
            String remainingShort = Utilities.buildShortTimeDiffString(remaining, 10);
            String remainingLong = Utilities.buildTimeDiffString(remaining, 2);
            
            String id;
            if (ban.getWho() instanceof GlobalPlayer) {
                id = (isAuto ? "tempban.display.broadcast.auto" : "tempban.display.broadcast");
            } else {
                id = (isAuto ? "iptempban.display.broadcast.auto" : "iptempban.display.broadcast");
            }
            
         // TODO: Display a player name if possible
            return Global.getMessages().get(
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
                return Global.getMessages().get(
                        (isAuto ? "ban.display.broadcast.auto" : "ban.display.broadcast"),
                        "player", ((GlobalPlayer)ban.getWho()).getDisplayName(),
                        "message", ban.getReason(),
                        "sender", ban.getBannedBy()
                        );
            } else {
                return Global.getMessages().get(
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
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(((GlobalPlayer)ban.getWho()).getUniqueId());
            if (player != null) {
                player.disconnect(message);
            }
        } else if (ban.getWho() instanceof InetAddress) {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
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
        ProxiedPlayer proxied = ProxyServer.getInstance().getPlayer(player.getUniqueId());
        if (proxied == null) {
            return new Result(Type.Fail, Global.getMessages().get("player.not-online", "player", player.getDisplayName()));
        }
        
        if (Strings.isNullOrEmpty(reason)) {
            reason = config.DefaultKickReason;
        }

        proxied.disconnect(TextComponent.fromLegacyText(Global.getMessages().get("kick.display.personal", "message", reason)));
        
        if (config.BroadcastKicks) {
            if (isAuto) {
                PlayerManager.sendBroadcast(Global.getMessages().get("kick.display.broadcast.auto", "player", player.getDisplayName(), "message", reason), player.getName());
            } else {
                PlayerManager.sendBroadcast(Global.getMessages().get("kick.display.broadcast", "player", player.getDisplayName(), "message", reason), player.getName());
            }
        }
        
        return new Result(Type.Success, ChatColor.GREEN + String.format("%s has been kicked", player.getDisplayName()));
    }

    @Override
    public Result kickAll(String reason) {
        if (Strings.isNullOrEmpty(reason)) {
            reason = config.DefaultKickReason;
        }

        reason = Global.getMessages().get("kick.display.personal", "message", reason);

        BaseComponent[] message = TextComponent.fromLegacyText(reason);
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            if (!p.hasPermission("gesuit.bypass.kickall")) {
                p.disconnect(message);
            }
        }
        
        return new Result(Type.Success, ChatColor.GREEN + "All players kicked");
    }
    
    @Override
    public BanInfo<InetAddress> getBan(InetAddress ip) {
        StorageSection storage = Global.getStorageProvider().create("geSuit.ipbans");
        String name = ip.getHostAddress();
        if (!storage.contains(name)) {
            return null;
        }
        
        BanInfo<InetAddress> ban = new BanInfo<InetAddress>(ip);
        storage.getStorable(name, ban);
        
        return ban;
    }
    
    @Override
    public void setBan(InetAddress ip, BanInfo<InetAddress> ban) {
        Preconditions.checkArgument(ban == null || ban.getWho().equals(ip));
        
        StorageSection storage = Global.getStorageProvider().create("geSuit.ipbans");
        String name = ip.getHostAddress();
        
        if (ban == null) {
            storage.remove(name);
        } else {
            storage.set(name, ban);
        }
        
        storage.update();
    }
    
    @Override
    public boolean isBanned(InetAddress ip) {
        return getBan(ip) != null;
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
}
