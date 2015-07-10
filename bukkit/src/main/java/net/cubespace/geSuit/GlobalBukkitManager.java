package net.cubespace.geSuit;

import net.cubespace.geSuit.core.GlobalManager;
import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.ServerManager;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.LangUpdateMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateRequestMessage;

public class GlobalBukkitManager extends GlobalManager {
    public GlobalBukkitManager(Channel<BaseMessage> channel, PlayerManager playerManager, ServerManager serverManager, Messages messages) {
        super(channel, playerManager, serverManager, messages);
    }

    /**
     * Requests that the proxy server send this server a full update
     * of all servers, players, configuration data, etc.
     */
    public void requestNetworkUpdate() {
        channel.send(new PlayerUpdateRequestMessage(), ChannelManager.PROXY);
    }
    
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        super.onDataReceive(channel, value, sourceId, isBroadcast);
        
        if (value instanceof PlayerUpdateMessage) {
            playerManager.handlePlayerUpdate((PlayerUpdateMessage)value);
        } else if (value instanceof LangUpdateMessage) {
            messages.load((LangUpdateMessage)value);
        }
    }
}
