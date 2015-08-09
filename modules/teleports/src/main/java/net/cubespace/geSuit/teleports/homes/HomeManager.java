package net.cubespace.geSuit.teleports.homes;

import java.util.Collections;
import java.util.Set;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.attachments.Homes;
import net.cubespace.geSuit.core.objects.Location;

import org.bukkit.command.CommandSender;

import com.google.common.base.Preconditions;

public class HomeManager {
    public static final String defaultHome = "home";
    public static final int maxServerHomes = 100;
    public static final int maxGlobalHomes = 300;
    
    /**
     * Sets a players home making sure to add the attachment if needed, and save.
     * @param player The player to set the home on
     * @param home The name of the home to set
     * @param location The location to set it to. This cannot be a relative location
     */
    public void setHome(GlobalPlayer player, String home, Location location) {
        // Ensure locations is absolute
        Preconditions.checkNotNull(location.getServer());
        Preconditions.checkNotNull(location.getWorld());
        
        Homes homes = player.getAttachment(Homes.class);
        if (homes == null) {
            homes = new Homes();
            player.addAttachment(homes);
        }
        
        homes.setHome(home, location);
        player.save();
    }
    
    /**
     * Deletes a players home handling removing the attachment if no longer required and saving
     * @param player The player to remove the home on
     * @param home The name of the home to delete
     */
    public void deleteHome(GlobalPlayer player, String home) {
        Homes homes = player.getAttachment(Homes.class);
        if (homes == null) {
            // No home to delete
            return;
        }
        
        homes.removeHome(home);
        
        if (homes.getAllHomes().isEmpty()) {
            player.removeAttachment(Homes.class);
        }
        
        player.save();
    }
    
    /**
     * Gets a players home handling if the attachment isnt present
     * @param player The player to get the home for
     * @param home The home to get
     * @return The location of the home, or null
     */
    public Location getHome(GlobalPlayer player, String home) {
        Homes homes = player.getAttachment(Homes.class);
        if (homes == null) {
            return null;
        }
        
        return homes.getHome(home);
    }
    
    public boolean hasHome(GlobalPlayer player, String home) {
        Homes homes = player.getAttachment(Homes.class);
        if (homes == null) {
            return false;
        }
        
        return homes.getHome(home) != null;
    }
    
    /**
     * Gets a set containing the names of all homes set by the player
     * @param player The player to get the home names for
     * @return A {@code Set<String>} containing the names of each home, or an empty set
     */
    public Set<String> getHomeNames(GlobalPlayer player) {
        Homes homes = player.getAttachment(Homes.class);
        if (homes == null) {
            return Collections.emptySet();
        }
        
        return Collections.unmodifiableSet(homes.getAllHomes().keySet());
    }
    
    public int getHomeCountServer(GlobalPlayer player) {
        Homes homes = player.getAttachment(Homes.class);
        if (homes == null) {
            return 0;
        }
        
        int count = 0;
        String serverName = Global.getServer().getName();
        
        for (Location loc : homes.getAllHomes().values()) {
            if (loc.getServer().equals(serverName)) {
                ++count;
            }
        }
        
        return count;
    }
    
    public int getHomeCount(GlobalPlayer player) {
        Homes homes = player.getAttachment(Homes.class);
        if (homes == null) {
            return 0;
        }
        
        return homes.getAllHomes().size();
    }
    
    public int getHomeLimitServer(CommandSender player) {
        return getHomeLimit(player, "server", maxServerHomes);
    }
    
    public int getHomeLimitGlobal(CommandSender player) {
        return getHomeLimit(player, "global", maxGlobalHomes);
    }
    
    private int getHomeLimit(CommandSender player, String section, int maxLimit) {
        int max = 0;
        
        String basePermission = "gesuit.homes.limits." + section + ".";
        
        if (player.hasPermission(basePermission + "*") || player.hasPermission("gesuit.homes.limits.*")) {
            return maxLimit;
        } else {
            // TODO: Look for a better way to do this. This can't be cheap
            for (int i = 0; i < maxLimit; i++) {
                if (player.hasPermission(basePermission + i)) {
                    max = i;
                }
            }

        }
        
        return max;
    }
}
