package net.cubespace.geSuit.moderation;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.cubespace.geSuit.config.ModerationConfig;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.objects.Tuple;
import net.cubespace.geSuit.general.BroadcastManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Iterables;
import com.google.common.net.InetAddresses;

public class TestMuteManager {
    private BroadcastManager broadcasts;
    private Messages messages;
    private ProxyServer proxy;
    private PlayerManager playerManager;
    
    private MuteManager manager;
    
    @Before
    public void initialize() {
        broadcasts = mock(BroadcastManager.class);
        messages = mock(Messages.class);
        proxy = mock(ProxyServer.class);
        playerManager = mock(PlayerManager.class);
        
        manager = new MuteManager(broadcasts, messages, proxy, playerManager);
        
        when(messages.get(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgumentAt(0, String.class);
            }
        });
        
        when(messages.get(anyString(), anyVararg())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgumentAt(0, String.class);
            }
        });
        
        manager.loadConfig(new ModerationConfig(null));
    }
    
    @Test
    public void testNotActive() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getAddress()).thenReturn(new InetSocketAddress(ip, 12345));
        
        GlobalPlayer gplayer = mock(GlobalPlayer.class);
        when(gplayer.getUniqueId()).thenReturn(playerId);
        
        when(playerManager.getPlayer(playerId)).thenReturn(gplayer);
        
        // Without the mute active, player must be able to chat
        assertTrue(manager.checkAllowChat(player, false, ""));
        assertTrue(manager.checkAllowChat(player, true, "/command"));
    }
    
    @Test
    public void testActiveGlobal() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getAddress()).thenReturn(new InetSocketAddress(ip, 12345));
        
        GlobalPlayer gplayer = mock(GlobalPlayer.class);
        when(gplayer.getUniqueId()).thenReturn(playerId);
        
        when(playerManager.getPlayer(playerId)).thenReturn(gplayer);
        
        manager.enableGlobalMute("");
        
        // Without bypass
        assertFalse(manager.checkAllowChat(player, false, ""));
        assertFalse(manager.checkAllowChat(player, true, "/command"));
        
        // With bypass
        when(player.hasPermission("gesuit.mute.global.exempt")).thenReturn(true);
        
        assertTrue(manager.checkAllowChat(player, false, ""));
        assertTrue(manager.checkAllowChat(player, true, "/command"));
    }
    
    @Test
    public void testActiveCommandWhitelisted() {
        ModerationConfig config = new ModerationConfig(null);
        config.Mutes.CommandListIsWhitelist = true;
        config.Mutes.CommandsList.add("command");
        manager.loadConfig(config);;
        
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getAddress()).thenReturn(new InetSocketAddress(ip, 12345));
        
        GlobalPlayer gplayer = mock(GlobalPlayer.class);
        when(gplayer.getUniqueId()).thenReturn(playerId);
        
        when(playerManager.getPlayer(playerId)).thenReturn(gplayer);
        
        manager.enableGlobalMute("");
        
        // Should be blocked
        assertFalse(manager.checkAllowChat(player, false, ""));
        // Should be allowed due to whitelist
        assertTrue(manager.checkAllowChat(player, true, "/command"));
    }
    
    @Test
    public void testActivePlayer() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getAddress()).thenReturn(new InetSocketAddress(ip, 12345));
        
        GlobalPlayer gplayer = mock(GlobalPlayer.class);
        when(gplayer.getUniqueId()).thenReturn(playerId);
        
        when(playerManager.getPlayer(playerId)).thenReturn(gplayer);
        
        manager.mute(gplayer, "");
        
        assertFalse(manager.checkAllowChat(player, false, ""));
        assertFalse(manager.checkAllowChat(player, true, "/command"));
    }
    
    @Test
    public void testActiveIP() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getAddress()).thenReturn(new InetSocketAddress(ip, 12345));
        
        GlobalPlayer gplayer = mock(GlobalPlayer.class);
        when(gplayer.getUniqueId()).thenReturn(playerId);
        
        when(playerManager.getPlayer(playerId)).thenReturn(gplayer);
        
        // Second player with different IP
        InetAddress ip2 = InetAddresses.forString("127.0.0.2");
        
        ProxiedPlayer player2 = mock(ProxiedPlayer.class);
        UUID playerId2 = UUID.randomUUID();
        when(player2.getUniqueId()).thenReturn(playerId2);
        when(player2.getAddress()).thenReturn(new InetSocketAddress(ip2, 12345));
        
        GlobalPlayer gplayer2 = mock(GlobalPlayer.class);
        when(gplayer2.getUniqueId()).thenReturn(playerId2);
        
        when(playerManager.getPlayer(playerId2)).thenReturn(gplayer2);
        
        manager.mute(ip, "");
        
        assertFalse(manager.checkAllowChat(player, false, ""));
        assertFalse(manager.checkAllowChat(player, true, "/command"));
        
        assertTrue(manager.checkAllowChat(player2, false, ""));
        assertTrue(manager.checkAllowChat(player2, true, "/command"));
    }
    
    @Test
    public void testGlobalMute() {
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        
        Result result = manager.enableGlobalMute("");
        
        assertEquals(Result.Type.Success, result.getType());
        
        // Make sure its active
        assertEquals(new Tuple<Boolean, Long>(true, MuteManager.Permanent), manager.getGlobalMute());
    }
    
    @Test
    public void testGlobalMuteAlreadyActive() {
        ModerationConfig config = new ModerationConfig(null);
        config.Mutes.AllowReMute = false;
        manager.loadConfig(config);
        
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        
        manager.enableGlobalMute("");
        Result result = manager.enableGlobalMute("");
        
        assertEquals(Result.Type.Fail, result.getType());
    }
    
    @Test
    public void testGlobalMuteDisable() {
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        
        manager.enableGlobalMute("");
        
        Result result = manager.disableGlobalMute();
        
        assertEquals(Result.Type.Success, result.getType());
        
        assertEquals(new Tuple<Boolean, Long>(false, 0L), manager.getGlobalMute());
    }
    
    @Test
    public void testMutePerm() {
        ModerationConfig config = new ModerationConfig(null);
        config.Mutes.AllowPermanentMutes = true;
        manager.loadConfig(config);
        
        GlobalPlayer player = mock(GlobalPlayer.class);
        UUID id = UUID.randomUUID();
        
        when(player.getUniqueId()).thenReturn(id);
        
        Result result = manager.mute(player, "");
        
        assertEquals(Result.Type.Success, result.getType());
        
        Map<UUID, Long> muted = manager.getMutedPlayers();
        assertEquals((Long)MuteManager.Permanent, muted.get(id));
    }
    
    @Test
    public void testMuteTemp() {
        GlobalPlayer player = mock(GlobalPlayer.class);
        UUID id = UUID.randomUUID();
        
        when(player.getUniqueId()).thenReturn(id);
        
        long until = System.currentTimeMillis() + 1000;
        Result result = manager.mute(player, until, "");
        
        assertEquals(Result.Type.Success, result.getType());
        
        Map<UUID, Long> muted = manager.getMutedPlayers();
        assertEquals((Long)until, muted.get(id));
    }
    
    @Test
    public void testMuteTempTooLong() {
        ModerationConfig config = new ModerationConfig(null);
        config.Mutes.MaximumMuteDuration = "10s";
        manager.loadConfig(config);
        
        GlobalPlayer player = mock(GlobalPlayer.class);
        UUID id = UUID.randomUUID();
        
        when(player.getUniqueId()).thenReturn(id);
        
        Result result = manager.mute(player, System.currentTimeMillis() + 10000000L, "");
        
        assertEquals(Result.Type.Fail, result.getType());
    }
    
    @Test
    public void testMuteIPPerm() {
        ModerationConfig config = new ModerationConfig(null);
        config.Mutes.AllowPermanentMutes = true;
        manager.loadConfig(config);
        
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        Result result = manager.mute(ip, "");
        
        assertEquals(Result.Type.Success, result.getType());
        
        Map<Tuple<InetAddress, String>, Long> muted = manager.getMutedIPs();
        Entry<Tuple<InetAddress, String>, Long> entry = Iterables.getFirst(muted.entrySet(), null);
        
        assertNotNull(entry);
        assertEquals(ip, entry.getKey().getA());
        
        assertEquals((Long)MuteManager.Permanent, entry.getValue());
    }
    
    @Test
    public void testMuteIPTemp() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        long until = System.currentTimeMillis() + 1000;
        Result result = manager.mute(ip, until, "");
        
        assertEquals(Result.Type.Success, result.getType());
        
        Map<Tuple<InetAddress, String>, Long> muted = manager.getMutedIPs();
        Entry<Tuple<InetAddress, String>, Long> entry = Iterables.getFirst(muted.entrySet(), null);
        
        assertNotNull(entry);
        assertEquals(ip, entry.getKey().getA());
        
        assertEquals((Long)until, entry.getValue());
    }
    
    @Test
    public void testMuteIPTempTooLong() {
        ModerationConfig config = new ModerationConfig(null);
        config.Mutes.MaximumMuteDuration = "10s";
        manager.loadConfig(config);
        
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        Result result = manager.mute(ip, System.currentTimeMillis() + 100000000L, "");
        
        assertEquals(Result.Type.Fail, result.getType());
    }
    
    @Test
    public void testExpirePlayerMutes() throws InterruptedException {
        GlobalPlayer player = mock(GlobalPlayer.class);
        UUID id = UUID.randomUUID();
        
        when(player.getUniqueId()).thenReturn(id);
        
        long until = System.currentTimeMillis() + 10;
        Result result = manager.mute(player, until, "");
        
        assertEquals(Result.Type.Success, result.getType());
        
        manager.expireAnyMutes();
        
        // Should not have been expired yet
        Map<UUID, Long> muted = manager.getMutedPlayers();
        assertEquals((Long)until, muted.get(id));
        
        Thread.sleep(11);
        
        manager.expireAnyMutes();
        
        // Should have been expired
        muted = manager.getMutedPlayers();
        assertNull(muted.get(id));
    }
    
    @Test
    public void testExpireIPMutes() throws InterruptedException {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        long until = System.currentTimeMillis() + 10;
        Result result = manager.mute(ip, until, "");
        
        assertEquals(Result.Type.Success, result.getType());
        
        manager.expireAnyMutes();
        
        // Should not have expired anything
        Map<Tuple<InetAddress, String>, Long> muted = manager.getMutedIPs();
        Entry<Tuple<InetAddress, String>, Long> entry = Iterables.getFirst(muted.entrySet(), null);
        
        assertNotNull(entry);
        assertEquals(ip, entry.getKey().getA());
        
        Thread.sleep(11);
        
        manager.expireAnyMutes();
        
        // Should have been expired
        muted = manager.getMutedIPs();
        entry = Iterables.getFirst(muted.entrySet(), null);
        
        assertNull(entry);
    }
    
    @Test
    public void testExpireGlobalMute() throws InterruptedException {
        long until = System.currentTimeMillis() + 10;
        Result result = manager.enableGlobalMute(until, "");
        
        assertEquals(Result.Type.Success, result.getType());
        
        manager.expireAnyMutes();
        
        // Should not have been expired yet
        Tuple<Boolean, Long> status = manager.getGlobalMute();
        assertEquals(true, status.getA());
        
        Thread.sleep(11);
        
        manager.expireAnyMutes();
        
        // Should have been expired
        status = manager.getGlobalMute();
        assertEquals(false, status.getA());
    }
}
