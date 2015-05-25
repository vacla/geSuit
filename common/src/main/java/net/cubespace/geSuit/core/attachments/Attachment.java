package net.cubespace.geSuit.core.attachments;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.storage.Storable;

/**
 * Attachments are objects you can add to {@link GlobalPlayer}'s to add either transient or stored data.
 */
public interface Attachment extends Storable {
    /**
     * @return True if this attachment should be saved in redis and synched to other servers
     */
    public boolean isSaved();
}
