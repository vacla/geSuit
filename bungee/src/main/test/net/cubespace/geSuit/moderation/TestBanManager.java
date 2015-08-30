package net.cubespace.geSuit.moderation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import net.cubespace.geSuit.config.ModerationConfig;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.Platform;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.moderation.GlobalBanEvent;
import net.cubespace.geSuit.core.events.moderation.GlobalUnbanEvent;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.FireBanEventMessage;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.storage.Storable;
import net.cubespace.geSuit.core.storage.StorageInterface;
import net.cubespace.geSuit.core.storage.StorageProvider;
import net.cubespace.geSuit.database.repositories.BanHistory;
import net.cubespace.geSuit.general.BroadcastManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.hamcrest.CustomMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import com.google.common.net.InetAddresses;

public class TestBanManager {
    private BanHistory repo;
    private BroadcastManager broadcasts;
    private Channel<BaseMessage> channel;
    private Messages messages;
    private StorageProvider provider;
    private ProxyServer proxy;
    private Platform platform;
    
    private StorageInterface ipBansSection;
    
    private BanManager manager;
    
    @Before
    public void initialize() {
        // Initialize the ban manager
        repo = mock(BanHistory.class);
        broadcasts = mock(BroadcastManager.class);
        channel = mock(Channel.class);
        messages = mock(Messages.class);
        provider = mock(StorageProvider.class);
        proxy = mock(ProxyServer.class);
        platform = mock(Platform.class);
        
        ipBansSection = mock(StorageInterface.class);
        when(provider.create(anyString())).thenReturn(ipBansSection);
        
        when(messages.get(anyString())).thenReturn("*message*");
        when(messages.get(anyString(), anyVararg())).thenReturn("*message*");
        
        manager = new BanManager(repo, broadcasts, channel, messages, provider, proxy, platform);
        manager.loadConfig(new ModerationConfig(null));
        
        // Clear the init stuff
        reset(provider, platform);
    }
    
    @Test
    public void testPlayerBanNew() throws SQLException {
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");

        // Do the ban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.banUntil(player, "test reason", Long.MAX_VALUE, "CONSOLE", whoBy, true);
        
        // Asserts
        assertEquals(Result.Type.Success, result.getType());
        
        // Make sure it was saved in ban history
        final ArgumentCaptor<BanInfo> banCaptor = ArgumentCaptor.forClass(BanInfo.class);
        verify(repo, times(1)).recordBan(banCaptor.capture());
        
        assertEquals(player, banCaptor.getValue().getWho());
        assertEquals("test reason", banCaptor.getValue().getReason());
        assertEquals("CONSOLE", banCaptor.getValue().getBannedBy());
        assertEquals(whoBy, banCaptor.getValue().getBannedById());
        assertEquals(Long.MAX_VALUE, banCaptor.getValue().getUntil());
        
        // Since its a new ban there should not be an unban
        verify(repo, never()).recordUnban(any(BanInfo.class), anyString(), anyString(), any(UUID.class));
        
        // Make sure it was saved in redis
        verify(player, times(1)).setBan(banCaptor.getValue());
        
        // Check the event
        verify(platform, times(1)).callEvent(any(GlobalBanEvent.class));
        verify(channel, times(1)).broadcast(argThat(new CustomMatcher<FireBanEventMessage>("ban event") {
            @Override
            public boolean matches(Object item) {
                FireBanEventMessage msg = (FireBanEventMessage)item;
                return (
                        msg.address == null &&
                        msg.auto &&
                        !msg.isUnban &&
                        msg.ban.equals(banCaptor.getValue())
                        );
            }
        }));
    }
    
    @Test
    public void testPlayerBanExistingPerm() throws SQLException {
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        
        BanInfo<GlobalPlayer> existing = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, 0, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(existing);

        // Do the ban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.banUntil(player, "test reason", Long.MAX_VALUE, "CONSOLE", whoBy, true);
        
        // Asserts
        assertEquals(Result.Type.Fail, result.getType());
        
        verifyZeroInteractions(repo, platform, channel);
    }
    
    @Test
    public void testPlayerBanExistingTemp() throws SQLException {
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        
        BanInfo<GlobalPlayer> existing = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, Long.MAX_VALUE, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(existing);

        // Do the ban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.banUntil(player, "test reason", Long.MAX_VALUE, "CONSOLE", whoBy, true);
        
        // Asserts
        assertEquals(Result.Type.Success, result.getType());
        
        // Make sure it was saved in ban history
        final ArgumentCaptor<BanInfo> banCaptor = ArgumentCaptor.forClass(BanInfo.class);
        verify(repo, times(1)).recordBan(banCaptor.capture());
        
        assertEquals(player, banCaptor.getValue().getWho());
        assertEquals("test reason", banCaptor.getValue().getReason());
        assertEquals("CONSOLE", banCaptor.getValue().getBannedBy());
        assertEquals(whoBy, banCaptor.getValue().getBannedById());
        assertEquals(Long.MAX_VALUE, banCaptor.getValue().getUntil());
        
        // Since the existing ban was active, the old ban should be revoked
        verify(repo, times(1)).recordUnban(eq(existing), anyString(), anyString(), any(UUID.class));
    }
    
    @Test
    public void testUnban() throws SQLException {
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        
        final BanInfo<GlobalPlayer> existing = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, Long.MAX_VALUE, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(existing);

        // Do the unban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.unban(player, "test reason", "CONSOLE", whoBy);
        
        // Asserts
        assertEquals(Result.Type.Success, result.getType());
        
        // Make sure it was saved in ban history
        verify(repo, times(1)).recordUnban(existing, "test reason", "CONSOLE", whoBy);
        
        // Make sure it was saved in redis
        verify(player, times(1)).setBan(null);
        
        // Check the event
        verify(platform, times(1)).callEvent(any(GlobalUnbanEvent.class));
        verify(channel, times(1)).broadcast(argThat(new CustomMatcher<FireBanEventMessage>("ban event") {
            @Override
            public boolean matches(Object item) {
                FireBanEventMessage msg = (FireBanEventMessage)item;
                return (
                        msg.address == null &&
                        !msg.auto &&
                        msg.isUnban &&
                        msg.ban.equals(existing)
                        );
            }
        }));
    }
    
    @Test
    public void testUnbanNoExisting() throws SQLException {
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");

        // Do the unban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.unban(player, "test reason", "CONSOLE", whoBy);
        
        // Asserts
        assertEquals(Result.Type.Fail, result.getType());
        
        verify(repo, never()).recordUnban(any(BanInfo.class), eq("test reason"), eq("CONSOLE"), eq(whoBy));
        verify(player, never()).setBan(null);
        
        // Check the event
        verify(platform, never()).callEvent(any(GlobalUnbanEvent.class));
        verify(channel, never()).broadcast(any(FireBanEventMessage.class));
    }
    
    @Test
    public void testIPBanNew() throws SQLException {
        final InetAddress ip = InetAddresses.forString("127.0.0.1");
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        when(player.getAddress()).thenReturn(ip);

        // Do the ban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.ipban(player, "test reason", "CONSOLE", whoBy, true);
        
        // Asserts
        assertEquals(Result.Type.Success, result.getType());
        
        // Make sure it was saved in ban history
        final ArgumentCaptor<BanInfo> banCaptor = ArgumentCaptor.forClass(BanInfo.class);
        verify(repo, times(2)).recordBan(banCaptor.capture());
        
        final BanInfo<GlobalPlayer> playerBan = banCaptor.getAllValues().get(0);
        BanInfo<InetAddress> ipBan = banCaptor.getAllValues().get(1);
        
        assertEquals(player, playerBan.getWho());
        assertEquals("test reason", playerBan.getReason());
        assertEquals("CONSOLE", playerBan.getBannedBy());
        assertEquals(whoBy, playerBan.getBannedById());
        
        assertEquals(ip, ipBan.getWho());
        assertEquals("test reason", ipBan.getReason());
        assertEquals("CONSOLE", ipBan.getBannedBy());
        assertEquals(whoBy, ipBan.getBannedById());
        
        // Since its a new ban there should not be an unban
        verify(repo, never()).recordUnban(any(BanInfo.class), anyString(), anyString(), any(UUID.class));
        
        // Make sure it was saved in redis
        verify(player, times(1)).setBan(playerBan);
        verify(ipBansSection, times(1)).set(anyString(), eq(ipBan));
        
        // Check the event
        verify(platform, times(1)).callEvent(any(GlobalBanEvent.class));
        verify(channel, times(1)).broadcast(argThat(new CustomMatcher<FireBanEventMessage>("ban event") {
            @Override
            public boolean matches(Object item) {
                FireBanEventMessage msg = (FireBanEventMessage)item;
                return (
                        msg.address.equals(ip) &&
                        msg.auto &&
                        !msg.isUnban &&
                        msg.ban.equals(playerBan)
                        );
            }
        }));
    }
    
    @Test
    public void testIPBanExistingPlayerBan() throws SQLException {
        final InetAddress ip = InetAddresses.forString("127.0.0.1");
        final GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        when(player.getAddress()).thenReturn(ip);
        
        final BanInfo<GlobalPlayer> existing = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, 0, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(existing);

        // Do the ban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.ipban(player, "test reason", "CONSOLE", whoBy, true);
        
        // Asserts
        assertEquals(Result.Type.Success, result.getType());
        
        // Make sure it was saved in ban history
        final ArgumentCaptor<BanInfo> banCaptor = ArgumentCaptor.forClass(BanInfo.class);
        verify(repo, times(1)).recordBan(banCaptor.capture()); // since player ban is already active, shouldnt be a player ban
        
        BanInfo<InetAddress> ipBan = banCaptor.getAllValues().get(0);
        
        assertEquals(ip, ipBan.getWho());
        assertEquals("test reason", ipBan.getReason());
        assertEquals("CONSOLE", ipBan.getBannedBy());
        assertEquals(whoBy, ipBan.getBannedById());
        
        // The ban should not have overridden the existing player ban
        verify(repo, never()).recordUnban(any(BanInfo.class), anyString(), anyString(), any(UUID.class));
        
        // Make sure it was saved in redis
        verify(player, never()).setBan(any(BanInfo.class));
        verify(ipBansSection, times(1)).set(anyString(), eq(ipBan));
        
        // Check the event
        verify(platform, times(1)).callEvent(any(GlobalBanEvent.class));
        verify(channel, times(1)).broadcast(argThat(new CustomMatcher<FireBanEventMessage>("ban event") {
            @Override
            public boolean matches(Object item) {
                FireBanEventMessage msg = (FireBanEventMessage)item;
                return (
                        msg.address.equals(ip) &&
                        msg.auto &&
                        !msg.isUnban &&
                        msg.ban.getWho().equals(player) && 
                        msg.ban.getReason().equals("test reason")
                        );
            }
        }));
    }
    
    @Test
    public void testIPBanExistingIPBan() throws SQLException {
        final InetAddress ip = InetAddresses.forString("127.0.0.1");
        final GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        when(player.getAddress()).thenReturn(ip);
        
        final BanInfo<InetAddress> existing = new BanInfo<InetAddress>(ip, 0, "reason", "", null, 12345L, 0, false);
        when(ipBansSection.getStorable(eq(ip.getHostAddress()), any(BanInfo.class))).thenReturn(existing);
        when(ipBansSection.contains(ip.getHostAddress())).thenReturn(true);

        // Do the ban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.ipban(player, "test reason", "CONSOLE", whoBy, true);
        
        // Asserts
        assertEquals(Result.Type.Success, result.getType());
        
        // Make sure it was saved in ban history
        final ArgumentCaptor<BanInfo> banCaptor = ArgumentCaptor.forClass(BanInfo.class);
        verify(repo, times(1)).recordBan(banCaptor.capture()); // since ip ban is already active, shouldnt be an ip ban
        
        BanInfo<GlobalPlayer> playerBan = banCaptor.getAllValues().get(0);
        
        assertEquals(player, playerBan.getWho());
        assertEquals("test reason", playerBan.getReason());
        assertEquals("CONSOLE", playerBan.getBannedBy());
        assertEquals(whoBy, playerBan.getBannedById());
        
        // The ban should not have overridden the existing ip ban
        verify(repo, never()).recordUnban(any(BanInfo.class), anyString(), anyString(), any(UUID.class));
        
        // Make sure it was saved in redis
        verify(player, times(1)).setBan(playerBan);
        verify(ipBansSection, never()).set(anyString(), any(BanInfo.class));
        
        // Check the event
        verify(platform, times(1)).callEvent(any(GlobalBanEvent.class));
        verify(channel, times(1)).broadcast(argThat(new CustomMatcher<FireBanEventMessage>("ban event") {
            @Override
            public boolean matches(Object item) {
                FireBanEventMessage msg = (FireBanEventMessage)item;
                return (
                        msg.address.equals(ip) &&
                        msg.auto &&
                        !msg.isUnban &&
                        msg.ban.getWho().equals(player) && 
                        msg.ban.getReason().equals("test reason")
                        );
            }
        }));
    }
    
    @Test
    public void testIPBanExistingBothBans() throws SQLException {
        final InetAddress ip = InetAddresses.forString("127.0.0.1");
        final GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        when(player.getAddress()).thenReturn(ip);
        
        final BanInfo<GlobalPlayer> existingPlayerBan = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, 0, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(existingPlayerBan);
        
        final BanInfo<InetAddress> existingIpBan = new BanInfo<InetAddress>(ip, 0, "reason", "", null, 12345L, 0, false);
        when(ipBansSection.getStorable(eq(ip.getHostAddress()), any(BanInfo.class))).thenReturn(existingIpBan);
        when(ipBansSection.contains(ip.getHostAddress())).thenReturn(true);

        // Do the ban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.ipban(player, "test reason", "CONSOLE", whoBy, true);
        
        // Asserts
        assertEquals(Result.Type.Fail, result.getType());
        
        verify(repo, never()).recordBan(any(BanInfo.class));
        // The ban should not have overridden the existing bans
        verify(repo, never()).recordUnban(any(BanInfo.class), anyString(), anyString(), any(UUID.class));
        
        // Nothing should be saved
        verify(player, never()).setBan(any(BanInfo.class));
        verify(ipBansSection, never()).set(anyString(), any(BanInfo.class));
        
        // Check the event
        verify(platform, never()).callEvent(any(GlobalBanEvent.class));
        verify(channel, never()).broadcast(any(FireBanEventMessage.class));
    }
    
    @Test
    public void testIPBanExistingPlayerTempBan() throws SQLException {
        final InetAddress ip = InetAddresses.forString("127.0.0.1");
        final GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        when(player.getAddress()).thenReturn(ip);
        
        final BanInfo<GlobalPlayer> existing = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, Long.MAX_VALUE, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(existing);

        // Do the ban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.ipban(player, "test reason", "CONSOLE", whoBy, true);
        
        // Asserts
        assertEquals(Result.Type.Success, result.getType());
        
        // Make sure it was saved in ban history
        final ArgumentCaptor<BanInfo> banCaptor = ArgumentCaptor.forClass(BanInfo.class);
        verify(repo, times(2)).recordBan(banCaptor.capture());
        
        final BanInfo<GlobalPlayer> playerBan = banCaptor.getAllValues().get(0);
        BanInfo<InetAddress> ipBan = banCaptor.getAllValues().get(1);
        
        assertEquals(player, playerBan.getWho());
        assertEquals("test reason", playerBan.getReason());
        assertEquals("CONSOLE", playerBan.getBannedBy());
        assertEquals(whoBy, playerBan.getBannedById());
        
        assertEquals(ip, ipBan.getWho());
        assertEquals("test reason", ipBan.getReason());
        assertEquals("CONSOLE", ipBan.getBannedBy());
        assertEquals(whoBy, ipBan.getBannedById());
        
        // The player ban should have been changed
        verify(repo, times(1)).recordUnban(any(BanInfo.class), anyString(), anyString(), any(UUID.class));
        
        // Make sure it was saved in redis
        verify(player, times(1)).setBan(playerBan);
        verify(ipBansSection, times(1)).set(anyString(), eq(ipBan));
        
        // Check the event
        verify(platform, times(1)).callEvent(any(GlobalBanEvent.class));
        verify(channel, times(1)).broadcast(argThat(new CustomMatcher<FireBanEventMessage>("ban event") {
            @Override
            public boolean matches(Object item) {
                FireBanEventMessage msg = (FireBanEventMessage)item;
                return (
                        msg.address.equals(ip) &&
                        msg.auto &&
                        !msg.isUnban &&
                        msg.ban.getWho().equals(player) && 
                        msg.ban.getReason().equals("test reason")
                        );
            }
        }));
    }
    
    @Test
    public void testIPBanExistingIPTempBan() throws SQLException {
        final InetAddress ip = InetAddresses.forString("127.0.0.1");
        final GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        when(player.getAddress()).thenReturn(ip);
        
        final BanInfo<InetAddress> existingIpBan = new BanInfo<InetAddress>(ip, 0, "reason", "", null, 12345L, Long.MAX_VALUE, false);
        when(ipBansSection.getStorable(eq(ip.getHostAddress()), any(BanInfo.class))).thenReturn(existingIpBan);
        when(ipBansSection.contains(ip.getHostAddress())).thenReturn(true);

        // Do the ban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.ipban(player, "test reason", "CONSOLE", whoBy, true);
        
        // Asserts
        assertEquals(Result.Type.Success, result.getType());
        
        // Make sure it was saved in ban history
        final ArgumentCaptor<BanInfo> banCaptor = ArgumentCaptor.forClass(BanInfo.class);
        verify(repo, times(2)).recordBan(banCaptor.capture());
        
        final BanInfo<GlobalPlayer> playerBan = banCaptor.getAllValues().get(0);
        BanInfo<InetAddress> ipBan = banCaptor.getAllValues().get(1);
        
        assertEquals(player, playerBan.getWho());
        assertEquals("test reason", playerBan.getReason());
        assertEquals("CONSOLE", playerBan.getBannedBy());
        assertEquals(whoBy, playerBan.getBannedById());
        
        assertEquals(ip, ipBan.getWho());
        assertEquals("test reason", ipBan.getReason());
        assertEquals("CONSOLE", ipBan.getBannedBy());
        assertEquals(whoBy, ipBan.getBannedById());
        
        // The player ban should have been changed
        verify(repo, times(1)).recordUnban(any(BanInfo.class), anyString(), anyString(), any(UUID.class));
        
        // Make sure it was saved in redis
        verify(player, times(1)).setBan(playerBan);
        verify(ipBansSection, times(1)).set(anyString(), eq(ipBan));
        
        // Check the event
        verify(platform, times(1)).callEvent(any(GlobalBanEvent.class));
        verify(channel, times(1)).broadcast(argThat(new CustomMatcher<FireBanEventMessage>("ban event") {
            @Override
            public boolean matches(Object item) {
                FireBanEventMessage msg = (FireBanEventMessage)item;
                return (
                        msg.address.equals(ip) &&
                        msg.auto &&
                        !msg.isUnban &&
                        msg.ban.getWho().equals(player) && 
                        msg.ban.getReason().equals("test reason")
                        );
            }
        }));
    }
    
    @Test
    public void testIPUnban() throws SQLException {
        final InetAddress ip = InetAddresses.forString("127.0.0.1");
        final GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        when(player.getAddress()).thenReturn(ip);
        
        final BanInfo<GlobalPlayer> existingPlayerBan = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, 0, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(existingPlayerBan);
        
        final BanInfo<InetAddress> existingIpBan = new BanInfo<InetAddress>(ip, 0, "reason", "", null, 12345L, 0, false);
        when(ipBansSection.getStorable(eq(ip.getHostAddress()), any(BanInfo.class))).thenReturn(existingIpBan);
        when(ipBansSection.contains(ip.getHostAddress())).thenReturn(true);

        // Do the ban
        UUID whoBy = UUID.randomUUID();
        Result result = manager.ipunban(player, "test reason", "CONSOLE", whoBy);
        
        // Asserts
        assertEquals(Result.Type.Success, result.getType());
        
        // Make sure it was saved in ban history
        final ArgumentCaptor<BanInfo> banCaptor = ArgumentCaptor.forClass(BanInfo.class);
        verify(repo, times(1)).recordUnban(existingPlayerBan, "test reason", "CONSOLE", whoBy);
        verify(repo, times(1)).recordUnban(existingIpBan, "test reason", "CONSOLE", whoBy);
        
        // Make sure it was saved in redis
        verify(player, times(1)).setBan(null);
        verify(ipBansSection, times(1)).remove(ip.getHostAddress());
        
        // Check the event
        verify(platform, times(1)).callEvent(any(GlobalUnbanEvent.class));
        verify(channel, times(1)).broadcast(argThat(new CustomMatcher<FireBanEventMessage>("ban event") {
            @Override
            public boolean matches(Object item) {
                FireBanEventMessage msg = (FireBanEventMessage)item;
                return (
                        msg.address.equals(ip) &&
                        !msg.auto &&
                        msg.isUnban &&
                        msg.ban.equals(existingPlayerBan)
                        );
            }
        }));
    }
    
    @Test
    public void testGetBanPlayer() {
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        
        BanInfo<GlobalPlayer> existing = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, 0, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(existing);
        
        assertSame(existing, manager.getBan(player));
    }
    
    @Test
    public void testGetBanIP1() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        BanInfo<InetAddress> ban = new BanInfo<InetAddress>(ip, 0, "reason", "", null, 12345L, System.currentTimeMillis() + 1000, false);
        
        when(ipBansSection.contains(ip.getHostAddress())).thenReturn(true);
        when(ipBansSection.getStorable(eq(ip.getHostAddress()), any(BanInfo.class))).thenReturn(ban);
        
        assertSame(ban, manager.getBan(ip));
    }
    
    @Test
    public void testGetBanIP2() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        BanInfo<InetAddress> ban = new BanInfo<InetAddress>(ip, 0, "reason", "", null, 12345L, System.currentTimeMillis() + 1000, false);
        
        when(ipBansSection.contains(ip.getHostAddress())).thenReturn(true);
        when(ipBansSection.getStorable(eq(ip.getHostAddress()), any(BanInfo.class))).thenReturn(ban);
        
        assertSame(ban, manager.getIPBan(ip));
    }
    
    @Test
    public void testGetBanTempExpired() {
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        
        BanInfo<GlobalPlayer> existing = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, System.currentTimeMillis()-1000, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(existing);
        
        assertNull(manager.getBan(player));
        
        verify(player, times(1)).setBan(null); // It removes expired bans
    }
    
    @Test
    public void testGetBanTempActive() {
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        
        BanInfo<GlobalPlayer> existing = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, System.currentTimeMillis() + 1000, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(existing);
        
        assertSame(existing, manager.getBan(player));
    }
    
    @Test
    public void testSetBanIP1() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        BanInfo<InetAddress> ban = new BanInfo<InetAddress>(ip, 0, "reason", "", null, 12345L, System.currentTimeMillis() + 1000, false);
        manager.setBan(ip, ban);
        
        verify(ipBansSection, times(1)).set(anyString(), eq(ban));
    }
    
    @Test
    public void testSetBanIP2() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        
        BanInfo<InetAddress> ban = new BanInfo<InetAddress>(ip, 0, "reason", "", null, 12345L, System.currentTimeMillis() + 1000, false);
        manager.setIPBan(ip, ban);
        
        verify(ipBansSection, times(1)).set(anyString(), eq(ban));
    }
    
    @Test
    public void testSetBanPlayer() {
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        
        BanInfo<GlobalPlayer> ban = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, System.currentTimeMillis() + 1000, false);
        
        manager.setBan(player, ban);
        
        verify(player, times(1)).setBan(eq(ban));
    }
    
    @Test
    public void testGetAnyBanNone() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        when(player.getAddress()).thenReturn(ip);
        
        BanInfo<?> ban = manager.getAnyBan(player);
        
        assertNull(ban);
    }
    
    @Test
    public void testGetAnyBanPlayer() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        when(player.getAddress()).thenReturn(ip);
        
        BanInfo<GlobalPlayer> existing = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, 0, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(existing);
        
        BanInfo<?> ban = manager.getAnyBan(player);
        
        assertEquals(existing, ban);
    }
    
    @Test
    public void testGetAnyBanIP() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        when(player.getAddress()).thenReturn(ip);
        
        BanInfo<InetAddress> existing = new BanInfo<InetAddress>(ip, 0, "reason", "", null, 12345L, 0, false);
        
        when(ipBansSection.contains(ip.getHostAddress())).thenReturn(true);
        when(ipBansSection.getStorable(eq(ip.getHostAddress()), any(BanInfo.class))).thenReturn(existing);
        
        BanInfo<?> ban = manager.getAnyBan(player);
        
        assertEquals(existing, ban);
    }
    
    @Test
    public void testGetAnyBanBoth() {
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        when(player.getAddress()).thenReturn(ip);
        
        BanInfo<GlobalPlayer> playerBan = new BanInfo<GlobalPlayer>(player, 0, "reason", "", null, 12345L, 0, false);
        when(player.isBanned()).thenReturn(true);
        when(player.getBanInfo()).thenReturn(playerBan);
        
        BanInfo<InetAddress> ipBan = new BanInfo<InetAddress>(ip, 0, "reason", "", null, 12345L, 0, false);
        
        when(ipBansSection.contains(ip.getHostAddress())).thenReturn(true);
        when(ipBansSection.getStorable(eq(ip.getHostAddress()), any(BanInfo.class))).thenReturn(ipBan);
        
        BanInfo<?> ban = manager.getAnyBan(player);
        
        assertEquals(playerBan, ban);
    }
    
    @Test
    public void testKick() {
        UUID id = UUID.randomUUID();
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(id);
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        
        ProxiedPlayer pplayer = mock(ProxiedPlayer.class);
        when(pplayer.getUniqueId()).thenReturn(id);
        when(pplayer.getName()).thenReturn("name");
        when(pplayer.getDisplayName()).thenReturn("name");
        
        when(proxy.getPlayer(id)).thenReturn(pplayer);
        
        // Do the test
        Result result = manager.kick(player, "test");
        
        assertEquals(Result.Type.Success, result.getType());
        verify(pplayer, times(1)).disconnect(any(TextComponent[].class));
    }
    
    @Test
    public void testKickOffline() {
        UUID id = UUID.randomUUID();
        GlobalPlayer player = mock(GlobalPlayer.class);
        when(player.getUniqueId()).thenReturn(id);
        when(player.getName()).thenReturn("name");
        when(player.getDisplayName()).thenReturn("name");
        
        // Do the test
        Result result = manager.kick(player, "test");
        
        assertEquals(Result.Type.Fail, result.getType());
    }
    
    @Test
    public void testKickAll() {
        ProxiedPlayer pplayer1 = mock(ProxiedPlayer.class);
        when(pplayer1.getUniqueId()).thenReturn(UUID.randomUUID());
        when(pplayer1.getName()).thenReturn("name1");
        when(pplayer1.getDisplayName()).thenReturn("name1");
        
        ProxiedPlayer pplayer2 = mock(ProxiedPlayer.class);
        when(pplayer2.getUniqueId()).thenReturn(UUID.randomUUID());
        when(pplayer2.getName()).thenReturn("name2");
        when(pplayer2.getDisplayName()).thenReturn("name2");
        
        when(proxy.getPlayers()).thenReturn(Arrays.asList(pplayer1, pplayer2));
        
        // Do the test
        Result result = manager.kickAll("test");
        
        assertEquals(Result.Type.Success, result.getType());
        verify(pplayer1, times(1)).disconnect(any(TextComponent[].class));
        verify(pplayer2, times(1)).disconnect(any(TextComponent[].class));
    }
}
