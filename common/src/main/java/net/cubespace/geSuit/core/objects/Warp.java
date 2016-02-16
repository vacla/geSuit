package net.cubespace.geSuit.core.objects;

import com.google.common.base.Strings;

public class Warp {
    private static String serializedDelimiter = "\001";
    private Location loc;
    private final String name;
    private boolean hidden;
    private boolean global;
    private String description;

    public Warp(String name, Location loc, boolean hidden, boolean global) {
        this.name = name;
        this.loc = loc;
        this.hidden = hidden;
        this.global = global;
        this.description = name;
    }

    public Warp(String name, Location loc, boolean hidden, boolean global, String description) {
        this.name = name;
        this.loc = loc;
        this.hidden = hidden;
        this.global = global;
        this.description = description;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    public Location getLocation() {
        return loc;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDescriptionOrName() {
        if (Strings.isNullOrEmpty(description))
            return name;
        else
            return description;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }
    
    public String toSerialized() {
        return String.format("%s%s%s%s%s%s%s",
                loc.toSerialized(), serializedDelimiter,
                hidden, serializedDelimiter,
                global, serializedDelimiter,
                description);
    }
    
    public static Warp fromSerialized(String name, String value) {
        String[] parts = value.split(serializedDelimiter);
        return new Warp(name,
                Location.fromSerialized(parts[0]),
                Boolean.valueOf(parts[1]),
                Boolean.valueOf(parts[2]),
                parts[3]);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
