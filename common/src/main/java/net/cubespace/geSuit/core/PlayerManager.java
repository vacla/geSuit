package net.cubespace.geSuit.core;

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
    
    private Channel<BaseMessage> channel;
    private RedisConnection redis;
    private boolean proxyMode;
    
    public PlayerManager(boolean proxyMode, ChannelManager manager) {
        playersById = Maps.newHashMap();
        playersByName = Maps.newHashMap();
        playersByNickname = Maps.newHashMap();
        
        this.proxyMode = proxyMode;
        channel = manager.createChannel("players", BaseMessage.class);
        channel.addReceiver(this);
        redis = ((RedisChannelManager)manager).getRedis();
    }
    
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
    
    public GlobalPlayer getOfflinePlayer(UUID id) {
        throw new UnsupportedOperationException("Not Implemented");
    }
    
    public GlobalPlayer getOfflinePlayer(String name) {
        return getOfflinePlayer(name, true);
    }
    
    public GlobalPlayer getOfflinePlayer(String name, boolean useNickname) {
        throw new UnsupportedOperationException("Not Implemented");
    }
    
    private void onUpdateMessage(PlayerUpdateMessage update) {
        switch (update.action) {
        case Reset:
            playersById.clear();
            playersByName.clear();
            playersByNickname.clear();
        case Add:
            for (Item item : update.items) {
                onPlayerJoin(item.id, item.username, item.nickname);
            }
            break;
        case Name:
            for (Item item : update.items) {
                onNickname(item.id, item.nickname);
            }
            break;
        case Remove:
            for (Item item : update.items) {
                onPlayerLeave(item.id);
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
    
    private void onPlayerNickname(GlobalPlayer player, String previous) {
        if (previous != null) {
            playersByNickname.remove(previous.toLowerCase());
        }
        
        if (player.hasNickname()) {
            playersByNickname.put(player.getNickname().toLowerCase(), player);
        }
    }
    
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value) {
        if (value instanceof PlayerUpdateMessage && !proxyMode) {
            onUpdateMessage((PlayerUpdateMessage)value);
        } else if (value instanceof PlayerUpdateRequestMessage && proxyMode) {
            onUpdateRequestMessage();
        }
    }
    
    // For proxy implementations
    protected void onPlayerJoin(UUID id, String name, String nickname) {
        GlobalPlayer player = new GlobalPlayer(id, redis, name, nickname);
        
        playersById.put(id, player);
        playersByName.put(name.toLowerCase(), player);
        if (nickname != null) {
            playersByNickname.put(nickname.toLowerCase(), player);
        }
    }
    
    protected void onPlayerLeave(UUID id) {
        GlobalPlayer player = playersById.remove(id);
        if (player != null) {
            playersByName.remove(player.getName().toLowerCase());
            
            if (player.hasNickname()) {
                playersByNickname.remove(player.getNickname().toLowerCase());
            }
        }
    }
    
    protected void onNickname(UUID id, String newName) {
        GlobalPlayer player = playersById.get(id);
        if (player != null) {
            String previous = player.getNickname();
            player.setNickname0(newName);
            onPlayerNickname(player, previous);
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
}
