package net.cubespace.geSuit.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;

import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.core.storage.StorageInterface;
import net.cubespace.geSuit.core.util.Utilities;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;

@SuppressWarnings({"rawtypes","unchecked"})
public class TestGlobalPlayer {
    @Test
    public void testLoad() {
        UUID id = UUID.randomUUID();
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "test");
        values.put("banned", "false");
        values.put("tp-enable", "true");
        values.put("new-player", "true");
        values.put("ip", "127.0.0.1");
        
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMap("info")).thenReturn(values);
        when(storage.getSetString("attachments")).thenReturn(Sets.<String>newHashSet());
        
        
        GlobalPlayer player = new GlobalPlayer(id, "test", null, manager, storage, null);
        
        player.refresh();
        
        assertEquals(false, player.isBanned());
        assertEquals(true, player.hasTPsEnabled());
        assertEquals(true, player.isNewPlayer());
        assertEquals(InetAddresses.forString("127.0.0.1"), player.getAddress());
    }
    
    @Test
    public void testLoadWithOptionals() {
        UUID id = UUID.randomUUID();
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "test");
        values.put("nickname", "nick");
        values.put("banned", "false");
        values.put("tp-enable", "true");
        values.put("new-player", "true");
        values.put("ip", "127.0.0.1");
        values.put("first-join", "2015-08-26 09:45:00");
        values.put("last-join", "2015-08-26 13:30:00");
        
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMap("info")).thenReturn(values);
        when(storage.getSetString("attachments")).thenReturn(Sets.<String>newHashSet());
        
        
        GlobalPlayer player = new GlobalPlayer(id, "test", null, manager, storage, null);
        
        player.refresh();
        
        assertEquals(false, player.isBanned());
        assertEquals(true, player.hasTPsEnabled());
        assertEquals(true, player.isNewPlayer());
        assertEquals(InetAddresses.forString("127.0.0.1"), player.getAddress());
        assertEquals(Utilities.parseDate("2015-08-26 09:45:00"), player.getFirstJoined());
        assertEquals(Utilities.parseDate("2015-08-26 13:30:00"), player.getLastOnline());
    }
    
    @Test
    public void testLoadWithBanInfo() {
        UUID id = UUID.randomUUID();
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "test");
        values.put("banned", "true");
        values.put("tp-enable", "true");
        values.put("new-player", "true");
        values.put("ip", "127.0.0.1");
        
        BanInfo<GlobalPlayer> temp = new BanInfo<GlobalPlayer>(null, 0, "test", "whoby", null, 12345L, 0L, false);
        final Map<String, String> banValues = Maps.newHashMap();
        temp.save(banValues);
        
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMap("info")).thenReturn(values);
        when(storage.getStorable(eq("baninfo"), any(BanInfo.class))).then(new Answer<BanInfo<GlobalPlayer>>() {
            @Override
            public BanInfo<GlobalPlayer> answer(InvocationOnMock invocation) throws Throwable {
                BanInfo arg = invocation.getArgumentAt(1, BanInfo.class);
                
                arg.load(banValues);
                return arg;
            }
        });
        
        when(storage.getSetString("attachments")).thenReturn(Sets.<String>newHashSet());
        
        GlobalPlayer player = new GlobalPlayer(id, "test", null, manager, storage, null);
        
        player.refresh();
        
        assertEquals(true, player.isBanned());
        assertNotNull(player.getBanInfo());
        assertEquals("test", player.getBanInfo().getReason());
        assertEquals("whoby", player.getBanInfo().getBannedBy());
    }
    
    @Test
    public void testSave() {
        UUID id = UUID.randomUUID();
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "test");
        values.put("banned", "false");
        values.put("tp-enable", "true");
        values.put("new-player", "true");
        values.put("ip", "127.0.0.1");
        
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMap("info")).thenReturn(values);
        when(storage.getSetString("attachments")).thenReturn(Sets.<String>newHashSet());
        
        
        GlobalPlayer player = new GlobalPlayer(id, "test", null, manager, storage, null);
        
        player.refresh();
        player.save();
        
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        
        verify(storage, times(1)).set(eq("info"), captor.capture());
        
        assertEquals(values, captor.getValue());
    }
    
    @Test
    public void testSaveWithOptionals() {
        UUID id = UUID.randomUUID();
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "test");
        values.put("nickname", "nick");
        values.put("banned", "false");
        values.put("tp-enable", "true");
        values.put("new-player", "true");
        values.put("ip", "127.0.0.1");
        values.put("first-join", "2015-08-26 09:45:00");
        values.put("last-join", "2015-08-26 13:30:00");
        
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMap("info")).thenReturn(values);
        when(storage.getSetString("attachments")).thenReturn(Sets.<String>newHashSet());
        
        
        GlobalPlayer player = new GlobalPlayer(id, "test", null, manager, storage, null);
        
        player.refresh();
        player.save();
        
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        
        verify(storage, times(1)).set(eq("info"), captor.capture());
        
        assertEquals(values, captor.getValue());
    }
    
    @Test
    public void testSaveWithBanInfo() {
        UUID id = UUID.randomUUID();
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "test");
        values.put("banned", "false");
        values.put("tp-enable", "true");
        values.put("new-player", "true");
        values.put("ip", "127.0.0.1");
        
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMap("info")).thenReturn(values);
        when(storage.getSetString("attachments")).thenReturn(Sets.<String>newHashSet());
        
        
        GlobalPlayer player = new GlobalPlayer(id, "test", null, manager, storage, null);
        
        BanInfo<GlobalPlayer> ban = new BanInfo<GlobalPlayer>(player, 0, "test", "whoby", null, 12345L, 0L, false);
        Map<String, String> banValues = Maps.newHashMap();
        ban.save(banValues);
        
        player.refresh();
        
        // Begin test
        player.setBan(ban);
        player.save();
        
        values.put("banned", "true");
        
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        
        verify(storage, times(1)).set(eq("info"), captor.capture());
        assertEquals(values, captor.getValue());
        
        ArgumentCaptor<BanInfo> storableCaptor = ArgumentCaptor.forClass(BanInfo.class);
        
        verify(storage, times(1)).set(eq("baninfo"), storableCaptor.capture());
        assertEquals(ban, storableCaptor.getValue());
    }

    @Test
    public void testLiteLoad() {
        UUID id = UUID.randomUUID();
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "test");
        
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMapPartial("info", "name", "nickname")).thenReturn(values);
        
        GlobalPlayer player = new GlobalPlayer(id, manager, storage, null);
        
        player.loadLite();
        
        assertEquals("test", player.getName());
    }
    
    @Test
    public void testLiteLoadWithNickname() {
        UUID id = UUID.randomUUID();
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "test");
        values.put("nickname", "nick");
        
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMapPartial("info", "name", "nickname")).thenReturn(values);
        
        GlobalPlayer player = new GlobalPlayer(id, manager, storage, null);
        
        player.loadLite();
        
        assertEquals("test", player.getName());
        assertEquals("nick", player.getNickname());
    }
    
    @Test
    public void testDisplayName() {
        PlayerManager manager = mock(PlayerManager.class);
        
        GlobalPlayer player1 = new GlobalPlayer(UUID.randomUUID(), "player1", null, manager, null, null);
        GlobalPlayer player2 = new GlobalPlayer(UUID.randomUUID(), "player1", "player2", manager, null, null);
        
        assertFalse(player1.hasNickname());
        assertEquals("player1", player1.getDisplayName());
        assertTrue(player2.hasNickname());
        assertEquals("player2", player2.getDisplayName());
    }
    
    @Test
    public void testLoadOnGetAddress() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.getAddress(); // should load
        player.getAddress(); // should not
        
        assertTrue(player.isLoaded());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnSetAddress() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.setAddress(null); // should load
        player.setAddress(null); // should not
        
        assertTrue(player.isLoaded());
        assertTrue(player.isDirty());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnGetLastOnline() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.getLastOnline(); // should load
        player.getLastOnline(); // should not
        
        assertTrue(player.isLoaded());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnSetLastOnline() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.setLastOnline(0); // should load
        player.setLastOnline(0); // should not
        
        assertTrue(player.isLoaded());
        assertTrue(player.isDirty());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnHasTPsEnabled() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.hasTPsEnabled(); // should load
        player.hasTPsEnabled(); // should not
        
        assertTrue(player.isLoaded());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnSetTPsEnabled() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.setTPsEnabled(false); // should load
        player.setTPsEnabled(false); // should not
        
        assertTrue(player.isLoaded());
        assertTrue(player.isDirty());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnIsNewPlayer() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.isNewPlayer(); // should load
        player.isNewPlayer(); // should not
        
        assertTrue(player.isLoaded());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnSetNewPlayer() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.setNewPlayer(false); // should load
        player.setNewPlayer(false); // should not
        
        assertTrue(player.isLoaded());
        assertTrue(player.isDirty());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnGetBanInfo() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.getBanInfo(); // should load
        player.getBanInfo(); // should not
        
        assertTrue(player.isLoaded());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnSetBan() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        BanInfo<GlobalPlayer> ban = new BanInfo<GlobalPlayer>(player);
        
        player.setBan(ban); // should load
        player.setBan(ban); // should not
        
        assertTrue(player.isLoaded());
        assertTrue(player.isDirty());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnRemoveBan() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.removeBan(); // should load
        player.removeBan(); // should not
        
        assertTrue(player.isLoaded());
        assertTrue(player.isDirty());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnHasPlayedBefore() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.hasPlayedBefore(); // should load
        player.hasPlayedBefore(); // should not
        
        assertTrue(player.isLoaded());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadOnSetSessionJoin() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.setSessionJoin(0); // should load
        player.setSessionJoin(0); // should not
        
        assertTrue(player.isLoaded());
        verify(storage, times(1)).contains("info");
    }
    
    @Test
    public void testLoadStatus() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        assertFalse(player.isLoaded());
        player.refresh();
        assertTrue(player.isLoaded());
        player.invalidate();
        assertFalse(player.isLoaded());
    }
    
    @Test
    public void testDirtyStatus() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.refresh();
        assertFalse(player.isDirty());
        player.markDirty();
        assertTrue(player.isDirty());
    }
    
    @Test
    public void testSaveIfModifiedClean() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.refresh();
        reset(storage); // Clean state for the test
        
        // Do the test
        player.saveIfModified();
        
        verifyZeroInteractions(storage);
    }
    
    @Test
    public void testSaveIfModifiedDirty() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "test");
        values.put("banned", "false");
        values.put("tp-enable", "true");
        values.put("new-player", "true");
        values.put("ip", "127.0.0.1");
        
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMap("info")).thenReturn(values);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.refresh();
        reset(storage); // Clean state for the test
        
        // Do the test
        player.markDirty();
        player.saveIfModified();
        
        verify(storage).set(eq("info"), any(Map.class));
    }
    
    @Test
    public void testSetNickname() {
        PlayerManager manager = mock(PlayerManager.class);
        StorageInterface storage = mock(StorageInterface.class);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), manager, storage, null);
        
        player.setNickname("test");
        
        verify(manager).trySetNickname(player, "test");
    }
}
