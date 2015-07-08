package net.cubespace.geSuit.core.attachments;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.storage.Storable;

/**
 * Attachments are objects you can add to {@link GlobalPlayer}'s to add either transient or stored data.
 */
public abstract class Attachment implements Storable {
    private boolean isDirty;
    
    /**
     * Marks this attachment as needing a save or sync
     */
    public void setDirty() {
        isDirty = true;
    }
    
    /**
     * Clears the dirty flag
     */
    public void clearDirty() {
        isDirty = false;
    }
    
    /**
     * @return Returns true if this attachment needs to be saved or synchronized
     */
    public boolean isDirty() {
        return isDirty;
    }
    
    /**
     * @return Returns the type of attachment this is. See {@link AttachmentType} for details on the types
     */
    public abstract AttachmentType getType();
    
    /**
     * Represents how the attachment will be available
     * @see #Persistent
     * @see #Local
     * @see #Session
     */
    public enum AttachmentType {
        /**
         * Persistent attachments will be saved to redis and available on every server (that has the attachment class)
         */
        Persistent,
        /**
         * Local attachments will only be available on this server
         */
        Local,
        /**
         * Session attachments will be available on every server, but only for the players session.
         */
        Session
    }
}
