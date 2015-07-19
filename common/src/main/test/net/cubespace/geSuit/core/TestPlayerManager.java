package net.cubespace.geSuit.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Test;

public class TestPlayerManager {
    
    private GlobalPlayer createPlayer(UUID id, String name, String nickname) {
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(id);
        when(player.getName()).thenReturn(name);
        if (nickname != null) {
            when(player.hasNickname()).thenReturn(true);
            when(player.getNickname()).thenReturn(nickname);
        } else {
            when(player.hasNickname()).thenReturn(false);
        }
        
        return player;
    }
    @Test
    public void testAddPlayerNoNickname() {
        PlayerManager playerManager = new PlayerManager(null, null, null, null) {};
        
        UUID playerId = UUID.randomUUID();
        GlobalPlayer player = createPlayer(playerId, "name", null);
        
        // Do test
        playerManager.addPlayer(player);
        
        // Assertions
        assertEquals(player, playerManager.getPlayer(playerId));
        assertEquals(player, playerManager.getPlayerExact("name", false));
    }
    
    @Test
    public void testAddPlayerNickname() {
        PlayerManager playerManager = new PlayerManager(null, null, null, null) {};
        
        UUID playerId = UUID.randomUUID();
        GlobalPlayer player = createPlayer(playerId, "name", "nickname");
        
        // Do test
        playerManager.addPlayer(player);
        
        // Assertions
        assertEquals(player, playerManager.getPlayer(playerId));
        assertEquals(player, playerManager.getPlayerExact("name", false));
        assertEquals(player, playerManager.getPlayerExact("nickname", true));
    }
    
    @Test
    public void testGetPlayerPartial() {
        PlayerManager playerManager = new PlayerManager(null, null, null, null) {};
        playerManager.addPlayer(createPlayer(UUID.randomUUID(), "alfred", null));
        playerManager.addPlayer(createPlayer(UUID.randomUUID(), "shell", null));
        playerManager.addPlayer(createPlayer(UUID.randomUUID(), "sherlock", null));
        playerManager.addPlayer(createPlayer(UUID.randomUUID(), "fred", null));
        
        assertNotNull(playerManager.getPlayer("alfred", false));
        
        assertNotNull(playerManager.getPlayer("she", false));
        // More significant match should win
        assertEquals("shell", playerManager.getPlayer("she", false).getName());
        
        assertEquals("fred", playerManager.getPlayer("fred", false).getName());
    }
    
    @Test
    public void testGetPlayerNickname() {
        PlayerManager playerManager = new PlayerManager(null, null, null, null) {};
        playerManager.addPlayer(createPlayer(UUID.randomUUID(), "alfred", "nickname"));
        playerManager.addPlayer(createPlayer(UUID.randomUUID(), "shell", "Shell"));
        playerManager.addPlayer(createPlayer(UUID.randomUUID(), "sherlock", "alf"));
        playerManager.addPlayer(createPlayer(UUID.randomUUID(), "fred", null));
        
        assertNotNull(playerManager.getPlayer("nickname", true));
        assertNotNull(playerManager.getPlayer("Shell", true));
        
        // Sherlock's nickname more significantly matches the input
        assertEquals("sherlock", playerManager.getPlayer("al", true).getName());
    }
}
