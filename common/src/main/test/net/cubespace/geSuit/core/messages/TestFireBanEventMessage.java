package net.cubespace.geSuit.core.messages;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.geCore;
import net.cubespace.geSuit.core.objects.BanInfo;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.net.InetAddresses;

public class TestFireBanEventMessage {
    private static geCore coreMock;
    
    @BeforeClass
    public static void prepare() {
        coreMock = mock(geCore.class);
        Global.setInstance(coreMock);
    }
    
    private GlobalPlayer createPlayer(UUID id, String name) {
        GlobalPlayer player = mock(GlobalPlayer.class);
        
        when(player.getUniqueId()).thenReturn(id);
        when(player.getName()).thenReturn(name);
        when(player.hasNickname()).thenReturn(false);
        
        return player;
    }
    
    @Test
    public void testConstruct() {
        // Prepare
        GlobalPlayer player = createPlayer(UUID.randomUUID(), "test");
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        BanInfo<GlobalPlayer> playerBan = new BanInfo<>(player, 0, "Reason", "a_player", UUID.randomUUID(), 12345L, 0L, false);
        
        // Do tests
        FireBanEventMessage message = FireBanEventMessage.createFromBan(playerBan, true);
        assertTrue(message.auto);
        assertEquals(playerBan, message.ban);
        assertFalse(message.isUnban);
        
        message = FireBanEventMessage.createFromBan(playerBan, false);
        assertFalse(message.auto);
        
        message = FireBanEventMessage.createFromIPBan(playerBan, ip, true);
        assertTrue(message.auto);
        assertEquals(playerBan, message.ban);
        assertEquals(ip, message.address);
        assertFalse(message.isUnban);
        
        // Unbans
        message = FireBanEventMessage.createFromUnban(playerBan);
        assertEquals(playerBan, message.ban);
        assertTrue(message.isUnban);
        
        message = FireBanEventMessage.createFromIPUnban(playerBan, ip);
        assertEquals(playerBan, message.ban);
        assertEquals(ip, message.address);
        assertTrue(message.isUnban);
    }
    
    @Test
    public void testIOPlayerBan() throws IOException {
        // Prepare
        reset(coreMock);
        GlobalPlayer player = createPlayer(UUID.randomUUID(), "test");
        when(coreMock.getOfflinePlayer(player.getUniqueId())).thenReturn(player);
        BanInfo<GlobalPlayer> playerBan = new BanInfo<>(player, 0, "Reason", "a_player", UUID.randomUUID(), 12345L, 0L, false);
        
        // Do tests
        FireBanEventMessage expected = FireBanEventMessage.createFromBan(playerBan, true);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        expected.write(out);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        FireBanEventMessage actual = new FireBanEventMessage();
        actual.read(in);
        
        assertEquals(expected.address, actual.address);
        assertEquals(expected.auto, actual.auto);
        assertEquals(expected.ban, actual.ban);
        assertEquals(expected.isUnban, actual.isUnban);
    }
    
    @Test
    public void testIOIPBan() throws IOException {
        // Prepare
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        BanInfo<InetAddress> ipBan = new BanInfo<>(ip, 1, "Banned", "console", UUID.randomUUID(), 55555L, 0L, false);
        
        // Do tests
        FireBanEventMessage expected = FireBanEventMessage.createFromBan(ipBan, true);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        expected.write(out);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        FireBanEventMessage actual = new FireBanEventMessage();
        actual.read(in);
        
        assertEquals(expected.address, actual.address);
        assertEquals(expected.auto, actual.auto);
        assertEquals(expected.ban, actual.ban);
        assertEquals(expected.isUnban, actual.isUnban);
    }
    
    @Test
    public void testIOIPPlayerBan() throws IOException {
        // Prepare
        reset(coreMock);
        GlobalPlayer player = createPlayer(UUID.randomUUID(), "test");
        when(coreMock.getOfflinePlayer(player.getUniqueId())).thenReturn(player);
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        BanInfo<GlobalPlayer> playerBan = new BanInfo<>(player, 0, "Reason", "a_player", UUID.randomUUID(), 12345L, 0L, false);
        
        // Do tests
        FireBanEventMessage expected = FireBanEventMessage.createFromIPBan(playerBan, ip, true);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        expected.write(out);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        FireBanEventMessage actual = new FireBanEventMessage();
        actual.read(in);
        
        assertEquals(expected.address, actual.address);
        assertEquals(expected.auto, actual.auto);
        assertEquals(expected.ban, actual.ban);
        assertEquals(expected.isUnban, actual.isUnban);
    }
    
    @Test
    public void testIOUnban() throws IOException {
        // Prepare
        InetAddress ip = InetAddresses.forString("127.0.0.1");
        BanInfo<InetAddress> ipBan = new BanInfo<>(ip, 1, "Banned", "console", UUID.randomUUID(), 55555L, 0L, false);
        
        // Do tests
        FireBanEventMessage expected = FireBanEventMessage.createFromUnban(ipBan);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        expected.write(out);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        FireBanEventMessage actual = new FireBanEventMessage();
        actual.read(in);
        
        assertEquals(expected.address, actual.address);
        assertEquals(expected.ban, actual.ban);
        assertEquals(expected.isUnban, actual.isUnban);
    }

}
