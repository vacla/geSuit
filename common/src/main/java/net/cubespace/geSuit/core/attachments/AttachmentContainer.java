package net.cubespace.geSuit.core.attachments;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.Platform;
import net.cubespace.geSuit.core.attachments.Attachment.AttachmentType;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.player.GlobalPlayerAttachmentUpdateEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.SyncAttachmentMessage;
import net.cubespace.geSuit.core.storage.StorageSection;

public class AttachmentContainer {
    // The full set of attachment class names including attachments that are
    // not loaded due to missing classes. Used for saving attachments
    private Set<String> attachmentSet;
    private Map<Class<? extends Attachment>, Attachment> attachments;
    
    private boolean hasLoaded;
    private boolean requiresSave;
    
    private GlobalPlayer owner;
    private Channel<BaseMessage> channel;
    private StorageSection storage;
    private Platform platform;
    
    public AttachmentContainer(GlobalPlayer owner, Channel<BaseMessage> channel, StorageSection storage, Platform platform) {
        this.owner = owner;
        this.channel = channel;
        this.storage = storage;
        this.platform = platform;
        
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
        } else if (attachment.getType() == AttachmentType.Session) {
            attachment.setDirty();
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
                
                if (attachment.isDirty()) {
                    platform.callEvent(new GlobalPlayerAttachmentUpdateEvent(owner, attachment));
                }
                
                attachment.clearDirty();
            }
        }
        
        if (requiresSave || hasChanges) {
            // Save the list of attachments
            storage.set("attachments", attachmentSet);
        }
    }
    
    private void saveAttachment(Attachment attachment) {
        String name = attachment.getClass().getSimpleName().toLowerCase();
        storage.set(name, attachment);
    }
    
    private void syncAttachment(Attachment attachment) {
        Map<String, String> values = Maps.newHashMap();
        attachment.save(values);
        
        SyncAttachmentMessage message = new SyncAttachmentMessage(owner.getUniqueId(), attachment.getClass(), values);
        
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
    
    private Class<? extends Attachment> getAttachmentClass(String className) {
        try {
            // Resolve attachment class
            Class<?> clazz = Class.forName(className);
            if (!Attachment.class.isAssignableFrom(clazz)) {
                return null;
            }
            
            return clazz.asSubclass(Attachment.class);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    private Attachment getAttachment(String className) {
        try {
            // Resolve attachment class
            Class<? extends Attachment> attachmentClass = getAttachmentClass(className);
            
            if (attachmentClass == null) {
                return null;
            }
            
            // Get or create the attachment instance
            if (attachments.containsKey(attachmentClass)) {
                return attachments.get(attachmentClass);
            } else {
                return attachmentClass.newInstance();
            }
        } catch (IllegalAccessException e) {
            // Ignore
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private void loadAttachment(String className) {
        Attachment attachment = getAttachment(className);
        if (attachment == null) {
            return;
        }
        
        // Make sure we arent using this for any attachment that isnt a persistent type attachment
        if (attachment.getType() != AttachmentType.Persistent) {
            return;
        }
        
        // Load it
        Attachment loaded = storage.getStorable(attachment.getClass().getSimpleName().toLowerCase(), attachment);
        if (loaded != null) {
            attachment = loaded;
        }
        attachment.clearDirty();
        
        attachments.put(attachment.getClass(), attachment);
    }
    
    /**
     * To be called upon receiving a relevant SyncAttachmentMessage
     * @param message The packet received
     */
    public void onAttachmentUpdate(SyncAttachmentMessage message) {
        // Check the owner
        if (!owner.getUniqueId().equals(message.owner)) {
            return;
        }
        
        Attachment attachment = getAttachment(message.className);
        if (attachment == null) {
            return;
        }
        
        // Make sure we arent using this for any attachment that isnt a session type attachment
        if (attachment.getType() != AttachmentType.Session) {
            return;
        }
        
        // Load it
        attachment.load(message.values);
        attachment.clearDirty();
        
        attachments.put(attachment.getClass(), attachment);
        
        platform.callEvent(new GlobalPlayerAttachmentUpdateEvent(owner, attachment));
    }
    
    public void invalidate() {
        hasLoaded = false;
    }
}
