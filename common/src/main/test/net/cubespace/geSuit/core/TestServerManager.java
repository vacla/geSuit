package net.cubespace.geSuit.core;

import static org.junit.Assert.*;

import java.util.Map;

import net.cubespace.geSuit.core.GlobalServer;
import net.cubespace.geSuit.core.ServerManager;
import net.cubespace.geSuit.core.messages.NetworkInfoMessage;

import org.junit.Test;

import com.google.common.collect.Maps;

public class TestServerManager {
    @Test
    public void testAddServer() {
        // Prepare
        GlobalServer server = new GlobalServer("test", 10000);
        
        ServerManager manager = new ServerManager();
        manager.addServer(server);
        
        // Assertions
        assertSame(server, manager.getServer(10000));
        assertSame(server, manager.getServer("test"));
    }
    
    @Test
    public void testThisServer() {
        // Prepare
        GlobalServer server = new GlobalServer("test", 10000);
        
        ServerManager manager = new ServerManager();
        manager.addServer(server);
        manager.setCurrentServer(server);
        
        // Assertions
        assertSame(server, manager.getCurrentServer());
    }
    
    @Test
    public void testListServer() {
        // Prepare
        GlobalServer server1 = new GlobalServer("test1", 10000);
        GlobalServer server2 = new GlobalServer("test2", 10001);
        GlobalServer server3 = new GlobalServer("test3", 10002);
        GlobalServer server4 = new GlobalServer("test4", 10003);
        
        ServerManager manager = new ServerManager();
        manager.addServer(server1);
        manager.addServer(server2);
        manager.addServer(server3);
        manager.addServer(server4);
        
        // Assertions
        assertEquals(4, manager.getServers().size());
        
        assertTrue(manager.getServers().contains(server1));
        assertTrue(manager.getServers().contains(server2));
        assertTrue(manager.getServers().contains(server3));
        assertTrue(manager.getServers().contains(server4));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testListServerModify() {
        GlobalServer server = new GlobalServer("test", 10000);
        ServerManager manager = new ServerManager();
        
        // This should fail
        manager.getServers().add(server);
    }
    
    @Test
    public void testCreatePacket() {
        GlobalServer server1 = new GlobalServer("test1", 10000);
        GlobalServer server2 = new GlobalServer("test2", 10001);
        
        ServerManager manager = new ServerManager();
        manager.addServer(server1);
        manager.addServer(server2);
        
        // Create the packet
        NetworkInfoMessage packet = manager.createNetworkInfoPacket(server1);
        
        assertEquals(10000, packet.serverId);
        assertEquals(2, packet.servers.size());
        
        assertEquals("test1", packet.servers.get(10000));
        assertEquals("test2", packet.servers.get(10001));
    }
    
    @Test
    public void testLoadPacket() {
        Map<Integer, String> servers = Maps.newHashMap();
        servers.put(10000, "test1");
        servers.put(10001, "test2");
        
        NetworkInfoMessage packet = new NetworkInfoMessage(10000, servers);
        
        // Try and load it
        ServerManager manager = new ServerManager();
        manager.handleNetworkInfoPacket(packet);
        
        // Assertions
        assertNotNull(manager.getServer("test1"));
        assertNotNull(manager.getServer("test2"));
        assertNotNull(manager.getCurrentServer());
        
        assertSame(manager.getServer("test1"), manager.getCurrentServer());
    }
    
    @Test
    public void testClearServers() {
        // Prepare
        GlobalServer server1 = new GlobalServer("test1", 10000);
        GlobalServer server2 = new GlobalServer("test2", 10001);
        GlobalServer server3 = new GlobalServer("test3", 10002);
        GlobalServer server4 = new GlobalServer("test4", 10003);
        
        ServerManager manager = new ServerManager();
        manager.addServer(server1);
        manager.addServer(server2);
        manager.addServer(server3);
        manager.addServer(server4);
        
        // Do the test
        manager.clearServers();
        assertTrue(manager.getServers().isEmpty());
    }
}
