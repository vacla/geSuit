package net.cubespace.geSuit.core.storage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.cubespace.geSuit.core.util.Utilities;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class RedisSection implements StorageSection {
    private final Map<String, RedisSection> sections;
    
    private final String path;
    private final String fullPath;
    
    private final RedisSection parent;
    private RedisStorageInterface root;
    
    public RedisSection() {
        this("");
    }
    
    // Constructor for the root element
    protected RedisSection(String root) {
        sections = Maps.newHashMap();
        path = root.indexOf('.') == -1 ? root : root.substring(root.lastIndexOf('.')+1);
        fullPath = root;
        parent = null;  
    }
    
    protected RedisSection(RedisSection parent, String path) {
        this.path = path;
        this.parent = parent;
        this.root = parent.root;
        this.fullPath = parent.getSubPath(path);
        
        sections = Maps.newHashMap();
    }
    
    private String getSubPath(String key) {
        if (fullPath.isEmpty()) {
            return key;
        } else {
            return fullPath + "." + key;
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
    public StorageInterface getRoot() {
        return root;
    }
    
    protected void setRoot(RedisStorageInterface root) {
        this.root = root;
    }
    
    @Override
    public boolean contains(String key) {
        return root.getProvider().contains(getSubPath(key));
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
        if (sections.containsKey(name)) {
            current = sections.get(name);
        } else {
            current = new RedisSection(this, name);
            sections.put(name, current);
        }
        
        if (pos >= 0) {
            return current.getSubsection(key.substring(pos+1));
        } else {
            return current;
        }
    }
    
    @Override
    public void remove(String key) {
        root.getProvider().remove(getSubPath(key));
    }

    @Override
    public <T extends Storable> T getStorable(String key, Class<T> type) {
        return root.getProvider().provideForStorable(getSubPath(key), type, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Storable> T getStorable(String key, T storable) {
        return root.getProvider().provideForStorable(getSubPath(key), (Class<T>)storable.getClass(), storable);
    }

    @Override
    public void set(String key, Storable value) {
        root.getProvider().set(getSubPath(key), value);
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    @Override
    public String getString(String key, String def) {
        String value = root.getProvider().provideForSimple(getSubPath(key), String.class);
        return Utilities.selectFirst(value, def);
    }

    @Override
    public void set(String key, String value) {
        root.getProvider().set(getSubPath(key), value);
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
        Integer value = root.getProvider().provideForSimple(getSubPath(key), Integer.class);
        return Utilities.selectFirst(value, def);
    }

    @Override
    public void set(String key, int value) {
        root.getProvider().set(getSubPath(key), value);
    }

    @Override
    public boolean isInt(String key) {
        return root.getProvider().provideForSimple(getSubPath(key), Integer.class) != null;
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        Boolean value = root.getProvider().provideForSimple(getSubPath(key), Boolean.class);
        return Utilities.selectFirst(value, def);
    }

    @Override
    public void set(String key, boolean value) {
        root.getProvider().set(getSubPath(key), value);
    }

    @Override
    public boolean isBoolean(String key) {
        return root.getProvider().provideForSimple(getSubPath(key), Boolean.class) != null;
    }

    @Override
    public long getLong(String key) {
        return getLong(key, 0);
    }

    @Override
    public long getLong(String key, long def) {
        Long value = root.getProvider().provideForSimple(getSubPath(key), Long.class);
        return Utilities.selectFirst(value, def);
    }

    @Override
    public void set(String key, long value) {
        root.getProvider().set(getSubPath(key), value);
    }

    @Override
    public boolean isLong(String key) {
        return root.getProvider().provideForSimple(getSubPath(key), Long.class) != null;
    }

    @Override
    public float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    @Override
    public float getFloat(String key, float def) {
        Float value = root.getProvider().provideForSimple(getSubPath(key), Float.class);
        return Utilities.selectFirst(value, def);
    }

    @Override
    public void set(String key, float value) {
        root.getProvider().set(getSubPath(key), value);
    }

    @Override
    public boolean isFloat(String key) {
        return root.getProvider().provideForSimple(getSubPath(key), Float.class) != null;
    }

    @Override
    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    @Override
    public double getDouble(String key, double def) {
        Double value = root.getProvider().provideForSimple(getSubPath(key), Double.class);
        return Utilities.selectFirst(value, def);
    }

    @Override
    public void set(String key, double value) {
        root.getProvider().set(getSubPath(key), value);
    }

    @Override
    public boolean isDouble(String key) {
        return root.getProvider().provideForSimple(getSubPath(key), Double.class) != null;
    }

    @Override
    public UUID getUUID(String key) {
        return getUUID(key, null);
    }

    @Override
    public UUID getUUID(String key, UUID def) {
        UUID value = root.getProvider().provideForSimple(getSubPath(key), UUID.class);
        return Utilities.selectFirst(value, def);
    }

    @Override
    public void set(String key, UUID value) {
        root.getProvider().set(getSubPath(key), value);
    }

    @Override
    public boolean isUUID(String key) {
        return root.getProvider().provideForSimple(getSubPath(key), UUID.class) != null;
    }

    @Override
    public <T extends SimpleStorable> T getSimpleStorable(String key, Class<T> type) {
        return root.getProvider().provideForSimple(getSubPath(key), type);
    }

    @Override
    public <T extends SimpleStorable> T getSimpleStorable(String key, T storable) {
        String value = root.getProvider().provideForSimple(getSubPath(key), String.class);
        if (value == null) {
            return null;
        }
        
        try {
            storable.load(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
        
        return storable;
    }

    @Override
    public void set(String key, SimpleStorable value) {
        root.getProvider().set(getSubPath(key), value);
    }
    
    @Override
    public void set(String key, Map<String, String> value) {
        root.getProvider().set(getSubPath(key), value);
    }
    
    @Override
    public Map<String, String> getMap(String key) {
        return getMap(key, null);
    }
    
    @Override
    public Map<String, String> getMap(String key, Map<String, String> def) {
        Map<String, String> map = root.getProvider().provideForMap(getSubPath(key), String.class, String.class);
        return Utilities.selectFirst(map, def);
    }
    
    @Override
    public Map<String, String> getMapPartial(String key, String... fields) throws StorageException {
        return getMapPartial(key, null, fields);
    }
    
    @Override
    public Map<String, String> getMapPartial(String key, Map<String, String> def, String... fields) throws StorageException {
        Map<String, String> map = root.getProvider().provideForPartialMap(getSubPath(key), String.class, String.class, Arrays.asList(fields));
        return Utilities.selectFirst(map, def);
    }
    
    @Override
    public boolean isMap(String key) {
        return getMap(key, null) != null;
    }
    
    @Override
    public void set(String key, List<?> list) {
        root.getProvider().set(getSubPath(key), list);
    }

    @Override
    public List<String> getListString(String key) {
        return root.getProvider().provideForList(getSubPath(key), String.class);
    }
    
    @Override
    public <T> List<T> getList(String key, Class<T> listType) throws StorageException, IllegalArgumentException {
        return root.getProvider().provideForList(getSubPath(key), listType);
    }

    @Override
    public boolean isList(String key) {
        return getListString(key) != null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void appendList(String key, Object value) throws IllegalArgumentException {
        Preconditions.checkNotNull(value);
        root.getProvider().appendCollection(getSubPath(key), value, List.class, (Class<Object>)value.getClass());
    }

    @Override
    public void set(String key, Set<?> set) {
        root.getProvider().set(getSubPath(key), set);
    }
    
    @Override
    public Set<String> getSetString(String key) {
        return root.getProvider().provideForSet(getSubPath(key), String.class);
    }

    @Override
    public <T> Set<T> getSet(String key, Class<T> setType) throws StorageException, IllegalArgumentException {
        return root.getProvider().provideForSet(getSubPath(key), setType);
    }

    @Override
    public boolean isSet(String key) {
        return getSetString(key) != null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void appendSet(String key, Object value) {
        Preconditions.checkNotNull(value);
        root.getProvider().appendCollection(getSubPath(key), value, Set.class, (Class<Object>)value.getClass());
    }
}
