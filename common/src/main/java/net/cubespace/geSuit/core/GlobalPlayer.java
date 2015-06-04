package net.cubespace.geSuit.core;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import net.cubespace.geSuit.core.attachments.Attachment;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.core.storage.RedisInterface;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.remote.moderation.BanActions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
    
    private boolean tpEnabled = true;
    private boolean newPlayer;
    
    private Map<Class<? extends Attachment>, Attachment> attachments;
    
    private boolean isDirty;
    private boolean isLoaded;
    private boolean isReal;
    private PlayerManager manager;
    
    GlobalPlayer(UUID id, PlayerManager manager, String name, String nickname) {
        this(id, manager);
        
        this.name = name;
        this.nickname = nickname;
    }
    
    GlobalPlayer(UUID id, PlayerManager manager) {
        this.id = id;
        this.manager = manager;
        
        attachments = Maps.newIdentityHashMap();
        isReal = true;
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
     * @return Gets the UNIX datetime in ms of the most recent time the player joined the server   
     */
    public long getLastJoined() {
        loadIfNeeded();
        return lastJoin;
    }
    
    /**
     * Updates the players most recent joined date
     * @param time The UNIX datetime in ms   
     */
    public void setLastJoined(long time) {
        loadIfNeeded();
        lastJoin = time;
        isDirty = true;
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
     * @see #addAttachment(Class, Attachment)
     */
    @SuppressWarnings("unchecked")
    public <T extends Attachment> T getAttachment(Class<T> type) {
        loadIfNeeded();
        return (T)attachments.get(type);
    }
    
    /**
     * Removes an attachment from this player. This can remove either stored or non-stored attachments.
     * @param type The class of the attachment. This cannot be a subclass or superclass.   
     * @return The attachment instance that was removed, or null if nothing was removed
     * @see #addAttachment(Class, Attachment)
     * @see #getAttachment(Class)
     */
    @SuppressWarnings("unchecked")
    public <T extends Attachment> T removeAttachment(Class<T> type) {
        loadIfNeeded();
        return (T)attachments.remove(type);
    }
    
    /**
     * Adds a new attachment to this player. 
     * <p>Attachments are available in 2 types: storable and non-storable. Which one it is depends on the value of {@link Attachment#isSaved()}</p>
     * <dl>
     *   <dt>Storable</dt>
     *     <dd>Storable attachments are synchronized to all servers that can have them (all classes are available)</dd>
     *   <dt>Non-Storable</dt>
     *     <dd>Non-Storable attachments are <b>not</b> synchronized and are available on this server for this session only</dd>
     * </dl>
     * 
     * Both types can be accessed through {@link #getAttachment(Class)} using the class specified in {@code type}.
     * <p>A final note about Storable attachables: Unlike the other methods in this class, changing something in an attachment will not
     * mark the player as modified. You will need to call {@link #save()} to sync the changes.</p>
     * 
     * @param type The class this attachment will be registered under. This class can be used in the other attachment methods to retrieve this attachment.
     * @param attachment The instance of the attachment to add
     */
    public <T extends Attachment> void addAttachment(Class<T> type, T attachment) {
        loadIfNeeded();
        Preconditions.checkState(!attachments.containsKey(type));
        attachments.put(type, attachment);
    }
    
    /**
     * Reloads or loads this players data including all stored attachables
     */
    public void refresh() {
        Preconditions.checkState(isReal, "This player is not real, you cannot save or load it");
        
        isDirty = false;
        isLoaded = true;
        
        manager.getRedis().new JedisRunner<Void>() {
            @Override
            public Void execute(Jedis jedis) throws Exception {
                load0(new RedisInterface(jedis));
                return null;
            }
        }.runAndThrow();
    }
    
    /**
     * Marks this player as unloaded forcing any attempt to read a value from it, to load the player
     */
    public void invalidate() {
        isLoaded = false;
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
        
        manager.getRedis().new JedisRunner<Void>() {
            @Override
            public Void execute(Jedis jedis) throws Exception {
                save0(jedis);
                return null;
            }
        }.runAndThrow();
        
        manager.invalidate(this);
    }
    
    private void load0(RedisInterface redis) {
        // Player settings
        if (!redis.getJedis().exists(String.format("geSuit.players.%s.info", Utilities.toString(id)))) {
            return;
        }
        
        Map<String, String> values = redis.getJedis().hgetAll(String.format("geSuit.players.%s.info", Utilities.toString(id)));
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
            banInfo = redis.load(String.format("geSuit.players.%s.baninfo", Utilities.toString(id)), new BanInfo<GlobalPlayer>(this));
        }
        
        // Attachments
        Set<String> attachmentNames = redis.getJedis().smembers(String.format("geSuit.players.%s.attachments", Utilities.toString(id)));
        
        for (String name : attachmentNames) {
            try {
                // Resolve attachment class
                Class<?> clazz = Class.forName(name);
                if (!Attachment.class.isAssignableFrom(clazz)) {
                    continue;
                }
                
                Class<? extends Attachment> attachmentClass = clazz.asSubclass(Attachment.class);
                
                // Get or create the attachment instance
                Attachment attachment;
                if (attachments.containsKey(attachmentClass)) {
                    attachment = attachments.get(attachmentClass);
                } else {
                    attachment = clazz.asSubclass(Attachment.class).newInstance();
                }
                
                // Load it
                redis.load(String.format("geSuit.players.%s.%s", Utilities.toString(id), clazz.getSimpleName().toLowerCase()), attachment);
                attachments.put(attachmentClass, attachment);
            } catch (ClassNotFoundException e) {
                continue;
            } catch (IllegalAccessException e) {
                continue;
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void save0(Jedis jedis) {
        Pipeline pipe = jedis.pipelined();
        
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
        
        pipe.hmset(String.format("geSuit.players.%s.info", Utilities.toString(id)), values);
        pipe.sadd("geSuit.players.all", Utilities.toString(id));
        
        // Ban info
        if (isBanned) {
            values.clear();
            banInfo.save(values);
            pipe.hmset(String.format("geSuit.players.%s.baninfo", Utilities.toString(id)), values);
        }
        
        // Attachments
        Set<String> classes = Sets.newHashSet();
        for (Entry<Class<? extends Attachment>, Attachment> attachment : attachments.entrySet()) {
            if (attachment.getValue().isSaved()) {
                classes.add(attachment.getKey().getName());
                
                // Create the map
                Map<String, String> attachmentValues = Maps.newHashMap();
                attachment.getValue().save(attachmentValues);
                
                // Save it
                pipe.hmset(String.format("geSuit.players.%s.%s", Utilities.toString(id), attachment.getKey().getSimpleName().toLowerCase()), attachmentValues);
            }
        }
        
        pipe.sadd(String.format("geSuit.players.%s.attachments", Utilities.toString(id)), classes.toArray(new String[classes.size()]));
        
        pipe.sync();
    }
    
    void loadLite() {
        Preconditions.checkState(isReal, "This player is not real, you cannot save or load it");
        
        manager.getRedis().new JedisRunner<Void>() {
            @Override
            public Void execute(Jedis jedis) throws Exception {
                loadLite0(jedis);
                return null;
            }
        }.runAndThrow();
    }
    
    private void loadLite0(Jedis redis) {
        // Player settings
        if (!redis.exists(String.format("geSuit.players.%s.info", Utilities.toString(id)))) {
            return;
        }
        
        List<String> values = redis.hmget(String.format("geSuit.players.%s.info", Utilities.toString(id)), "name", "nickname");
        
        name = values.get(0);
        nickname = values.get(1);
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
