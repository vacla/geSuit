package net.cubespace.geSuit.teleports;

import java.util.Map;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.attachments.Attachment;
import net.cubespace.geSuit.core.objects.Location;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class TeleportsAttachment extends Attachment {
    private Location lastDeath;
    private Location lastTeleport;
    private boolean useLastDeath;
    
    private boolean useNewPlayerSpawn;
    
    private GlobalPlayer tpaTarget;
    private ScheduledTask tpaTask;
    private GlobalPlayer tpaHereSource;
    private ScheduledTask tpaHereTask;
    
    public Location getLastDeath() {
        return lastDeath;
    }
    
    public void setLastDeath(Location location) {
        lastDeath = location;
        useLastDeath = true;
    }
    
    public boolean hasLastDeath() {
        return getLastDeath() != null;
    }
    
    public Location getLastTeleport() {
        return lastTeleport;
    }
    
    public void setLastTeleport(Location location) {
        lastTeleport = location;
        useLastDeath = false;
    }
    
    public boolean hasLastTeleport() {
        return getLastTeleport() != null;
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
    
    public void setTPA(GlobalPlayer target, ScheduledTask expireTask) {
        if (tpaTask != null) {
            tpaTask.cancel();
        }
        
        tpaTarget = target;
        tpaTask = expireTask;
    }
    
    public GlobalPlayer getTPA() {
        return tpaTarget;
    }
    
    public void setTPAHere(GlobalPlayer source, ScheduledTask expireTask) {
        if (tpaHereTask != null) {
            tpaHereTask.cancel();
        }
        
        tpaHereSource = source;
        tpaHereTask = expireTask;
    }
    
    public GlobalPlayer getTPAHere() {
        return tpaHereSource;
    }
    
    @Override
    public AttachmentType getType() {
        return AttachmentType.Local;
    }
    
    @Override
    public void save(Map<String, String> values) {}

    @Override
    public void load(Map<String, String> values) {}
}
