package net.cubespace.geSuit;

import net.cubespace.geSuit.core.GlobalServer;
import net.cubespace.geSuit.core.ServerManager;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class BungeeServerManager extends ServerManager {
    private ProxyServer proxy;
    public BungeeServerManager(ProxyServer proxy) {
        this.proxy = proxy;
    }
    
    public void updateServers() {
        // Do the child servers first
        for (ServerInfo serverInfo : proxy.getServers().values()) {
            int id = serverInfo.getAddress().getPort();
            
            if (getServer(id) == null) {
                GlobalServer server = new GlobalServer(serverInfo.getName(), id);
                addServer(server);
            }
        }
        
        // This server
        if (getServer(ChannelManager.PROXY) == null) {
            GlobalServer server = new GlobalServer("proxy", ChannelManager.PROXY);
            addServer(server);
        }
    }
}
