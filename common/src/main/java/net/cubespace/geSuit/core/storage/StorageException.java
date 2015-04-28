package net.cubespace.geSuit.core.storage;

public class StorageException extends RuntimeException {
    private static final long serialVersionUID = -6716020893978400425L;

    public StorageException(String message) {
        super(message);
    }
    
    public StorageException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
