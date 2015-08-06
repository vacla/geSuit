package net.cubespace.geSuit.core.objects;

import java.util.Map;

import com.google.common.base.Preconditions;

import net.cubespace.geSuit.core.storage.Storable;

public class Portal implements Storable {
    private String name;
    private boolean enabled;
    private Type type;
    private FillType fill;
    private Location min;
    private Location max;
    
    private String dest;
    private Location destLocation;
    
    public Portal(String name) {
        this.name = name;
    }
    
    public Portal(String name, Type type, String dest, FillType fill, Location min, Location max) {
        Preconditions.checkArgument(type != Type.Teleport);
        
        this.name = name;
        this.type = type;
        this.dest = dest;
        this.fill = fill;
        this.min = min;
        this.max = max;
        
        enabled = true;
    }
    
    public Portal(String name, Location dest, FillType fill, Location min, Location max) {
        this.name = name;
        this.type = Type.Teleport;
        this.destLocation = dest;
        this.fill = fill;
        this.min = min;
        this.max = max;
        
        enabled = true;
    }
    
    public String getName() {
        return name;
    }
    
    public String getServer() {
        return min.getServer();
    }
    
    public Type getType() {
        return type;
    }
    
    public FillType getFill() {
        return fill;
    }
    
    public void setFill(FillType fill) {
        this.fill = fill;
    }
    
    public Location getMinCorner() {
        return min;
    }
    
    public Location getMaxCorner() {
        return max;
    }
    
    public void setBounds(Location min, Location max) {
        this.min = min;
        this.max = max;
    }
    
    public String getDestWarp() {
        Preconditions.checkState(type == Type.Warp);
        return dest;
    }
    
    public String getDestServer() {
        Preconditions.checkState(type == Type.Server);
        return dest;
    }
    
    public Location getDestLocation() {
        Preconditions.checkState(type == Type.Teleport);
        return destLocation;
    }
    
    public void setDestWarp(String warp) {
        type = Type.Warp;
        dest = warp;
    }
    
    public void setDestServer(String server) {
        type = Type.Server;
        dest = server;
    }
    
    public void setDestLocation(Location location) {
        type = Type.Teleport;
        destLocation = location;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public void save(Map<String, String> values) {
        values.put("fill", fill.name());
        values.put("type", type.name());
        
        switch (type) {
        default:
        case Server:
        case Warp:
            values.put("dest", dest);
            break;
        case Teleport:
            values.put("dest", destLocation.toSerialized());
            break;
        }
        
        values.put("min", min.toSerialized());
        values.put("max", max.toSerialized());
        
        values.put("enabled", String.valueOf(enabled));
    }

    @Override
    public void load(Map<String, String> values) {
        fill = FillType.valueOf(values.get("fill"));
        type = Type.valueOf(values.get("type"));
        
        switch (type) {
        default:
        case Server:
        case Warp:
            dest = values.get("dest");
            break;
        case Teleport:
            destLocation = Location.fromSerialized(values.get("dest"));
            break;
        }
        
        min = Location.fromSerialized(values.get("min"));
        max = Location.fromSerialized(values.get("max"));
        
        enabled = Boolean.parseBoolean(values.get("enabled"));
    }

    public enum FillType {
        Air,
        Water,
        Lava,
        Web,
        SugarCane,
        Portal,
        EndPortal
    }
    
    public enum Type {
        Warp,
        Teleport,
        Server
    }
}
