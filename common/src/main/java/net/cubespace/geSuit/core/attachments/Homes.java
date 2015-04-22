package net.cubespace.geSuit.core.attachments;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import net.cubespace.geSuit.core.objects.Location;

public class Homes implements Attachment {
    private Map<String, Location> homes;
    
    public Homes() {
        homes = Maps.newHashMap();
    }
    
    public void setHome(String name, Location location) {
        homes.put(name.toLowerCase(), location);
    }
    
    public void removeHome(String name) {
        homes.remove(name.toLowerCase());
    }
    
    public Location getHome(String name) {
        return homes.get(name.toLowerCase());
    }
    
    public Map<String, Location> getAllHomes() {
        return Collections.unmodifiableMap(homes);
    }
    
    @Override
    public boolean isSaved() {
        return true;
    }
    
    @Override
    public void save(Map<String, String> values) {
        for (Entry<String, Location> entry : homes.entrySet()) {
            values.put(entry.getKey(), entry.getValue().toSerialized());
        }
    }

    @Override
    public void load(Map<String, String> values) {
        homes.clear();
        for (Entry<String, String> entry : values.entrySet()) {
            homes.put(entry.getKey().toLowerCase(), Location.fromSerialized(entry.getValue()));
        }
    }
}
