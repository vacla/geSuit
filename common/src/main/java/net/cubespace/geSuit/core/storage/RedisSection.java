package net.cubespace.geSuit.core.storage;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.Set;
import java.util.UUID;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.util.Utilities;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class RedisSection implements StorageSection {
    private Map<String, Object> cached;
    
    private String path;
    private String fullPath;
    
    private RedisSection parent;
    private RedisSection root;
    
    // Set on root only!
    private RedisConnection connection;
    private Jedis jedis;
    
    public RedisSection(RedisConnection connection) {
        this.connection = connection;
        
        cached = Maps.newHashMap();
        path = "";
        fullPath = "";
        parent = null;
        root = this;
    }
    
    protected RedisSection(RedisSection parent, String path) {
        this.path = path;
        this.parent = parent;
        this.root = (RedisSection)parent.getRoot();
        this.fullPath = createPath(parent, path);
        
        cached = Maps.newHashMap();
    }
    
    private static String createPath(RedisSection section, String key) {
        StringBuilder builder = new StringBuilder();
        
        StorageSection parent = section;
        while(parent != null) {
            if (builder.length() > 0) {
                builder.insert(0, '.');
            }
            builder.insert(0, parent.getName());
            parent = parent.getParent();
        }
        
        if (key != null) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(key);
        }
        
        return builder.toString();
    }
    
    private Jedis getJedis() {
        if (this == root) {
            if (jedis == null) {
                jedis = connection.getJedis();
            }
            return jedis;
        } else {
            return root.getJedis();
        }
    }
    
    private StorageException handleJedisException(JedisException e) {
        if (e instanceof JedisConnectionException) {
            root.connection.returnJedis(getJedis(), e);
            root.jedis = null;
            return new StorageException("Redis connection is unavailable");
        } else {
            return new StorageException("Redis error: " + e.getMessage());
        }
    }
    
    @Override
    public boolean contains(String key) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            if (cached.containsKey(key)) {
                return true;
            } else {
                try {
                    Jedis jedis = getJedis();
                    return jedis.exists(fullPath + "." + key);
                } catch (JedisException e) {
                    throw handleJedisException(e);
                }
            }
        } else {
            return section.contains(getName(key));
        }
    }

    @Override
    public String getCurrentPath() {
        return fullPath;
    }

    @Override
    public String getName() {
        return path;
    }
    
    @Override
    public StorageSection getParent() {
        return parent;
    }
    
    @Override
    public StorageSection getRoot() {
        return root;
    }

    @Override
    public void update() {
        if (this != root) {
            root.update();
            return;
        }
        
        try {
            Jedis jedis = getJedis();
            Pipeline pipe = jedis.pipelined();
            
            saveAll(pipe);
        } catch (JedisException e) {
            throw handleJedisException(e);
        }
        
        reset();
    }
    
    @SuppressWarnings("unchecked")
    private void saveAll(Pipeline pipe) {
        for (Entry<String, Object> entry : cached.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String path = (this == root ? key : fullPath + "." + key);
            pipe.del(path);
            
            if (value instanceof StorageSection) {
                ((RedisSection)value).saveAll(pipe);
            } else if (value instanceof Storable) {
                saveStorable(pipe, path, (Storable)value);
            } else if (value instanceof SimpleStorable) {
                saveSimpleStorable(pipe, path, (SimpleStorable)value);
            } else if (value instanceof List<?>) {
                saveList(pipe, path, (List<?>)value);
            } else if (value instanceof Set<?>) {
                saveSet(pipe, path, (Set<?>)value);
            } else if (value instanceof Map<?, ?>) {
                // Suppress warnings for here as we can only add maps of this type
                pipe.hmset(path, (Map<String, String>)value);
            } else if (value == null) {
                // Already deleted
            } else {
                pipe.set(path, DataConversion.toString(value));
            }
        }
        
        cached.clear();
    }
    
    private void saveStorable(Pipeline pipe, String path, Storable s) {
        Map<String, String> values = Maps.newHashMap();
        s.save(values);
        pipe.hmset(path, values);
    }
    
    private void saveSimpleStorable(Pipeline pipe, String path, SimpleStorable s) {
        pipe.set(path, s.save());
    }
    
    private void saveList(Pipeline pipe, String path, List<?> list) {
        pipe.del(path);
        
        if (list.isEmpty()) {
            return;
        }
        
        String[] values = new String[list.size()];
        for (int i = 0; i < values.length; ++i) {
            values[i] = DataConversion.toString(list.get(i));
        }
        
        pipe.lpush(path, values);
    }
    
    private void saveSet(Pipeline pipe, String path, Set<?> set) {
        pipe.del(path);
        
        if (set.isEmpty()) {
            return;
        }
        
        String[] values = new String[set.size()];
        int index = 0;
        for (Object value : set) {
            values[index++] = DataConversion.toString(value);
        }
        
        pipe.sadd(path, values);
    }

    @Override
    public void reset() {
        if (this != root) {
            root.reset();
            return;
        }
        
        if (jedis != null) {
            connection.returnJedis(jedis);
        }
        
        clearCache();
    }
    
    private void clearCache() {
        for (Object value : cached.values()) {
            if (value instanceof RedisSection) {
                ((RedisSection)value).clearCache();
            }
        }
        
        cached.clear();
    }

    private RedisSection getParentSubsection(String key) {
        int pos = key.lastIndexOf('.');
        if (pos >= 0) {
            return getSubsection(key.substring(0, pos));
        } else {
            return this;
        }
    }
    
    private String getName(String key) {
        int pos = key.lastIndexOf('.');
        if (pos >= 0) {
            return key.substring(pos+1);
        } else {
            return key;
        }
    }
    
    @Override
    public RedisSection getSubsection(String key) {
        if (key.isEmpty()) {
            return this;
        }
        
        int pos = key.indexOf('.');
        String name = key;
        if (pos >= 0) {
            name = key.substring(0, pos);
        }
        
        RedisSection current;
        if (cached.containsKey(name)) {
            Object value = cached.get(name);
            if (value instanceof StorageSection) {
                current = (RedisSection)value;
            } else {
                throw new IllegalArgumentException(key + " is not a section");
            }
        } else {
            current = new RedisSection(this, name);
            cached.put(name, current);
        }
        
        if (pos >= 0) {
            return current.getSubsection(key.substring(pos+1));
        } else {
            return current;
        }
    }
    
    @Override
    public void remove(String key) {
        cached.put(key, null);
    }

    @Override
    public <T extends Storable> T getStorable(String key, Class<T> type) {
        try {
            T storable = type.newInstance();
            return getStorable(key, storable);
        } catch (InstantiationException e) {
            Global.getPlatform().getLogger().log(Level.WARNING, "Tried to create storable to load from redis: ", e);
        } catch (IllegalAccessException e) {
            Global.getPlatform().getLogger().log(Level.WARNING, "Tried to create storable to load from redis: ", e);
        }
        return null;
    }

    @Override
    public <T extends Storable> T getStorable(String key, T storable) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            if (cached.containsKey(key)) {
                Object value = cached.get(key);
                if (storable.getClass().isInstance(value)) {
                    return (T)value;
                } else {
                    return null;
                }
            } else {
                try {
                    Jedis jedis = getJedis();
                    if (jedis.type(fullPath + "." + key).equals("hash")) {
                        Map<String, String> values = jedis.hgetAll(fullPath + "." + key);
                        storable.load(values);
                        return storable;
                    } else {
                        return null;
                    }
                } catch (JedisException e) {
                    throw handleJedisException(e);
                }
            }
        } else {
            return section.getStorable(getName(key), storable);
        }
    }

    @Override
    public void set(String key, Storable value) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            cached.put(key, value);
        } else {
            section.set(getName(key), value);
        }
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    @Override
    public String getString(String key, String def) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            if (cached.containsKey(key)) {
                String value = DataConversion.toString(cached.get(key));
                if (value == null) {
                    return def;
                } else {
                    return value;
                }
            } else {
                try {
                    Jedis jedis = getJedis();
                    String value = jedis.get(fullPath + "." + key);
                    if (value == null) {
                        return def;
                    } else {
                        return value;
                    }
                } catch (JedisException e) {
                    throw handleJedisException(e);
                }
            }
        } else {
            return section.getString(getName(key), def);
        }
    }

    @Override
    public void set(String key, String value) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            cached.put(key, value);
        } else {
            section.set(getName(key), value);
        }
    }

    @Override
    public boolean isString(String key) {
        String value = getString(key, null);
        return value != null;
    }

    @Override
    public int getInt(String key) {
        return getInt(key, 0);
    }

    @Override
    public int getInt(String key, int def) {
        String value = getString(key, null);
        if (value == null) {
            return def;
        } else {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return def;
            }
        }
    }

    @Override
    public void set(String key, int value) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            cached.put(key, value);
        } else {
            section.set(getName(key), value);
        }
    }

    @Override
    public boolean isInt(String key) {
        String value = getString(key, null);
        if (value == null) {
            return false;
        } else {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        String value = getString(key, null);
        if (value == null) {
            return def;
        } else {
            if (value.equalsIgnoreCase("true")) {
                return true;
            } else if (value.equalsIgnoreCase("false")) {
                return false;
            } else {
                try {
                    int val = Integer.parseInt(value);
                    return val != 0;
                } catch (NumberFormatException e) {
                    return def;
                }
            }
        }
    }

    @Override
    public void set(String key, boolean value) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            cached.put(key, value);
        } else {
            section.set(getName(key), value);
        }
    }

    @Override
    public boolean isBoolean(String key) {
        String value = getString(key, null);
        if (value == null) {
            return false;
        } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return true;
        } else {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    @Override
    public long getLong(String key) {
        return getLong(key, 0);
    }

    @Override
    public long getLong(String key, long def) {
        String value = getString(key, null);
        if (value == null) {
            return def;
        } else {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return def;
            }
        }
    }

    @Override
    public void set(String key, long value) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            cached.put(key, value);
        } else {
            section.set(getName(key), value);
        }
    }

    @Override
    public boolean isLong(String key) {
        String value = getString(key, null);
        if (value == null) {
            return false;
        } else {
            try {
                Long.parseLong(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    @Override
    public float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    @Override
    public float getFloat(String key, float def) {
        String value = getString(key, null);
        if (value == null) {
            return def;
        } else {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                return def;
            }
        }
    }

    @Override
    public void set(String key, float value) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            cached.put(key, value);
        } else {
            section.set(getName(key), value);
        }
    }

    @Override
    public boolean isFloat(String key) {
        String value = getString(key, null);
        if (value == null) {
            return false;
        } else {
            try {
                Float.parseFloat(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    @Override
    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    @Override
    public double getDouble(String key, double def) {
        String value = getString(key, null);
        if (value == null) {
            return def;
        } else {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return def;
            }
        }
    }

    @Override
    public void set(String key, double value) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            cached.put(key, value);
        } else {
            section.set(getName(key), value);
        }
    }

    @Override
    public boolean isDouble(String key) {
        String value = getString(key, null);
        if (value == null) {
            return false;
        } else {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    @Override
    public UUID getUUID(String key) {
        return getUUID(key, null);
    }

    @Override
    public UUID getUUID(String key, UUID def) {
        String value = getString(key, null);
        if (value == null) {
            return def;
        } else {
            try {
                return Utilities.makeUUID(value);
            } catch (IllegalArgumentException e) {
                return def;
            }
        }
    }

    @Override
    public void set(String key, UUID value) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            cached.put(key, value);
        } else {
            section.set(getName(key), value);
        }
    }

    @Override
    public boolean isUUID(String key) {
        String value = getString(key, null);
        if (value == null) {
            return false;
        } else {
            try {
                Utilities.makeUUID(key);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    @Override
    public <T extends SimpleStorable> T getSimpleStorable(String key, Class<T> type) {
        try {
            T storable = type.newInstance();
            return getSimpleStorable(key, storable);
        } catch (InstantiationException e) {
            Global.getPlatform().getLogger().log(Level.WARNING, "Tried to create simple storable to load from redis: ", e);
        } catch (IllegalAccessException e) {
            Global.getPlatform().getLogger().log(Level.WARNING, "Tried to create simple storable to load from redis: ", e);
        }
        return null;
    }

    @Override
    public <T extends SimpleStorable> T getSimpleStorable(String key, T storable) {
        String value = getString(key, null);
        if (value == null) {
            return null;
        } else {
            try {
                storable.load(value);
                return storable;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @Override
    public void set(String key, SimpleStorable value) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            cached.put(key, value);
        } else {
            section.set(getName(key), value.save());
        }
    }
    
    @Override
    public void set(String key, Map<String, String> value) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            cached.put(key, value);
        } else {
            section.set(getName(key), value);
        }
    }
    
    @Override
    public Map<String, String> getMap(String key) {
        return getMap(key, null);
    }
    
    @Override
    public Map<String, String> getMap(String key, Map<String, String> def) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            if (cached.containsKey(key)) {
                Object value = cached.get(key);
                if (value instanceof Map<?, ?>) {
                    return (Map<String, String>)value;
                } else {
                    return def;
                }
            } else {
                try {
                    Jedis jedis = getJedis();
                    Map<String, String> values = jedis.hgetAll(fullPath + "." + key);
                    if (values == null) {
                        return def;
                    } else {
                        return values;
                    }
                } catch (JedisException e) {
                    throw handleJedisException(e);
                }
            }
        } else {
            return section.getMap(getName(key), def);
        }
    }
    
    @Override
    public boolean isMap(String key) {
        return getMap(key, null) != null;
    }
    
    @Override
    public void set(String key, List<?> list) {
        if (list.isEmpty()) {
            cached.put(key, Lists.<String>newArrayList());
        } else {
            cached.put(key, DataConversion.reverseConvertList((List<Object>)list));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getListString(String key) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            if (cached.containsKey(key)) {
                Object value = cached.get(key);
                
                if (value instanceof List<?>) {
                    // The only type of list allowed to be stored
                    return (List<String>)value;
                } else {
                    return null;
                }
            } else {
                try {
                    Jedis jedis = getJedis();
                    key = fullPath + "." + key;
                    if (jedis.type(key).equals("list")) {
                        return jedis.lrange(key, 0, jedis.llen(key));
                    } else {
                        return null;
                    }
                } catch (JedisException e) {
                    throw handleJedisException(e);
                }
            }
        } else {
            return section.getListString(getName(key));
        }
    }

    @Override
    public List<Integer> getListInt(String key) {
        List<String> stringList = getListString(key);
        if (stringList == null) {
            return null;
        }
        
        return DataConversion.convertList(stringList, Integer.class);
    }

    @Override
    public List<Long> getListLong(String key) {
        List<String> stringList = getListString(key);
        if (stringList == null) {
            return null;
        }
        
        return DataConversion.convertList(stringList, Long.class);
    }

    @Override
    public List<Float> getListFloat(String key) {
        List<String> stringList = getListString(key);
        if (stringList == null) {
            return null;
        }
        
        return DataConversion.convertList(stringList, Float.class);
    }

    @Override
    public List<Double> getListDouble(String key) {
        List<String> stringList = getListString(key);
        if (stringList == null) {
            return null;
        }
        
        return DataConversion.convertList(stringList, Double.class);
    }

    @Override
    public List<Boolean> getListBoolean(String key) {
        List<String> stringList = getListString(key);
        if (stringList == null) {
            return null;
        }
        
        return DataConversion.convertList(stringList, Boolean.class);
    }
    
    @Override
    public List<UUID> getListUUID(String key) {
        List<String> stringList = getListString(key);
        if (stringList == null) {
            return null;
        }
        
        return DataConversion.convertList(stringList, UUID.class);
    }

    @Override
    public <T extends SimpleStorable> List<T> getListSimpleStorable(String key, final Class<T> type) {
        List<String> stringList = getListString(key);
        if (stringList == null) {
            return null;
        }
        
        return DataConversion.convertList(stringList, type);
    }

    @Override
    public boolean isList(String key) {
        return getListString(key) != null;
    }

    @Override
    public void set(String key, Set<?> set) {
        if (set.isEmpty()) {
            cached.put(key, Sets.<String>newHashSet());
        } else {
            cached.put(key, DataConversion.reverseConvertSet((Set<Object>)set));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getSetString(String key) {
        RedisSection section = getParentSubsection(key);
        
        if (section == this) {
            if (cached.containsKey(key)) {
                Object value = cached.get(key);
                
                if (value instanceof Set<?>) {
                    // The only type of set allowed to be stored
                    return (Set<String>)value;
                } else {
                    return null;
                }
            } else {
                try {
                    Jedis jedis = getJedis();
                    key = fullPath + "." + key;
                    if (jedis.type(key).equals("set")) {
                        return jedis.smembers(key);
                    } else {
                        return null;
                    }
                } catch (JedisException e) {
                    throw handleJedisException(e);
                }
            }
        } else {
            return section.getSetString(getName(key));
        }
    }

    @Override
    public Set<Integer> getSetInt(String key) {
        Set<String> set = getSetString(key);
        if (set == null) {
            return null;
        }
        
        return DataConversion.convertSet(set, Integer.class);
    }

    @Override
    public Set<Long> getSetLong(String key) {
        Set<String> set = getSetString(key);
        if (set == null) {
            return null;
        }
        
        return DataConversion.convertSet(set, Long.class);
    }

    @Override
    public Set<Float> getSetFloat(String key) {
        Set<String> set = getSetString(key);
        if (set == null) {
            return null;
        }
        
        return DataConversion.convertSet(set, Float.class);
    }

    @Override
    public Set<Double> getSetDouble(String key) {
        Set<String> set = getSetString(key);
        if (set == null) {
            return null;
        }
        
        return DataConversion.convertSet(set, Double.class);
    }

    @Override
    public Set<Boolean> getSetBoolean(String key) {
        Set<String> set = getSetString(key);
        if (set == null) {
            return null;
        }
        
        return DataConversion.convertSet(set, Boolean.class);
    }
    
    @Override
    public Set<UUID> getSetUUID(String key) {
        Set<String> set = getSetString(key);
        if (set == null) {
            return null;
        }
        
        return DataConversion.convertSet(set, UUID.class);
    }

    @Override
    public <T extends SimpleStorable> Set<T> getSetSimpleStorable(String key, final Class<T> type) {
        Set<String> set = getSetString(key);
        if (set == null) {
            return null;
        }
        
        return DataConversion.convertSet(set, type);
    }

    @Override
    public boolean isSet(String key) {
        return getSetString(key) != null;
    }
}
