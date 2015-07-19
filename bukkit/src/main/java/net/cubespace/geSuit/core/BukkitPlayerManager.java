package net.cubespace.geSuit.core;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.Platform;
import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.player.GlobalPlayerJoinEvent;
import net.cubespace.geSuit.core.events.player.GlobalPlayerQuitEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Item;
import net.cubespace.geSuit.core.storage.RedisConnection;
import net.cubespace.geSuit.core.storage.StorageProvider;

public class BukkitPlayerManager extends PlayerManager {
    private Platform platform;
    public BukkitPlayerManager(Channel<BaseMessage> channel, RedisConnection redis, StorageProvider storageProvider, Platform platform) {
        super(channel, redis, storageProvider, platform);
        
        this.platform = platform;
    }
    
    @Override
    public void handlePlayerUpdate(PlayerUpdateMessage update) {
        super.handlePlayerUpdate(update);
        
        boolean isReset = false;
        switch (update.action) {
        case Reset:
            clearPlayers();
            isReset = true;
        case Add:
            for (Item item : update.items) {
                GlobalPlayer player = loadPlayer(item.id, item.username, item.nickname);
                addPlayer(player);
                
                if (!isReset) {
                    onJoin(player);
                }
            }
            break;
        case Remove:
            for (Item item : update.items) {
                GlobalPlayer player = getPlayer(item.id);
                removePlayer(player);
                
                onLeave(player);
            }
            break;
        default:
            // Already handled
            break;
        }
    }
    
    private void onJoin(GlobalPlayer player) {
        player.setSessionJoin(System.currentTimeMillis());
        platform.callEvent(new GlobalPlayerJoinEvent(player));
    }
    
    private void onLeave(GlobalPlayer player) {
        platform.callEvent(new GlobalPlayerQuitEvent(player));
    }
}
