package net.cubespace.geSuit.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;
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
    
    @Test
    public void testGetPlayers() {
        PlayerManager playerManager = new PlayerManager(null, null, null, null) {};
        playerManager.addPlayer(createPlayer(UUID.randomUUID(), "alfred", "nickname"));
        GlobalPlayer shell = createPlayer(UUID.randomUUID(), "shell", "Shell");
        playerManager.addPlayer(shell);
        GlobalPlayer sherl = createPlayer(UUID.randomUUID(), "sherl", "alf");
        playerManager.addPlayer(sherl);
        playerManager.addPlayer(createPlayer(UUID.randomUUID(), "fred", null));
        
        List<GlobalPlayer> players = playerManager.getPlayers("she", true);
        
        assertEquals(2, players.size());
        assertTrue(players.contains(shell));
        assertTrue(players.contains(sherl));
    }
    
    @Test
    public void testGetAllPlayers() {
        PlayerManager playerManager = new PlayerManager(null, null, null, null) {};
        GlobalPlayer alfred = createPlayer(UUID.randomUUID(), "alfred", "nickname");
        GlobalPlayer shell = createPlayer(UUID.randomUUID(), "shell", "Shell");
        GlobalPlayer sherlock = createPlayer(UUID.randomUUID(), "sherlock", "alf");
        GlobalPlayer fred = createPlayer(UUID.randomUUID(), "fred", null);
        
        playerManager.addPlayer(alfred);
        playerManager.addPlayer(shell);
        playerManager.addPlayer(sherlock);
        playerManager.addPlayer(fred);
        
        Collection<GlobalPlayer> players = playerManager.getPlayers();
        
        assertEquals(4, players.size());
        assertTrue(players.contains(alfred));
        assertTrue(players.contains(shell));
        assertTrue(players.contains(sherlock));
        assertTrue(players.contains(fred));
    }
}
