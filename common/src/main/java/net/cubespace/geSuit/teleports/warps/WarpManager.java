package net.cubespace.geSuit.teleports.warps;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.UpdateWarpMessage;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.objects.Warp;
import net.cubespace.geSuit.core.storage.StorageSection;

public class WarpManager implements ChannelDataReceiver<BaseMessage> {
    private Map<String, Warp> globalWarps;
    private boolean globalChanged;
    private Map<String, Warp> serverWarps;
    private boolean serverChanged;
    private Channel<BaseMessage> channel;
    
    public WarpManager(Channel<BaseMessage> channel) {
        globalWarps = Maps.newHashMap();
        serverWarps = Maps.newHashMap();
        
        this.channel = channel;
        channel.addReceiver(this);
    }
    
    /**
     * Loads the warps from the backend
     */
    public void loadWarps() {
        if (Global.getServer() == null) {
            return;
        }
        
        StorageSection root = Global.getStorageProvider().create("geSuit.warps");
        
        // Load global warps
        globalWarps.clear();
        globalChanged = false;
        Map<String, String> rawWarps = root.getMap("#global");
        
        if (rawWarps != null) {
            for(Entry<String, String> entry : rawWarps.entrySet()) {
                globalWarps.put(entry.getKey(), Warp.fromSerialized(entry.getKey(), entry.getValue()));
            }
        }
        
        // Load server warps
        serverWarps.clear();
        serverChanged = false;
        rawWarps = root.getMap(Global.getServer().getName());
        
        if (rawWarps != null) {
            for(Entry<String, String> entry : rawWarps.entrySet()) {
                serverWarps.put(entry.getKey(), Warp.fromSerialized(entry.getKey(), entry.getValue()));
            }
        }
    }
    
    private void saveWarps() {
        if (Global.getServer() == null) {
            return;
        }
        
        StorageSection root = Global.getStorageProvider().create("geSuit.warps");
        // Save global
        if (globalChanged) {
            root.set("#global", toRedis(globalWarps));
        }
        
        if (serverChanged) {
            root.set(Global.getServer().getName(), toRedis(serverWarps));
        }
        
        root.update();
        // Only update other servers for global warps
        if (globalChanged) {
            channel.broadcast(new UpdateWarpMessage());
        }
        
        globalChanged = false;
        serverChanged = false;
    }
    
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        if (value instanceof UpdateWarpMessage) {
            loadWarps();
        }
    }
    
    /**
     * Gets a global warp with {@code name}
     * @param name The name of the warp, case insentisive
     * @return The warp, or null
     */
    public Warp getGlobalWarp(String name) {
        return globalWarps.get(name.toLowerCase());
    }
    
    /**
     * Gets a local warp with {@code name}
     * @param name The name of the warp, case insentisive
     * @return The warp, or null
     */
    public Warp getLocalWarp(String name) {
        return serverWarps.get(name.toLowerCase());
    }
    
    /**
     * Gets a warp (global or local) with {@code name}.
     * Local warps override global warps
     * @param name The name of the warp, case insentisive
     * @return The warp, or null
     */
    public Warp getWarp(String name) {
        name = name.toLowerCase();
        if (serverWarps.containsKey(name)) {
            return serverWarps.get(name);
        } else {
            return globalWarps.get(name);
        }
    }
    
    /**
     * Checks if a global warp is set
     * @param name The name of the warp case insensitive
     * @return True if it exists
     */
    public boolean hasGlobalWarp(String name) {
        return globalWarps.containsKey(name.toLowerCase());
    }
    
    /**
     * Checks if a local warp is set
     * @param name The name of the warp case insensitive
     * @return True if it exists
     */
    public boolean hasLocalWarp(String name) {
        return serverWarps.containsKey(name.toLowerCase());
    }
    
    /**
     * Checks if a warp (global or local) is set
     * @param name The name of the warp case insensitive
     * @return True if it exists
     */
    public boolean hasWarp(String name) {
        return serverWarps.containsKey(name.toLowerCase()) || globalWarps.containsKey(name.toLowerCase());
    }
    
    /**
     * Creates or updates the <b>global</b> warp {@code name} setting it to {@code location} <br>
     * <b>Note:</b> This method will sync this change with all servers and is blocking
     * @param name The name of the warp, will be made lowercase
     * @param location The location to set, cannot be null
     * @param hidden When true, this warp will not appear in any lists
     */
    public void setGlobalWarp(String name, Location location, boolean hidden) {
        Preconditions.checkNotNull(location);
        
        globalWarps.put(name.toLowerCase(), new Warp(name.toLowerCase(), location, hidden, true));
        globalChanged = true;
        saveWarps();
    }
    
    /**
     * Creates or updates the <b>local</b> warp {@code name} setting it to {@code location}.
     * This warp will only be available on this server <br>
     * <b>Note:</b> This method will sync this change with all servers and is blocking
     * @param name The name of the warp, will be made lowercase
     * @param location The location to set, cannot be null
     * @param hidden When true, this warp will not appear in any lists
     */
    public void setLocalWarp(String name, Location location, boolean hidden) {
        Preconditions.checkNotNull(location);
        
        serverWarps.put(name.toLowerCase(), new Warp(name.toLowerCase(), location, hidden, false));
        serverChanged = true;
        saveWarps();
    }
    
    /**
     * Removes a <b>global</b> warp with {@code name}. 
     * Will do nothing if that warp doesn't exist. <br>
     * <b>Note:</b> This method will sync this change with all servers and is blocking
     * @param name The name of the warp
     */
    public void removeGlobalWarp(String name) {
        if (globalWarps.remove(name.toLowerCase()) != null) {
            globalChanged = true;
            saveWarps();
        }
    }
    
    /**
     * Removes a <b>local</b> warp with {@code name}. 
     * Will do nothing if that warp doesn't exist. <br>
     * <b>Note:</b> This method will sync this change with all servers and is blocking
     * @param name The name of the warp
     */
    public void removeLocalWarp(String name) {
        if (serverWarps.remove(name.toLowerCase()) != null) {
            serverChanged = true;
            saveWarps();
        }
    }
    
    /**
     * Gets all warps available (global and local) including hidden warps.
     * @return An immutable list of all warps
     */
    public List<Warp> getWarps() {
        return getWarps(true);
    }
    
    /**
     * Gets all global warps available including hidden warps.
     * @return An immutable list of all warps
     */
    public List<Warp> getGlobalWarps() {
        return getGlobalWarps(true);
    }
    
    /**
     * Gets all local warps available including hidden warps.
     * @return An immutable list of all warps
     */
    public List<Warp> getLocalWarps() {
        return getLocalWarps(true);
    }
    
    /**
     * Gets all warps available (global and local)
     * @param includeHidden When true, all warps including hidden ones are returned, when false, only warps that are visible are returned.
     * @return An immutable list of all warps based on {@code includeHidden}
     */
    public List<Warp> getWarps(boolean includeHidden) {
        if (includeHidden) {
            return ImmutableList.copyOf(Iterables.concat(globalWarps.values(), serverWarps.values()));
        } else {
            return ImmutableList.copyOf(Iterables.filter(Iterables.concat(globalWarps.values(), serverWarps.values()), new Predicate<Warp>() {
                @Override
                public boolean apply(Warp input) {
                    return !input.isHidden();
                }
            }));
        }
    }
    
    /**
     * Gets all global warps available
     * @param includeHidden When true, all warps including hidden ones are returned, when false, only warps that are visible are returned.
     * @return An immutable list of all warps based on {@code includeHidden}
     */
    public List<Warp> getGlobalWarps(boolean includeHidden) {
        if (includeHidden) {
            return ImmutableList.copyOf(globalWarps.values());
        } else {
            return ImmutableList.copyOf(Iterables.filter(globalWarps.values(), new Predicate<Warp>() {
                @Override
                public boolean apply(Warp input) {
                    return !input.isHidden();
                }
            }));
        }
    }
    
    /**
     * Gets all local warps available
     * @param includeHidden When true, all warps including hidden ones are returned, when false, only warps that are visible are returned.
     * @return An immutable list of all warps based on {@code includeHidden}
     */
    public List<Warp> getLocalWarps(boolean includeHidden) {
        if (includeHidden) {
            return ImmutableList.copyOf(serverWarps.values());
        } else {
            return ImmutableList.copyOf(Iterables.filter(serverWarps.values(), new Predicate<Warp>() {
                @Override
                public boolean apply(Warp input) {
                    return !input.isHidden();
                }
            }));
        }
    }
    
    private Map<String, String> toRedis(Map<String, Warp> warps) {
        Map<String, String> map = Maps.newHashMapWithExpectedSize(warps.size());
        
        for (Entry<String, Warp> entry : warps.entrySet()) {
            map.put(entry.getKey(), entry.getValue().toSerialized());
        }
        
        return map;
    }
}
