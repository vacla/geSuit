package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.database.*;
import net.cubespace.geSuit.geSuit;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class DatabaseManager {
    public static ConnectionPool connectionPool;
    public static Homes homes;
    public static Bans bans;
    public static Players players;
    public static Portals portals;
    public static Spawns spawns;
    public static Warps warps;
    public static Tracking tracking;
    public static OnTime ontime;

    static {
        players = new Players();
        homes = new Homes();
        bans = new Bans();
        portals = new Portals();
        spawns = new Spawns();
        warps = new Warps();
        tracking = new Tracking();
        ontime = new OnTime();

        connectionPool = new ConnectionPool();
        connectionPool.addRepository(players);
        connectionPool.addRepository(homes);
        connectionPool.addRepository(bans);
        connectionPool.addRepository(portals);
        connectionPool.addRepository(spawns);
        connectionPool.addRepository(warps);
        connectionPool.addRepository(tracking);
        connectionPool.addRepository(ontime);
        try {
            connectionPool.initialiseConnections(ConfigManager.main.Database);
            // Add the description column to the warps table if missing
            connectionPool.AddStringColumnIfMissing("warps", "description", 128);
            AnnouncementManager.loadAnnouncements();
            WarpsManager.loadWarpLocations();
            PortalManager.loadPortals();
            SpawnManager.loadSpawns();
        
        } catch (IllegalStateException e) {
            geSuit.instance.getLogger().warning("Gesuit could not initaliaze the database.... as " +
                    "a result no warps portals or spawn locations are loaded....");
        }
    }

}
