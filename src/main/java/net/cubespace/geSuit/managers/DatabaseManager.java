package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.database.Bans;
import net.cubespace.geSuit.database.ConnectionPool;
import net.cubespace.geSuit.database.Homes;
import net.cubespace.geSuit.database.Players;
import net.cubespace.geSuit.database.Portals;
import net.cubespace.geSuit.database.Spawns;
import net.cubespace.geSuit.database.Warps;

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

    static {
        players = new Players();
        homes = new Homes();
        bans = new Bans();
        portals = new Portals();
        spawns = new Spawns();
        warps = new Warps();

        connectionPool = new ConnectionPool();
        connectionPool.addRepository(players);
        connectionPool.addRepository(homes);
        connectionPool.addRepository(bans);
        connectionPool.addRepository(portals);
        connectionPool.addRepository(spawns);
        connectionPool.addRepository(warps);
        connectionPool.initialiseConnections(ConfigManager.main.Database);

        AnnouncementManager.loadAnnouncements();
        WarpsManager.loadWarpLocations();
        PortalManager.loadPortals();
        SpawnManager.loadSpawns();
    }
}
