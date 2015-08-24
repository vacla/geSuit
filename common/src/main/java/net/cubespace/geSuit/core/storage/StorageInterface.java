package net.cubespace.geSuit.core.storage;

public interface StorageInterface extends StorageSection {
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
}
