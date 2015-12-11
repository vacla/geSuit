package net.cubespace.geSuit.core.attachments;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.Platform;
import net.cubespace.geSuit.core.attachments.Attachment.AttachmentType;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.player.GlobalPlayerAttachmentUpdateEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.SyncAttachmentMessage;
import net.cubespace.geSuit.core.storage.StorageSection;

public class AttachmentContainer {
    // All existing attachments for each type 
    private final SetMultimap<AttachmentType, String> definedAttachments;
    // All attachments regardless of type. May or may not be loaded depending on the class availability
    private final Map<String, Optional<? extends Attachment>> attachments;
    // Keeps track of removed attachments so they can be removed from the backend
    private final Set<String> removed;
    
    private Set<String> modified;
    
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
        
        definedAttachments = HashMultimap.create();
        attachments = Maps.newHashMap();
        removed = Sets.newHashSet();
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
        String typeName = type.getName();
        Preconditions.checkState(!attachments.containsKey(typeName));
        
        removed.remove(typeName);
        definedAttachments.put(attachment.getType(), typeName);
        attachments.put(typeName, Optional.of(attachment));
        
        // Queue save if needed
        switch (attachment.getType()) {
        case Persistent:
        case Session:
            requiresSave = true;
            attachment.setDirty();
            break;
        default:
            // Do nothing
            break;
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
        Optional<? extends Attachment> attachment = attachments.get(attachmentClass.getName());
        if (attachment == null || !attachment.isPresent()) {
            return null;
        }
        
        return (T)attachment.get();
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
        Optional<? extends Attachment> attachment = attachments.remove(attachmentClass.getName());
        if (attachment == null) {
            return null;
        }
        
        AttachmentType type;
        if (attachment.isPresent()) {
            definedAttachments.remove(attachment.get().getType(), attachmentClass.getName());
            type = attachment.get().getType();
        } else {
            type = null;
            // Remove the definition, dont know what type it is
            for (AttachmentType t : AttachmentType.values()) {
                if (definedAttachments.remove(t, attachmentClass.getName())) {
                    type = t;
                }
            }
        }
        
        switch (type) {
        case Persistent:
        case Session:
            requiresSave = true;
            removed.add(attachmentClass.getName());
            break;
        default:
            // Do nothing
            break;
        }
        
        return (T)attachment.orNull();
    }
    
    /**
     * Removes all attachments of a specific type
     * @param type The type of attachment
     */
    public void removeAll(AttachmentType type) {
        for (String id : definedAttachments.removeAll(type)) {
            attachments.remove(id);
        }
        
        if (type != AttachmentType.Local) {
            requiresSave = true;
        }
    }
    
    /**
     * @return Returns an unmodifiable collection of all attachments currently loaded. 
     */
    public Collection<Attachment> getAttachments() {
        ImmutableSet.Builder<Attachment> builder = ImmutableSet.builder();
        
        for (Optional<? extends Attachment> attachment : attachments.values()) {
            if (attachment.isPresent()) {
                builder.add(attachment.get());
            }
        }
        
        return builder.build();
    }
    
    /**
     * Checks if this container has modified attachments in it
     * @return True if at least one attachment is modified
     */
    public boolean isModified() {
        if (requiresSave) {
            return true;
        }
        
        for (Optional<? extends Attachment> attachment : attachments.values()) {
            if (!attachment.isPresent()) {
                continue;
            }
            
            Attachment value = attachment.get();
            if (value.isDirty()) {
                return true;
            }
        }
        
        return false;
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
        // Delete all 'removed' attachments
        for (String id : removed) {
            storage.remove("attachment." + id);
        }
        removed.clear();
        
        modified = Sets.newHashSet();
        
        // Save attachments
        for (Optional<? extends Attachment> attachment : attachments.values()) {
            if (!attachment.isPresent()) {
                continue;
            }
            
            Attachment value = attachment.get();
            // Check for modification
            if (!force && !value.isDirty()) {
                continue;
            }
            
            // Handle the update event
            if (value.isDirty()) {
                platform.callEvent(new GlobalPlayerAttachmentUpdateEvent(owner, value));
            }
            
            value.clearDirty();
            
            if (value.getType() != AttachmentType.Local) {
                storage.set("attachment." + value.getClass().getName(), value);
                modified.add(value.getClass().getName());
                hasChanges = true;
            }
        }
        
        // Update the sets
        if (requiresSave || hasChanges || force) {
            // Save the list of attachments
            storage.set("attachments.persist", definedAttachments.get(AttachmentType.Persistent));
            storage.set("attachments.session", definedAttachments.get(AttachmentType.Session));
        }
    }
    
    /**
     * Broadcasts any modifications made in {@link #update(boolean)}.
     * This should be called <b>after</b> the changes are written to
     * the backend
     */
    public void broadcastChanges() {
        if (modified == null || modified.isEmpty()) {
            return;
        }
        
        SyncAttachmentMessage message = new SyncAttachmentMessage(owner.getUniqueId(), modified);
        channel.broadcast(message);
        
        modified = null;
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
        // Update the known attachments
        definedAttachments.removeAll(AttachmentType.Persistent);
        definedAttachments.removeAll(AttachmentType.Session);
        definedAttachments.putAll(AttachmentType.Persistent, storage.getSetString("attachments.persist"));
        definedAttachments.putAll(AttachmentType.Session, storage.getSetString("attachments.session"));
        
        // Remove unknown attachments
        Iterator<String> it = attachments.keySet().iterator();
        while (it.hasNext()) {
            String id = it.next();
            
            if (!definedAttachments.containsValue(id)) {
                it.remove();
            }
        }
        
        // Load the attachments from the backend
        Iterable<String> stored = Iterables.concat(
                definedAttachments.get(AttachmentType.Persistent),
                definedAttachments.get(AttachmentType.Session)
                );
        
        for (String id : stored) {
            Optional<? extends Attachment> attachmentHolder = attachments.get(id);
            Attachment attachment;
            if (attachmentHolder == null || !attachmentHolder.isPresent()) {
                attachment = newAttachment(id);
            } else {
                attachment = attachmentHolder.get();
            }
            
            // Load the data
            if (attachment != null) {
                attachment = storage.getStorable("attachment." + id, attachment);
            }
            
            attachments.put(id, Optional.fromNullable(attachment));
            if (attachment != null) {
                attachment.clearDirty();
            }
        }
        
        hasLoaded = true;
    }
    
    /**
     * Gets the string class name as a class object
     * @param className The input class name
     * @return The class as Attachment subclass or null if not possible
     */
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
    
    /**
     * Creates a new attachment from the input class name
     * @param className The name of the class
     * @return The attachment, or null if it couldnt be created
     */
    private Attachment newAttachment(String className) {
        try {
            // Resolve attachment class
            Class<? extends Attachment> attachmentClass = getAttachmentClass(className);
            
            if (attachmentClass == null) {
                return null;
            }
            
            return attachmentClass.newInstance();
        } catch (IllegalAccessException e) {
            // Ignore
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Called to refresh attachments when they change.
     * This also fires update events as needed
     * @param message The update packet
     */
    public void onAttachmentUpdate(SyncAttachmentMessage message) {
        // Reload all attachments
        load();
        
        // Now fire update events for all changed attachments if loaded locally
        for (String id : message.updatedAttachments) {
            Optional<? extends Attachment> attachment = attachments.get(id);
            if (attachment != null && attachment.isPresent()) {
                platform.callEvent(new GlobalPlayerAttachmentUpdateEvent(owner, attachment.get()));
            }
        }
    }
    
    /**
     * Forces attachments to be reloaded next time they are requested
     */
    public void invalidate() {
        hasLoaded = false;
    }
}
