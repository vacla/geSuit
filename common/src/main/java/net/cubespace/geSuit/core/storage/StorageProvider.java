package net.cubespace.geSuit.core.storage;

import net.cubespace.geSuit.core.Platform;

/**
 * Provides StorageSections to interface with redis.
 */
public class StorageProvider {
    private RedisConnection connection;
    private Platform platform;

    public StorageProvider(RedisConnection connection, Platform platform) {
        this.connection = connection;
        this.platform = platform;
    }
    
    /**
     * Creates a new StorageSection at the absolute root.
     * This StorageSection is not shared.
     * @param enableLogging True to log debug messages
     * @return A read-write StorageSection
     */
    public StorageInterface create(boolean enableLogging) {
        return new RedisStorageInterface(new CachedRedisProvider(connection, platform, enableLogging), "");
    }

    /**
     * Creates a new StorageSection rooted at the specified path.
     * This StorageSection is not shared.
     * @param path The root path
     * @param enableLogging True to log debug messages
     * @return A read-write StorageSection
     */
    public StorageInterface create(String path, boolean enableLogging) {
        return new RedisStorageInterface(new CachedRedisProvider(connection, platform, enableLogging), path);
    }
}
