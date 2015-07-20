package net.cubespace.geSuit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;

import net.cubespace.geSuit.core.channel.ChannelManager;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.junit.Test;

import com.google.common.collect.Maps;

public class TestBungeeServerManager {
    @Test
    public void testServerList() {
        // Prepare the server list
        Map<String, ServerInfo> servers = Maps.newHashMap();
        servers.put("test1", new TestServer("test1", 10000));
        servers.put("test2", new TestServer("test2", 10001));
        servers.put("test3", new TestServer("test3", 10002));
        servers.put("test4", new TestServer("test4", 10003));
        servers.put("test5", new TestServer("test5", 10004));
        
        // Prepare the proxy
        ProxyServer proxy = mock(ProxyServer.class);
        when(proxy.getServers()).thenReturn(servers);
        
        // Begin
        BungeeServerManager serverManager = new BungeeServerManager(proxy, null);
        serverManager.updateServers();
        
        // Assertions
        assertEquals(6, serverManager.getServers().size());
        
        assertNotNull(serverManager.getServer("test1"));
        assertNotNull(serverManager.getServer("test2"));
        assertNotNull(serverManager.getServer("test3"));
        assertNotNull(serverManager.getServer("test4"));
        assertNotNull(serverManager.getServer("test5"));
        
        assertNotNull(serverManager.getServer(10000));
        assertNotNull(serverManager.getServer(10001));
        assertNotNull(serverManager.getServer(10002));
        assertNotNull(serverManager.getServer(10003));
        assertNotNull(serverManager.getServer(10004));
        
        assertNotNull(serverManager.getServer("proxy"));
        assertNotNull(serverManager.getServer(ChannelManager.PROXY));
    }

    private static class TestServer implements ServerInfo
    {
        private String name;
        private InetSocketAddress address;
        
        public TestServer(String name, int id) {
            this.name = name;
            this.address = new InetSocketAddress(InetAddress.getLoopbackAddress(), id);
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public InetSocketAddress getAddress() {
            return address;
        }

        @Override
        public Collection<ProxiedPlayer> getPlayers() {
            return null;
        }

        @Override
        public String getMotd() {
            return null;
        }

        @Override
        public boolean canAccess(CommandSender sender) {
            return false;
        }

        @Override
        public void sendData(String channel, byte[] data) {
        }

        @Override
        public void ping(Callback<ServerPing> callback) {
        }

        @Override
        public boolean sendData(String arg0, byte[] arg1, boolean arg2) {
            return false;
        }
    }
}
