package net.cubespace.geSuit.core.objects;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import org.junit.Test;

import com.google.common.net.InetAddresses;

public class TestTrack {
    @Test
    public void testConstruct() {
        UUID testUUID = UUID.randomUUID();
        InetAddress testIP = InetAddresses.forString("127.0.0.1");
        
        Track track = new Track("name", "nickname", testUUID, testIP, 4, 5, 0, 1);
        
        // Test it
        assertEquals("name", track.getName());
        assertEquals("nickname", track.getNickname());
        assertEquals(testUUID, track.getUniqueId());
        assertEquals(testIP, track.getIp());
        assertEquals(4, track.getFirstSeen());
        assertEquals(5, track.getLastSeen());
        assertFalse(track.isNameBanned());
        assertTrue(track.isIpBanned());
    }
    
    @Test
    public void testConstructNoNickname() {
        UUID testUUID = UUID.randomUUID();
        InetAddress testIP = InetAddresses.forString("127.0.0.1");
        
        Track track = new Track("name", null, testUUID, testIP, 4, 5, 0, 1);
        
        // Test it
        assertEquals("name", track.getName());
        assertNull(track.getNickname());
        assertEquals(testUUID, track.getUniqueId());
        assertEquals(testIP, track.getIp());
        assertEquals(4, track.getFirstSeen());
        assertEquals(5, track.getLastSeen());
        assertFalse(track.isNameBanned());
        assertTrue(track.isIpBanned());
    }
    
    @Test
    public void testConstructBlankNickname() {
        UUID testUUID = UUID.randomUUID();
        InetAddress testIP = InetAddresses.forString("127.0.0.1");
        
        Track track = new Track("name", "", testUUID, testIP, 4, 5, 0, 1);
        
        // Test it
        assertEquals("name", track.getName());
        assertNull(track.getNickname());
        assertEquals(testUUID, track.getUniqueId());
        assertEquals(testIP, track.getIp());
        assertEquals(4, track.getFirstSeen());
        assertEquals(5, track.getLastSeen());
        assertFalse(track.isNameBanned());
        assertTrue(track.isIpBanned());
    }
    
    @Test
    public void testBanValues() {
        // No bans
        Track track = new Track(null, null, null, null, 0, 0, 0, 0);
        assertFalse(track.isNameBanned());
        assertFalse(track.isIpBanned());
        
        // Perm name ban
        track = new Track(null, null, null, null, 0, 0, 1, 0);
        assertTrue(track.isNameBanned());
        assertFalse(track.isNameBanTemp());
        assertFalse(track.isIpBanned());
        
        // Temp name ban
        track = new Track(null, null, null, null, 0, 0, 2, 0);
        assertTrue(track.isNameBanned());
        assertTrue(track.isNameBanTemp());
        assertFalse(track.isIpBanned());
        
        // Perm IP ban
        track = new Track(null, null, null, null, 0, 0, 0, 1);
        assertFalse(track.isNameBanned());
        assertTrue(track.isIpBanned());
        assertFalse(track.isIpBanTemp());
        
        // Temp IP ban
        track = new Track(null, null, null, null, 0, 0, 0, 2);
        assertFalse(track.isNameBanned());
        assertTrue(track.isIpBanned());
        assertTrue(track.isIpBanTemp());
    }
    
    @Test
    public void testDisplayName() {
        // With nickname
        Track track = new Track("name", "nickname", null, null, 0, 0, 0, 0);
        assertEquals("nickname", track.getDisplayName());
        
        // Without
        track = new Track("name", null, null, null, 0, 0, 0, 0);
        assertEquals("name", track.getDisplayName());
    }
    
    @Test
    public void testSaveLoadNickname() throws IOException {
        UUID testUUID = UUID.randomUUID();
        InetAddress testIP = InetAddresses.forString("127.0.0.1");
        
        Track track = new Track("name", "nickname", testUUID, testIP, 4, 5, 0, 1);
        
        // Save it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        track.save(out);
        
        // Load it
        Track inTrack = new Track();
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        inTrack.load(in);
        
        // Test it
        assertEquals(track.getName(), inTrack.getName());
        assertEquals(track.getNickname(), inTrack.getNickname());
        assertEquals(track.getUniqueId(), inTrack.getUniqueId());
        assertEquals(track.getIp(), inTrack.getIp());
        assertEquals(track.getFirstSeen(), inTrack.getFirstSeen());
        assertEquals(track.getLastSeen(), inTrack.getLastSeen());
        assertEquals(track.isNameBanned(), inTrack.isNameBanned());
        assertEquals(track.isNameBanTemp(), inTrack.isNameBanTemp());
        assertEquals(track.isIpBanned(), inTrack.isIpBanned());
        assertEquals(track.isIpBanTemp(), inTrack.isIpBanTemp());
    }
    
    @Test
    public void testSaveLoadNoNickname() throws IOException {
        UUID testUUID = UUID.randomUUID();
        InetAddress testIP = InetAddresses.forString("127.0.0.1");
        
        Track track = new Track("name", null, testUUID, testIP, 4, 5, 0, 1);
        
        // Save it
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(ostream);
        track.save(out);
        
        // Load it
        Track inTrack = new Track();
        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        DataInputStream in = new DataInputStream(istream);
        inTrack.load(in);
        
        // Test it
        assertEquals(track.getName(), inTrack.getName());
        assertEquals(track.getNickname(), inTrack.getNickname());
        assertEquals(track.getUniqueId(), inTrack.getUniqueId());
        assertEquals(track.getIp(), inTrack.getIp());
        assertEquals(track.getFirstSeen(), inTrack.getFirstSeen());
        assertEquals(track.getLastSeen(), inTrack.getLastSeen());
        assertEquals(track.isNameBanned(), inTrack.isNameBanned());
        assertEquals(track.isNameBanTemp(), inTrack.isNameBanTemp());
        assertEquals(track.isIpBanned(), inTrack.isIpBanned());
        assertEquals(track.isIpBanTemp(), inTrack.isIpBanTemp());
    }
}
