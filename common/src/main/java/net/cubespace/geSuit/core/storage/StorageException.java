package net.cubespace.geSuit.core.storage;

/**
 * This exception is thrown to indicate a problem with the storage backend. 
 * This may be caused by an error in syntax, an IOException, etc.
 */
public class StorageException extends RuntimeException {
    private static final long serialVersionUID = -6716020893978400425L;

    public StorageException(String message) {
        super(message);
    }
    
    public StorageException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
