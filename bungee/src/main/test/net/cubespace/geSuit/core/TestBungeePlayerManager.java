package net.cubespace.geSuit.core;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

import net.cubespace.geSuit.core.BungeePlayerManager;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.player.GlobalPlayerJoinEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage;
import net.cubespace.geSuit.core.storage.StorageProvider;
import net.md_5.bungee.api.connection.PendingConnection;

import org.junit.Test;

import com.google.common.net.InetAddresses;

public class TestBungeePlayerManager {
    @Test
    public void testPreLoginNewPlayer() {
        BungeePlayerManager manager = spy(new BungeePlayerManager(null, null, mock(StorageProvider.class), null));
        
        // Setup the player
        GlobalPlayer player = mock(GlobalPlayer.class);
        UUID id = UUID.randomUUID();
        
        when(player.getName()).thenReturn("name");
        when(player.getAddress()).thenReturn(InetAddress.getLoopbackAddress());
        when(player.getUniqueId()).thenReturn(id);
        when(player.hasPlayedBefore()).thenReturn(false); // New player
        
        // Override loading as we dont want to actually load the player
        doReturn(player).when(manager).loadPlayer(any(UUID.class), anyString(), anyString());
        
        // Prepare the pending connection
        PendingConnection connection = mock(PendingConnection.class);
        when(connection.getName()).thenReturn("name");
        when(connection.getUniqueId()).thenReturn(id);
        when(connection.getAddress()).thenReturn(new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345));
        
        // Do the test
        manager.beginPreLogin(connection);
        
        // Assertions
        verify(player).setNewPlayer(true); // They are a new player so it should have updated
        
        // Connection matches the loaded player so no changes should be done
        verify(player, never()).setAddress(any(InetAddress.class));
        verify(player, never()).setName(anyString());
    }
    
    @Test
    public void testPreLoginExistingPlayer() {
        BungeePlayerManager manager = spy(new BungeePlayerManager(null, null, mock(StorageProvider.class), null));
        
        // Setup the player
        GlobalPlayer player = mock(GlobalPlayer.class);
        UUID id = UUID.randomUUID();
        
        when(player.getName()).thenReturn("name");
        when(player.getAddress()).thenReturn(InetAddress.getLoopbackAddress());
        when(player.getUniqueId()).thenReturn(id);
        when(player.hasPlayedBefore()).thenReturn(true); // Existing player
        
        // Override loading as we dont want to actually load the player
        doReturn(player).when(manager).loadPlayer(any(UUID.class), anyString(), anyString());
        
        // Prepare the pending connection
        PendingConnection connection = mock(PendingConnection.class);
        when(connection.getName()).thenReturn("name");
        when(connection.getUniqueId()).thenReturn(id);
        when(connection.getAddress()).thenReturn(new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345));
        
        // Do the test
        manager.beginPreLogin(connection);
        
        // Assertions
        
        // Connection matches the loaded player so no changes should be done
        verify(player, never()).setNewPlayer(anyBoolean());
        verify(player, never()).setAddress(any(InetAddress.class));
        verify(player, never()).setName(anyString());
    }
    
    @Test
    public void testPreLoginNameChange() {
        BungeePlayerManager manager = spy(new BungeePlayerManager(null, null, mock(StorageProvider.class), null));
        
        // Setup the player
        GlobalPlayer player = mock(GlobalPlayer.class);
        UUID id = UUID.randomUUID();
        
        when(player.getName()).thenReturn("name");
        when(player.getAddress()).thenReturn(InetAddress.getLoopbackAddress());
        when(player.getUniqueId()).thenReturn(id);
        when(player.hasPlayedBefore()).thenReturn(true); // Existing player
        
        // Override loading as we dont want to actually load the player
        doReturn(player).when(manager).loadPlayer(any(UUID.class), anyString(), anyString());
        
        // Prepare the pending connection
        PendingConnection connection = mock(PendingConnection.class);
        when(connection.getName()).thenReturn("newname");
        when(connection.getUniqueId()).thenReturn(id);
        when(connection.getAddress()).thenReturn(new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345));
        
        // Do the test
        manager.beginPreLogin(connection);
        
        // Assertions
        verify(player).setName("newname");
        
        // Connection matches the loaded player so no changes should be done
        verify(player, never()).setNewPlayer(anyBoolean());
        verify(player, never()).setAddress(any(InetAddress.class));
    }
    
    @Test
    public void testPreLoginAddressChange() {
        BungeePlayerManager manager = spy(new BungeePlayerManager(null, null, mock(StorageProvider.class), null));
        
        // Setup the player
        GlobalPlayer player = mock(GlobalPlayer.class);
        UUID id = UUID.randomUUID();
        
        when(player.getName()).thenReturn("name");
        when(player.getAddress()).thenReturn(InetAddresses.forString("127.0.0.2"));
        when(player.getUniqueId()).thenReturn(id);
        when(player.hasPlayedBefore()).thenReturn(true); // Existing player
        
        // Override loading as we dont want to actually load the player
        doReturn(player).when(manager).loadPlayer(any(UUID.class), anyString(), anyString());
        
        // Prepare the pending connection
        PendingConnection connection = mock(PendingConnection.class);
        when(connection.getName()).thenReturn("name");
        when(connection.getUniqueId()).thenReturn(id);
        when(connection.getAddress()).thenReturn(new InetSocketAddress(InetAddress.getLoopbackAddress(), 12345));
        
        // Do the test
        manager.beginPreLogin(connection);
        
        // Assertions
        verify(player).setAddress(InetAddress.getLoopbackAddress());
        
        // Connection matches the loaded player so no changes should be done
        verify(player, never()).setNewPlayer(anyBoolean());
        verify(player, never()).setName(anyString());
    }
    
    @Test
    public void testEndPreLogin() {
        BungeePlayerManager manager = new BungeePlayerManager(null, null, mock(StorageProvider.class), null);
        GlobalPlayer player = mock(GlobalPlayer.class);
        
        UUID id = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(id);
        
        // Do the test
        manager.endPreLogin(player);
        
        // Player should be preloaded, but not online
        assertTrue(manager.isPreloaded(player.getUniqueId()));
        assertNull(manager.getPlayer(id));
    }
    
    @Test
    public void testFinishPreload() {
        Platform platform = mock(Platform.class);
        Channel<BaseMessage> channel = mock(Channel.class);
        
        BungeePlayerManager manager = new BungeePlayerManager(channel, null, mock(StorageProvider.class), platform);
        GlobalPlayer player = mock(GlobalPlayer.class);
        
        UUID id = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(id);
        when(player.getName()).thenReturn("test");
        
        manager.endPreLogin(player);
        
        // Do the test
        manager.finishPreload(id);
        
        // Assertions
        assertEquals(player, manager.getPlayer(id));
        assertFalse(manager.isPreloaded(id));
        
        verify(channel).broadcast(any(PlayerUpdateMessage.class));
        verify(platform).callEvent(any(GlobalPlayerJoinEvent.class));
    }
    
    @Test
    public void testRemovePlayer() {
        Channel<BaseMessage> channel = mock(Channel.class);
        Platform platform = mock(Platform.class);
        
        BungeePlayerManager manager = new BungeePlayerManager(channel, null, mock(StorageProvider.class), platform);
        GlobalPlayer player = mock(GlobalPlayer.class);
        
        UUID id = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(id);
        when(player.getName()).thenReturn("test");
        
        manager.addPlayer(player);
        
        // Do the test
        manager.removePlayer(player);
        
        // Assertions
        verify(channel).broadcast(any(PlayerUpdateMessage.class));
        assertNull(manager.getPlayer(id));
    }
    
    @Test
    public void testRemovePreloadPlayer() {
        Channel<BaseMessage> channel = mock(Channel.class);
        
        BungeePlayerManager manager = new BungeePlayerManager(channel, null, mock(StorageProvider.class), null);
        GlobalPlayer player = mock(GlobalPlayer.class);
        
        UUID id = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(id);
        
        manager.endPreLogin(player);
        
        // Do the test
        manager.removePlayer(player);
        
        // Assertions
        verifyZeroInteractions(channel);
        assertFalse(manager.isPreloaded(id));
    }

}
