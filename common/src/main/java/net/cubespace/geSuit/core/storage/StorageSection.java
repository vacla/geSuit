package net.cubespace.geSuit.core.storage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface StorageSection {
    public String getCurrentPath();
    public String getName();
    
    public StorageSection getParent();
    public StorageSection getRoot();
    
    public void update() throws StorageException;
    public void reset();
    
    public boolean contains(String key) throws StorageException;
    
    public StorageSection getSubsection(String key);
    
    public <T extends Storable> T getStorable(String key, Class<T> type) throws StorageException;
    public <T extends Storable> T getStorable(String key, T storable) throws StorageException;
    public void set(String key, Storable value);
    
    public String getString(String key) throws StorageException;
    public String getString(String key, String def) throws StorageException;
    public void set(String key, String value);
    public boolean isString(String key);
    
    public Map<String, String> getMap(String key) throws StorageException;
    public Map<String, String> getMap(String key, Map<String, String> def) throws StorageException;
    public void set(String key, Map<String, String> value);
    public boolean isMap(String key);
    
    public int getInt(String key) throws StorageException;
    public int getInt(String key, int def) throws StorageException;
    public void set(String key, int value);
    public boolean isInt(String key);
    
    public boolean getBoolean(String key) throws StorageException;
    public boolean getBoolean(String key, boolean def) throws StorageException;
    public void set(String key, boolean value);
    public boolean isBoolean(String key);
    
    public long getLong(String key) throws StorageException;
    public long getLong(String key, long def) throws StorageException;
    public void set(String key, long value);
    public boolean isLong(String key);
    
    public float getFloat(String key) throws StorageException;
    public float getFloat(String key, float def) throws StorageException;
    public void set(String key, float value);
    public boolean isFloat(String key);
    
    public double getDouble(String key) throws StorageException;
    public double getDouble(String key, double def) throws StorageException;
    public void set(String key, double value);
    public boolean isDouble(String key);
    
    public UUID getUUID(String key) throws StorageException;
    public UUID getUUID(String key, UUID def) throws StorageException;
    public void set(String key, UUID value);
    public boolean isUUID(String key);
    
    public <T extends SimpleStorable> T getSimpleStorable(String key, Class<T> type) throws StorageException;
    public <T extends SimpleStorable> T getSimpleStorable(String key, T storable) throws StorageException;
    public void set(String key, SimpleStorable storable);
    
    public void set(String key, List<?> list);
    public List<String> getListString(String key) throws StorageException;
    public List<Integer> getListInt(String key) throws StorageException;
    public List<Long> getListLong(String key) throws StorageException;
    public List<Float> getListFloat(String key) throws StorageException;
    public List<Double> getListDouble(String key) throws StorageException;
    public List<Boolean> getListBoolean(String key) throws StorageException;
    public List<UUID> getListUUID(String key) throws StorageException;
    public <T extends SimpleStorable> List<T> getListSimpleStorable(String key, Class<T> type) throws StorageException;
    public boolean isList(String key);
    
    public void set(String key, Set<?> set);
    public Set<String> getSetString(String key) throws StorageException;
    public Set<Integer> getSetInt(String key) throws StorageException;
    public Set<Long> getSetLong(String key) throws StorageException;
    public Set<Float> getSetFloat(String key) throws StorageException;
    public Set<Double> getSetDouble(String key) throws StorageException;
    public Set<Boolean> getSetBoolean(String key) throws StorageException;
    public Set<UUID> getSetUUID(String key) throws StorageException;
    public <T extends SimpleStorable> Set<T> getSetSimpleStorable(String key, Class<T> type) throws StorageException;
    public boolean isSet(String key);
}
