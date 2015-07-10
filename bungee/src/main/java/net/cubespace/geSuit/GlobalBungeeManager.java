package net.cubespace.geSuit;

import net.cubespace.geSuit.core.GlobalManager;
import net.cubespace.geSuit.core.GlobalServer;
import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.ServerManager;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateRequestMessage;

public class GlobalBungeeManager extends GlobalManager {
    public GlobalBungeeManager(Channel<BaseMessage> channel, PlayerManager playerManager, ServerManager serverManager, Messages messages) {
        super(channel, playerManager, serverManager, messages);
    }
    
    /**
     * Sends all update info to all servers
     */
    public void broadcastNetworkUpdate() {
        for (GlobalServer server : serverManager.getServers()) {
            if (server != serverManager.getCurrentServer()) {
                sendServerUpdate(server);
            }
        }
        
        broadcastPlayerUpdate();
        broadcastLangUpdate();
    }
    
    /**
     * Sends all update info to the specified server
     * @param server The server to receive it
     */
    public void sendNetworkUpdate(GlobalServer server) {
        sendServerUpdate(server);
        
        broadcastPlayerUpdate();
        broadcastLangUpdate();
    }
    

    private void broadcastPlayerUpdate() {
        channel.broadcast(playerManager.createFullUpdatePacket());
    }
    
    private void broadcastLangUpdate() {
        channel.broadcast(messages.createUpdatePacket());
    }
    
    private void sendServerUpdate(GlobalServer server) {
        channel.send(serverManager.createNetworkInfoPacket(server), server.getId());
    }
    
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        super.onDataReceive(channel, value, sourceId, isBroadcast);
        
        // Handle updates
        // TODO: Rename this packet to NetworkUpdateRequestMessage
        if (value instanceof PlayerUpdateRequestMessage) {
            sendNetworkUpdate(serverManager.getServer(sourceId));
        }
    }
}
