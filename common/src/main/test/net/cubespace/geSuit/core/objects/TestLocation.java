package net.cubespace.geSuit.core.objects;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestLocation {

    @Test
    public void testToSerialized() {
        Location location = new Location(null, null, 1, 2, 3);
        assertEquals("\0|\0|1.0000|2.0000|3.0000|0.0000|0.0000", location.toSerialized());
        
        location = new Location(null, "world", 1, 2, 3);
        assertEquals("\0|world|1.0000|2.0000|3.0000|0.0000|0.0000", location.toSerialized());
        
        location = new Location("server", "world", 1, 2, 3);
        assertEquals("server|world|1.0000|2.0000|3.0000|0.0000|0.0000", location.toSerialized());
        
        location = new Location("server", "world", 1, 2, 3, 4, 5);
        assertEquals("server|world|1.0000|2.0000|3.0000|4.0000|5.0000", location.toSerialized());
    }

    @Test
    public void testSave() {
        Location location = new Location(null, null, 1, 2, 3);
        assertEquals("\0|\0|1.0000|2.0000|3.0000|0.0000|0.0000", location.save());
        
        location = new Location(null, "world", 1, 2, 3);
        assertEquals("\0|world|1.0000|2.0000|3.0000|0.0000|0.0000", location.save());
        
        location = new Location("server", "world", 1, 2, 3);
        assertEquals("server|world|1.0000|2.0000|3.0000|0.0000|0.0000", location.save());
        
        location = new Location("server", "world", 1, 2, 3, 4, 5);
    }

    @Test
    public void testLoad() {
        Location location = new Location();
        location.load("\0|\0|1.000|2.000|3.000|0.000|0.000");
        assertEquals(null, location.getServer());
        assertEquals(null, location.getWorld());
        assertEquals(1, location.getX(), 0);
        assertEquals(2, location.getY(), 0);
        assertEquals(3, location.getZ(), 0);
        assertEquals(0, location.getYaw(), 0);
        assertEquals(0, location.getPitch(), 0);
        
        location = new Location();
        location.load("\0|world|1.000|2.000|3.000|0.000|0.000");
        assertEquals(null, location.getServer());
        assertEquals("world", location.getWorld());
        assertEquals(1, location.getX(), 0);
        assertEquals(2, location.getY(), 0);
        assertEquals(3, location.getZ(), 0);
        assertEquals(0, location.getYaw(), 0);
        assertEquals(0, location.getPitch(), 0);
        
        location = new Location();
        location.load("server|world|1.000|2.000|3.000|0.000|0.000");
        assertEquals("server", location.getServer());
        assertEquals("world", location.getWorld());
        assertEquals(1, location.getX(), 0);
        assertEquals(2, location.getY(), 0);
        assertEquals(3, location.getZ(), 0);
        assertEquals(0, location.getYaw(), 0);
        assertEquals(0, location.getPitch(), 0);
        
        location = new Location();
        location.load("server|world|1.000|2.000|3.000|4.000|5.000");
        assertEquals("server", location.getServer());
        assertEquals("world", location.getWorld());
        assertEquals(1, location.getX(), 0);
        assertEquals(2, location.getY(), 0);
        assertEquals(3, location.getZ(), 0);
        assertEquals(4, location.getYaw(), 0);
        assertEquals(5, location.getPitch(), 0);
    }

    @Test
    public void testFromSerialized() {
        Location location = Location.fromSerialized("\0|\0|1.000|2.000|3.000|0.000|0.000");
        assertEquals(null, location.getServer());
        assertEquals(null, location.getWorld());
        assertEquals(1, location.getX(), 0);
        assertEquals(2, location.getY(), 0);
        assertEquals(3, location.getZ(), 0);
        assertEquals(0, location.getYaw(), 0);
        assertEquals(0, location.getPitch(), 0);
        
        location = Location.fromSerialized("\0|world|1.000|2.000|3.000|0.000|0.000");
        assertEquals(null, location.getServer());
        assertEquals("world", location.getWorld());
        assertEquals(1, location.getX(), 0);
        assertEquals(2, location.getY(), 0);
        assertEquals(3, location.getZ(), 0);
        assertEquals(0, location.getYaw(), 0);
        assertEquals(0, location.getPitch(), 0);
        
        location = Location.fromSerialized("server|world|1.000|2.000|3.000|0.000|0.000");
        assertEquals("server", location.getServer());
        assertEquals("world", location.getWorld());
        assertEquals(1, location.getX(), 0);
        assertEquals(2, location.getY(), 0);
        assertEquals(3, location.getZ(), 0);
        assertEquals(0, location.getYaw(), 0);
        assertEquals(0, location.getPitch(), 0);
        
        location = Location.fromSerialized("server|world|1.000|2.000|3.000|4.000|5.000");
        assertEquals("server", location.getServer());
        assertEquals("world", location.getWorld());
        assertEquals(1, location.getX(), 0);
        assertEquals(2, location.getY(), 0);
        assertEquals(3, location.getZ(), 0);
        assertEquals(4, location.getYaw(), 0);
        assertEquals(5, location.getPitch(), 0);
    }

    @Test
    public void testBoundingBoxContainsLoc() {
        Location min = new Location(null, null, 0, 0, 0);
        Location max = new Location(null, null, 10, 20, 30);
        
        assertTrue(Location.contains(min, max, new Location(null, null, 0, 0, 0)));
        assertTrue(Location.contains(min, max, new Location(null, null, 10, 20, 30)));
        assertTrue(Location.contains(min, max, new Location(null, null, 5, 5, 5)));
    }

    @Test
    public void testBoundingBoxContainsPos() {
        Location min = new Location(null, null, 0, 0, 0);
        Location max = new Location(null, null, 10, 20, 30);
        
        assertTrue(Location.contains(min, max, 0, 0, 0));
        assertTrue(Location.contains(min, max, 10, 20, 30));
        assertTrue(Location.contains(min, max, 5, 5, 5));
    }

}
