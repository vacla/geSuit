package net.cubespace.geSuit.core;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import net.cubespace.geSuit.core.attachments.Attachment;
import net.cubespace.geSuit.core.attachments.AttachmentContainer;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.core.storage.StorageInterface;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.remote.moderation.BanActions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * This class represents a player that is either online or offline. The player can be anywhere on the server network.
 * <h1>Offline Players</h1>
 * When representing an offline player. By default only a subset of the information is available:
 * <ul>
 * <li>unique id</li>
 * <li>name</li>
 * <li>nickname</li>
 * </ul>
 * Calling {@link #refresh()} will load all information stored for this player
 */
public class GlobalPlayer {
    private String name;
    private String nickname;
    private UUID id;
    
    private InetAddress address;
    
    private boolean isBanned;
    private BanInfo<GlobalPlayer> banInfo;
    
    private long firstJoin;
    private long lastJoin;
    private long sessionJoin;
    
    private boolean tpEnabled = true;
    private boolean newPlayer;
    
    private AttachmentContainer attachments;
    private StorageInterface storage;
    
    private boolean isDirty;
    private boolean isLoaded;
    private boolean isReal;
    private PlayerManager manager;
    
    GlobalPlayer(UUID id, String name, String nickname, PlayerManager manager, StorageInterface storage) {
        this(id, manager, storage);
        
        this.name = name;
        this.nickname = nickname;
    }
    
    GlobalPlayer(UUID id, PlayerManager manager, StorageInterface storage) {
        this.id = id;
        this.manager = manager;
        this.storage = storage;
        
        isReal = true;
        attachments = new AttachmentContainer(id, manager.getUpdateChannel(), storage);
    }
    
    GlobalPlayer(String name) {
        this.id = UUID.nameUUIDFromBytes(("Invalid " + name).getBytes());
        this.name = name;
        
        isReal = false;
    }
    
    /**
     * @return Returns the players actual name (as of the most recent login)
     */
    public String getName() {
        return name;
    }
    
    void setName(String name) {
        loadIfNeeded();
        this.name = name;
        isDirty = true;
    }
    
    /**
     * @return Returns the players name for display purposes. If the player has a nickname, the nickname will be returned, otherwise the real name will be returned.
     */
    public String getDisplayName() {
        if (nickname == null) {
            return name;
        } else {
            return nickname;
        }
    }
    
    /**
     * @return Returns the current nickname of the player. If no nickname has been set, null will be returned.
     */
    public String getNickname() {
        return nickname;
    }
    
    void setNickname0(String nickname) {
        this.nickname = nickname;
    }
    
    /**
     * Changes the players nickname. This method can also be used to remove the nickname (use null for nickname).
     * This method <b>will</b> check the validity of the nickname.
     * <h2>Nickname requirements</h2>
     * <ul>
     * <li>Cannot be a real name</li>
     * <li>Cannot be a nickname for another player</li>
     * </ul>
     * @param nickname The new nickname to use, or null to remove it
     * @throws IllegalArgumentException Thrown if the supplied name does not meet the requirements.
     */
    public void setNickname(String nickname) throws IllegalArgumentException {
        Preconditions.checkState(isReal, "This player is not real, you cannot modify it");
        loadIfNeeded();
        
        manager.trySetNickname(this, nickname);
        isDirty = true;
    }
    
    /**
     * @return Returns true if a nickname exists for this player
     */
    public boolean hasNickname() {
        return nickname != null;
    }
    
    /**
     * @return Returns the players id
     */
    public UUID getUniqueId() {
        return id;
    }
    
    /**
     * @return Returns the players most recent IP address as seen by the proxy
     */
    public InetAddress getAddress() {
        loadIfNeeded();
        return address;
    }
    
    void setAddress(InetAddress address) {
        loadIfNeeded();
        isDirty = true;
        
        this.address = address;
    }
    
    /**
     * @return Returns true if this player has a name ban
     */
    public boolean isBanned() {
        loadIfNeeded();
        return isBanned;
    }
    
    /**
     * @return Returns the current name ban on this user. If no ban is present, null will be returned
     */
    public BanInfo<GlobalPlayer> getBanInfo() {
        loadIfNeeded();
        return banInfo;
    }
    
    /**
     * Sets the active ban on this user. <br>
     * <b>WARNING: This does not record ban history when done, please use the methods in {@link BanActions}</b>
     * @param ban The ban to use. Cannot be null
     */
    public void setBan(BanInfo<GlobalPlayer> ban) {
        Preconditions.checkNotNull(ban);
        Preconditions.checkArgument(ban.getWho() == this);
        
        loadIfNeeded();
        
        banInfo = ban;
        isBanned = true;
        isDirty = true;
    }
    
    /**
     * Removes the current ban on this user. <br>
     * <b>WARNING: This does not record ban history when done, please use the methods in {@link BanActions}</b>
     */
    public void removeBan() {
        loadIfNeeded();
        
        banInfo = null;
        isBanned = false;
        isDirty = true;
    }
    
    /**
     * @return Gets the UNIX datetime in ms when the player first joined the server   
     */
    public long getFirstJoined() {
        loadIfNeeded();
        return firstJoin;
    }
    
    /**
     * @return Gets the UNIX datetime in ms of the most recent time the player was online.
     *         In practice, this is not updated until the player disconnects   
     */
    public long getLastOnline() {
        loadIfNeeded();
        return lastJoin;
    }
    
    /**
     * Updates the players most recent date they were online
     * @param time The UNIX datetime in ms
     */
    public void setLastOnline(long time) {
        loadIfNeeded();
        lastJoin = time;
        isDirty = true;
    }
    
    /**
     * @return Gets the UNIX datetime in ms of the time and date this player session started. If they are not online, the value is undefined.
     */
    public long getSessionJoin() {
        return sessionJoin;
    }
    
    /**
     * Sets the session join datetime for the player
     * @param time The UNIX datetime in ms
     */
    public void setSessionJoin(long time) {
        loadIfNeeded();
        sessionJoin = time;
        
        // Update the first join if unset
        if (firstJoin == 0) {
            firstJoin = time;
            isDirty = true;
        }
    }
    
    /**
     * @return True if the player has ever joined the server
     */
    public boolean hasPlayedBefore() {
        loadIfNeeded();
        return firstJoin != 0;
    }
    
    /**
     * @return True if the player has teleports enabled. This applies to requests to teleport only
     */
    public boolean hasTPsEnabled() {
        loadIfNeeded();
        return tpEnabled;
    }
    
    /**
     * Sets the players teleport state
     * @param enabled When true, players are able to request a teleport to them
     */
    public void setTPsEnabled(boolean enabled) {
        loadIfNeeded();
        tpEnabled = enabled;
        isDirty = true;
    }
    
    /**
     * @return Returns true if this is the first join of a player
     */
    public boolean isNewPlayer() {
        loadIfNeeded();
        return newPlayer;
    }
    
    /**
     * Changes the new status of a player
     * @param isNew True if they are to be treated as a new player
     */
    public void setNewPlayer(boolean isNew) {
        loadIfNeeded();
        newPlayer = isNew;
        isDirty = true;
    }
    
    /**
     * Gets a previously added attachment. This can include attachments that are loaded from storage.
     * 
     * @param type The class of the attachment. This cannot be a subclass or superclass.  
     * @return The attachment instance, or null if not found
     * @see #removeAttachment(Class)
     * @see #addAttachment(Attachment)
     */
    public <T extends Attachment> T getAttachment(Class<T> type) {
        attachments.loadIfNeeded();
        return attachments.getAttachment(type);
    }
    
    /**
     * Removes an attachment from this player. This can remove any attachment available on this server.
     * @param type The class of the attachment. This cannot be a subclass or superclass.   
     * @return The attachment instance that was removed, or null if nothing was removed
     * @see #addAttachment(Attachment)
     * @see #getAttachment(Class)
     */
    public <T extends Attachment> T removeAttachment(Class<T> type) {
        attachments.loadIfNeeded();
        return attachments.removeAttachment(type);
    }
    
    /**
     * Adds a new attachment to this player. 
     * Attachments can be accessed through {@link #getAttachment(Class)} using the type of the attachment.
     * <p>A final note about attachables: Unlike the other methods in this class, changing something in an attachment will not
     * mark the player as modified. You will need to call {@link #save()} to sync the changes.</p>
     * 
     * @param attachment The instance of the attachment to add
     * @throws IllegalStateException thrown if the attachment type is already registered
     */
    public <T extends Attachment> void addAttachment(T attachment) throws IllegalStateException {
        attachments.loadIfNeeded();
        attachments.addAttachment(attachment);
    }
    
    /**
     * @return Returns an unmodifiable collection of all attachments currently loaded. 
     */
    public Collection<Attachment> getAttachments() {
        attachments.loadIfNeeded();
        return attachments.getAttachments();
    }
    
    AttachmentContainer getAttachmentContainer() {
        return attachments;
    }
    
    /**
     * Reloads or loads this players data including all stored attachables
     */
    public void refresh() {
        Preconditions.checkState(isReal, "This player is not real, you cannot save or load it");
        
        isDirty = false;
        isLoaded = true;
        
        load0();
    }
    
    /**
     * Marks this player as unloaded forcing any attempt to read a value from it, to load the player
     */
    public void invalidate() {
        isLoaded = false;
        attachments.invalidate();
    }
    
    /**
     * @return True if the player has been loaded
     */
    public boolean isLoaded() {
        return isLoaded;
    }
    
    private void loadIfNeeded() {
        if (!isLoaded) {
            refresh();
        }
    }
    
    /**
     * @return Returns true if a value in this player has been modified and it needs saving
     */
    public boolean isDirty() {
        return isDirty;
    }
    
    /**
     * Marks this player as needing saving
     */
    public void markDirty() {
        isDirty = true;
    }
    
    /**
     * Saves this player only if it has been modified.
     * @see #markDirty()
     * @see #isDirty()
     */
    public void saveIfModified() {
        if (isDirty) {
            save();
        }
    }
    
    /**
     * Saves this player to redis
     */
    public void save() {
        Preconditions.checkState(isReal, "This player is not real, you cannot save or load it");
        
        if (!isLoaded) {
            throw new IllegalStateException("This player has not been loaded yet. Please load it first with refresh() before you can save it");
        }
        
        isDirty = false;
        
        save0();
        
        manager.invalidate(this);
    }
    
    private void load0() {
        // Player settings
        if (!storage.contains("info")) {
            return;
        }
        
        Map<String, String> values = storage.getMap("info");
        name = values.get("name");
        nickname = values.get("nickname");
        if (values.containsKey("first-join")) {
            firstJoin = Utilities.parseDate(values.get("first-join"));
            lastJoin = Utilities.parseDate(values.get("last-join"));
        } else {
            firstJoin = lastJoin = 0;
        }
        
        isBanned = Boolean.parseBoolean(values.get("banned"));
        
        tpEnabled = Boolean.parseBoolean(values.get("tp-enable"));
        newPlayer = Boolean.parseBoolean(values.get("new-player"));
        address = Utilities.makeInetAddress(values.get("ip"));
        
        // Ban info
        if (isBanned) {
            banInfo = storage.getStorable("baninfo", new BanInfo<GlobalPlayer>(this));
        }
        
        // Attachments
        attachments.load();
    }
    
    private void save0() {
        // Player settings
        Map<String, String> values = Maps.newHashMap();
        values.put("name", name);
        if (nickname != null) {
            values.put("nickname", nickname);
        }
        
        if (firstJoin != 0) {
            values.put("first-join", Utilities.formatDate(firstJoin));
            values.put("last-join", Utilities.formatDate(lastJoin));
        }
        
        values.put("banned", String.valueOf(isBanned));
        
        values.put("tp-enable", String.valueOf(tpEnabled));
        values.put("new-player", String.valueOf(newPlayer));
        
        values.put("ip", address.getHostAddress());
        
        storage.set("info", values);
        
        // Ban info
        if (isBanned) {
            storage.set("baninfo", banInfo);
        } else {
            storage.remove("baninfo");
        }
        
        // Attachments
        // TODO: We need to make it so attachments do not have to be loaded and saved with everything else
        attachments.update();
        
        manager.onPlayerSave(this);
        
        storage.updateAtomic();
    }
    
    void loadLite() {
        Preconditions.checkState(isReal, "This player is not real, you cannot save or load it");
        
        loadLite0();
    }
    
    private void loadLite0() {
        // Player settings
        if (!storage.contains("info")) {
            return;
        }
        
        Map<String, String> values = storage.getMapPartial("info", "name", "nickname");
        
        name = values.get("name");
        nickname = values.get("nickname");
    }
    
    boolean isReal() {
        return isReal;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlobalPlayer)) {
            return false;
        }
        
        return ((GlobalPlayer)obj).id.equals(id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("GlobalPlayer %s", name);
    }
}
