package net.cubespace.geSuit.core.objects;

import static org.junit.Assert.*;

import java.util.Map;

import net.cubespace.geSuit.core.objects.Portal.FillType;
import net.cubespace.geSuit.core.objects.Portal.Type;

import org.junit.Test;

import com.google.common.collect.Maps;

public class TestPortal {
    @Test
    public void testGetDestWarp() {
        Portal portal = new Portal("name", Type.Warp, "warpName", FillType.Water, null, null);
        assertEquals(Type.Warp, portal.getType());
        assertEquals("warpName", portal.getDestWarp());
    }

    @Test
    public void testGetDestServer() {
        Portal portal = new Portal("name", Type.Server, "serverName", FillType.Water, null, null);
        assertEquals(Type.Server, portal.getType());
        assertEquals("serverName", portal.getDestServer());
    }

    @Test
    public void testGetDestLocation() {
        Location loc = new Location("server", "world", 5, 2, 1);
        
        Portal portal = new Portal("name", loc, FillType.Water, null, null);
        assertEquals(Type.Teleport, portal.getType());
        assertEquals(loc, portal.getDestLocation());
    }

    @Test
    public void testSetDestWarp() {
        Portal portal = new Portal("name", Type.Server, "serverName", FillType.Water, null, null);
        portal.setDestWarp("warpName");
        
        assertEquals(Type.Warp, portal.getType());
        assertEquals("warpName", portal.getDestWarp());
    }

    @Test
    public void testSetDestServer() {
        Portal portal = new Portal("name", Type.Warp, "warpName", FillType.Water, null, null);
        portal.setDestServer("serverName");
        
        assertEquals(Type.Server, portal.getType());
        assertEquals("serverName", portal.getDestServer());
    }

    @Test
    public void testSetDestLocation() {
        Portal portal = new Portal("name", Type.Server, "serverName", FillType.Water, null, null);
        Location loc = new Location("server", "world", 5, 2, 1);
        portal.setDestLocation(loc);
        
        assertEquals(Type.Teleport, portal.getType());
        assertEquals(loc, portal.getDestLocation());
    }

    @Test
    public void testSave() {
        Location min = new Location("server", "world", 0, 0, 0);
        Location max = new Location("server", "world", 1, 10, 5);
        
        // Server portal
        Portal portal = new Portal("name", Type.Server, "serverName", FillType.Water, min, max);
        
        Map<String, String> values = Maps.newHashMap();
        portal.save(values);
        
        // Test it
        assertEquals("Water", values.get("fill"));
        assertEquals("Server", values.get("type"));
        assertEquals("serverName", values.get("dest"));
        assertEquals(min.toSerialized(), values.get("min"));
        assertEquals(max.toSerialized(), values.get("max"));
        
        // Warp portal
        portal = new Portal("name", Type.Warp, "warpName", FillType.Lava, min, max);
        
        values = Maps.newHashMap();
        portal.save(values);
        
        // Test it
        assertEquals("Lava", values.get("fill"));
        assertEquals("Warp", values.get("type"));
        assertEquals("warpName", values.get("dest"));
        assertEquals(min.toSerialized(), values.get("min"));
        assertEquals(max.toSerialized(), values.get("max"));
        
        // Teleport portal
        Location dest = new Location("server2", "world", 100, 300, 40, 20, 0);
        portal = new Portal("name", dest, FillType.Air, min, max);
        
        values = Maps.newHashMap();
        portal.save(values);
        
        // Test it
        assertEquals("Air", values.get("fill"));
        assertEquals("Teleport", values.get("type"));
        assertEquals(dest.toSerialized(), values.get("dest"));
        assertEquals(min.toSerialized(), values.get("min"));
        assertEquals(max.toSerialized(), values.get("max"));
    }

    @Test
    public void testLoad() {
        Location minExpected = new Location("server", "world", 0, 0, 0);
        Location maxExpected = new Location("server", "world", 1, 10, 5);
        
        // Server portal
        Portal portal = new Portal("name");
        
        Map<String, String> values = Maps.newHashMap();
        values.put("fill", "Water");
        values.put("type", "Server");
        values.put("dest", "serverName");
        values.put("min", minExpected.toSerialized());
        values.put("max", maxExpected.toSerialized());
        
        portal.load(values);
        
        // Test it
        assertEquals(FillType.Water, portal.getFill());
        assertEquals(Type.Server, portal.getType());
        assertEquals("serverName", portal.getDestServer());
        assertEquals(minExpected, portal.getMinCorner());
        assertEquals(maxExpected, portal.getMaxCorner());
        
        // Warp portal
        portal = new Portal("name");
        
        values = Maps.newHashMap();
        values.put("fill", "Lava");
        values.put("type", "Warp");
        values.put("dest", "warpName");
        values.put("min", minExpected.toSerialized());
        values.put("max", maxExpected.toSerialized());
        
        portal.load(values);
        
        // Test it
        assertEquals(FillType.Lava, portal.getFill());
        assertEquals(Type.Warp, portal.getType());
        assertEquals("warpName", portal.getDestWarp());
        assertEquals(minExpected, portal.getMinCorner());
        assertEquals(maxExpected, portal.getMaxCorner());
        
        // Teleport portal
        Location dest = new Location("server2", "world", 100, 300, 40, 20, 0);
        portal = new Portal("name");
        
        values = Maps.newHashMap();
        values.put("fill", "Air");
        values.put("type", "Teleport");
        values.put("dest", dest.toSerialized());
        values.put("min", minExpected.toSerialized());
        values.put("max", maxExpected.toSerialized());
        
        portal.load(values);
        
        // Test it
        assertEquals(FillType.Air, portal.getFill());
        assertEquals(Type.Teleport, portal.getType());
        assertEquals(dest, portal.getDestLocation());
        assertEquals(minExpected, portal.getMinCorner());
        assertEquals(maxExpected, portal.getMaxCorner());
    }

}
