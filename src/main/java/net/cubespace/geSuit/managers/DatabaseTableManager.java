package net.cubespace.geSuit.managers;

public class DatabaseTableManager {

    public static void createDefaultTables() {
         //BungeeWarps
         //runTableQuery("BungeeWarps", "CREATE TABLE BungeeWarps (warpname VARCHAR(100), server VARCHAR(100), world VARCHAR(100), x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, hidden TINYINT(1) DEFAULT 0,global TINYINT(1) DEFAULT 1, CONSTRAINT pk_warp PRIMARY KEY (warpname))");
    }
}
