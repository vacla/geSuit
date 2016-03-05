package net.cubespace.geSuit.teleports;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.UpdateSpawnMessage;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.storage.StorageSection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SpawnManager implements ChannelDataReceiver<BaseMessage> {
    private final TeleportsManager teleportManager;
    
    private Location spawnNewPlayer;
    
    public SpawnManager(TeleportsManager teleportsManager) {
        this.teleportManager = teleportsManager;
    }
    
    public void loadSpawns() {
        boolean enableLogging = true;
        StorageSection spawns = Global.getStorageProvider().create("gesuit.spawns", enableLogging);
        spawnNewPlayer = spawns.getSimpleStorable("#new-player", Location.class);
    }
    
    public boolean isSetNewPlayer() {
        return spawnNewPlayer != null;
    }
    
    public Location getSpawnNewPlayer() {
        return spawnNewPlayer;
    }
    
    public void teleportPlayerToNewSpawn(ProxiedPlayer player, ServerInfo current) {
        if (isSetNewPlayer()) {
            teleportManager.teleportToInConnection(player, spawnNewPlayer, current, true);
        }
    }
    
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        if (value instanceof UpdateSpawnMessage) {
            loadSpawns();
        }
    }
}
