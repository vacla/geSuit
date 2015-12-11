package net.cubespace.geSuit.core;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.attachments.Attachment.AttachmentType;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.player.GlobalPlayerJoinEvent;
import net.cubespace.geSuit.core.events.player.GlobalPlayerQuitEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Action;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Item;
import net.cubespace.geSuit.core.storage.RedisConnection;
import net.cubespace.geSuit.core.storage.StorageProvider;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Listener;

public class BungeePlayerManager extends PlayerManager implements Listener {
    private Map<UUID, GlobalPlayer> loadingPlayers;
    private Platform platform;
    
    public BungeePlayerManager(Channel<BaseMessage> channel, RedisConnection connection, StorageProvider storageProvider, Platform platform) {
        super(channel, connection, storageProvider, platform);
        
        this.platform = platform;
        
        loadingPlayers = Maps.newHashMap();
    }
    
    public GlobalPlayer beginPreLogin(PendingConnection connection) {
        GlobalPlayer player = loadPlayer(connection.getUniqueId(), connection.getName(), null);
        
        if (!player.isLoaded()) {
            player.refresh();
        }
        
        // Remove existing session and local attachments for a fresh start
        player.getAttachmentContainer().removeAll(AttachmentType.Session);
        player.getAttachmentContainer().removeAll(AttachmentType.Local);
        
        // Mark as new player if needed
        if (!player.hasPlayedBefore()) {
            player.setNewPlayer(true);
        }
        
        // Update name for name changes
        if (!player.getName().equals(connection.getName())) {
            player.setName(connection.getName());
        }
        
        // Update IP for address changes
        if (!connection.getAddress().getAddress().equals(player.getAddress())) {
            player.setAddress(connection.getAddress().getAddress());
        }
        
        return player;
    }
    
    public void endPreLogin(GlobalPlayer player) {
        loadingPlayers.put(player.getUniqueId(), player);
    }
    
    public GlobalPlayer getPreloadedPlayer(UUID id) {
        return loadingPlayers.get(id);
    }
    
    public boolean isPreloaded(UUID id) {
        return loadingPlayers.containsKey(id);
    }
    
    public GlobalPlayer finishPreload(UUID id) {
        GlobalPlayer player = loadingPlayers.remove(id);
        addPlayer(player);
        player.saveIfModified();
        
        player.setSessionJoin(System.currentTimeMillis());
        platform.callEvent(new GlobalPlayerJoinEvent(player));
        channel.broadcast(new PlayerUpdateMessage(Action.Add, new Item(id, player.getName(), player.getNickname())));
        
        return player;
    }
    
    @Override
    public void removePlayer(GlobalPlayer player) {
        if (loadingPlayers.remove(player.getUniqueId()) != null) {
            return;
        }
        super.removePlayer(player);
        
        platform.callEvent(new GlobalPlayerQuitEvent(player));
        channel.broadcast(new PlayerUpdateMessage(Action.Remove, new Item(player.getUniqueId(), null, null)));
    }
}
