package net.cubespace.geSuit.core;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.channel.RedisChannelManager;
import net.cubespace.geSuit.core.events.player.GlobalPlayerJoinEvent;
import net.cubespace.geSuit.core.events.player.GlobalPlayerNicknameEvent;
import net.cubespace.geSuit.core.events.player.GlobalPlayerQuitEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Action;
import net.cubespace.geSuit.core.messages.PlayerUpdateRequestMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Item;
import net.cubespace.geSuit.core.storage.RedisConnection;
import net.cubespace.geSuit.core.storage.RedisConnection.JedisRunner;
import net.cubespace.geSuit.core.util.PlayerCache;
import net.cubespace.geSuit.core.util.Utilities;

public class PlayerManager implements ChannelDataReceiver<BaseMessage> {
    
    private Map<UUID, GlobalPlayer> playersById;
    private Map<String, GlobalPlayer> playersByName;
    private Map<String, GlobalPlayer> playersByNickname;
    
    private PlayerCache offlineCache;
    
    private Map<UUID, GlobalPlayer> loadingPlayers;
    
    private Channel<BaseMessage> channel;
    private RedisConnection redis;
    private boolean proxyMode;
    
    public PlayerManager(boolean proxyMode, ChannelManager manager) {
        playersById = Maps.newHashMap();
        playersByName = Maps.newHashMap();
        playersByNickname = Maps.newHashMap();
        loadingPlayers = Maps.newHashMap();
        
        offlineCache = new PlayerCache(TimeUnit.MINUTES.toMillis(10));
        
        this.proxyMode = proxyMode;
        channel = manager.createChannel("players", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        channel.addReceiver(this);
        
        redis = ((RedisChannelManager)manager).getRedis();
        loadScripts();
    }
    
    public RedisConnection getRedis() {
        return redis;
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
    
    private void onUpdateMessage(PlayerUpdateMessage update) {
        boolean isReset = false;
        switch (update.action) {
        case Reset:
            playersById.clear();
            playersByName.clear();
            playersByNickname.clear();
            isReset = true;
        case Add:
            for (Item item : update.items) {
                addPlayer(item.id, item.username, item.nickname, isReset);
            }
            break;
        case Name:
            for (Item item : update.items) {
                GlobalPlayer player = playersById.get(item.id);
                if (player != null) {
                    onNickname(player, item.nickname, false);
                }
            }
            break;
        case Remove:
            for (Item item : update.items) {
                removePlayer(item.id);
            }
            break;
        case Invalidate:
            for (Item item : update.items) {
                GlobalPlayer player = playersById.get(item.id);
                if (player != null) {
                    player.invalidate();
                }
            }
            break;
        }
    }
    
    private void onUpdateRequestMessage() {
        broadcastFullUpdate();
    }
    
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        System.out.println("Got message " + value);
        if (value instanceof PlayerUpdateMessage && !proxyMode) {
            onUpdateMessage((PlayerUpdateMessage)value);
        } else if (value instanceof PlayerUpdateRequestMessage && proxyMode) {
            onUpdateRequestMessage();
        }
    }
    
    //=================================================
    //               Player manipulation
    //=================================================
    
    
    private void addPlayer(UUID id, String name, String nickname, boolean isReset) {
        GlobalPlayer player = offlineCache.get(id);
        if (player == null) {
            player = new GlobalPlayer(id, this, name, nickname);
        }
        
        addPlayer(player, isReset);
    }
    
    private void addPlayer(GlobalPlayer player, boolean isReset) {
        playersById.put(player.getUniqueId(), player);
        playersByName.put(player.getName().toLowerCase(), player);
        if (player.hasNickname()) {
            playersByNickname.put(player.getNickname().toLowerCase(), player);
        }
        
        offlineCache.remove(player);
        
        // Dont call the event on a reset
        if (!isReset) {
            Global.getPlatform().callEvent(new GlobalPlayerJoinEvent(player));
        }
        System.out.println("Adding player " + player.getName());
    }
    
    private boolean removePlayer(UUID id) {
        GlobalPlayer player = playersById.remove(id);
        if (player != null) {
            Global.getPlatform().callEvent(new GlobalPlayerQuitEvent(player));
            
            playersByName.remove(player.getName().toLowerCase());
            
            if (player.hasNickname()) {
                playersByNickname.remove(player.getNickname().toLowerCase());
            }
            
            offlineCache.add(player);
            
            System.out.println("Removing player " + player.getName());
            return true;
        }
        
        return false;
    }
    
    private GlobalPlayer loadOfflinePlayer(UUID id) {
        GlobalPlayer player = new GlobalPlayer(id, this);
        player.loadLite();
        
        offlineCache.add(player);
        return player;
    }
    
    //=================================================
    //               Proxy ONLY methods
    //=================================================
    
    protected GlobalPlayer getPreloadedPlayer(UUID id) {
        return loadingPlayers.get(id);
    }
    
    protected GlobalPlayer loadPlayer(UUID id, String name, InetAddress address) {
        GlobalPlayer player = offlineCache.get(id);
        
        if (player == null) {
            player = new GlobalPlayer(id, this, name, null);
        }
        
        if (!player.isLoaded()) {
            player.refresh();
        }
        
        // Update name for name changes
        if (!player.getName().equals(name)) {
            player.setName(name);
        }
        
        // Update IP for address changes
        if (!address.equals(player.getAddress())) {
            player.setAddress(address);
        }
        
        return player;
    }
    
    /**
     * To be called on the proxy upon completing the initial login stage.
     * @param player The player that is joining
     */
    protected void onPlayerLoginInitComplete(GlobalPlayer player) {
        // They are now loaded, but they are not yet ready to be visible to all servers
        loadingPlayers.put(player.getUniqueId(), player);
    }
    
    /**
     * To be called on the proxy upon connecting to a server
     * @param id The UUID of the joining player
     * @return true if this was the first connection
     */
    protected boolean onServerConnect(UUID id) {
        GlobalPlayer player = loadingPlayers.remove(id);
        if (player != null) {
            offlineCache.remove(player);
            addPlayer(player, false);
            player.saveIfModified();
            channel.broadcast(new PlayerUpdateMessage(Action.Add, new Item(id, player.getName(), player.getNickname())));
            
            return true;
        }
        
        return false;
    }
    
    /**
     * To be called upon a player disconnecting
     * @param id The UUID of the quitting player
     */
    protected void onPlayerLeave(UUID id) {
        if (removePlayer(id)) {
            channel.broadcast(new PlayerUpdateMessage(Action.Remove, new Item(id, null, null)));
        }
        
        loadingPlayers.remove(id);
    }
    
    public void broadcastFullUpdate() {
        Preconditions.checkState(proxyMode);
        
        Item[] items = new Item[playersById.size()];
        
        int index = 0;
        for (GlobalPlayer player : playersById.values()) {
            items[index++] = new Item(player.getUniqueId(), player.getName(), player.getNickname());
        }
        
        channel.broadcast(new PlayerUpdateMessage(Action.Reset, items));
    }
    
    //=================================================
    //               Client ONLY methods
    //=================================================
    
    public void requestFullUpdate() {
        Preconditions.checkState(!proxyMode);
        
        channel.broadcast(new PlayerUpdateRequestMessage());
    }
    
    //=================================================
    //            GlobalPlayer interactions
    //=================================================
    
    public void trySetNickname(GlobalPlayer player, String nickname) throws IllegalArgumentException {
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
        
        onNickname(player, nickname, true);
    }
    
    private void onNickname(GlobalPlayer player, String newName, boolean broadcast) {
        String previous = player.getNickname();
        player.setNickname0(newName);

        if (previous != null) {
            playersByNickname.remove(player.getNickname().toLowerCase());
        }
        if (player.hasNickname()) {
            playersByNickname.put(player.getNickname().toLowerCase(), player);
        }
        
        if (broadcast) {
            channel.broadcast(new PlayerUpdateMessage(Action.Name, new Item(player.getUniqueId(), null, newName)));
        }
        
        offlineCache.onUpdateNickname(player, previous);
        Global.getPlatform().callEvent(new GlobalPlayerNicknameEvent(player, previous));
    }
    
    public void invalidate(GlobalPlayer player) {
        channel.broadcast(new PlayerUpdateMessage(Action.Invalidate, new Item(player.getUniqueId(), null, null)));
    }
    
    //=================================================
    //                 Redis scripts
    //=================================================
    
    private void loadScripts() {
        redis.new JedisRunner<Void>() {
            @Override
            public Void execute(Jedis jedis) throws Exception {
                scriptSHAGetPlayerByName = jedis.scriptLoad(scriptGetPlayerByName());
                return null;
            }
        }.runAndThrow();
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
        JedisRunner<String> runner = redis.new JedisRunner<String>() {
            @Override
            public String execute(Jedis jedis) throws Exception {
                return (String)jedis.evalsha(scriptSHAGetPlayerByName, 0, name.toLowerCase(), String.valueOf(useNicknames));
            }
        };
        
        if (runner.runAndThrow()) {
            String result = runner.getReturnedValue();
            if (result == null) {
                return null;
            } else {
                return Utilities.makeUUID(result);
            }
        } else {
            throw new RuntimeException("Error running getPlayerByName redis script", runner.getLastError());
        }
    }
}
