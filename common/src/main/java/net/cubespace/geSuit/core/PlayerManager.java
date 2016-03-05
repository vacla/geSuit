package net.cubespace.geSuit.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.attachments.Attachment;
import net.cubespace.geSuit.core.attachments.Attachment.AttachmentType;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.player.GlobalPlayerNicknameEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage;
import net.cubespace.geSuit.core.messages.SyncAttachmentMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Action;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Item;
import net.cubespace.geSuit.core.storage.RedisConnection;
import net.cubespace.geSuit.core.storage.StorageInterface;
import net.cubespace.geSuit.core.storage.StorageProvider;
import net.cubespace.geSuit.core.util.PlayerCache;
import net.cubespace.geSuit.core.util.Utilities;

public abstract class PlayerManager {
    
    private Map<UUID, GlobalPlayer> playersById;
    private Map<String, GlobalPlayer> playersByName;
    private Map<String, GlobalPlayer> playersByNickname;
    
    private PlayerCache offlineCache;
    
    protected Channel<BaseMessage> channel;
    private RedisConnection redis;
    private Platform platform;
    private StorageProvider storageProvider;
    private StorageInterface allPlayerStorage;
    private boolean storageLogging;

    public PlayerManager(Channel<BaseMessage> channel, RedisConnection redis, StorageProvider storageProvider, Platform platform) {
        playersById = Maps.newHashMap();
        playersByName = Maps.newHashMap();
        playersByNickname = Maps.newHashMap();
        
        offlineCache = new PlayerCache(TimeUnit.MINUTES.toMillis(10));
        
        this.channel = channel;
        this.redis = redis;
        this.storageProvider = storageProvider;
        this.platform = platform;

        storageLogging = true;
        allPlayerStorage = storageProvider.create("geSuit.players", storageLogging);
    }
    
    public RedisConnection getRedis() {
        return redis;
    }
    
    public Channel<BaseMessage> getUpdateChannel() {
        return channel;
    }
    
    public void initRedis() {
        loadScripts();
    }
    
    //=================================================
    //           Player retrieval methods
    //=================================================
    
    public GlobalPlayer getPlayer(String name, boolean useNickname) {
        List<GlobalPlayer> players = getPlayers(name, useNickname);
        if (players.isEmpty()) {
            return null;
        } else {
            return players.get(0);
        }
    }
    
    public List<GlobalPlayer> getPlayers(String name, boolean useNickname) {
        int best = Integer.MAX_VALUE;
        List<GlobalPlayer> bestPlayers = Lists.newArrayList();
        
        name = name.toLowerCase();
        
        for (GlobalPlayer player : playersById.values()) {
            String playerName = player.getName().toLowerCase();

            if (playerName.contains(name)) {
                int diff = playerName.length() - name.length();
                if (diff < best) {
                    best = diff;
                    bestPlayers.clear();
                    bestPlayers.add(player);
                } else if (diff == best) {
                    bestPlayers.add(player);
                }
            }

            if (player.hasNickname()) {
                String nickname = player.getNickname().toLowerCase();
                if (useNickname && !playerName.equalsIgnoreCase(nickname) && nickname.contains(name)) {
                    int diff = nickname.length() - name.length();
                    if (diff < best) {
                        best = diff;
                        bestPlayers.clear();
                        bestPlayers.add(player);
                    } else if (diff == best) {
                        bestPlayers.add(player);
                    }
                }
            }
        }
        
        return bestPlayers;
    }
    
    public GlobalPlayer getPlayerExact(String name, boolean useNickname) {
        GlobalPlayer player = playersByName.get(name.toLowerCase());
        
        if (player == null && useNickname) {
            return playersByNickname.get(name.toLowerCase());
        } else {
            return player;
        }
    }
    
    public GlobalPlayer getPlayer(UUID id) {
        return playersById.get(id);
    }
    
    public Collection<GlobalPlayer> getPlayers() {
        return Collections.unmodifiableCollection(playersById.values());
    }
    
    //=================================================
    //           Offline player retrieval
    //=================================================
    
    public GlobalPlayer getOfflinePlayer(UUID id) {
        GlobalPlayer player = playersById.get(id);
        if (player == null) {
            player = offlineCache.get(id);
        }
        
        if (player != null) {
            if (!player.isReal()) {
                return null;
            } else {
                return player;
            }
        }
        
        return loadOfflinePlayer(id);
    }
    
    public GlobalPlayer getOfflinePlayer(String name) {
        return getOfflinePlayer(name, true);
    }
    
    public GlobalPlayer getOfflinePlayer(String name, boolean useNickname) {
        GlobalPlayer player = getPlayerExact(name, useNickname);
        if (player == null) {
            player = offlineCache.getFromName(name, useNickname);
        }
        
        if (player != null) {
            if (!player.isReal()) {
                return null;
            } else {
                return player;
            }
        }
        
        UUID id = executeGetPlayerByName(name, useNickname);
        if (id == null) {
            // The purpose of this is to cache the name lookup so we can prevent re-doing the lookup within a 
            // short amount of time as it is quite expensive
            GlobalPlayer fake = new GlobalPlayer(name);
            offlineCache.add(fake);
            return null;
        } else {
            return loadOfflinePlayer(id);
        }
    }
    
    //=================================================
    //            Packet handling methods
    //=================================================
    
    public void handlePlayerUpdate(PlayerUpdateMessage update) {
        switch (update.action) {
        case Name:
            for (Item item : update.items) {
                GlobalPlayer player = getPlayer(item.id);
                if (player != null) {
                    updateNickname(player, item.nickname);
                } else {
                    player = offlineCache.get(item.id);
                    if (player != null) {
                        updateNickname(player, item.nickname);
                    }
                }
            }
            break;
        case Invalidate:
            for (Item item : update.items) {
                GlobalPlayer player = getPlayer(item.id);
                if (player != null) {
                    player.invalidate();
                } else {
                    player = offlineCache.get(item.id);
                    if (player != null) {
                        player.invalidate();
                    }
                }
            }
            break;
        default:
            // Not handled for common
            break;
        }
    }
    
    public void handleSyncAttachment(SyncAttachmentMessage message) {
        GlobalPlayer player = playersById.get(message.owner);
        if (player == null) {
            player = offlineCache.get(message.owner);
            
            if (player == null) {
                return;
            }
        }
        
        player.getAttachmentContainer().onAttachmentUpdate(message);
    }
    
    //=================================================
    //               Player manipulation
    //=================================================
    
    protected void clearPlayers() {
        playersById.clear();
        playersByName.clear();
        playersByNickname.clear();
    }
    
    private StorageInterface getStorageSection(UUID playerId) {
        return storageProvider.create("geSuit.players." + Utilities.toString(playerId), storageLogging);
    }
    
    protected GlobalPlayer loadPlayer(UUID id, String name, String nickname) {
        GlobalPlayer player = offlineCache.get(id);
        if (player == null) {
            player = new GlobalPlayer(id, name, nickname, this, getStorageSection(id), platform);
        }
        
        return player;
    }
    
    private GlobalPlayer loadOfflinePlayer(UUID id) {
        GlobalPlayer player = new GlobalPlayer(id, this, getStorageSection(id), platform);
        player.loadLite();
        
        offlineCache.add(player);
        return player;
    }
    
    protected void addPlayer(GlobalPlayer player) {
        playersById.put(player.getUniqueId(), player);
        playersByName.put(player.getName().toLowerCase(), player);
        if (player.hasNickname()) {
            playersByNickname.put(player.getNickname().toLowerCase(), player);
        }
        
        offlineCache.remove(player);
    }
    
    protected void removePlayer(GlobalPlayer player) {
        if (player == null) {
            return;
        }
        
        playersById.remove(player.getUniqueId());
        playersByName.remove(player.getName());
        
        if (player.hasNickname()) {
            playersByNickname.remove(player.getNickname());
        }
        
        // Remove session and local attachments
        List<Attachment> toRemove = Lists.newArrayList();
        for (Attachment attachment : player.getAttachmentContainer().getAttachments()) {
            if (attachment.getType() == AttachmentType.Session || attachment.getType() == AttachmentType.Local) {
                toRemove.add(attachment);
            }
        }
        
        for (Attachment attachment : toRemove) {
            player.getAttachmentContainer().removeAttachment(attachment.getClass());
        }
        
        offlineCache.add(player);
    }
    
    //=================================================
    //               Proxy ONLY methods
    //=================================================
    
    public PlayerUpdateMessage createFullUpdatePacket() {
        Item[] items = new Item[playersById.size()];
        
        int index = 0;
        for (GlobalPlayer player : playersById.values()) {
            items[index++] = new Item(player.getUniqueId(), player.getName(), player.getNickname());
        }
        
        return new PlayerUpdateMessage(Action.Reset, items);
    }
    
    //=================================================
    //            GlobalPlayer interactions
    //=================================================
    
    void trySetNickname(GlobalPlayer player, String nickname) throws IllegalArgumentException {
        if (nickname != null) {
            // Make sure its not the same as any existing username or nickname
            if (!nickname.equalsIgnoreCase(player.getName())) {
                if (playersByName.containsKey(nickname.toLowerCase())) {
                    throw new IllegalArgumentException("Name already in use");
                } else if (playersByNickname.containsKey(nickname.toLowerCase())) {
                    throw new IllegalArgumentException("Name already in use");
                }
            // Make sure its not exactly the same as the players name (can be case changed)
            } else if (nickname.equals(player.getName())) {
                throw new IllegalArgumentException("Name already in use");
            }
        }
        
        updateNickname(player, nickname);
        channel.broadcast(new PlayerUpdateMessage(Action.Name, new Item(player.getUniqueId(), null, nickname)));
    }
    
    private void updateNickname(GlobalPlayer player, String newName) {
        String previous = player.getNickname();
        player.setNickname0(newName);

        // Online users only
        if (playersById.containsKey(player.getUniqueId())) {
            if (previous != null) {
                playersByNickname.remove(previous.toLowerCase());
            }
            if (player.hasNickname()) {
                playersByNickname.put(player.getNickname().toLowerCase(), player);
            }
            platform.callEvent(new GlobalPlayerNicknameEvent(player, previous));
        }
        
        offlineCache.onUpdateNickname(player, previous);
    }
    
    void invalidateOthers(GlobalPlayer player) {
        channel.broadcast(new PlayerUpdateMessage(Action.Invalidate, new Item(player.getUniqueId(), null, null)));
    }
    
    void onPlayerSave(GlobalPlayer player) {
        allPlayerStorage.appendSet("all", player.getUniqueId());
        allPlayerStorage.update();
    }
    
    //=================================================
    //                 Redis scripts
    //=================================================
    
    private void loadScripts() {
        Jedis jedis = null;
        try {
            jedis = redis.getJedis();
            scriptSHAGetPlayerByName = jedis.scriptLoad(scriptGetPlayerByName());
        } catch (JedisException e) {
            redis.returnJedis(jedis, e);
            throw new RuntimeException(e);
        } finally {
            redis.returnJedis(jedis);
        }
    }
    private String scriptSHAGetPlayerByName;
    private String scriptGetPlayerByName() {
        return    "local ids = redis.call('SMEMBERS','geSuit.players.all')\n"
                + "local useNickname = (ARGV[2] == 'true')\n"
                + "local target = ARGV[1]\n"
                + "local match = ''\n"
                + "for _, id in pairs(ids) do\n"
                + "  local key = 'geSuit.players.' .. id .. '.info'\n"
                + "  local name = redis.call('HGET', key, 'name'):lower()\n"
                + "  if name == target then\n"
                + "    return id\n"
                + "  elseif useNickname and redis.call('HEXISTS', key, 'nickname') ~= 0 then\n"
                + "    name = redis.call('HGET', key, 'nickname'):lower()\n"
                + "    if name == target then\n"
                + "      match = id\n"
                + "    end\n"
                + "  end\n"
                + "end\n"
                + "if match:len() > 0 then\n"
                + "  return match\n"
                + "end";
    }
    
    private UUID executeGetPlayerByName(final String name, final boolean useNicknames) {
        Jedis jedis = null;
        try {
            jedis = redis.getJedis();
            String rawId = (String)jedis.evalsha(scriptSHAGetPlayerByName, 0, name.toLowerCase(), String.valueOf(useNicknames));
            if (rawId == null) {
                return null;
            } else {
                return Utilities.makeUUID(rawId);
            }
        } catch (JedisException e) {
            redis.returnJedis(jedis, e);
            throw new RuntimeException("Error running getPlayerByName redis script", e);
        } finally {
            redis.returnJedis(jedis);
        }
    }
}
