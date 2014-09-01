package net.cubespace.geSuit.database;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.Home;
import net.cubespace.geSuit.objects.Location;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            addHome.setString(1, (home.owner.getUuid() != null) ? home.owner.getUuid() : home.owner.getName());
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
            updateHome.setString(8, (home.owner.getUuid() != null) ? home.owner.getUuid() : home.owner.getName());
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
            deleteHome.setString(2, (home.owner.getUuid() != null) ? home.owner.getUuid() : home.owner.getName());

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
                homes.add(new Home(PlayerManager.matchOnlinePlayer(player), res.getString("home_name"), l));
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
        return new String[]{ConfigManager.main.Table_Homes, "player VARCHAR(100), " +
                "home_name VARCHAR(100), " +
                "server VARCHAR(100), " +
                "world VARCHAR(100), " +
                "x DOUBLE, " +
                "y DOUBLE, " +
                "z DOUBLE, " +
                "yaw FLOAT, " +
                "pitch FLOAT, " +
                "CONSTRAINT pk_home PRIMARY KEY (player,home_name,server), " +
                "FOREIGN KEY fk_playerhome(player) REFERENCES "+ ConfigManager.main.Table_Players +" (uuid) ON UPDATE CASCADE ON DELETE CASCADE"};
    }

    @Override
    public void registerPreparedStatements(ConnectionHandler connection) {
        connection.addPreparedStatement("addHome", "INSERT INTO "+ ConfigManager.main.Table_Homes +" (player,home_name,server,world,x,y,z,yaw,pitch) VALUES(?,?,?,?,?,?,?,?,?)");
        connection.addPreparedStatement("updateHome", "UPDATE "+ ConfigManager.main.Table_Homes +" SET server = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE player = ? AND home_name = ?");
        connection.addPreparedStatement("getAllHomesForPlayer", "SELECT * FROM "+ ConfigManager.main.Table_Homes +" WHERE player = ?");
        connection.addPreparedStatement("deleteHome", "DELETE FROM "+ ConfigManager.main.Table_Homes +" WHERE home_name = ? AND player = ?");
        connection.addPreparedStatement("getHomes", "SELECT * FROM "+ ConfigManager.main.Table_Homes);
        connection.addPreparedStatement("updateHomesToUUID", "UPDATE "+ ConfigManager.main.Table_Homes +" SET player = ? WHERE player = ?");
    }

    @Override
    public void checkUpdate() {
        //What current Version of the Database is this ?
        int installedVersion = ConfigManager.main.Version_Database_Homes;

        System.out.println("Current Version of the Homes Database: " + installedVersion);

        if (installedVersion < 2) {
            // Version 2 adds UUIDs as Field
            ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
            try {
                connectionHandler.getConnection().createStatement().execute("ALTER TABLE `"+ ConfigManager.main.Table_Homes +"` DROP FOREIGN KEY `homes_ibfk_1`;");
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            } finally {
                connectionHandler.release();
            }

            connectionHandler = DatabaseManager.connectionPool.getConnection();
            // Convert all Names to UUIDs
            PreparedStatement getHomes = connectionHandler.getPreparedStatement("getHomes");
            try {
                ResultSet res = getHomes.executeQuery();
                while (res.next()) {
                    String player = res.getString("player");
                    String uuid = Utilities.getUUID(player);

                    if (uuid != null) {
                        ConnectionHandler connectionHandler1 = DatabaseManager.connectionPool.getConnection();

                        try {
                            PreparedStatement updateHomesToUUID = connectionHandler1.getPreparedStatement("updateHomesToUUID");
                            updateHomesToUUID.setString(1, uuid);
                            updateHomesToUUID.setString(2, player);
                            updateHomesToUUID.executeUpdate();
                        } catch (SQLException e) {
                            System.out.println("Could not update Home for update to version 2");
                            e.printStackTrace();
                        } finally {
                            connectionHandler1.release();
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println("Could not get Homes for update to version 2");
                e.printStackTrace();
                return;
            } finally {
                connectionHandler.release();
            }

            connectionHandler = DatabaseManager.connectionPool.getConnection();

            try {
                connectionHandler.getConnection().createStatement().execute("ALTER TABLE `"+ ConfigManager.main.Table_Homes +"` ADD  CONSTRAINT `homes_ibfk_1` FOREIGN KEY (`player`) REFERENCES `"+ ConfigManager.main.Table_Players +"`(`uuid`) ON DELETE CASCADE ON UPDATE CASCADE;");
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }

        ConfigManager.main.Version_Database_Homes = 2;
        try {
            ConfigManager.main.save();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
