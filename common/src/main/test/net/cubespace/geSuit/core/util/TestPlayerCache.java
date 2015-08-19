package net.cubespace.geSuit.core.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.cubespace.geSuit.core.GlobalPlayer;

import org.junit.Test;

public class TestPlayerCache {
    private GlobalPlayer makePlayer(UUID id, String name, String nickname) {
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getName()).thenReturn(name);
        when(player.getUniqueId()).thenReturn(id);
        when(player.hasNickname()).thenReturn(nickname != null);
        when(player.getNickname()).thenReturn(nickname);
        
        return player;
    }
    
    @Test
    public void testAdd() {
        PlayerCache cache = new PlayerCache(TimeUnit.MINUTES.toMillis(1));
        
        UUID id = UUID.randomUUID();
        GlobalPlayer player = makePlayer(id, "test", null);
        
        cache.add(player);
        
        assertEquals(player, cache.get(id));
    }
    
    @Test
    public void testGetByName() {
        PlayerCache cache = new PlayerCache(TimeUnit.MINUTES.toMillis(1));
        
        UUID id = UUID.randomUUID();
        GlobalPlayer player = makePlayer(id, "test", null);
        
        cache.add(player);
        
        assertEquals(player, cache.getFromName("test", false));
    }
    
    @Test
    public void testGetByNickname() {
        PlayerCache cache = new PlayerCache(TimeUnit.MINUTES.toMillis(1));
        
        UUID id = UUID.randomUUID();
        GlobalPlayer player = makePlayer(id, "test", "abcde");
        
        cache.add(player);
        
        assertEquals(player, cache.getFromName("abcde", true));
    }
    
    @Test
    public void testNicknameChange() {
        PlayerCache cache = new PlayerCache(TimeUnit.MINUTES.toMillis(1));
        
        UUID id = UUID.randomUUID();
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getName()).thenReturn("test");
        when(player.getUniqueId()).thenReturn(id);
        when(player.hasNickname()).thenReturn(true);
        when(player.getNickname()).thenReturn("abcde").thenReturn("newnickname");
        
        cache.add(player);
        
        assertEquals(player, cache.getFromName("abcde", true));
        
        cache.onUpdateNickname(player, "abcde");
        
        assertNull(cache.getFromName("abcde", true));
        assertEquals(player, cache.getFromName("newnickname", true));
    }
    
    @Test
    public void textExpire() throws InterruptedException {
        PlayerCache cache = new PlayerCache(TimeUnit.MILLISECONDS.toMillis(1));
        
        UUID id = UUID.randomUUID();
        GlobalPlayer player = makePlayer(id, "test", "nick");
        
        cache.add(player);
        assertEquals(player, cache.get(id));
        
        Thread.sleep(10);
        
        // Should be removed
        assertNull(cache.get(id));
        assertNull(cache.getFromName("test", true));
        assertNull(cache.getFromName("nick", true));
    }
    
    @Test
    public void testRemote() {
        PlayerCache cache = new PlayerCache(TimeUnit.MINUTES.toMillis(1));
        
        UUID id = UUID.randomUUID();
        GlobalPlayer player = makePlayer(id, "test", "nick");
        
        cache.add(player);
        
        cache.remove(player);
        
        // Should be removed
        assertNull(cache.get(id));
        assertNull(cache.getFromName("test", true));
        assertNull(cache.getFromName("nick", true));
    }
}
