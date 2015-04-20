package net.cubespace.geSuit.core.storage;

import java.util.Map;

import com.google.common.collect.Maps;

import redis.clients.jedis.Jedis;

public class RedisInterface {
    private Jedis jedis;
    
    public RedisInterface(Jedis jedis) {
        this.jedis = jedis;
    }
    
    public <T extends Storable> T load(String key, Class<T> type) throws InstantiationException {
        try {
            T object = type.newInstance();
            return load(key, object);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Constructor for type " + type.getSimpleName() + " is not public");
        }
    }
    
    public <T extends Storable> T load(String key, T object) {
        Map<String, String> values = jedis.hgetAll(key);
        object.load(values);
        
        return object;
    }
    
    public void save(String key, Storable object) {
        Map<String, String> values = Maps.newHashMap(); 
        object.save(values);
        
        jedis.hmset(key, values);
    }
    
    public Jedis getJedis() {
        return jedis;
    }
}
