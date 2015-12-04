package net.cubespace.geSuit.core.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.RedisPipeline;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

@SuppressWarnings("unchecked")
class CachedRedisProvider {
    private final RedisConnection redis;
    
    private final Map<String, Object> cache;
    private final Set<String> modified;
    private final Set<String> hasLoaded;
    
    public CachedRedisProvider(RedisConnection redis) {
        this.redis = redis;
        
        cache = Maps.newHashMap();
        hasLoaded = Sets.newHashSet();
        modified = Sets.newHashSet();
    }
    
    private StorageException handleJedisException(Jedis jedis, JedisException e) {
        if (e instanceof JedisConnectionException) {
            redis.returnJedis(jedis, e);
            return new StorageException("Redis connection is unavailable");
        } else {
            return new StorageException("Redis error: " + e.getMessage());
        }
    }
    
    public void invalidate() {
        cache.clear();
        hasLoaded.clear();
        modified.clear();
    }
    
    public boolean contains(String key) throws StorageException {
        if (cache.containsKey(key)) {
            return !(cache.get(key) instanceof DeletionMarker);
        }
        
        // Dont check redis again if it was loaded
        if (hasLoaded.contains(key)) {
            return false;
        }
        
        // Check redis
        Jedis jedis = null;
        try {
            jedis = redis.getJedis();
            return jedis.exists(key);
        } catch (JedisException e) {
            throw handleJedisException(jedis, e);
        } finally {
            redis.returnJedis(jedis);
        }
    }
    
    private <T> T convert(Object rawValue, Class<T> type) {
        try {
            if (type.isInstance(rawValue)) {
                return (T)rawValue;
            } else if (rawValue instanceof String) {
                return DataConversion.fromString((String)rawValue, type);
            } else if (rawValue != null) {
                String stringValue = DataConversion.toString(rawValue);
                return DataConversion.fromString(stringValue, type);
            } else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Provides a value in the specified type from either redis or the cache
     * @param key The key to get
     * @param type The type of value to get. This should be a simple type (ie. not a collection of values)
     * @return The retrieved value or null if it didnt exist
     */
    public <T> T provideForSimple(String key, Class<T> type) throws StorageException {
        // Try load from cache
        if (cache.containsKey(key)) {
            Object cachedValue = cache.get(key);
            if (!(cachedValue instanceof DeletionMarker)) {
                T converted = convert(cachedValue, type);
                
                return converted;
            }
        }
        
        // This value has been loaded before but there was no value
        if (hasLoaded.contains(key)) {
            return null;
        }
        
        // Load from redis
        Jedis jedis = null;
        try {
            jedis = redis.getJedis();
            String stringValue = jedis.get(key);
            hasLoaded.add(key); // mark it so it never loads again
            if (stringValue != null) {
                cache.put(key, stringValue);
                return convert(stringValue, type);
            }
        } catch (JedisException e) {
            throw handleJedisException(jedis, e);
        } finally {
            redis.returnJedis(jedis);
        }
        
        // no value
        return null;
    }
    
    public void set(String key, Object value) {
        cache.put(key, value);
        modified.add(key);
    }
    
    public void remove(String key) {
        cache.put(key, new DeletionMarker());
        modified.add(key);
    }
    
    private <K,V> Map<K,V> convertMap(Map<?, ?> source, Class<K> keyType, Class<V> valueType) {
        Map<K,V> map = Maps.newHashMapWithExpectedSize(source.size());
        
        for (Entry<?, ?> entry : source.entrySet()) {
            K keyValue = convert(entry.getKey(), keyType);
            V value = convert(entry.getValue(), valueType);
            
            if (keyValue != null && value != null) {
                map.put(keyValue, value);
            } else {
                return null;
            }
        }
        
        return map;
    }
    
    public <K,V> Map<K,V> provideForMap(String key, Class<K> keyType, Class<V> valueType) throws StorageException {
        // Try load from cache
        if (cache.containsKey(key)) {
            Object cachedValue = cache.get(key);
            
            if (!(cachedValue instanceof DeletionMarker)) {
                if (cachedValue instanceof Map<?, ?>) {
                    Map<?,?> rawMap = (Map<?,?>)cachedValue;
                    
                    if (rawMap.isEmpty()) {
                        return (Map<K,V>)rawMap;
                    } else {
                        // Check KV types
                        Entry<?, ?> first = Iterables.getFirst(rawMap.entrySet(), null);
                        if (keyType.isInstance(first.getKey()) && valueType.isInstance(first.getValue())) {
                            // types match its good
                            return (Map<K,V>)rawMap;
                        } else {
                            // Convert the map
                            return convertMap(rawMap, keyType, valueType);
                        }
                    }
                } else if (cachedValue instanceof Storable) {
                    Map<String, String> rawMap = Maps.newHashMap();
                    ((Storable)cachedValue).save(rawMap);
                    
                    if (keyType.equals(String.class) && valueType.equals(String.class)) {
                        return (Map<K,V>)rawMap;
                    } else {
                        return convertMap(rawMap, keyType, valueType);
                    }
                } else {
                    // If the cached value is not a map then we are not interested
                    return null;
                }
            }
        }
        
        // This value has been loaded before but there was no value
        if (hasLoaded.contains(key)) {
            return null;
        }
        
        // Load from redis
        Jedis jedis = null;
        try {
            jedis = redis.getJedis();
            Map<String, String> rawMap = jedis.hgetAll(key);
            hasLoaded.add(key); // mark it so it never loads again
            if (rawMap != null) {
                cache.put(key, rawMap);
                
                return convertMap(rawMap, keyType, valueType);
            }
        } catch (JedisException e) {
            throw handleJedisException(jedis, e);
        } finally {
            redis.returnJedis(jedis);
        }
        
        // no value
        return null;
    }
    
    public <K,V> Map<K,V> provideForPartialMap(String key, Class<K> keyType, Class<V> valueType, final Collection<K> keys) throws StorageException {
        // Try load from cache
        if (cache.containsKey(key)) {
            Object cachedValue = cache.get(key);
            Map<K,V> targetMap;
            
            if (!(cachedValue instanceof DeletionMarker)) {
                if (cachedValue instanceof Map<?, ?>) {
                    Map<?,?> rawMap = (Map<?,?>)cachedValue;
                    if (rawMap.isEmpty()) {
                        targetMap = (Map<K,V>)rawMap;
                    } else {
                        // Check KV types
                        Entry<?, ?> first = Iterables.getFirst(rawMap.entrySet(), null);
                        if (keyType.isInstance(first.getKey()) && valueType.isInstance(first.getValue())) {
                            // types match its good
                            targetMap = (Map<K,V>)rawMap;
                        } else {
                            // Convert the map
                            targetMap = convertMap(rawMap, keyType, valueType);
                        }
                    }
                } else if (cachedValue instanceof Storable) {
                    Map<String, String> rawMap = Maps.newHashMap();
                    ((Storable)cachedValue).save(rawMap);
                    
                    if (keyType.equals(String.class) && valueType.equals(String.class)) {
                        targetMap = (Map<K,V>)rawMap;
                    } else {
                        targetMap = convertMap(rawMap, keyType, valueType);
                    }
                } else {
                    // If the cached value is not a map then we are not interested
                    return null;
                }
                
                // Limit to target fields
                return Maps.filterKeys(targetMap, new Predicate<K>() {
                    @Override
                    public boolean apply(K key) {
                        return keys.contains(key);
                    }
                });
            }
        }
        
        // This value has been loaded before but there was no value
        if (hasLoaded.contains(key)) {
            return null;
        }
        
        // Load from redis
        Jedis jedis = null;
        try {
            jedis = redis.getJedis();
            List<String> stringKeys = Lists.newArrayList(Collections2.transform(keys, DataConversion.getConverter(keyType).reverse()));
            List<String> rawValues = jedis.hmget(key, Iterables.toArray(stringKeys, String.class));
            
            Map<String, String> rawMap = Maps.newHashMap();
            for (int i = 0; i < stringKeys.size(); ++i) {
                rawMap.put(stringKeys.get(i), rawValues.get(i));
            }
            
            // Dont cache partial map loads so more can be loaded later
            
            if (rawMap != null) {
                return convertMap(rawMap, keyType, valueType);
            }
        } catch (JedisException e) {
            throw handleJedisException(jedis, e);
        } finally {
            redis.returnJedis(jedis);
        }
        
        // no value
        return null;
    }
    
    /**
     * Provides the value at {@code key} as a Storable.
     * @param key The key to load from
     * @param type The type of storable to load
     * @param optionalInstance The instance to load into or null to create a new one
     * @return The actual instance loaded into or null if it could not
     */
    public <T extends Storable> T provideForStorable(String key, Class<T> type, T optionalInstance) {
        // Try load from cache
        if (cache.containsKey(key)) {
            Object cachedValue = cache.get(key);
            
            if (!(cachedValue instanceof DeletionMarker)) {
                // The correctly typed storable is present
                if (type.isInstance(cachedValue)) {
                    // Copy into provided storable if present
                    if (optionalInstance != null) {
                        Map<String, String> values = Maps.newHashMap();
                        ((Storable)cachedValue).save(values);
                        optionalInstance.load(values);
                        
                        return optionalInstance;
                    } else {
                        return (T)cachedValue;
                    }
                } else if (cachedValue instanceof Storable) {
                    // Incorrectly typed storable
                    return null;
                } else if (cachedValue instanceof Map<?, ?>) {
                    // Attempt to load from map
                    Map<?,?> rawMap = (Map<?,?>)cachedValue;
                    Map<String, String> values;
                    
                    if (rawMap.isEmpty()) {
                        values = (Map<String,String>)rawMap;
                    } else {
                        // Check KV types
                        Entry<?, ?> first = Iterables.getFirst(rawMap.entrySet(), null);
                        if (first.getKey() instanceof String && first.getValue() instanceof String) {
                            // types match its good
                            values = (Map<String,String>)rawMap;
                        } else {
                            // Convert the map
                            values = convertMap(rawMap, String.class, String.class);
                        }
                    }
                    
                    if (optionalInstance != null) {
                        try {
                            optionalInstance.load(values);
                            return optionalInstance;
                        } catch (Exception e) {
                            // Not valid
                            return null;
                        }
                    } else {
                        // Create one
                        T storable = createStorable(type);
                        if (storable != null) {
                            try {
                                storable.load(values);
                                return storable;
                            } catch (Exception e) {
                                // Not valid
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                } else {
                    // If the cached value is not a storable then we are not interested
                    return null;
                }
            }
        }
        
        // This value has been loaded before but there was no value
        if (hasLoaded.contains(key)) {
            return null;
        }
        
        // Load from redis
        Jedis jedis = null;
        try {
            jedis = redis.getJedis();
            Map<String, String> rawMap = jedis.hgetAll(key);
            hasLoaded.add(key); // mark it so it never loads again
            if (rawMap != null) {
                // Get the storable
                T storable = optionalInstance;
                if (storable == null) {
                    storable = createStorable(type);
                }
                
                // Just in case it failed to create, dont waste the loaded map
                if (storable == null) {
                    cache.put(key, rawMap);
                    return null;
                }
                
                // Attempt to load the storable. If successful cache the storable else the map
                try {
                    storable.load(rawMap);
                    cache.put(key, storable);
                    return storable;
                } catch (Exception e) {
                    // Not valid
                    cache.put(key, rawMap);
                    return null;
                }
            }
        } catch (JedisException e) {
            throw handleJedisException(jedis, e);
        } finally {
            redis.returnJedis(jedis);
        }
        
        // no value
        return null;
    }
    
    private <T extends Storable> T createStorable(Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // NOTE: This only creates a copy of the list so any changes made will NOT apply to the source
    private <T extends Collection<E>, E> T transformCollection(Collection<?> source, Class<T> collectionType, Class<E> elementType) {
        T collection;
        if (List.class.equals(collectionType)) {
            collection = (T)Lists.newArrayList();
        } else if (Set.class.equals(collectionType)) {
            collection = (T)Sets.newHashSet();
        } else {
            throw new AssertionError();
        }
        
        for (Object item : source) {
            E converted = convert(item, elementType);
            if (converted == null) {
                return null;
            } else {
                collection.add(converted);
            }
        }
        
        return collection;
    }
    
    private Collection<String> loadCollection(String key, Class<?> collectionType) throws StorageException {
        Jedis jedis = null;
        try {
            jedis = redis.getJedis();
            if (collectionType.equals(List.class)) {
                return jedis.lrange(key, 0, jedis.llen(key));
            } else if (collectionType.equals(Set.class)) {
                return jedis.smembers(key);
            } else {
                throw new AssertionError();
            }
        } catch (JedisException e) {
            throw handleJedisException(jedis, e);
        } finally {
            redis.returnJedis(jedis);
        }
    }
    
    /**
     * Provides a collection of values from either redis or the cache.
     * This will load it into a set. This may or may not be a readonly set
     * so use {@link #set(String, Object)} after changes 
     * @param key The key to get
     * @param elementType The type of element held in the set
     * @return A set with the values or null
     */
    public <E> Set<E> provideForSet(String key, Class<E> elementType) {
        return provideForCollection(key, Set.class, elementType);
    }
    
    /**
     * Provides a collection of values from either redis or the cache.
     * This will load it into a list. This may or may not be a readonly list
     * so use {@link #set(String, Object)} after changes 
     * @param key The key to get
     * @param elementType The type of element held in the list
     * @return A list with the values or null
     */
    public <E> List<E> provideForList(String key, Class<E> elementType) {
        return provideForCollection(key, List.class, elementType);
    }
    
    private <T extends Collection<E>,E> T provideForCollection(String key, Class<T> collectionType, Class<E> elementType) throws StorageException {
        Preconditions.checkArgument(collectionType.equals(List.class) || collectionType.equals(Set.class));
        
        // Added from markers
        List<E> appendedElements = Collections.emptyList();
        // Try load from cache
        if (cache.containsKey(key)) {
            Object cachedValue = cache.get(key);
            
            if (!(cachedValue instanceof DeletionMarker)) {
                // cachedValue is a collection
                if (collectionType.isInstance(cachedValue)) {
                    Collection<?> rawCollection = (Collection<?>)cachedValue;
                    
                    // Empty collections will work as any type
                    if (rawCollection.isEmpty()) {
                        return (T)rawCollection;
                    } else {
                        // Check that the elements are of the desired type
                        Object first = Iterables.getFirst(rawCollection, null);
                        if (elementType.isInstance(first)) {
                            // element type matches so we can use this collection
                            return (T)rawCollection;
                        } else {
                            // We will have to transform it
                            return transformCollection(rawCollection, collectionType, elementType);
                        }
                    }
                // Cached value is a marker
                } else if (cachedValue instanceof CollectionMarker) {
                    // Since we are retrieving the collection, we will add and appended items after loading the real collection
                    CollectionMarker marker = (CollectionMarker)cachedValue;
                    if (marker.collectionType.equals(collectionType)) {
                        appendedElements = Lists.newArrayListWithCapacity(marker.valuesToAdd.size());
                        for (Object value : marker.valuesToAdd) {
                            appendedElements.add(convert(value, elementType));
                        }
                    }
                } else {
                    // If the cached value is not a collection or marker then we are not interested
                    return null;
                }
            }
        }
        
        // This value has been loaded before but there was no value
        if (hasLoaded.contains(key)) {
            return null;
        }
        
        // Load from redis
        Collection<String> collection = loadCollection(key, collectionType);
        hasLoaded.add(key); // mark it so it never loads again
        if (collection != null) {
            // Convert if possible
            // Redis only stores strings
            if (!elementType.equals(String.class)) {
                T finalCollection = transformCollection(collection, collectionType, elementType);
                if (finalCollection == null) {
                    // didnt convert
                    cache.put(key, collection);
                    
                    // Add any appended elements
                    List<String> convertedElements = transformCollection(appendedElements, List.class, String.class);
                    if (convertedElements != null) {
                        collection.addAll(convertedElements);
                    }
                    return null; // no conversion
                } else {
                    cache.put(key, finalCollection);
                    finalCollection.addAll(appendedElements);
                    return finalCollection;
                }
            } else {
                cache.put(key, collection);
                collection.addAll((List<String>)appendedElements);
                return (T)collection;
            }
        }
        
        // no value
        return null;
    }
    
    public <T extends Collection<E>,E> void appendCollection(String key, E value, Class<T> collectionType, Class<E> elementType) {
        Preconditions.checkArgument(collectionType.equals(List.class) || collectionType.equals(Set.class));
        
        if (cache.containsKey(key)) {
            Object cachedValue = cache.get(key);
            // Append to a collection if there is one
            if (cachedValue instanceof Collection<?>) {
                Collection<?> rawCollection = (Collection<?>)cachedValue;
                
                if (collectionType.isInstance(rawCollection)) {
                    // Empty collections will work as any type
                    if (rawCollection.isEmpty()) {
                        ((T)rawCollection).add(value);
                    } else {
                        // Check that the elements are of the desired type
                        Object first = Iterables.getFirst(rawCollection, null);
                        if (elementType.isInstance(first)) {
                            // element type matches so we can use this collection
                            ((T)rawCollection).add(value);
                        } else {
                            // Try and transform the value
                            Object otherValue = convert(value, first.getClass());
                            if (otherValue != null) {
                                ((Collection<Object>)rawCollection).add(otherValue);
                            } else {
                                throw new IllegalArgumentException("Unable to append to collection at " + key + ": The value cannot be converted.");
                            }
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Unable to append to collection at " + key + ": The collection present is not compatible.");
                }
            // Append onto the marker if present
            } else if (cachedValue instanceof CollectionMarker) {
                CollectionMarker marker = (CollectionMarker)cachedValue;
                
                if (marker.collectionType.equals(collectionType)) {
                    if (marker.elementType.isInstance(value)) {
                        marker.valuesToAdd.add(value);
                    } else {
                        Object otherValue = convert(value, marker.elementType);
                        if (otherValue != null) {
                            marker.valuesToAdd.add(otherValue);
                        } else {
                            throw new IllegalArgumentException("Unable to append to collection at " + key + ": The value cannot be converted.");
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Unable to append to collection at " + key + ": The existing value is not compatible.");
            }
        } else {
            CollectionMarker marker = new CollectionMarker(elementType, collectionType);
            marker.valuesToAdd.add(value);
            cache.put(key, marker);
            modified.add(key);
        }
    }
    
    public void saveChanges() throws StorageException {
        Jedis jedis = null;
        try {
            jedis = redis.getJedis();
            Pipeline pipe = jedis.pipelined();
            
            saveChanges(pipe);
            pipe.sync();
        } catch (JedisException e) {
            throw handleJedisException(jedis, e);
        } finally {
            redis.returnJedis(jedis);
        }
    }
    
    public void saveChangesAtomically() throws StorageException {
        Jedis jedis = null;
        try {
            jedis = redis.getJedis();
            Transaction transaction = jedis.multi();
            saveChanges(transaction);
            transaction.exec();
        } catch (JedisException e) {
            throw handleJedisException(jedis, e);
        } finally {
            redis.returnJedis(jedis);
        }
    }
    
    private void saveChanges(RedisPipeline pipe) {
        for (Entry<String, Object> entry : cache.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (!modified.contains(key)) {
                continue;
            }
            
            if (value instanceof Storable) {
                saveStorable(pipe, key, (Storable)value);
            } else if (value instanceof Collection<?>) {
                saveCollection(pipe, key, (Collection<?>)value);
            } else if (value instanceof CollectionMarker) {
                appendCollection(pipe, key, (CollectionMarker)value);
            } else if (value instanceof Map<?, ?>) {
                saveMap(pipe, key, (Map<?,?>)value);
            } else if (value instanceof DeletionMarker) {
                pipe.del(key);
            } else {
                pipe.set(key, DataConversion.toString(value));
            }
        }
        
        modified.clear();
    }
    
    private void saveStorable(RedisPipeline pipe, String key, Storable value) {
        Map<String, String> values = Maps.newHashMap();
        value.save(values);
        pipe.hmset(key, values);
    }
    
    private void saveCollection(RedisPipeline pipe, String key, Collection<?> value) {
        if (value instanceof List<?>) {
            pipe.del(key);
            List<String> list = DataConversion.reverseConvertList((List<Object>)value);
            pipe.lpush(key, Iterables.toArray(list, String.class));
        } else if (value instanceof Set<?>) {
            pipe.del(key);
            Set<String> set = DataConversion.reverseConvertSet((Set<Object>)value);
            pipe.sadd(key, Iterables.toArray(set, String.class));
        } else {
            throw new AssertionError();
        }
    }
    
    private void saveMap(RedisPipeline pipe, String key, Map<?, ?> map) {
        Map<String, String> dest = convertMap(map, String.class, String.class);
        pipe.del(key);
        pipe.hmset(key, dest);
    }

    private void appendCollection(RedisPipeline pipe, String key, CollectionMarker marker) {
        if (marker.collectionType.equals(List.class)) {
            List<String> list = DataConversion.reverseConvertList(marker.valuesToAdd);
            pipe.lpush(key, Iterables.toArray(list, String.class));
        } else if (marker.collectionType.equals(Set.class)) {
            List<String> list = DataConversion.reverseConvertList(marker.valuesToAdd);
            pipe.sadd(key, Iterables.toArray(list, String.class));
        } else {
            throw new AssertionError();
        }
    }
    
    private static class CollectionMarker {
        public final List<Object> valuesToAdd;
        public final Class<?> elementType;
        public final Class<? extends Collection<?>> collectionType;
        
        public CollectionMarker(Class<?> elementType, Class<? extends Collection<?>> collectionType) {
            this.elementType = elementType;
            this.collectionType = collectionType;
            
            valuesToAdd = Lists.newArrayList();
        }
    }
    
    // Class used to mark that a value is to be deleted
    private static class DeletionMarker {
    }
}
