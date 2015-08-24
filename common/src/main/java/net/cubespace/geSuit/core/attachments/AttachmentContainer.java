package net.cubespace.geSuit.core.attachments;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.cubespace.geSuit.core.attachments.Attachment.AttachmentType;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.SyncAttachmentMessage;
import net.cubespace.geSuit.core.storage.StorageInterface;

public class AttachmentContainer {
    // The full set of attachment class names including attachments that are
    // not loaded due to missing classes. Used for saving attachments
    private Set<String> attachmentSet;
    private Map<Class<? extends Attachment>, Attachment> attachments;
    
    private boolean hasLoaded;
    private boolean requiresSave;
    
    private UUID owner;
    private Channel<BaseMessage> channel;
    private StorageInterface storage;
    
    public AttachmentContainer(UUID owner, Channel<BaseMessage> channel, StorageInterface storage) {
        this.owner = owner;
        this.channel = channel;
        this.storage = storage;
        
        attachmentSet = Sets.newHashSet();
        attachments = Maps.newHashMap();
    }
    
    // Manipulation
    /**
     * Adds a new attachment to this player. 
     * Attachments can be accessed through {@link #getAttachment(Class)} using the type of the attachment.
     * 
     * @param attachment The instance of the attachment to add
     * @return Returns the provided attachment
     * @throws IllegalStateException Thrown if you try to add duplicate attachments
     */
    public <T extends Attachment> T addAttachment(T attachment) throws IllegalStateException {
        Class<? extends Attachment> type = attachment.getClass();
        Preconditions.checkState(!attachments.containsKey(type));
        
        attachments.put(type, attachment);
        if (attachment.getType() == AttachmentType.Persistent) {
            attachmentSet.add(type.getName());
            requiresSave = true;
        }
        
        return attachment;
    }
    
    /**
     * Gets a previously added attachment. This can include attachments that are loaded from storage.
     * 
     * @param attachmentClass The class of the attachment. This cannot be a subclass or superclass.  
     * @return The attachment instance, or null if not found
     * @see #removeAttachment(Class)
     * @see #addAttachment(Attachment)
     */
    @SuppressWarnings("unchecked")
    public <T extends Attachment> T getAttachment(Class<T> attachmentClass) {
        return (T)attachments.get(attachmentClass);
    }
    
    /**
     * Removes an attachment from this player. This can remove any attachment available on this server.
     * @param attachmentClass The class of the attachment. This cannot be a subclass or superclass.   
     * @return The attachment instance that was removed, or null if nothing was removed
     * @see #addAttachment(Attachment)
     * @see #getAttachment(Class)
     */
    @SuppressWarnings("unchecked")
    public <T extends Attachment> T removeAttachment(Class<T> attachmentClass) {
        attachmentSet.remove(attachmentClass.getName());
        T attachment = (T)attachments.remove(attachmentClass);
        if (attachment != null && attachment.getType() == AttachmentType.Persistent) {
            requiresSave = true;
        }
        
        return attachment;
    }
    
    /**
     * @return Returns an unmodifiable collection of all attachments currently loaded. 
     */
    public Collection<Attachment> getAttachments() {
        return Collections.unmodifiableCollection(attachments.values());
    }
    
    // Save load
    /**
     * Saves and synchronizes all attachments that have been modified
     */
    public void update() {
        update(false);
    }
    
    /**
     * Saves and synchronizes all attachments.
     * @param force When false, only attachments that have been marked dirty
     *        will be saved / synchronized. When true, all non local
     *        attachments will be saved / synchronized
     */
    public void update(boolean force) {
        boolean hasChanges = false;
        for (Attachment attachment : attachments.values()) {
            if (force || attachment.isDirty()) {
                switch (attachment.getType()) {
                case Persistent:
                    saveAttachment(attachment);
                    hasChanges = true;
                    break;
                case Session:
                    syncAttachment(attachment);
                    break;
                default:
                    // Do nothing
                    break;
                }
                
                attachment.clearDirty();
            }
        }
        
        if (requiresSave || hasChanges) {
            // Save the list of attachments
            storage.set("attachments", attachmentSet);
            
            // Do the save
            storage.update();
        }
    }
    
    private void saveAttachment(Attachment attachment) {
        String name = attachment.getClass().getSimpleName().toLowerCase();
        storage.set(name, attachment);
    }
    
    private void syncAttachment(Attachment attachment) {
        Map<String, String> values = Maps.newHashMap();
        attachment.save(values);
        
        SyncAttachmentMessage message = new SyncAttachmentMessage(owner, attachment.getClass(), values);
        
        channel.broadcast(message);
    }
    
    /**
     * Loads all contained attachments if they are not loaded
     */
    public void loadIfNeeded() {
        if (!hasLoaded) {
            load();
        }
    }
    
    /**
     * Loads all contained attachments
     */
    public void load() {
        attachmentSet = Sets.newHashSet(storage.getSetString("attachments"));
        
        for (String name : attachmentSet) {
            loadAttachment(name);
        }
        
        hasLoaded = true;
    }
    
    private void loadAttachment(String className) {
        try {
            // Resolve attachment class
            Class<?> clazz = Class.forName(className);
            if (!Attachment.class.isAssignableFrom(clazz)) {
                return;
            }
            
            Class<? extends Attachment> attachmentClass = clazz.asSubclass(Attachment.class);
            
            // Get or create the attachment instance
            Attachment attachment;
            if (attachments.containsKey(attachmentClass)) {
                attachment = attachments.get(attachmentClass);
            } else {
                attachment = attachmentClass.newInstance();
            }
            
            // Make sure we arent using this for any attachment that isnt a persistent type attachment
            if (attachment.getType() != AttachmentType.Persistent) {
                return;
            }
            
            // Load it
            attachment = storage.getStorable(clazz.getSimpleName().toLowerCase(), attachment);
            attachment.clearDirty();
            
            attachments.put(attachmentClass, attachment);
        } catch (ClassNotFoundException e) {
            // Ignore
        } catch (IllegalAccessException e) {
            // Ignore
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * To be called upon receiving a relevant SyncAttachmentMessage
     * @param message The packet received
     */
    public void onAttachmentUpdate(SyncAttachmentMessage message) {
        try {
            // Check the owner
            if (!owner.equals(message.owner)) {
                return;
            }
            
            // Resolve attachment class
            Class<?> clazz = Class.forName(message.className);
            if (!Attachment.class.isAssignableFrom(clazz)) {
                return;
            }
            
            Class<? extends Attachment> attachmentClass = clazz.asSubclass(Attachment.class);
            
            // Get or create the attachment instance
            Attachment attachment;
            if (attachments.containsKey(attachmentClass)) {
                attachment = attachments.get(attachmentClass);
            } else {
                attachment = attachmentClass.newInstance();
            }
            
            // Make sure we arent using this for any attachment that isnt a session type attachment
            if (attachment.getType() != AttachmentType.Session) {
                return;
            }
            
            // Load it
            attachment.load(message.values);
            attachment.clearDirty();
            
            attachments.put(attachmentClass, attachment);
        } catch (ClassNotFoundException e) {
            // Ignore
        } catch (IllegalAccessException e) {
            // Ignore
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
    
    public void invalidate() {
        hasLoaded = false;
    }
}
