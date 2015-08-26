package net.cubespace.geSuit.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.player.GlobalPlayerJoinEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Action;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Item;
import net.cubespace.geSuit.core.storage.StorageProvider;

import org.junit.Test;

public class TestBukkitPlayerManager {
    @Test
    public void testAddPlayers() {
        Platform platform = mock(Platform.class);
        
        BukkitPlayerManager manager = spy(new BukkitPlayerManager(null, null, mock(StorageProvider.class), platform));
        
        UUID playerId = UUID.randomUUID();
        
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getName()).thenReturn("name");
        when(player.getNickname()).thenReturn("nickname");
        when(player.hasNickname()).thenReturn(true);
        
        // Overload load so we dont actually load the player
        doReturn(player).when(manager).loadPlayer(any(UUID.class), anyString(), anyString());
        
        // Prepare the packet
        Item[] items = new Item[1];
        items[0] = new Item(playerId, "name", "nickname");
        
        PlayerUpdateMessage packet = new PlayerUpdateMessage(Action.Add, items);
        
        // Do the test
        manager.handlePlayerUpdate(packet);
        
        // Assertions
        assertNotNull(manager.getPlayer(playerId));
        verify(platform).callEvent(any(GlobalPlayerJoinEvent.class));
    }
    
    @Test
    public void testResetPlayers() {
        Platform platform = mock(Platform.class);
        
        BukkitPlayerManager manager = spy(new BukkitPlayerManager(null, null, mock(StorageProvider.class), platform));
        
        UUID player1Id = UUID.randomUUID();
        GlobalPlayer player1 = mock(GlobalPlayer.class);
        when(player1.getUniqueId()).thenReturn(player1Id);
        when(player1.getName()).thenReturn("name");
        when(player1.getNickname()).thenReturn("nickname");
        when(player1.hasNickname()).thenReturn(true);
        
        UUID player2Id = UUID.randomUUID();
        GlobalPlayer player2 = mock(GlobalPlayer.class);
        when(player2.getUniqueId()).thenReturn(player2Id);
        when(player2.getName()).thenReturn("other");
        when(player2.hasNickname()).thenReturn(false);
        
        // Overload load so we dont actually load the player
        doReturn(player2).when(manager).loadPlayer(any(UUID.class), anyString(), anyString());
        
        // Prepare the packet
        Item[] items = new Item[1];
        items[0] = new Item(player2Id, "other", null);
        
        PlayerUpdateMessage packet = new PlayerUpdateMessage(Action.Reset, items);
        
        // Do the test
        manager.addPlayer(player1);
        manager.handlePlayerUpdate(packet);
        
        // Assertions
        assertNull(manager.getPlayer(player1Id));
        assertNotNull(manager.getPlayer(player2Id));
        
        verifyZeroInteractions(platform);
    }
    
    @Test
    public void testRemovePlayers() {
        Platform platform = mock(Platform.class);
        Channel<BaseMessage> channel = mock(Channel.class);
        
        BukkitPlayerManager manager = spy(new BukkitPlayerManager(channel, null, mock(StorageProvider.class), platform));
        
        UUID playerId = UUID.randomUUID();
        
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getName()).thenReturn("name");
        when(player.getNickname()).thenReturn("nickname");
        when(player.hasNickname()).thenReturn(true);
        
        // Prepare the packet
        Item[] items = new Item[1];
        items[0] = new Item(playerId, "name", "nickname");
        
        PlayerUpdateMessage packet = new PlayerUpdateMessage(Action.Remove, items);
        
        // Do the test
        manager.addPlayer(player);
        manager.handlePlayerUpdate(packet);
        
        // Assertions
        assertNull(manager.getPlayer(playerId));
    }
}
