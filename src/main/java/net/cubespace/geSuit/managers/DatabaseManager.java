package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.database.Bans;
import net.cubespace.geSuit.database.ConnectionPool;
import net.cubespace.geSuit.database.Homes;
import net.cubespace.geSuit.database.OnTime;
import net.cubespace.geSuit.database.Players;
import net.cubespace.geSuit.database.Portals;
import net.cubespace.geSuit.database.Spawns;
import net.cubespace.geSuit.database.Tracking;
import net.cubespace.geSuit.database.Warps;
import net.cubespace.geSuit.geSuit;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class DatabaseManager {
    public static boolean enabled = false;
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
        //geSuit.getInstance().getLogger().log(Level.INFO, "Instantiating DatabaseManager");
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
        geSuit.getInstance().getProxy().getScheduler().runAsync(geSuit.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {

                    if (connectionPool.initialiseConnections(ConfigManager.main.Database)) {
                        connectionPool.AddStringColumnIfMissing("warps", "description", 128);
                        AnnouncementManager.loadAnnouncements();
                        WarpsManager.loadWarpLocations();
                        PortalManager.loadPortals();
                        SpawnManager.loadSpawns();
                        enabled = true;
                    }
                } catch (IllegalStateException e) {
                    geSuit.getInstance().getLogger().warning("Gesuit could not initaliaze the database.... as " +
                            "a result no warps portals or spawn locations are loaded....");
                    enabled = false;
                }
            }
        });
    }

}
