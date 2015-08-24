package net.cubespace.geSuit.core.storage;

/**
 * Provides StorageSections to interface with redis.
 */
public class StorageProvider {
    private RedisConnection connection;
    
    public StorageProvider(RedisConnection connection) {
        this.connection = connection;
    }
    
    /**
     * Creates a new StorageSection at the absolute root.
     * This StorageSection is not shared.
     * @return A read-write StorageSection
     */
    public StorageInterface create() {
        return new RedisStorageInterface(new CachedRedisProvider(connection), "");
    }
    
    /**
     * Creates a new StorageSection rooted at the specified path.
     * This StorageSection is not shared. 
     * @param path The root path
     * @return A read-write StorageSection
     */
    public StorageInterface create(String path) {
        return new RedisStorageInterface(new CachedRedisProvider(connection), path);
    }
}
