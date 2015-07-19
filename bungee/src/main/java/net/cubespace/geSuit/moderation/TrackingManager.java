package net.cubespace.geSuit.moderation;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.config.ConfigReloadListener;
import net.cubespace.geSuit.config.ModerationConfig;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.TimeRecord;
import net.cubespace.geSuit.core.objects.Track;
import net.cubespace.geSuit.core.storage.StorageException;
import net.cubespace.geSuit.database.repositories.OnTime;
import net.cubespace.geSuit.database.repositories.Tracking;
import net.cubespace.geSuit.events.JoinNotificationsEvent;
import net.cubespace.geSuit.remote.moderation.TrackingActions;
import net.md_5.bungee.api.ProxyServer;

public class TrackingManager implements TrackingActions, ConfigReloadListener {
    private Tracking trackingRepo;
    private OnTime ontimeRepo;
    private Logger logger;
    
    private ModerationConfig config;
    
    public TrackingManager(Tracking tracking, OnTime ontime, Logger logger) {
        this.trackingRepo = tracking;
        this.ontimeRepo = ontime;
        this.logger = logger;
    }
    
    public void loadConfig(ModerationConfig config) {
        this.config = config;
    }
    
    @Override
    public void onConfigReloaded(ConfigManager manager) {
        loadConfig(manager.moderation());
    }
    
    public void updateTracking(GlobalPlayer player) {
        try {
            trackingRepo.insertTracking(player);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to update tracking history for " + player.getDisplayName(), e);
        }
    }
    
    @Override
    public List<Track> getHistory(UUID id) throws StorageException {
        try {
            return trackingRepo.getTrackingForUUID(id);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to get tracking history for " + id, e);
            throw new StorageException("Unable to retrieve tracking history");
        }
    }
    
    @Override
    public List<Track> getHistory(String name) throws StorageException {
        try {
            return trackingRepo.getTrackingForName(name);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to get tracking history for " + name, e);
            throw new StorageException("Unable to retrieve tracking history");
        }
    }
    
    @Override
    public List<Track> getHistory(InetAddress ip) throws StorageException {
        try {
            return trackingRepo.getTrackingForIP(ip);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to get tracking history for " + ip.getHostAddress(), e);
            throw new StorageException("Unable to retrieve tracking history");
        }
    }
    
    @Override
    public List<Track> getNameHistory(GlobalPlayer player) throws StorageException {
        try {
            return trackingRepo.getNameHistory(player.getUniqueId());
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to get name history for " + player.getDisplayName(), e);
            throw new StorageException("Unable to retrieve name history");
        }
    }
    
    @Override
    public TimeRecord getOntime(GlobalPlayer player) throws StorageException {
        try {
            return ontimeRepo.getPlayerOnTime(player.getUniqueId());
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to get ontime for " + player.getDisplayName(), e);
            throw new StorageException("Unable to retrieve ontime");
        }
    }
    
    @Override
    public List<TimeRecord> getOntimeTop(int offset, int size) throws StorageException {
        try {
            Map<UUID, Long> times = ontimeRepo.getOnTimeTop(offset, size);
            List<TimeRecord> results = Lists.newArrayListWithCapacity(times.size());
            for (Entry<UUID, Long> time : times.entrySet()) {
                results.add(new TimeRecord(time.getKey(), time.getValue()));
            }
            
            return results;
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to get ontime top", e);
            throw new StorageException("Unable to retrieve ontime top");
        }
    }
    
    public void updatePlayerOnTime(GlobalPlayer player) throws StorageException {
        Preconditions.checkArgument(ProxyServer.getInstance().getPlayer(player.getUniqueId()) != null, "Player is not online");
        try {
            ontimeRepo.updatePlayerOnTime(player, player.getSessionJoin(), System.currentTimeMillis());
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to update " + player.getDisplayName() + "'s ontime records", e);
            throw new StorageException("Unable to update ontime");
        }
    }
    
    @Override
    public List<UUID> matchPlayers(String name) throws StorageException {
        try {
            return trackingRepo.matchPlayers(name);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to match players", e);
            throw new StorageException("Unable to match the name");
        }
    }
    
    @Override
    public List<UUID> matchFullPlayers(String name) throws StorageException {
        try {
            return trackingRepo.matchFullPlayers(name);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to match players", e);
            throw new StorageException("Unable to match the name");
        }
    }
    
    public Track checkNameChange(GlobalPlayer player) throws StorageException {
        try {
            Track track = trackingRepo.checkNameChange(player.getUniqueId(), player.getName());
            if (track == null) {
                return null;
            }
            
            // If there is still the same nickname (even though the name changed) dont show the change
            if (player.hasNickname() && player.getNickname().equalsIgnoreCase(track.getNickname())) {
                return null;
            }
            
            // See if its too old
            if (System.currentTimeMillis() - track.getLastSeen() > TimeUnit.DAYS.toMillis(config.NameChangeNotifyTime)) {
                return null;
            }
            
            return track;
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to check name change on " + player.getName(), e);
            throw new StorageException("Unable to check for name change");
        }
    }
    
    void addPlayerInfo(JoinNotificationsEvent event) {
        // Check for alt accounts and notify staff (used later)
        if (!config.ShowAltAccounts) {
            return;
        }
        
        GlobalPlayer player = event.getPlayer();
        
        try {
            Track alt = trackingRepo.getPlayerAlt(player);
            
            if (alt == null) {
                return;
            }
            
            String message;
            // Is banned?
            if (config.ShowBannedAltAccounts && (alt.isIpBanned() || alt.isNameBanned())) {
                message = Global.getMessages().get(
                        "connect.alt-join.banned",
                        "player", player.getDisplayName(),
                        "alt", alt.getDisplayName(),
                        "ip", player.getAddress().getHostAddress()
                        );
            } else {
                message = Global.getMessages().get(
                        "connect.alt-join",
                        "player", player.getDisplayName(),
                        "alt", alt.getDisplayName(),
                        "ip", player.getAddress().getHostAddress()
                        );
            }
            
            event.addGroupNotification(message, "StaffNotice");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean shouldTrackOntime() {
        return config.TrackOnTime;
    }
}
