package net.cubespace.geSuit.core.storage;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import redis.clients.jedis.Jedis;
import static org.mockito.Mockito.*;

public class TestRedisSection {
    @Test
    public void testOffsetRoot() {
        RedisSection root = new RedisSection((RedisConnection)null, "test.other");
        
        assertEquals("test.other", root.getCurrentPath());
        assertEquals("other", root.getName());
        assertNull(root.getParent());
        assertSame(root, root.getRoot());
        
        // Now try to do a subsection
        RedisSection section = root.getSubsection("next");
        assertEquals("test.other.next", section.getCurrentPath());
    }

    @Test
    public void testSubsection() {
        RedisSection root = new RedisSection(null);
        RedisSection section = root.getSubsection("test");
        RedisSection section2 = root.getSubsection("test.other");
        
        // Ensure all three objects are separate
        assertNotEquals(root, section);
        assertNotEquals(section, section2);
        
        // Ensure that "test" is a child of root and that "other" is a child of "test"
        assertSame(section.getParent(), root);
        assertSame(section2.getParent(), section);
        assertSame(section2.getRoot(), root);
        assertSame(section.getSubsection("other"), section2);
    }
    
    @Test
    public void testPrimitive() {
        Jedis jedis = mock(Jedis.class);
        
        RedisConnection connection = mock(RedisConnection.class);
        stub(connection.getJedis()).toReturn(jedis);
        
        RedisSection root = new RedisSection(connection);
        
        // Places the values in cache
        root.set("bool", true);
        root.set("int", 123);
        root.set("long", 12345678901234L);
        root.set("float", 332.5f);
        root.set("double", 55446.1123442);
        root.set("string", "Test value");
        root.set("uuid", UUID.nameUUIDFromBytes("test".getBytes()));
        
        // Retrieve and check the values. The values should all be in cache
        assertEquals(true, root.getBoolean("bool", false));
        verifyZeroInteractions(jedis);
        
        assertEquals(123, root.getInt("int", 0));
        verifyZeroInteractions(jedis);
        
        assertEquals(12345678901234L, root.getLong("long", 0));
        verifyZeroInteractions(jedis);
        
        assertEquals(332.5f, root.getFloat("float", 0.0f), 0.01f);
        verifyZeroInteractions(jedis);
        
        assertEquals(55446.1123442, root.getDouble("double", 0.0), 0.0000001f);
        verifyZeroInteractions(jedis);
        
        assertEquals("Test value", root.getString("string", null));
        verifyZeroInteractions(jedis);
        
        assertEquals(UUID.nameUUIDFromBytes("test".getBytes()), root.getUUID("uuid", null));
        verifyZeroInteractions(jedis);
    }
    
    @Test
    public void testBadPrimitives() {
        Jedis jedis = mock(Jedis.class);
        
        RedisConnection connection = mock(RedisConnection.class);
        stub(connection.getJedis()).toReturn(jedis);
        
        RedisSection root = new RedisSection(connection);
        
        // Places the values in cache
        root.set("bool", true);
        root.set("int", 123);
        root.set("long", 12345678901234L);
        root.set("float", 332.5f);
        root.set("double", 55446.1123442);
        root.set("string", "Test value");
        root.set("uuid", UUID.nameUUIDFromBytes("test".getBytes()));
        
        assertEquals(-1, root.getInt("bool", -1));
        assertEquals(-1, root.getInt("string", -1));
        assertEquals(-1, root.getInt("uuid", -1));
        
        assertEquals(false, root.getBoolean("string", false));

        verifyZeroInteractions(jedis);
    }
    
    @Test
    public void testList() {
        Jedis jedis = mock(Jedis.class);
        
        RedisConnection connection = mock(RedisConnection.class);
        stub(connection.getJedis()).toReturn(jedis);
        
        RedisSection root = new RedisSection(connection);
        
        // Test a string list
        List<String> stringList = Lists.newArrayList("1", "2", "3", "4", "5");
        root.set("test1", Lists.newArrayList(stringList));
        
        assertEquals(stringList, root.getListString("test1"));
        verifyZeroInteractions(jedis);
        
        // Try convert to an int list
        List<Integer> ints = Lists.newArrayList(1, 2, 3, 4, 5);
        assertEquals(ints, root.getListInt("test1"));
        verifyZeroInteractions(jedis);
    }
    
    @Test
    public void testSet() {
        Jedis jedis = mock(Jedis.class);
        
        RedisConnection connection = mock(RedisConnection.class);
        stub(connection.getJedis()).toReturn(jedis);
        
        RedisSection root = new RedisSection(connection);
        
        // Test a string set
        Set<String> stringSet = Sets.newHashSet("1", "2", "3", "4", "5");
        root.set("test1", Sets.newHashSet(stringSet));
        
        assertEquals(stringSet, root.getSetString("test1"));
        verifyZeroInteractions(jedis);
        
        // Try convert to an int set
        Set<Integer> ints = Sets.newHashSet(1, 2, 3, 4, 5);
        assertEquals(ints, root.getSetInt("test1"));
        verifyZeroInteractions(jedis);
    }
    
    @Test
    public void testMap() {
        Jedis jedis = mock(Jedis.class);
        
        RedisConnection connection = mock(RedisConnection.class);
        stub(connection.getJedis()).toReturn(jedis);
        
        RedisSection root = new RedisSection(connection);
        
        Map<String, String> values = Maps.newHashMap();
        values.put("val1", "asdf");
        values.put("val2", "a value");
        values.put("val3", "true");
        values.put("val4", "1111");
        
        root.set("map", values);
        
        Map<String, String> other = root.getMap("map");
        
        assertEquals(values, other);
        verifyZeroInteractions(jedis);
    }
    
    @Test
    public void testSimpleStorable() {
        Jedis jedis = mock(Jedis.class);
        
        RedisConnection connection = mock(RedisConnection.class);
        stub(connection.getJedis()).toReturn(jedis);
        
        RedisSection root = new RedisSection(connection);
        
        SimpleStorable storable = new SimpleStorable() {
            @Override
            public String save() {
                return "Test value";
            }
            
            @Override
            public void load(String value) {
                assertEquals("Test value", value);
            }
        };
        
        root.set("storable", storable);
        assertEquals("Test value", root.getString("storable"));
        root.getSimpleStorable("storable", storable);
        
        verifyZeroInteractions(jedis);
    }
}
