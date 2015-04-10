package net.cubespace.geSuit.database;

import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.objects.Warp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Warps implements IRepository {
    public List<Warp> getWarps() {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
        List<Warp> warps = new ArrayList<>();

        try {
            PreparedStatement getWarps = connectionHandler.getPreparedStatement("getWarps");

            ResultSet res = getWarps.executeQuery();
            while (res.next()) {
                Location location = new Location(res.getString("server"), res.getString("world"), res.getDouble("x"), res.getDouble("y"), res.getDouble("z"), res.getFloat("yaw"), res.getFloat("pitch"));
                warps.add(new Warp(res.getString("warpname"), location, res.getBoolean("hidden"), res.getBoolean("global")));
            }
            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return warps;
    }

    public void insertWarp(Warp warp) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement insertWarp = connectionHandler.getPreparedStatement("insertWarp");
            insertWarp.setString(1, warp.getName());
            insertWarp.setString(2, warp.getLocation().getServer().getName());
            insertWarp.setString(3, warp.getLocation().getWorld());
            insertWarp.setDouble(4, warp.getLocation().getX());
            insertWarp.setDouble(5, warp.getLocation().getY());
            insertWarp.setDouble(6, warp.getLocation().getZ());
            insertWarp.setFloat(7, warp.getLocation().getYaw());
            insertWarp.setFloat(8, warp.getLocation().getPitch());
            insertWarp.setBoolean(9, warp.isHidden());
            insertWarp.setBoolean(10, warp.isGlobal());

            insertWarp.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public void updateWarp(Warp warp) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement updateWarp = connectionHandler.getPreparedStatement("updateWarp");
            updateWarp.setString(1, warp.getLocation().getServer().getName());
            updateWarp.setString(2, warp.getLocation().getWorld());
            updateWarp.setDouble(3, warp.getLocation().getX());
            updateWarp.setDouble(4, warp.getLocation().getY());
            updateWarp.setDouble(5, warp.getLocation().getZ());
            updateWarp.setFloat(6, warp.getLocation().getYaw());
            updateWarp.setFloat(7, warp.getLocation().getPitch());
            updateWarp.setBoolean(8, warp.isHidden());
            updateWarp.setBoolean(9, warp.isGlobal());
            updateWarp.setString(10, warp.getName());

            updateWarp.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public void deleteWarp(String warp) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement deleteWarp = connectionHandler.getPreparedStatement("deleteWarp");
            deleteWarp.setString(1, warp);

            deleteWarp.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    @Override
    public String[] getTable() {
        return new String[]{ConfigManager.main.Table_Warps, "warpname VARCHAR(100), " +
                "server VARCHAR(100), " +
                "world VARCHAR(100), " +
                "x DOUBLE, " +
                "y DOUBLE, " +
                "z DOUBLE, " +
                "yaw FLOAT, " +
                "pitch FLOAT, " +
                "hidden TINYINT(1) DEFAULT 0," +
                "global TINYINT(1) DEFAULT 1, " +
                "CONSTRAINT pk_warp PRIMARY KEY (warpname)"};
    }

    @Override
    public void registerPreparedStatements(ConnectionHandler connection) {
        connection.addPreparedStatement("getWarps", "SELECT * FROM "+ ConfigManager.main.Table_Warps + " ORDER BY warpname");
        connection.addPreparedStatement("insertWarp", "INSERT INTO "+ ConfigManager.main.Table_Warps +" (warpname, server, world, x, y, z, yaw, pitch, hidden, global) VALUES (?,?,?,?,?,?,?,?,?,?)");
        connection.addPreparedStatement("updateWarp", "UPDATE "+ ConfigManager.main.Table_Warps +" SET server=?, world=?, x=?, y=?, z=?, yaw=?, pitch=?, hidden=?, global=? WHERE warpname=?");
        connection.addPreparedStatement("deleteWarp", "DELETE FROM "+ ConfigManager.main.Table_Warps +" WHERE warpname=?");
    }

    @Override
    public void checkUpdate() {

    }
}
