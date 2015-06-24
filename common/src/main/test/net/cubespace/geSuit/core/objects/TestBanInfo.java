package net.cubespace.geSuit.core.objects;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;

public class TestBanInfo {
    @Test
    public void testFullReadWriteBytes() throws IOException {
        InetAddress who = InetAddresses.forString("127.0.0.1");
        // The ban
        BanInfo<InetAddress> ban = new BanInfo<InetAddress>(
                who,
                3,
                "Ban reason",
                "User",
                UUID.randomUUID(),
                10000000,
                10003000,
                false
                );
        
        // Save it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        ban.save(out);
        
        // Load it
        BanInfo<InetAddress> inBan = new BanInfo<InetAddress>(who);
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        inBan.load(in);
        
        // Test
        assertEquals(ban.getBannedBy(), inBan.getBannedBy());
        assertEquals(ban.getBannedById(), inBan.getBannedById());
        assertEquals(ban.getDatabaseKey(), inBan.getDatabaseKey());
        assertEquals(ban.getDate(), inBan.getDate());
        assertEquals(ban.getReason(), inBan.getReason());
        assertEquals(ban.getUntil(), inBan.getUntil());
        assertEquals(ban.isTemporary(), inBan.isTemporary());
        assertEquals(ban.isUnban(), inBan.isUnban());
    }
    
    @Test
    public void testLimitedReadWriteBytes() throws IOException {
        InetAddress who = InetAddresses.forString("127.0.0.1");
        // The ban
        BanInfo<InetAddress> ban = new BanInfo<InetAddress>(
                who,
                3,
                null,
                "CONSOLE",
                null,
                10000000,
                0,
                false
                );
        
        // Save it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        ban.save(out);
        
        // Load it
        BanInfo<InetAddress> inBan = new BanInfo<InetAddress>(who);
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        inBan.load(in);
        
        // Test
        assertEquals(ban.getBannedBy(), inBan.getBannedBy());
        assertEquals(ban.getBannedById(), inBan.getBannedById());
        assertEquals(ban.getDatabaseKey(), inBan.getDatabaseKey());
        assertEquals(ban.getDate(), inBan.getDate());
        assertEquals(ban.getReason(), inBan.getReason());
        assertEquals(ban.getUntil(), inBan.getUntil());
        assertEquals(ban.isTemporary(), inBan.isTemporary());
        assertEquals(ban.isUnban(), inBan.isUnban());
    }
    
    @Test
    public void testFullReadWriteMap() {
        InetAddress who = InetAddresses.forString("127.0.0.1");
        // The ban
        BanInfo<InetAddress> ban = new BanInfo<InetAddress>(
                who,
                3,
                "Ban reason",
                "User",
                UUID.randomUUID(),
                10000000,
                10003000,
                false
                );
        
        // Save it
        Map<String, String> values = Maps.newHashMap();
        ban.save(values);
        
        // Load it
        BanInfo<InetAddress> inBan = new BanInfo<InetAddress>(who);
        inBan.load(values);
        
        // Test
        assertEquals(ban.getBannedBy(), inBan.getBannedBy());
        assertEquals(ban.getBannedById(), inBan.getBannedById());
        assertEquals(ban.getDatabaseKey(), inBan.getDatabaseKey());
        assertEquals(ban.getDate(), inBan.getDate());
        assertEquals(ban.getReason(), inBan.getReason());
        assertEquals(ban.getUntil(), inBan.getUntil());
        assertEquals(ban.isTemporary(), inBan.isTemporary());
        assertEquals(ban.isUnban(), inBan.isUnban());
    }
    
    @Test
    public void testLimitedReadWriteMap() {
        InetAddress who = InetAddresses.forString("127.0.0.1");
        // The ban
        BanInfo<InetAddress> ban = new BanInfo<InetAddress>(
                who,
                3,
                null,
                "CONSOLE",
                null,
                10000000,
                0,
                false
                );
        
        // Save it
        Map<String, String> values = Maps.newHashMap();
        ban.save(values);
        
        // Load it
        BanInfo<InetAddress> inBan = new BanInfo<InetAddress>(who);
        inBan.load(values);
        
        // Test
        assertEquals(ban.getBannedBy(), inBan.getBannedBy());
        assertEquals(ban.getBannedById(), inBan.getBannedById());
        assertEquals(ban.getDatabaseKey(), inBan.getDatabaseKey());
        assertEquals(ban.getDate(), inBan.getDate());
        assertEquals(ban.getReason(), inBan.getReason());
        assertEquals(ban.getUntil(), inBan.getUntil());
        assertEquals(ban.isTemporary(), inBan.isTemporary());
        assertEquals(ban.isUnban(), inBan.isUnban());
    }
    
    @Test
    public void testValidState() {
        InetAddress who = InetAddresses.forString("127.0.0.1");
        // Perm ban
        BanInfo<InetAddress> ban = new BanInfo<InetAddress>(
                who,
                3,
                "Ban reason",
                "CONSOLE",
                null,
                10000000,
                0,
                false
                );
        
        assertEquals("CONSOLE", ban.getBannedBy());
        assertEquals("Ban reason", ban.getReason());
        assertNull(ban.getBannedById());
        assertEquals(3, ban.getDatabaseKey());
        assertEquals(who, ban.getWho());
        assertEquals(10000000, ban.getDate());
        assertFalse(ban.isTemporary());
        assertFalse(ban.isUnban());
        
        // Temp ban
        ban = new BanInfo<InetAddress>(
                who,
                3,
                "Ban reason",
                "CONSOLE",
                null,
                10000000,
                10003000,
                false
                );
        
        assertEquals("CONSOLE", ban.getBannedBy());
        assertEquals("Ban reason", ban.getReason());
        assertNull(ban.getBannedById());
        assertEquals(3, ban.getDatabaseKey());
        assertEquals(who, ban.getWho());
        assertEquals(10000000, ban.getDate());
        assertTrue(ban.isTemporary());
        assertEquals(10003000, ban.getUntil());
        assertFalse(ban.isUnban());
        
        // Unban
        UUID userId = UUID.randomUUID();
        ban = new BanInfo<InetAddress>(
                who,
                4,
                "Unban reason",
                "User",
                userId,
                10000000,
                0,
                true
                );
        
        assertEquals("User", ban.getBannedBy());
        assertEquals(userId, ban.getBannedById());
        assertEquals("Unban reason", ban.getReason());
        assertEquals(4, ban.getDatabaseKey());
        assertEquals(who, ban.getWho());
        assertEquals(10000000, ban.getDate());
        assertTrue(ban.isUnban());
    }
}
