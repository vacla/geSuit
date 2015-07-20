package net.cubespace.geSuit.core;

import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.NetworkInfoMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage;
import net.cubespace.geSuit.core.messages.SyncAttachmentMessage;

public abstract class GlobalManager implements ChannelDataReceiver<BaseMessage> {
    protected Channel<BaseMessage> channel;
    
    protected PlayerManager playerManager;
    protected ServerManager serverManager;
    protected Messages messages;
    
    public GlobalManager(Channel<BaseMessage> channel, PlayerManager playerManager, ServerManager serverManager, Messages messages) {
        this.channel = channel;
        this.playerManager = playerManager;
        this.serverManager = serverManager;
        this.messages = messages;
        
        channel.addReceiver(this);
    }
    
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        if (value instanceof PlayerUpdateMessage) {
            playerManager.handlePlayerUpdate((PlayerUpdateMessage)value);
        } else if (value instanceof SyncAttachmentMessage) {
            playerManager.handleSyncAttachment((SyncAttachmentMessage)value);
        } else if (value instanceof NetworkInfoMessage) {
            serverManager.handleNetworkInfoPacket((NetworkInfoMessage)value);
        }
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public ServerManager getServerManager() {
        return serverManager;
    }
    
    public Messages getMessages() {
        return messages;
    }
}
