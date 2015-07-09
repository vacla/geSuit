package net.cubespace.geSuit.core.storage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * StorageSections provide a simple and efficient way to interface with the backend.
 * This provides permanent storage of almost any data.
 * 
 * <p><b>While not a real restriction, plugins should not access or write to any 'gesuit.' sections as it can break geSuit.</b></p>
 */
public interface StorageSection {
    /**
     * @return Returns the current path from root of this section
     */
    public String getCurrentPath();
    /**
     * @return Returns the name of this section (the last part of the full path)
     */
    public String getName();
    
    /**
     * @return Returns the parent section of this section
     */
    public StorageSection getParent();
    /**
     * @return Returns the root section of all sections
     */
    public StorageSection getRoot();
    
    /**
     * Writes all changes to the backend. <b>This method is blocking</b>
     * @throws StorageException Thrown if an error occurs writing to the backend.
     */
    public void update() throws StorageException;
    
    /**
     * Performs an atomic write to the backend. <b>This method is blocking</b>
     * @throws StorageException Thrown if an error occurs writing to the backend.
     */
    public void updateAtomic() throws StorageException;
    
    /**
     * Resets all changes.
     */
    public void reset();
    
    /**
     * Checks if the specified key is defined. <b>This method is blocking</b>
     * @param key The key to look for
     * @return True if it is defined
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean contains(String key) throws StorageException;
    
    /**
     * Gets a StorageSection representing the key as a base
     * @param key The new root to use
     * @return The section
     */
    public StorageSection getSubsection(String key);
    
    /**
     * Removes the specified key
     * @param key The key to remove
     */
    public void remove(String key);
    
    /**
     * Gets the key as a Storable of {@code type}.
     * This method requires that the {@code type} has a default constructor.
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param type The type of Storable to get
     * @return The loaded storable or null if it was not a storable
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public <T extends Storable> T getStorable(String key, Class<T> type) throws StorageException;
    /**
     * Loads the key into {@code storable}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param storable The storable to load into
     * @return The loaded storable or null if it was not a storable
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public <T extends Storable> T getStorable(String key, T storable) throws StorageException;
    /**
     * Sets the key as a Storable
     * @param key The key to set
     * @param value The value to set
     */
    public void set(String key, Storable value);
    
    /**
     * Gets the key as a String
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The loaded value or null if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public String getString(String key) throws StorageException;
    /**
     * Gets the key as a String
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param def The default value to use if the key does not exist or is of the wrong type.
     * @return The loaded value or the default value if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public String getString(String key, String def) throws StorageException;
    /**
     * Sets the key as a String
     * @param key The key to set
     * @param value The value to set
     */
    public void set(String key, String value);
    /**
     * Checks if the key points to a String
     * <b>This method may need to contact the backend and is blocking</b> 
     * @param key The key to check
     * @return True if it does
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean isString(String key) throws StorageException;
    
    /**
     * Gets the key as a {@code Map<String,String>}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The loaded value or null if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public Map<String, String> getMap(String key) throws StorageException;
    /**
     * Gets the key as a {@code Map<String,String>}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param def The default value to use if the key does not exist or is of the wrong type.
     * @return The loaded value or the default value if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public Map<String, String> getMap(String key, Map<String, String> def) throws StorageException;
    /**
     * Gets the key as a {@code Map<String,String>}.
     * This allows retrieving just the specified fields for when you dont need all values
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param fields The only fields you want retrieved
     * @return The loaded value or null if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public Map<String, String> getMapPartial(String key, String... fields) throws StorageException;
    /**
     * Gets the key as a {@code Map<String,String>}.
     * This allows retrieving just the specified fields for when you dont need all values
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param def The default value to use if the key does not exist or is of the wrong type.
     * @param fields The only fields you want retrieved
     * @return The loaded value or the default value if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public Map<String, String> getMapPartial(String key, Map<String, String> def, String... fields) throws StorageException;
    /**
     * Sets the key as a {@code Map<String,String>}
     * @param key The key to set
     * @param value The value to set
     */
    public void set(String key, Map<String, String> value);
    /**
     * Checks if the key points to a {@code Map<String,String>}
     * <b>This method may need to contact the backend and is blocking</b> 
     * @param key The key to check
     * @return True if it does
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean isMap(String key) throws StorageException;
    
    /**
     * Gets the key as an Integer
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The loaded value or 0 if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public int getInt(String key) throws StorageException;
    /**
     * Gets the key as an Integer
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param def The default value to use if the key does not exist or is of the wrong type.
     * @return The loaded value or the default value if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public int getInt(String key, int def) throws StorageException;
    /**
     * Sets the key as an Integer
     * @param key The key to set
     * @param value The value to set
     */
    public void set(String key, int value);
    /**
     * Checks if the key points to an Integer
     * <b>This method may need to contact the backend and is blocking</b> 
     * @param key The key to check
     * @return True if it does
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean isInt(String key) throws StorageException;
    
    /**
     * Gets the key as a Boolean
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The loaded value or false if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean getBoolean(String key) throws StorageException;
    /**
     * Gets the key as a Boolean
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param def The default value to use if the key does not exist or is of the wrong type.
     * @return The loaded value or the default value if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean getBoolean(String key, boolean def) throws StorageException;
    /**
     * Sets the key as a Boolean
     * @param key The key to set
     * @param value The value to set
     */
    public void set(String key, boolean value);
    /**
     * Checks if the key points to a Boolean
     * <b>This method may need to contact the backend and is blocking</b> 
     * @param key The key to check
     * @return True if it does
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean isBoolean(String key) throws StorageException;
    
    /**
     * Gets the key as a Long
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The loaded value or 0 if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public long getLong(String key) throws StorageException;
    /**
     * Gets the key as a Long
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param def The default value to use if the key does not exist or is of the wrong type.
     * @return The loaded value or the default value if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public long getLong(String key, long def) throws StorageException;
    /**
     * Sets the key as a Long
     * @param key The key to set
     * @param value The value to set
     */
    public void set(String key, long value);
    /**
     * Checks if the key points to a Long
     * <b>This method may need to contact the backend and is blocking</b> 
     * @param key The key to check
     * @return True if it does
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean isLong(String key) throws StorageException;
    
    /**
     * Gets the key as a Float
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The loaded value or 0 if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public float getFloat(String key) throws StorageException;
    /**
     * Gets the key as a Float
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param def The default value to use if the key does not exist or is of the wrong type.
     * @return The loaded value or the default value if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */    
    public float getFloat(String key, float def) throws StorageException;
    /**
     * Sets the key as a Float
     * @param key The key to set
     * @param value The value to set
     */
    public void set(String key, float value);
    /**
     * Checks if the key points to a Float
     * <b>This method may need to contact the backend and is blocking</b> 
     * @param key The key to check
     * @return True if it does
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean isFloat(String key) throws StorageException;
    
    /**
     * Gets the key as a Double
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The loaded value or 0 if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public double getDouble(String key) throws StorageException;
    /**
     * Gets the key as a Double
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param def The default value to use if the key does not exist or is of the wrong type.
     * @return The loaded value or the default value if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public double getDouble(String key, double def) throws StorageException;
    /**
     * Sets the key as a Double
     * @param key The key to set
     * @param value The value to set
     */
    public void set(String key, double value);
    /**
     * Checks if the key points to a Double
     * <b>This method may need to contact the backend and is blocking</b> 
     * @param key The key to check
     * @return True if it does
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean isDouble(String key) throws StorageException;
    
    /**
     * Gets the key as a UUID
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The loaded value or null if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public UUID getUUID(String key) throws StorageException;
    /**
     * Gets the key as a UUID
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @param def The default value to use if the key does not exist or is of the wrong type.
     * @return The loaded value or the default value if it did not exist or was not the correct type.
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public UUID getUUID(String key, UUID def) throws StorageException;
    /**
     * Sets the key as a UUID
     * @param key The key to set
     * @param value The value to set
     */
    public void set(String key, UUID value);
    /**
     * Checks if the key points to a UUID
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to check
     * @return True if it does
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean isUUID(String key) throws StorageException;
    
    public <T extends SimpleStorable> T getSimpleStorable(String key, Class<T> type) throws StorageException;
    public <T extends SimpleStorable> T getSimpleStorable(String key, T storable) throws StorageException;
    /**
     * Sets the key as a SimpleStorable
     * @param key The key to set
     * @param storable The value to set
     */
    public void set(String key, SimpleStorable storable);
    
    /**
     * Sets the key as a List. The list is converted through {@link DataConversion#reverseConvertList(List)} to a string list
     * @param key The key to set
     * @param list The value to set
     */
    public void set(String key, List<?> list);
    /**
     * Gets a String list at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The list, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public List<String> getListString(String key) throws StorageException;
    /**
     * Gets an Integer list at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The list, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public List<Integer> getListInt(String key) throws StorageException;
    /**
     * Gets a Long list at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The list, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public List<Long> getListLong(String key) throws StorageException;
    /**
     * Gets a Float list at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The list, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public List<Float> getListFloat(String key) throws StorageException;
    /**
     * Gets a Double list at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The list, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public List<Double> getListDouble(String key) throws StorageException;
    /**
     * Gets a Boolean list at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The list, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public List<Boolean> getListBoolean(String key) throws StorageException;
    /**
     * Gets a UUID list at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The list, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public List<UUID> getListUUID(String key) throws StorageException;
    /**
     * Gets a list containing {@code type} SimpleStorables at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The list, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     * @throws IllegalArgumentException Thrown if the type cannot be instantiated (no default constructor)
     */
    public <T extends SimpleStorable> List<T> getListSimpleStorable(String key, Class<T> type) throws StorageException;
    /**
     * Checks if the key points to a List of some kind
     * <b>This method may need to contact the backend and is blocking</b> 
     * @param key The key to check
     * @return True if it does
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean isList(String key) throws StorageException;
    
    /**
     * Sets the key as a Set. The list is converted through {@link DataConversion#reverseConvertSet(Set)} to a string set
     * @param key The key to set
     * @param set The value to set
     */
    public void set(String key, Set<?> set);
    /**
     * Gets a String set at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The set, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public Set<String> getSetString(String key) throws StorageException;
    /**
     * Gets an Integer set at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The set, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public Set<Integer> getSetInt(String key) throws StorageException;
    /**
     * Gets a Long set at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The set, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public Set<Long> getSetLong(String key) throws StorageException;
    /**
     * Gets a Float set at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The set, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public Set<Float> getSetFloat(String key) throws StorageException;
    /**
     * Gets a Double set at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The set, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public Set<Double> getSetDouble(String key) throws StorageException;
    /**
     * Gets a Boolean set at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The set, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public Set<Boolean> getSetBoolean(String key) throws StorageException;
    /**
     * Gets a UUID set at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The set, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public Set<UUID> getSetUUID(String key) throws StorageException;
    /**
     * Gets a set containing {@code type} SimpleStorables at {@code key}
     * <b>This method may need to contact the backend and is blocking</b>
     * @param key The key to get
     * @return The set, or null if it didnt exist or was the wrong type
     * @throws StorageException Thrown if an error occurs reading from the backend.
     * @throws IllegalArgumentException Thrown if the type cannot be instantiated (no default constructor)
     */
    public <T extends SimpleStorable> Set<T> getSetSimpleStorable(String key, Class<T> type) throws StorageException;
    /**
     * Checks if the key points to a Set of some kind
     * <b>This method may need to contact the backend and is blocking</b> 
     * @param key The key to check
     * @return True if it does
     * @throws StorageException Thrown if an error occurs reading from the backend.
     */
    public boolean isSet(String key) throws StorageException;
}
