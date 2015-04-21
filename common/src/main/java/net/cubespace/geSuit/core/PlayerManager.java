package net.cubespace.geSuit.core;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.channel.RedisChannelManager;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Action;
import net.cubespace.geSuit.core.messages.PlayerUpdateRequestMessage;
import net.cubespace.geSuit.core.messages.PlayerUpdateMessage.Item;
import net.cubespace.geSuit.core.storage.RedisConnection;

public class PlayerManager implements ChannelDataReceiver<BaseMessage> {
    
    private Map<UUID, GlobalPlayer> playersById;
    private Map<String, GlobalPlayer> playersByName;
    private Map<String, GlobalPlayer> playersByNickname;
    
    private Map<UUID, GlobalPlayer> loadingPlayers;
    
    private Channel<BaseMessage> channel;
    private RedisConnection redis;
    private boolean proxyMode;
    
    public PlayerManager(boolean proxyMode, ChannelManager manager) {
        playersById = Maps.newHashMap();
        playersByName = Maps.newHashMap();
        playersByNickname = Maps.newHashMap();
        loadingPlayers = Maps.newHashMap();
        
        this.proxyMode = proxyMode;
        channel = manager.createChannel("players", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        channel.addReceiver(this);
        redis = ((RedisChannelManager)manager).getRedis();
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
            String nickname = player.getNickname().toLowerCase();

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
        throw new UnsupportedOperationException("Not Implemented");
    }
    
    public GlobalPlayer getOfflinePlayer(String name) {
        return getOfflinePlayer(name, true);
    }
    
    public GlobalPlayer getOfflinePlayer(String name, boolean useNickname) {
        throw new UnsupportedOperationException("Not Implemented");
    }
    
    //=================================================
    //            Packet handling methods
    //=================================================
    
    private void onUpdateMessage(PlayerUpdateMessage update) {
        switch (update.action) {
        case Reset:
            playersById.clear();
            playersByName.clear();
            playersByNickname.clear();
        case Add:
            for (Item item : update.items) {
                addPlayer(item.id, item.username, item.nickname);
            }
            break;
        case Name:
            for (Item item : update.items) {
                onNickname(item.id, item.nickname, false);
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
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value) {
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
    
    
    private void addPlayer(UUID id, String name, String nickname) {
        GlobalPlayer player = new GlobalPlayer(id, redis, name, nickname);
        addPlayer(player);
    }
    
    private void addPlayer(GlobalPlayer player) {
        playersById.put(player.getUniqueId(), player);
        playersByName.put(player.getName().toLowerCase(), player);
        if (player.hasNickname()) {
            playersByNickname.put(player.getNickname().toLowerCase(), player);
        }
        
        System.out.println("Adding player " + player.getName());
    }
    
    private boolean removePlayer(UUID id) {
        GlobalPlayer player = playersById.remove(id);
        if (player != null) {
            playersByName.remove(player.getName().toLowerCase());
            
            if (player.hasNickname()) {
                playersByNickname.remove(player.getNickname().toLowerCase());
            }
            
            System.out.println("Removing player " + player.getName());
            return true;
        }
        
        return false;
    }
    
    //=================================================
    //               Proxy ONLY methods
    //=================================================
    
    protected GlobalPlayer loadPlayer(UUID id, String name, InetAddress address) {
        GlobalPlayer player;
        
        if (playersById.containsKey(id)) {
            player = playersById.get(id);
        } else {
            player = new GlobalPlayer(id, redis, name, null);
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
            addPlayer(player);
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
    
    protected void onNickname(UUID id, String newName, boolean broadcast) {
        GlobalPlayer player = playersById.get(id);
        if (player != null) {
            if (player.hasNickname()) {
                playersByNickname.remove(player.getNickname().toLowerCase());
            }
            
            player.setNickname0(newName);
            
            if (player.hasNickname()) {
                playersByNickname.put(player.getNickname().toLowerCase(), player);
            }
            
            if (broadcast) {
                channel.broadcast(new PlayerUpdateMessage(Action.Name, new Item(id, null, newName)));
            }
        }
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
}
