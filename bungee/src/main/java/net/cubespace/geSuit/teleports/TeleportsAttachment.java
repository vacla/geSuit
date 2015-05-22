package net.cubespace.geSuit.teleports;

import java.util.Map;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.attachments.Attachment;
import net.cubespace.geSuit.core.objects.Location;

public class TeleportsAttachment implements Attachment {
    private Location lastDeath;
    private Location lastTeleport;
    private boolean useLastDeath;
    
    private boolean useNewPlayerSpawn;
    
    private GlobalPlayer tpaTarget;
    private GlobalPlayer tpaHereSource;
    
    public Location getLastDeath() {
        return lastDeath;
    }
    
    public void setLastDeath(Location location) {
        lastDeath = location;
        useLastDeath = true;
    }
    
    public Location getLastTeleport() {
        return lastTeleport;
    }
    
    public void setLastTeleport(Location location) {
        lastTeleport = location;
        useLastDeath = false;
    }
    
    public Location getLastLocation() {
        if (useLastDeath) {
            return lastDeath;
        } else {
            return lastTeleport;
        }
    }
    
    public void setUseNewPlayerSpawn(boolean use) {
        useNewPlayerSpawn = use;
    }
    
    public boolean getUseNewPlayerSpawn() {
        return useNewPlayerSpawn;
    }
    
    public boolean hasLastLocation() {
        return getLastLocation() != null;
    }
    
    public void setTPA(GlobalPlayer target) {
        tpaTarget = target;
    }
    
    public GlobalPlayer getTPA() {
        return tpaTarget;
    }
    
    public void setTPAHere(GlobalPlayer source) {
        tpaHereSource = source;
    }
    
    public GlobalPlayer getTPAHere() {
        return tpaHereSource;
    }
    
    @Override
    public boolean isSaved() {
        return false;
    }
    
    @Override
    public void save(Map<String, String> values) {}

    @Override
    public void load(Map<String, String> values) {}
}
