package net.cubespace.geSuit.core.util;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.GlobalPlayer;

public class PlayerCache {
    private Map<UUID, CacheEntry> playersById;
    private Map<String, UUID> idsByName;
    private Map<String, UUID> idsByNickname;
    
    private long expireTime;
    
    public PlayerCache(long expireTime) {
        playersById = Maps.newHashMap();
        idsByName = Maps.newHashMap();
        idsByNickname = Maps.newHashMap();
        
        this.expireTime = expireTime;
    }
    
    public GlobalPlayer get(UUID id) {
        expireOld();
        
        CacheEntry entry = playersById.get(id);
        if (entry != null) {
            entry.refresh();
            return entry.player;
        } else {
            return null;
        }
    }
    
    public GlobalPlayer getFromName(String name, boolean useNickname) {
        expireOld();
        
        UUID id = idsByName.get(name.toLowerCase());
        if (id == null) {
            id = idsByNickname.get(name.toLowerCase());
        }
        
        if (id == null) {
            return null;
        }
        
        CacheEntry entry = playersById.get(id);
        if (entry != null) {
            entry.refresh();
            return entry.player;
        } else {
            return null;
        }
    }
    
    public void add(GlobalPlayer player) {
        CacheEntry entry = new CacheEntry(player);
        playersById.put(player.getUniqueId(), entry);
        idsByName.put(player.getName().toLowerCase(), player.getUniqueId());
        
        if (player.hasNickname()) {
            idsByNickname.put(player.getNickname().toLowerCase(), player.getUniqueId());
        }
    }
    
    public void remove(GlobalPlayer player) {
        CacheEntry entry = playersById.remove(player.getUniqueId());
        
        if (entry != null) {
            idsByName.remove(entry.player.getName().toLowerCase());
            if (entry.player.hasNickname()) {
                idsByNickname.remove(entry.player.getNickname().toLowerCase());
            }
        }
    }
    
    public void onUpdateNickname(GlobalPlayer player, String oldName) {
        if (!playersById.containsKey(player.getUniqueId())) {
            return;
        }
        
        if (oldName != null) {
            idsByNickname.remove(oldName.toLowerCase());
        }
        
        if (player.hasNickname()) {
            idsByNickname.put(player.getNickname().toLowerCase(), player.getUniqueId());
        }
    }
    
    private void expireOld() {
        Iterator<CacheEntry> it = playersById.values().iterator();
        
        while(it.hasNext()) {
            CacheEntry entry = it.next();
            
            if (System.currentTimeMillis() - entry.lastAccess > expireTime) {
                it.remove();
                
                GlobalPlayer player = entry.player;
                idsByName.remove(player.getName().toLowerCase());
                if (player.hasNickname()) {
                    idsByNickname.remove(player.getNickname().toLowerCase());
                }
            }
        }
    }
    
    private class CacheEntry {
        public GlobalPlayer player;
        public long lastAccess;
        
        public CacheEntry(GlobalPlayer player) {
            this.player = player;
            lastAccess = System.currentTimeMillis();
        }
        
        public void refresh() {
            lastAccess = System.currentTimeMillis();
        }
    }
}
