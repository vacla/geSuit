package net.cubespace.geSuit.core.remote;

/**
 * This exception usually indicates that a remote is inconsistent between 
 * the calling and execution sides
 */
public class InvalidRemoteException extends RuntimeException {
    private static final long serialVersionUID = 4807675328919028609L;

    public InvalidRemoteException() {
        super();
    }
    
    public InvalidRemoteException(String message) {
        super(message);
    }
    
    public InvalidRemoteException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidRemoteException(Throwable cause) {
        super(cause);
    }
}
