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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class GlobalPlayer {
    private String name;
    private String nickname;
    private UUID id;
    
    private InetAddress address;
    
    private boolean isBanned;
    private BanInfo<GlobalPlayer> banInfo;
    
    private long firstJoin;
    private long lastJoin;
    
    private boolean tpEnabled;
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
    
    public String getName() {
        return name;
    }
    
    void setName(String name) {
        loadIfNeeded();
        this.name = name;
        isDirty = true;
    }
    
    public String getDisplayName() {
        if (nickname == null) {
            return name;
        } else {
            return nickname;
        }
    }
    
    public String getNickname() {
        return nickname;
    }
    
    void setNickname0(String nickname) {
        this.nickname = nickname;
    }
    
    public void setNickname(String nickname) throws IllegalArgumentException {
        Preconditions.checkState(isReal, "This player is not real, you cannot modify it");
        loadIfNeeded();
        
        manager.trySetNickname(this, nickname);
        isDirty = true;
    }
    
    public boolean hasNickname() {
        return nickname != null;
    }
    
    public UUID getUniqueId() {
        return id;
    }
    
    public InetAddress getAddress() {
        return address;
    }
    
    void setAddress(InetAddress address) {
        loadIfNeeded();
        isDirty = true;
        
        this.address = address;
    }
    
    public boolean isBanned() {
        loadIfNeeded();
        return isBanned;
    }
    
    public BanInfo<GlobalPlayer> getBanInfo() {
        loadIfNeeded();
        return banInfo;
    }
    
    public void setBan(BanInfo<GlobalPlayer> ban) {
        Preconditions.checkNotNull(ban);
        Preconditions.checkArgument(ban.getWho() == this);
        
        loadIfNeeded();
        
        banInfo = ban;
        isBanned = true;
        isDirty = true;
    }
    
    public void removeBan() {
        loadIfNeeded();
        
        banInfo = null;
        isBanned = false;
        isDirty = true;
    }
    
    public long getFirstJoined() {
        loadIfNeeded();
        return firstJoin;
    }
    
    public long getLastJoined() {
        loadIfNeeded();
        return lastJoin;
    }
    
    public void setLastJoined(long time) {
        loadIfNeeded();
        lastJoin = time;
        isDirty = true;
    }
    
    public boolean hasPlayedBefore() {
        loadIfNeeded();
        return firstJoin != 0;
    }
    
    public boolean hasTPsEnabled() {
        loadIfNeeded();
        return tpEnabled;
    }
    
    public void setTPsEnabled(boolean enabled) {
        loadIfNeeded();
        tpEnabled = enabled;
        isDirty = true;
    }
    
    public boolean isNewPlayer() {
        loadIfNeeded();
        return newPlayer;
    }
    
    public void setNewPlayer(boolean isNew) {
        loadIfNeeded();
        newPlayer = isNew;
        isDirty = true;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Attachment> T getAttachment(Class<T> type) {
        loadIfNeeded();
        return (T)attachments.get(type);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Attachment> T removeAttachment(Class<T> type) {
        loadIfNeeded();
        return (T)attachments.remove(type);
    }
    
    public <T extends Attachment> void addAttachment(Class<T> type, T attachment) {
        loadIfNeeded();
        Preconditions.checkState(!attachments.containsKey(type));
        attachments.put(type, attachment);
    }
    
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
    
    public void invalidate() {
        isLoaded = false;
    }
    
    public boolean isLoaded() {
        return isLoaded;
    }
    
    private void loadIfNeeded() {
        if (!isLoaded) {
            refresh();
        }
    }
    
    public boolean isDirty() {
        return isDirty;
    }
    
    public void markDirty() {
        isDirty = true;
    }
    
    public void saveIfModified() {
        if (isDirty) {
            save();
        }
    }
    
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
                redis.load(String.format("geSuit.players.%s.%s", Utilities.toString(id)), attachment);
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
