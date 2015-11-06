package net.cubespace.geSuit.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.cubespace.geSuit.core.attachments.Attachment;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage;
import net.cubespace.geSuit.core.messages.SyncAttachmentMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Action;
import net.cubespace.geSuit.core.storage.RedisConnection;
import net.cubespace.geSuit.core.storage.StorageInterface;
import net.cubespace.geSuit.core.storage.StorageProvider;
import net.cubespace.geSuit.core.util.Utilities;

import org.junit.Test;
import org.mockito.Matchers;

import redis.clients.jedis.Jedis;

import com.google.common.collect.Maps;

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
        
        when(player.isReal()).thenReturn(true);
        
        return player;
    }
    @Test
    public void testAddPlayerNoNickname() {
        PlayerManager playerManager = new PlayerManager(null, null, mock(StorageProvider.class), null) {};
        
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
        PlayerManager playerManager = new PlayerManager(null, null, mock(StorageProvider.class), null) {};
        
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
        PlayerManager playerManager = new PlayerManager(null, null, mock(StorageProvider.class), null) {};
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
        PlayerManager playerManager = new PlayerManager(null, null, mock(StorageProvider.class), null) {};
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
        PlayerManager playerManager = new PlayerManager(null, null, mock(StorageProvider.class), null) {};
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
        PlayerManager playerManager = new PlayerManager(null, null, mock(StorageProvider.class), null) {};
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
    
    @Test
    public void testGetOfflinePlayerLoad() {
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, null, provider, null) {};
        
        UUID id = UUID.randomUUID();
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "testPlayer");
        values.put("nickname", "nick");
        
        StorageInterface storage = mock(StorageInterface.class);
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMapPartial(eq("info"), Matchers.<String>anyVararg())).thenReturn(values);
        
        when(provider.create(anyString())).thenReturn(storage);

        // Do the test
        GlobalPlayer player = playerManager.getOfflinePlayer(id);
        
        assertNotNull(player);
        assertEquals(id, player.getUniqueId());
        assertEquals("testPlayer", player.getName());
        assertEquals("nick", player.getNickname());
        assertFalse(player.isLoaded()); // Should not load the player on offline player retrieve
        
        // And test the cache
        GlobalPlayer samePlayer = playerManager.getOfflinePlayer(id);
        assertSame(player, samePlayer);
        
        verify(storage, times(1)).contains("info"); // Should have only loaded once
    }
    
    @Test
    public void testGetOfflinePlayerOnline() {
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, null, provider, null) {};
        
        reset(provider); // clear the create() call
        
        UUID id = UUID.randomUUID();
        GlobalPlayer onlinePlayer = createPlayer(id, "testPlayer", "nick");
        playerManager.addPlayer(onlinePlayer);

        // Do the test
        GlobalPlayer player = playerManager.getOfflinePlayer(id);
        
        assertSame(onlinePlayer, player);
        
        // Should not have contacted the backend
        verifyZeroInteractions(provider);
    }
    
    @Test
    public void testGetOfflinePlayerNameValid() {
        RedisConnection redis = mock(RedisConnection.class);
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, redis, provider, null) {};
        
        // Prepare the backend for the name query
        Jedis jedis = mock(Jedis.class);
        when(redis.getJedis()).thenReturn(jedis);
        
        // Prepare for the load
        UUID id = UUID.randomUUID();
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "testPlayer");
        values.put("nickname", "nick");
        
        StorageInterface storage = mock(StorageInterface.class);
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMapPartial(eq("info"), Matchers.<String>anyVararg())).thenReturn(values);
        
        when(provider.create(anyString())).thenReturn(storage);
        
        when(jedis.evalsha(anyString(), anyInt(), Matchers.<String>anyVararg())).thenReturn(Utilities.toString(id));

        // Do the test
        GlobalPlayer player = playerManager.getOfflinePlayer("testPlayer", true);
        
        assertNotNull(player);
        assertEquals(id, player.getUniqueId());
        assertFalse(player.isLoaded()); // Should not load the player on offline player retrieve
        
        // And test the cache
        GlobalPlayer samePlayer = playerManager.getOfflinePlayer(id);
        assertSame(player, samePlayer);
        
        verify(storage, times(1)).contains("info"); // Should have only loaded once
    }
    
    @Test
    public void testGetOfflinePlayerNameInvalid() {
        RedisConnection redis = mock(RedisConnection.class);
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, redis, provider, null) {};
        
        // Prepare the backend for the name query
        Jedis jedis = mock(Jedis.class);
        when(redis.getJedis()).thenReturn(jedis);
        
        // Prepare for the load
        Map<String, String> values = Maps.newHashMap();
        values.put("name", "testPlayer");
        values.put("nickname", "nick");
        
        StorageInterface storage = mock(StorageInterface.class);
        when(storage.contains("info")).thenReturn(true);
        when(storage.getMapPartial(eq("info"), Matchers.<String>anyVararg())).thenReturn(values);
        
        when(provider.create(anyString())).thenReturn(storage);
        
        when(jedis.evalsha(anyString(), eq(0), eq("testPlayer"), anyString())).thenReturn(null);

        // Do the test
        GlobalPlayer player = playerManager.getOfflinePlayer("testPlayer", true);
        assertNull(player);
        
        // This lookup should not be done again
        player = playerManager.getOfflinePlayer("testPlayer", true);
 
        verify(jedis, times(1)).evalsha(anyString(), eq(0), Matchers.<String>anyVararg());
    }
    
    @Test
    public void testGetOfflineNoLoad() {
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, null, provider, null) {};
        
        GlobalPlayer player = createPlayer(UUID.randomUUID(), "testPlayer", "nick");
        
        // Place the player in the offline cache
        playerManager.addPlayer(player);
        playerManager.removePlayer(player);
        
        // Do the test
        assertSame(player, playerManager.getOfflinePlayer(player.getUniqueId()));
        assertSame(player, playerManager.getOfflinePlayer("testPlayer", true));
        assertSame(player, playerManager.getOfflinePlayer("nick", true));
    }
    
    @Test
    public void testPlayerUpdateName() {
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, null, provider, mock(Platform.class)) {};
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), "testPlayer", "nick", playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player);
        
        // Do the test
        PlayerUpdateMessage message = new PlayerUpdateMessage(Action.Name, new PlayerUpdateMessage.Item(player.getUniqueId(), "testPlayer", "newNickname"));
        playerManager.handlePlayerUpdate(message);
        
        assertNull(playerManager.getPlayerExact("nick", true));
        assertSame(player, playerManager.getPlayerExact("newNickname", true));
    }
    
    @Test
    public void testPlayerUpdateNameOffline() {
        RedisConnection redis = mock(RedisConnection.class);
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, redis, provider, mock(Platform.class)) {};
        
        // Prepare the backend for the name query
        Jedis jedis = mock(Jedis.class);
        when(redis.getJedis()).thenReturn(jedis);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), "testPlayer", "nick", playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player);
        playerManager.removePlayer(player);
        
        // Do the test
        PlayerUpdateMessage message = new PlayerUpdateMessage(Action.Name, new PlayerUpdateMessage.Item(player.getUniqueId(), "testPlayer", "newNickname"));
        playerManager.handlePlayerUpdate(message);
        
        assertNull(playerManager.getOfflinePlayer("nick", true));
        assertSame(player, playerManager.getOfflinePlayer("newNickname", true));
    }
    
    @Test
    public void testPlayerInvalidate() {
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, null, provider, null) {};
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), "testPlayer", "nick", playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player);
        
        player.refresh();
        
        // Do the test
        assertTrue(player.isLoaded());
        
        PlayerUpdateMessage message = new PlayerUpdateMessage(Action.Invalidate, new PlayerUpdateMessage.Item(player.getUniqueId(), "testPlayer", "newNickname"));
        playerManager.handlePlayerUpdate(message);
        
        assertFalse(player.isLoaded());
    }
    
    @Test
    public void testPlayerUpdateInvalidateOffline() {
        RedisConnection redis = mock(RedisConnection.class);
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, redis, provider, null) {};
        
        // Prepare the backend for the name query
        Jedis jedis = mock(Jedis.class);
        when(redis.getJedis()).thenReturn(jedis);
        
        StorageInterface storage = mock(StorageInterface.class);
        when(provider.create(anyString())).thenReturn(storage);
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), "testPlayer", "nick", playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player);
        playerManager.removePlayer(player);
        
        player.refresh();
        
        // Do the test
        assertTrue(player.isLoaded());
        
        PlayerUpdateMessage message = new PlayerUpdateMessage(Action.Invalidate, new PlayerUpdateMessage.Item(player.getUniqueId(), "testPlayer", "newNickname"));
        playerManager.handlePlayerUpdate(message);
        
        assertFalse(player.isLoaded());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSetNicknameErrName() {
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(mock(Channel.class), null, provider, null) {};
        
        GlobalPlayer player1 = new GlobalPlayer(UUID.randomUUID(), "testPlayer", "nick", playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player1);
        
        GlobalPlayer player2 = new GlobalPlayer(UUID.randomUUID(), "nextPlayer", null, playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player2);
        
        // Do test
        playerManager.trySetNickname(player2, "testPlayer");
        // Should have errored due to having the same name as another player
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSetNicknameErrNickname() {
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(mock(Channel.class), null, provider, null) {};
        
        GlobalPlayer player1 = new GlobalPlayer(UUID.randomUUID(), "testPlayer", "nick", playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player1);
        
        GlobalPlayer player2 = new GlobalPlayer(UUID.randomUUID(), "nextPlayer", null, playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player2);
        
        // Do test
        playerManager.trySetNickname(player2, "nick");
        // Should have errored due to having the same name as another player's nickname
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSetNicknameErrSameName() {
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(mock(Channel.class), null, provider, null) {};
        
        GlobalPlayer player1 = new GlobalPlayer(UUID.randomUUID(), "testPlayer", "nick", playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player1);
        
        GlobalPlayer player2 = new GlobalPlayer(UUID.randomUUID(), "nextPlayer", null, playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player2);
        
        // Do test
        playerManager.trySetNickname(player2, "nextPlayer");
        // Should have errored due to being exactly the same as their name
    }
    
    @Test
    public void testSetNickname() {
        Channel<BaseMessage> channel = mock(Channel.class);
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(channel, null, provider, mock(Platform.class)) {};
        
        GlobalPlayer player1 = new GlobalPlayer(UUID.randomUUID(), "testPlayer", "nick", playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player1);
        
        GlobalPlayer player2 = new GlobalPlayer(UUID.randomUUID(), "nextPlayer", null, playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player2);
        
        // Do test
        playerManager.trySetNickname(player2, "newName");
        // Should not have errored
        
        assertSame(player2, playerManager.getPlayerExact("newName", true));
        verify(channel).broadcast(any(PlayerUpdateMessage.class));
    }
    
    @Test
    public void testSetNicknameChangeCase() {
        Channel<BaseMessage> channel = mock(Channel.class);
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(channel, null, provider, mock(Platform.class)) {};
        
        GlobalPlayer player1 = new GlobalPlayer(UUID.randomUUID(), "testPlayer", "nick", playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player1);
        
        GlobalPlayer player2 = new GlobalPlayer(UUID.randomUUID(), "nextPlayer", null, playerManager, mock(StorageInterface.class), null);
        playerManager.addPlayer(player2);
        
        // Do test
        playerManager.trySetNickname(player2, "NeXtPlAyEr");
        // Should be fine as case changes are allowed
        
        assertSame(player2, playerManager.getPlayerExact("NeXtPlAyEr", true));
        verify(channel).broadcast(any(PlayerUpdateMessage.class));
    }
    
    @Test
    public void testAttachmentSync() {
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, null, provider, null) {};
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), "testPlayer", null, playerManager, mock(StorageInterface.class), mock(Platform.class));
        player.addAttachment(new TestAttachment());
        playerManager.addPlayer(player);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("test", "value");
        
        // Do test
        SyncAttachmentMessage message = new SyncAttachmentMessage(player.getUniqueId(), TestAttachment.class, values);
        playerManager.handleSyncAttachment(message);
        
        TestAttachment attachment = player.getAttachment(TestAttachment.class);
        
        assertEquals(values, attachment.values);
    }
    
    @Test
    public void testAttachmentSyncOffline() {
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, null, provider, null) {};
        
        GlobalPlayer player = new GlobalPlayer(UUID.randomUUID(), "testPlayer", null, playerManager, mock(StorageInterface.class), mock(Platform.class));
        player.addAttachment(new TestAttachment());
        playerManager.addPlayer(player);
        playerManager.removePlayer(player);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("test", "value");
        
        // Do test
        SyncAttachmentMessage message = new SyncAttachmentMessage(player.getUniqueId(), TestAttachment.class, values);
        playerManager.handleSyncAttachment(message);
        
        TestAttachment attachment = player.getAttachment(TestAttachment.class);
        
        assertEquals(values, attachment.values);
    }
    
    @Test
    public void testAttachmentSyncInvalid() {
        StorageProvider provider = mock(StorageProvider.class);
        PlayerManager playerManager = new PlayerManager(null, null, provider, null) {};
        
        Map<String, String> values = Maps.newHashMap();
        values.put("test", "value");
        
        // Do test
        SyncAttachmentMessage message = new SyncAttachmentMessage(UUID.randomUUID(), TestAttachment.class, values);
        playerManager.handleSyncAttachment(message);
        
        // It should do nothing
    }
    
    @Test
    public void testInvalidate() {
        StorageProvider provider = mock(StorageProvider.class);
        Channel<BaseMessage> channel = mock(Channel.class);
        
        PlayerManager playerManager = new PlayerManager(channel, null, provider, null) {};
        
        UUID id = UUID.randomUUID();
        GlobalPlayer player = createPlayer(id, "testPlayer", "nick");
        playerManager.addPlayer(player);
        
        // Do test
        playerManager.invalidateOthers(player);
        
        verify(channel, times(1)).broadcast(any(PlayerUpdateMessage.class));
    }
    
    @Test
    public void testOnPlayerSave() {
        StorageProvider provider = mock(StorageProvider.class);
        
        StorageInterface storage = mock(StorageInterface.class);
        when(provider.create(anyString())).thenReturn(storage);
        
        PlayerManager playerManager = new PlayerManager(null, null, provider, null) {};
        
        UUID id = UUID.randomUUID();
        GlobalPlayer player = createPlayer(id, "testPlayer", "nick");
        playerManager.addPlayer(player);
        
        // Do test
        playerManager.onPlayerSave(player);
        
        verify(storage, times(1)).appendSet("all", id);
        verify(storage, times(1)).update();
    }
    
    public static class TestAttachment extends Attachment {
        public Map<String, String> values;
        
        @Override
        public void save(Map<String, String> values) {
        }

        @Override
        public void load(Map<String, String> values) {
            this.values = Maps.newHashMap(values);
        }

        @Override
        public AttachmentType getType() {
            return AttachmentType.Session;
        }
        
    }
}
