package net.cubespace.geSuit.core.storage;

import java.util.Map;

/**
 * Storable indicates this class can be saved as a collection of key-value pairs. This is mainly used for redis storage
 */
public interface Storable {
    /**
     * When overridden, saves this class in a map
     * @param values The map to write to
     */
    public void save(Map<String, String> values);
    /**
     * When overridden, reads this class from a map
     * @param values The map to read from
     */
    public void load(Map<String, String> values);
}
