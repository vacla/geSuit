package net.cubespace.geSuit.database;

import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.Home;
import net.cubespace.geSuit.objects.Location;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Homes implements IRepository {
    public void addHome(Home home) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement addHome = connectionHandler.getPreparedStatement("addHome");
            addHome.setString(1, home.owner);
            addHome.setString(2, home.name);
            addHome.setString(3, home.loc.getServer().getName());
            addHome.setString(4, home.loc.getWorld());
            addHome.setDouble(5, home.loc.getX());
            addHome.setDouble(6, home.loc.getY());
            addHome.setDouble(7, home.loc.getZ());
            addHome.setFloat(8, home.loc.getYaw());
            addHome.setFloat(9, home.loc.getPitch());

            addHome.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public void updateHome(Home home) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement updateHome = connectionHandler.getPreparedStatement("updateHome");
            updateHome.setString(1, home.loc.getServer().getName());
            updateHome.setString(2, home.loc.getWorld());
            updateHome.setDouble(3, home.loc.getX());
            updateHome.setDouble(4, home.loc.getY());
            updateHome.setDouble(5, home.loc.getZ());
            updateHome.setFloat(6, home.loc.getYaw());
            updateHome.setFloat(7, home.loc.getPitch());
            updateHome.setString(8, home.owner);
            updateHome.setString(9, home.name);

            updateHome.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public void deleteHome(Home home) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement deleteHome = connectionHandler.getPreparedStatement("deleteHome");
            deleteHome.setString(1, home.name);
            deleteHome.setString(2, home.owner);

            deleteHome.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public List<Home> getHomesForPlayer(String player) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
        List<Home> homes = new ArrayList<>();

        try {
            PreparedStatement getAllHomesForPlayer = connectionHandler.getPreparedStatement("getAllHomesForPlayer");
            getAllHomesForPlayer.setString(1, player);

            ResultSet res = getAllHomesForPlayer.executeQuery();
            while (res.next()) {
                String server = res.getString("server");
                Location l = new Location(server, res.getString("world"), res.getDouble("x"), res.getDouble("y"), res.getDouble("z"), res.getFloat("yaw"), res.getFloat("pitch"));
                homes.add(new Home(player, res.getString("home_name"), l));
            }
            res.close();

            return homes;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return null;
    }

    @Override
    public String[] getTable() {
        return new String[]{"homes", "player VARCHAR(100), " +
                "home_name VARCHAR(100), " +
                "server VARCHAR(100), " +
                "world VARCHAR(100), " +
                "x DOUBLE, " +
                "y DOUBLE, " +
                "z DOUBLE, " +
                "yaw FLOAT, " +
                "pitch FLOAT, " +
                "CONSTRAINT pk_home PRIMARY KEY (player,home_name,server), " +
                "CONSTRAINT fk_playerhome FOREIGN KEY (player) REFERENCES players (playername) ON UPDATE CASCADE ON DELETE CASCADE"};
    }

    @Override
    public void registerPreparedStatements(ConnectionHandler connection) {
        connection.addPreparedStatement("addHome", "INSERT INTO homes (player,home_name,server,world,x,y,z,yaw,pitch) VALUES(?,?,?,?,?,?,?,?,?)");
        connection.addPreparedStatement("updateHome", "UPDATE homes SET server = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE player = ? AND home_name = ?");
        connection.addPreparedStatement("getAllHomesForPlayer", "SELECT * FROM homes WHERE player = ?");
        connection.addPreparedStatement("deleteHome", "DELETE FROM BungeeHomes WHERE home_name = ? AND player = ?");
    }
}
