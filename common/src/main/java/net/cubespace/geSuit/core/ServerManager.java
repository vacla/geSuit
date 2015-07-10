package net.cubespace.geSuit.core;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.messages.NetworkInfoMessage;

public class ServerManager {
    private GlobalServer thisServer;
    private Map<String, GlobalServer> serversByName;
    private Map<Integer, GlobalServer> serversById;
    
    public ServerManager() {
        serversByName = Maps.newHashMap();
        serversById = Maps.newHashMap();
    }
    
    public GlobalServer getCurrentServer() {
        return thisServer;
    }
    
    public GlobalServer getServer(int id) {
        return serversById.get(id);
    }
    
    public GlobalServer getServer(String name) {
        return serversByName.get(name);
    }
    
    public Collection<GlobalServer> getServers() {
        return serversById.values();
    }
    
    public void handleNetworkInfoPacket(NetworkInfoMessage message) {
        serversById.clear();
        serversByName.clear();
        
        for (Entry<Integer, String> server : message.servers.entrySet()) {
            GlobalServer gs = new GlobalServer(server.getValue(), server.getKey());
            addServer(gs);
            
            // Update current server
            if (gs.getId() == message.serverId) {
                setCurrentServer(gs);
            }
        }
    }
    
    public NetworkInfoMessage createNetworkInfoPacket(GlobalServer dest) {
        Map<Integer, String> servers = Maps.newHashMapWithExpectedSize(serversById.size());
        
        for (GlobalServer server : serversById.values()) {
            servers.put(server.getId(), server.getName());
        }
        
        return new NetworkInfoMessage(dest.getId(), servers);
    }
    
    protected final void clearServers() {
        serversById.clear();
        serversByName.clear();
    }
    
    protected final void addServer(GlobalServer server) {
        serversById.put(server.getId(), server);
        serversByName.put(server.getName().toLowerCase(), server);
    }
    
    protected final void setCurrentServer(GlobalServer server) {
        Preconditions.checkArgument(serversById.containsKey(server.getId()));
        
        thisServer = server;
    }
}
