package net.cubespace.geSuit.teleports;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.UpdateSpawnMessage;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.storage.StorageSection;

public class SpawnManager implements ChannelDataReceiver<BaseMessage> {
    private Location spawnNewPlayer;
    
    private Channel<BaseMessage> channel;
    
    public SpawnManager() {
        channel = Global.getChannelManager().createChannel("spawns", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        channel.addReceiver(this);
        
        loadSpawns();
    }
    
    public void loadSpawns() {
        StorageSection spawns = Global.getStorage().getSubsection("gesuit.spawns");
        spawnNewPlayer = spawns.getSimpleStorable("#new-player", Location.class);
    }
    
    public boolean isSetNewPlayer() {
        return spawnNewPlayer != null;
    }
    
    public Location getSpawnNewPlayer() {
        return spawnNewPlayer;
    }
    
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        if (value instanceof UpdateSpawnMessage) {
            loadSpawns();
        }
    }
}
