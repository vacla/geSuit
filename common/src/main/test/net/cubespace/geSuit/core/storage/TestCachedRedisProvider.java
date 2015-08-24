package net.cubespace.geSuit.core.storage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.cubespace.geSuit.core.util.Utilities;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TestCachedRedisProvider {
    @Test
    public void testCache() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.set("test", 1234);
        assertEquals((Integer)1234, provider.provideForSimple("test", Integer.class));
        
        provider.set("test2", "value");
        assertEquals("value", provider.provideForSimple("test2", String.class));
        
        assertTrue(provider.contains("test"));
        assertTrue(provider.contains("test2"));
        
        provider.remove("test");
        
        assertFalse(provider.contains("test"));
        
        verifyZeroInteractions(redis);
    }

    @Test
    public void testSimpleConvert() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.set("test", 1234);
        assertEquals((Integer)1234, provider.provideForSimple("test", Integer.class));
        assertEquals("1234", provider.provideForSimple("test", String.class));
        assertEquals((Float)1234f, provider.provideForSimple("test", Float.class));
        
        // Try invalid conversion
        assertNull(provider.provideForSimple("test", UUID.class));
        assertNull(provider.provideForList("test", String.class));
        assertNull(provider.provideForSet("test", String.class));
        assertNull(provider.provideForMap("test", String.class, String.class));
        
        verifyZeroInteractions(redis);
    }
    
    @Test
    public void testList() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        List<String> source = Lists.newArrayList("1", "2", "3", "4");
        provider.set("test", source);
        
        List<Integer> expected = Lists.newArrayList(1, 2, 3, 4);
        assertEquals(expected, provider.provideForList("test", Integer.class));
        
        assertSame(source, provider.provideForList("test", String.class));
    }
    
    @Test
    public void testSet() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Set<String> source = Sets.newHashSet("1", "2", "3", "4");
        provider.set("test", source);
        
        Set<Integer> expected = Sets.newHashSet(1, 2, 3, 4);
        assertEquals(expected, provider.provideForSet("test", Integer.class));
        
        assertSame(source, provider.provideForSet("test", String.class));
    }
    
    @Test
    public void testInvalidList() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.set("test", Lists.newArrayList("1", "2", "3", "4"));
        
        assertNull(provider.provideForList("test", UUID.class));
    }
    
    @Test
    public void testMap() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Map<String, String> map = Maps.newHashMap();
        map.put("1", "true");
        map.put("2", "false");
        map.put("3", "true");
        
        provider.set("test", map);
        Map<Integer, Boolean> converted = provider.provideForMap("test", Integer.class, Boolean.class);
        
        assertTrue(converted.get(1));
        assertFalse(converted.get(2));
        assertTrue(converted.get(3));
        
        Map<String, Boolean> partialConvert = provider.provideForMap("test", String.class, Boolean.class);
        
        assertTrue(partialConvert.get("1"));
        assertFalse(partialConvert.get("2"));
        assertTrue(partialConvert.get("3"));
        
        assertSame(map, provider.provideForMap("test", String.class, String.class));
    }
    
    @Test
    public void testMapStorable() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Storable storable = new Storable() {
            @Override
            public void save(Map<String, String> values) {
                values.put("1", "true");
                values.put("2", "false");
                values.put("3", "true");
            }
            
            @Override
            public void load(Map<String, String> values) {
            }
        };
        
        provider.set("test", storable);
        
        Map<String, String> expected = Maps.newHashMap();
        expected.put("1", "true");
        expected.put("2", "false");
        expected.put("3", "true");
        
        assertEquals(expected, provider.provideForMap("test", String.class, String.class));
    }
    
    @Test
    public void testMapStorableConvert() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Storable storable = new Storable() {
            @Override
            public void save(Map<String, String> values) {
                values.put("1", "true");
                values.put("2", "false");
                values.put("3", "true");
            }
            
            @Override
            public void load(Map<String, String> values) {
            }
        };
        
        provider.set("test", storable);
        
        Map<Integer, Boolean> expected = Maps.newHashMap();
        expected.put(1, true);
        expected.put(2, false);
        expected.put(3, true);
        
        assertEquals(expected, provider.provideForMap("test", Integer.class, Boolean.class));
    }
    
    @Test
    public void testEmptyCollections() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        List<String> emptyList = Lists.newArrayList();
        Set<String> emptySet = Sets.newHashSet();
        Map<String, String> emptyMap = Maps.newHashMap();
        
        provider.set("list", emptyList);
        provider.set("set", emptySet);
        provider.set("map", emptyMap);
        
        assertSame(emptyList, provider.provideForList("list", UUID.class));
        assertSame(emptySet, provider.provideForSet("set", UUID.class));
        assertSame(emptyMap, provider.provideForMap("map", Integer.class, UUID.class));
    }
    
    @Test
    public void testRedisContains() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        when(jedis.exists("test1")).thenReturn(true);
        when(jedis.exists("test2")).thenReturn(false);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // It should retrieve from redis
        assertTrue(provider.contains("test1"));
        assertFalse(provider.contains("test2"));
        
        verify(jedis, times(1)).exists("test1");
        verify(jedis, times(1)).exists("test2");
    }
    
    @Test(expected=StorageException.class)
    public void testRedisContainsException() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        when(jedis.exists("test1")).thenThrow(new JedisConnectionException("Exception"));
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.contains("test1");
    }
    
    @Test
    public void testRedisLoadSimple() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        when(jedis.get("test")).thenReturn("1234");
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // It should retrieve from redis
        assertEquals("1234", provider.provideForSimple("test", String.class));
        // This one should be in cache
        provider.provideForSimple("test", String.class);
        
        // Make sure it cached
        verify(jedis, times(1)).get("test");
    }
    
    @Test
    public void testRedisLoadAndConvertSimple() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        when(jedis.get("test")).thenReturn("1234");
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // It should retrieve from redis
        assertEquals((Integer)1234, provider.provideForSimple("test", Integer.class));
        verify(jedis, times(1)).get("test");
    }
    
    @Test
    public void testRedisLoadSimpleFail() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        when(jedis.get("test")).thenReturn(null);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // It should retrieve from redis
        assertNull(provider.provideForSimple("test", String.class));
        // This one should be in cache
        provider.provideForSimple("test", String.class);
        
        // Make sure it cached
        verify(jedis, times(1)).get("test");
    }
    
    @Test(expected=StorageException.class)
    public void testRedisLoadSimpleException() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        when(jedis.get("test1")).thenThrow(new JedisConnectionException("Exception"));
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.provideForSimple("test1", String.class);
    }
    
    @Test
    public void testRedisLoadList() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        List<String> targetList = Lists.newArrayList("5","2","8","1","0");
        
        when(jedis.llen("test")).thenReturn(5L);
        when(jedis.lrange("test", 0, 5L)).thenReturn(targetList);
        
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // It should retrieve from redis
        assertEquals(targetList, provider.provideForList("test", String.class));
        // This one should be in cache
        provider.provideForList("test", String.class);
        
        // Make sure it cached
        verify(jedis, times(1)).lrange(eq("test"), anyLong(), anyLong());
    }
    
    @Test
    public void testRedisLoadListConvert() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        List<String> targetList = Lists.newArrayList("5","2","8","1","0");
        List<Integer> expected = Lists.newArrayList(5,2,8,1,0);
        
        when(jedis.llen("test")).thenReturn(5L);
        when(jedis.lrange("test", 0, 5L)).thenReturn(targetList);
        
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // It should retrieve from redis
        assertEquals(expected, provider.provideForList("test", Integer.class));
    }
    
    @Test
    public void testRedisLoadListBadConvert() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        List<String> targetList = Lists.newArrayList("5","2","8","1","0");
        List<Integer> expected = Lists.newArrayList(5,2,8,1,0);
        
        when(jedis.llen("test")).thenReturn(5L);
        when(jedis.lrange("test", 0, 5L)).thenReturn(targetList);
        
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // It should retrieve from redis
        assertNull(provider.provideForList("test", UUID.class));
        // This should now be from cache
        assertEquals(expected, provider.provideForList("test", Integer.class));
        
        verify(jedis, times(1)).lrange(eq("test"), anyLong(), anyLong());
    }
    
    @Test
    public void testRedisLoadListMissing() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        when(jedis.llen("test")).thenReturn(0L);
        when(jedis.lrange(eq("test"), anyLong(), anyLong())).thenReturn(null);
        
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // It should retrieve from redis
        assertNull(provider.provideForList("test", String.class));
        // This should now be from cache
        assertNull(provider.provideForList("test", String.class));
        
        verify(jedis, times(1)).lrange(eq("test"), anyLong(), anyLong());
    }
    
    @Test(expected=StorageException.class)
    public void testRedisLoadListException() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        when(jedis.llen("test1")).thenThrow(new JedisConnectionException("Exception"));
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.provideForList("test1", String.class);
    }
    
    @Test
    public void testRedisLoadMap() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Map<String, String> rawMap = Maps.newHashMap();
        rawMap.put("1", "true");
        rawMap.put("2", "false");
        rawMap.put("3", "false");
        
        when(jedis.hgetAll("test")).thenReturn(rawMap);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // Should load from redis
        assertEquals(rawMap, provider.provideForMap("test", String.class, String.class));
        // Should be from cache
        provider.provideForMap("test", String.class, String.class);
        
        verify(jedis, times(1)).hgetAll("test");
    }
    
    @Test
    public void testRedisLoadMapConvert() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Map<String, String> rawMap = Maps.newHashMap();
        rawMap.put("1", "true");
        rawMap.put("2", "false");
        rawMap.put("3", "false");
        
        when(jedis.hgetAll("test")).thenReturn(rawMap);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Map<Integer, Boolean> expectedMap = Maps.newHashMap();
        expectedMap.put(1, true);
        expectedMap.put(2, false);
        expectedMap.put(3, false);
        
        // Should load from redis
        assertEquals(expectedMap, provider.provideForMap("test", Integer.class, Boolean.class));
    }
    
    @Test
    public void testRedisLoadMapConvertBad() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Map<String, String> rawMap = Maps.newHashMap();
        rawMap.put("1", "true");
        rawMap.put("2", "false");
        rawMap.put("3", "false");
        
        when(jedis.hgetAll("test")).thenReturn(rawMap);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Map<Integer, Boolean> expectedMap = Maps.newHashMap();
        expectedMap.put(1, true);
        expectedMap.put(2, false);
        expectedMap.put(3, false);
        
        // Should load from redis
        assertNull(provider.provideForMap("test", Integer.class, UUID.class));
        // Should be in cache
        assertEquals(expectedMap, provider.provideForMap("test", Integer.class, Boolean.class));
        
        verify(jedis, times(1)).hgetAll("test");
    }
    
    @Test
    public void testRedisLoadMapMissing() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        when(jedis.hgetAll("test")).thenReturn(null);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // Should load from redis
        assertNull(provider.provideForMap("test", String.class, String.class));
        // Should be in cache
        assertNull(provider.provideForMap("test", String.class, String.class));
        
        verify(jedis, times(1)).hgetAll("test");
    }
    
    @Test(expected=StorageException.class)
    public void testRedisLoadMapException() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        when(jedis.hgetAll("test1")).thenThrow(new JedisConnectionException("Exception"));
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.provideForMap("test1", String.class, String.class);
    }
    
    @Test
    public void testRedisLoadPartialMap() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        List<String> rawValues = Lists.newArrayList("value1", "value2", "value3");
        
        when(jedis.hmget("test", "1", "2", "3")).thenReturn(rawValues);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // Should load from redis
        Map<String, String> expected = Maps.newHashMap();
        expected.put("1", "value1");
        expected.put("2", "value2");
        expected.put("3", "value3");
        assertEquals(expected, provider.provideForPartialMap("test", String.class, String.class, Arrays.asList("1", "2", "3")));
    }
    
    @Test
    public void testPartialMapExisting() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Map<String, String> existing = Maps.newHashMap();
        existing.put("1", "value1");
        existing.put("2", "value2");
        existing.put("3", "value3");
        existing.put("4", "value4");
        existing.put("5", "value5");
        
        provider.set("test", existing);
        
        Map<String, String> expected = Maps.newHashMap();
        expected.put("1", "value1");
        expected.put("2", "value2");
        expected.put("3", "value3");
        assertEquals(expected, provider.provideForPartialMap("test", String.class, String.class, Arrays.asList("1", "2", "3")));
    }
    
    @Test
    public void testPartialMapExistingConvert() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Map<Integer, String> existing = Maps.newHashMap();
        existing.put(1, "value1");
        existing.put(2, "value2");
        existing.put(3, "value3");
        existing.put(4, "value4");
        existing.put(5, "value5");
        
        provider.set("test", existing);
        
        Map<String, String> expected = Maps.newHashMap();
        expected.put("1", "value1");
        expected.put("2", "value2");
        expected.put("3", "value3");
        assertEquals(expected, provider.provideForPartialMap("test", String.class, String.class, Arrays.asList("1", "2", "3")));
    }
    
    @Test
    public void testPartialMapExistingConvert2() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Map<String, String> existing = Maps.newHashMap();
        existing.put("1", "value1");
        existing.put("2", "value2");
        existing.put("3", "value3");
        existing.put("4", "value4");
        existing.put("5", "value5");
        
        provider.set("test", existing);
        
        Map<Integer, String> expected = Maps.newHashMap();
        expected.put(1, "value1");
        expected.put(2, "value2");
        expected.put(3, "value3");
        assertEquals(expected, provider.provideForPartialMap("test", Integer.class, String.class, Arrays.asList(1, 2, 3)));
    }
    
    @Test
    public void testPartialMapExistingStorable() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Storable testStorable = new Storable() {
            @Override
            public void save(Map<String, String> values) {
                values.put("1", "value1");
                values.put("2", "value2");
                values.put("3", "value3");
                values.put("4", "value4");
                values.put("5", "value5");
            }
            
            @Override
            public void load(Map<String, String> values) {
            }
        };
        
        provider.set("test", testStorable);
        
        Map<String, String> expected = Maps.newHashMap();
        expected.put("1", "value1");
        expected.put("2", "value2");
        expected.put("3", "value3");
        assertEquals(expected, provider.provideForPartialMap("test", String.class, String.class, Arrays.asList("1", "2", "3")));
    }
    
    @Test
    public void testPartialMapExistingStorableConvert() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Storable testStorable = new Storable() {
            @Override
            public void save(Map<String, String> values) {
                values.put("1", "value1");
                values.put("2", "value2");
                values.put("3", "value3");
                values.put("4", "value4");
                values.put("5", "value5");
            }
            
            @Override
            public void load(Map<String, String> values) {
            }
        };
        
        provider.set("test", testStorable);
        
        Map<Integer, String> expected = Maps.newHashMap();
        expected.put(1, "value1");
        expected.put(2, "value2");
        expected.put(3, "value3");
        assertEquals(expected, provider.provideForPartialMap("test", Integer.class, String.class, Arrays.asList(1, 2, 3)));
    }
    
    @Test
    public void testAppendOnExisting() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        List<String> existing = Lists.newArrayList("existing");
        provider.set("test", existing);
        
        provider.appendCollection("test", "value1", List.class, String.class);
        provider.appendCollection("test", "value2", List.class, String.class);
        
        List<String> expected = Lists.newArrayList("existing", "value1", "value2");
        
        assertEquals(expected, provider.provideForList("test", String.class));
    }
    
    @Test
    public void testAppendOnLoad() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        when(jedis.llen("test")).thenReturn(0L);
        when(jedis.lrange("test", 0L, 0L)).thenReturn(Lists.<String>newArrayList());
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.appendCollection("test", "value1", List.class, String.class);
        provider.appendCollection("test", "value2", List.class, String.class);
        
        List<String> expected = Lists.newArrayList("value1", "value2");
        
        // Now these should be appended onto the blank value loaded from redis
        assertEquals(expected, provider.provideForList("test", String.class));
    }
    
    @Test
    public void testAppendConvertOnExisting() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        List<String> existing = Lists.newArrayList("1");
        provider.set("test", existing);
        
        provider.appendCollection("test", 2, List.class, Integer.class);
        provider.appendCollection("test", 3, List.class, Integer.class);
        
        List<String> expected = Lists.newArrayList("1", "2", "3");
        
        assertEquals(expected, provider.provideForList("test", String.class));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testAppendConvertOnExistingFail() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        List<UUID> existing = Lists.newArrayList(UUID.randomUUID());
        provider.set("test", existing);
        
        provider.appendCollection("test", 1, List.class, Integer.class);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testAppendConvertFail() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.appendCollection("test", 1, List.class, Integer.class);
        provider.appendCollection("test", "value", List.class, String.class);
    }
    
    @Test
    public void testAppendConvert() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.appendCollection("test", 1, List.class, Integer.class);
        provider.appendCollection("test", "2", List.class, String.class);
        // If it got here it converted and appended the value onto the marker
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testAppendBadExist() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.set("test", 1);
        provider.appendCollection("test", 1, List.class, Integer.class);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testAppendBadExist2() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.set("test", Sets.newHashSet());
        provider.appendCollection("test", 1, List.class, Integer.class);
    }
    
    @Test
    public void testSimpleSave() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Pipeline pipe = mock(FakePipeline.class);
        
        when(jedis.pipelined()).thenReturn(pipe);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.set("test1", 5);
        provider.set("test2", "string");
        UUID id = UUID.randomUUID();
        provider.set("test3", id);
        
        provider.saveChanges();
        
        // Verify
        verify(pipe, times(1)).set("test1", "5");
        verify(pipe, times(1)).set("test2", "string");
        verify(pipe, times(1)).set("test3", Utilities.toString(id));
        verify(pipe, times(1)).sync();
    }
    
    @Test
    public void testListSave() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Pipeline pipe = mock(FakePipeline.class);
        
        when(jedis.pipelined()).thenReturn(pipe);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        List<String> stringList = Lists.newArrayList("value1", "value2", "value3");
        List<Integer> intList = Lists.newArrayList(1,2,3,4,5);
        provider.set("test1", stringList);
        provider.set("test2", intList);
        
        provider.saveChanges();
        
        // Verify
        verify(pipe, times(1)).del("test1");
        verify(pipe, times(1)).lpush("test1", new String[] {"value1", "value2", "value3"});
        verify(pipe, times(1)).del("test2");
        verify(pipe, times(1)).lpush("test2", new String[] {"1", "2", "3", "4", "5"});
        
        verify(pipe, times(1)).sync();
    }
    
    @Test
    public void testSetSave() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Pipeline pipe = mock(FakePipeline.class);
        
        when(jedis.pipelined()).thenReturn(pipe);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Set<String> stringSet = Sets.newHashSet("value1", "value2", "value3");
        Set<Integer> intSet = Sets.newHashSet(1,2,3,4,5);
        provider.set("test1", stringSet);
        provider.set("test2", intSet);
        
        provider.saveChanges();
        
        // Verify
        verify(pipe, times(1)).del("test1");
        verify(pipe, times(1)).sadd(eq("test1"), (String[])anyVararg());
        verify(pipe, times(1)).del("test2");
        verify(pipe, times(1)).sadd(eq("test2"), (String[])anyVararg());
        
        verify(pipe, times(1)).sync();
    }
    
    @Test
    public void testSetMap() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Pipeline pipe = mock(FakePipeline.class);
        
        when(jedis.pipelined()).thenReturn(pipe);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Map<String, String> stringMap = Maps.newHashMap();
        Map<Integer, Boolean> intMap = Maps.newHashMap();
        
        stringMap.put("1", "val1");
        stringMap.put("2", "val2");
        stringMap.put("3", "val3");
        
        intMap.put(1, true);
        intMap.put(2, false);
        intMap.put(3, true);
        
        provider.set("test1", stringMap);
        provider.set("test2", intMap);
        
        provider.saveChanges();
        
        // Verify
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        
        verify(pipe, times(1)).hmset(eq("test1"), captor.capture());
        assertEquals(stringMap, captor.getValue());
        
        verify(pipe, times(1)).hmset(eq("test2"), captor.capture());
        Map<String, String> intMapResult = Maps.newHashMap();
        intMapResult.put("1", "true");
        intMapResult.put("2", "false");
        intMapResult.put("3", "true");
        assertEquals(intMapResult, captor.getValue());
        
        verify(pipe, times(1)).sync();
    }
    
    @Test
    public void testSaveTypes() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Pipeline pipe = mock(FakePipeline.class);
        Transaction transaction = mock(FakeTransaction.class);
        
        when(jedis.pipelined()).thenReturn(pipe);
        when(jedis.multi()).thenReturn(transaction);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        List<String> list = Lists.newArrayList("1", "value");
        UUID id = UUID.randomUUID();
        
        provider.set("test1", 5);
        provider.set("test2", "string");
        provider.set("test3", list);
        provider.set("test4", id);
        
        provider.saveChanges();
        
        provider.set("test1", 5);
        provider.set("test2", "string");
        provider.set("test3", list);
        provider.set("test4", id);
        
        provider.saveChangesAtomically();
        
        // Verify that both work the same
        verify(pipe, times(1)).set("test1", "5");
        verify(pipe, times(1)).set("test2", "string");
        verify(pipe, times(1)).lpush("test3", Iterables.toArray(list, String.class));
        verify(pipe, times(1)).set("test4", Utilities.toString(id));
        verify(pipe, times(1)).sync();
        
        verify(transaction, times(1)).set("test1", "5");
        verify(transaction, times(1)).set("test2", "string");
        verify(transaction, times(1)).lpush("test3", Iterables.toArray(list, String.class));
        verify(transaction, times(1)).set("test4", Utilities.toString(id));
        verify(transaction, times(1)).exec();
    }
    
    @Test
    public void testDelete() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Pipeline pipe = mock(FakePipeline.class);
        
        when(jedis.pipelined()).thenReturn(pipe);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.remove("test");
        provider.saveChanges();
        
        verify(pipe, times(1)).del("test");
    }
    
    @Test
    public void testSaveIngoreUnmodified() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Pipeline pipe = mock(FakePipeline.class);
        
        when(jedis.pipelined()).thenReturn(pipe);
        
        when(jedis.get("test1")).thenReturn("1");
        when(jedis.get("test2")).thenReturn("true");
        
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.provideForSimple("test1", Integer.class);
        provider.provideForSimple("test2", Boolean.class);
        
        provider.set("test2", "string");
        
        provider.saveChanges();
        
        // Verify
        verify(pipe, never()).set(eq("test1"), anyString());
        verify(pipe, times(1)).set("test2", "string");
        
        verify(pipe, times(1)).sync();
    }
    
    @Test
    public void testAppendListSave() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        Pipeline pipe = mock(FakePipeline.class);
        
        when(jedis.pipelined()).thenReturn(pipe);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.appendCollection("test", 2, List.class, Integer.class);
        provider.appendCollection("test", 3, List.class, Integer.class);
        
        provider.saveChanges();
        
        verify(pipe, never()).del("test");
        verify(pipe, times(1)).lpush("test", new String[] {"2", "3"});
        verify(pipe, times(1)).sync();
    }
    
    @Test
    public void testAppendSetSave() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        Pipeline pipe = mock(FakePipeline.class);
        
        when(jedis.pipelined()).thenReturn(pipe);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        provider.appendCollection("test", 2, Set.class, Integer.class);
        
        provider.saveChanges();
        
        verify(pipe, never()).del("test");
        verify(pipe, times(1)).sadd("test", new String[] {"2"});
        verify(pipe, times(1)).sync();
    }
    
    @Test
    public void testStorable() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Map<String, String> content = Maps.newHashMap();
        content.put("1", "true");
        content.put("2", "false");
        content.put("3", "true");
        
        StorableTest test = new StorableTest(content);
        
        provider.set("test", test);
        
        // Make sure it retrieves from cache
        StorableTest retrieved = provider.provideForStorable("test", StorableTest.class, null);
        assertSame(test, retrieved);
        
        // Try populating existing one
        StorableTest populated = new StorableTest();
        retrieved = provider.provideForStorable("test", StorableTest.class, populated);
        assertSame(populated, retrieved);
        assertEquals(test, populated);
    }
    
    @Test
    public void testStorableFromMap() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Map<String, String> content = Maps.newHashMap();
        content.put("1", "true");
        content.put("2", "false");
        content.put("3", "true");
        
        provider.set("test", content);
        
        StorableTest retrieved = provider.provideForStorable("test", StorableTest.class, null);
        assertEquals(content, retrieved.values);
        
        StorableTest populated = new StorableTest();
        retrieved = provider.provideForStorable("test", StorableTest.class, populated);
        assertEquals(content, populated.values);
    }
    
    @Test
    public void testStorableFromMapConvert() {
        RedisConnection redis = mock(RedisConnection.class);
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        Map<Integer, Boolean> content = Maps.newHashMap();
        content.put(1, true);
        content.put(2, false);
        content.put(3, true);
        
        Map<String, String> expected = Maps.newHashMap();
        expected.put("1", "true");
        expected.put("2", "false");
        expected.put("3", "true");
        
        provider.set("test", content);
        
        StorableTest retrieved = provider.provideForStorable("test", StorableTest.class, null);
        assertEquals(expected, retrieved.values);
        
        StorableTest populated = new StorableTest();
        retrieved = provider.provideForStorable("test", StorableTest.class, populated);
        assertEquals(expected, populated.values);
    }
    
    @Test
    public void testRedisLoadStorable() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Map<String, String> content = Maps.newHashMap();
        content.put("1", "true");
        content.put("2", "false");
        content.put("3", "true");
        
        when(jedis.hgetAll("test")).thenReturn(content);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // Make sure it retrieves from redis
        StorableTest retrieved = provider.provideForStorable("test", StorableTest.class, null);
        assertEquals(content, retrieved.values);
        
        verify(jedis, times(1)).hgetAll("test");
    }
    
    @Test
    public void testRedisLoadStorablePopulate() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Map<String, String> content = Maps.newHashMap();
        content.put("1", "true");
        content.put("2", "false");
        content.put("3", "true");
        
        when(jedis.hgetAll("test")).thenReturn(content);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        // Make sure it retrieves from redis
        StorableTest populated = new StorableTest();
        provider.provideForStorable("test", StorableTest.class, populated);
        assertEquals(content, populated.values);
        
        verify(jedis, times(1)).hgetAll("test");
    }
    
    @Test
    public void testRedisLoadStorableFail() {
        RedisConnection redis = mock(RedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        
        Map<String, String> content = Maps.newHashMap();
        content.put("1", "true");
        content.put("2", "false");
        content.put("3", "true");
        
        when(jedis.hgetAll("test")).thenReturn(content);
        when(redis.getJedis()).thenReturn(jedis);
        
        CachedRedisProvider provider = new CachedRedisProvider(redis);
        
        StorableErrorTest test = provider.provideForStorable("test", StorableErrorTest.class, null);
        assertNull(test);
        
        // Now make sure it cached the map anyway
        
        Map<String, String> map = provider.provideForMap("test", String.class, String.class);
        assertEquals(content, map);
        
        verify(jedis, times(1)).hgetAll("test");
    }
    
    // Due to a bug in CGLIB, the non public base class of pipeline cannot be mocked
    // so we have to pull these methods out
    public static class FakePipeline extends Pipeline {
        @Override
        public Response<String> set(String key, String value) {
            return super.set(key, value);
        }
        
        @Override
        public Response<Long> lpush(String key, String... string) {
            return super.lpush(key, string);
        }
        
        @Override
        public Response<Long> sadd(String key, String... member) {
            return super.sadd(key, member);
        }
        
        @Override
        public Response<Long> del(String key) {
            return super.del(key);
        }
        
        @Override
        public Response<String> hmset(String key, Map<String, String> hash) {
            return super.hmset(key, hash);
        }
    }
    
    public static class FakeTransaction extends Transaction {
        @Override
        public Response<String> set(String key, String value) {
            return super.set(key, value);
        }
        
        @Override
        public Response<Long> lpush(String key, String... string) {
            return super.lpush(key, string);
        }
        
        @Override
        public Response<Long> sadd(String key, String... member) {
            return super.sadd(key, member);
        }
        
        @Override
        public Response<Long> del(String key) {
            return super.del(key);
        }
        
        @Override
        public Response<String> hmset(String key, Map<String, String> hash) {
            return super.hmset(key, hash);
        }
    }
    
    public static class StorableTest implements Storable {
        public Map<String, String> values;
        
        public StorableTest() {
        }
        
        public StorableTest(Map<String,String> values) {
            this.values = values;
        }
        
        @Override
        public void save(Map<String, String> values) {
            values.putAll(this.values);
        }

        @Override
        public void load(Map<String, String> values) {
            this.values = Maps.newHashMap(values);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StorableTest)) {
                return false;
            }
            
            return values.equals(((StorableTest)obj).values);
        }
    }
    
    public static class StorableErrorTest implements Storable {
        @Override
        public void save(Map<String, String> values) {
        }

        @Override
        public void load(Map<String, String> values) {
            throw new IllegalArgumentException();
        }
    }
}
