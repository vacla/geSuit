package net.cubespace.geSuit;

import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.channel.ChannelManager;

public class BukkitPlayerManager extends PlayerManager {
    public BukkitPlayerManager(ChannelManager manager) {
        super(false, manager);
        
        requestFullUpdate();
    }
}
