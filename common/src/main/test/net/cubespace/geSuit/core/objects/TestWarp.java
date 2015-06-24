package net.cubespace.geSuit.core.objects;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestWarp {
    @Test
    public void testConstruct() {
        Location location = new Location("server", "world", 0, 1, 2);
        Warp warp = new Warp("name", location, true, true);
        
        assertEquals("name", warp.getName());
        assertEquals(location, warp.getLocation());
        assertTrue(warp.isHidden());
        assertTrue(warp.isGlobal());
    }
    
    @Test
    public void testSerialize() {
        Location location = new Location("server", "world", 0, 1, 2);
        Warp warp = new Warp("name", location, true, true);
        assertEquals(location.toSerialized() + "\001true\001true", warp.toSerialized());
        
        warp = new Warp("name", location, false, true);
        assertEquals(location.toSerialized() + "\001false\001true", warp.toSerialized());
        
        warp = new Warp("name", location, true, false);
        assertEquals(location.toSerialized() + "\001true\001false", warp.toSerialized());
    }
    
    @Test
    public void testDeserialize() {
        Location location = new Location("server", "world", 0, 1, 2);
        
        // All true
        Warp warp = Warp.fromSerialized("name", location.toSerialized() + "\001true\001true");
        
        assertEquals("name", warp.getName());
        assertEquals(location, warp.getLocation());
        assertTrue(warp.isHidden());
        assertTrue(warp.isGlobal());
        
        // Not hidden
        warp = Warp.fromSerialized("name", location.toSerialized() + "\001false\001true");
        
        assertEquals("name", warp.getName());
        assertEquals(location, warp.getLocation());
        assertFalse(warp.isHidden());
        assertTrue(warp.isGlobal());
        
        // Not global but hidden
        warp = Warp.fromSerialized("name", location.toSerialized() + "\001true\001false");
        
        assertEquals("name", warp.getName());
        assertEquals(location, warp.getLocation());
        assertTrue(warp.isHidden());
        assertFalse(warp.isGlobal());
    }
}
