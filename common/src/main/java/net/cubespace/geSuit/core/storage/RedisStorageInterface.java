package net.cubespace.geSuit.core.storage;

class RedisStorageInterface extends RedisSection implements StorageInterface {
    private final CachedRedisProvider provider;
    
    public RedisStorageInterface(CachedRedisProvider provider, String rootPath) {
        super(rootPath);
        setRoot(this);
        
        this.provider = provider;
    }
    
    protected CachedRedisProvider getProvider() {
        return provider;
    }

    @Override
    public void update() throws StorageException {
        provider.saveChanges();
    }

    @Override
    public void updateAtomic() throws StorageException {
        provider.saveChangesAtomically();
    }

    @Override
    public void reset() {
        provider.invalidate();
    }
    
}
