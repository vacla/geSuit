package net.cubespace.geSuit;

import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.storage.RedisConnection;

public class BukkitPlayerManager extends PlayerManager {
    public BukkitPlayerManager(Channel<BaseMessage> channel, RedisConnection redis) {
        super(false, channel, redis);
    }
}
