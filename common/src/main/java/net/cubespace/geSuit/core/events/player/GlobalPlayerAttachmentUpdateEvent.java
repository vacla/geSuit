package net.cubespace.geSuit.core.events.player;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.attachments.Attachment;

/**
 * Called when an attachment is updated.
 */
public class GlobalPlayerAttachmentUpdateEvent extends GlobalPlayerEvent {
    private final Attachment attachment;
    
    public GlobalPlayerAttachmentUpdateEvent(GlobalPlayer player, Attachment attachment) {
        super(player);
        this.attachment = attachment;
    }
    
    /**
     * Gets the attachment that was updated
     * @return The attachment
     */
    public Attachment getAttachment() {
        return attachment;
    }
}
