package net.cubespace.geSuit.database.convert;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.FeatureDetector;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.database.ConnectionHandler;
import net.cubespace.geSuit.database.ConnectionPool;
import net.cubespace.geSuit.database.IRepository;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.Home;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.objects.Portal;
import net.cubespace.geSuit.objects.Spawn;
import net.cubespace.geSuit.objects.Warp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Converter {
    private ConnectionPool connectionPool = new ConnectionPool();

    private class Players implements IRepository {
        public void convert() {
            ConnectionHandler connectionHandler = connectionPool.getConnection();

            try {
                PreparedStatement selectPlayers = connectionHandler.getPreparedStatement("selectPlayers");

                ResultSet resultSet = selectPlayers.executeQuery();

                while(resultSet.next()) {
                    DatabaseManager.players.insertPlayerConvert(resultSet.getString("playername"), resultSet.getDate("lastonline"), resultSet.getString("ipaddress"), resultSet.getBoolean("tps"));
                }

                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connectionHandler.release();
            }
        }

        @Override
        public String[] getTable() {
            return new String[]{"BungeePlayers"};
        }

        @Override
        public void registerPreparedStatements(ConnectionHandler connection) {
            connection.addPreparedStatement("selectPlayers", "SELECT * FROM BungeePlayers");
        }

        @Override
        public void checkUpdate() {

        }
    }

    private class Homes implements IRepository {
        public void convert() {
            ConnectionHandler connectionHandler = connectionPool.getConnection();

            try {
                PreparedStatement selectHomes = connectionHandler.getPreparedStatement("selectHomes");

                ResultSet res = selectHomes.executeQuery();
                while (res.next()) {
                    Location l = new Location(res.getString("server"), res.getString("world"), res.getDouble("x"), res.getDouble("y"), res.getDouble("z"), res.getFloat("yaw"), res.getFloat("pitch"));
                    DatabaseManager.homes.addHome(new Home(DatabaseManager.players.loadPlayer(res.getString("player")), res.getString("home_name"), l));
                }

                res.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connectionHandler.release();
            }
        }

        @Override
        public String[] getTable() {
            return new String[]{"BungeeHomes"};
        }

        @Override
        public void registerPreparedStatements(ConnectionHandler connection) {
            connection.addPreparedStatement("selectHomes", "SELECT * FROM BungeeHomes");
        }

        @Override
        public void checkUpdate() {

        }
    }

    private class Portals implements IRepository {
        public void convert() {
            ConnectionHandler connectionHandler = connectionPool.getConnection();

            try {
                PreparedStatement selectPortals = connectionHandler.getPreparedStatement("selectPortals");

                ResultSet res = selectPortals.executeQuery();
                while (res.next()) {
                    String name = res.getString("portalname");
                    String server = res.getString("server");
                    String type = res.getString("type");
                    String dest = res.getString("destination");
                    String world = res.getString("world");
                    String fill = res.getString("filltype");
                    double xmax = res.getDouble("xmax");
                    double xmin = res.getDouble("xmin");
                    double ymax = res.getDouble("ymax");
                    double ymin = res.getDouble("ymin");
                    double zmax = res.getDouble("zmax");
                    double zmin = res.getDouble("zmin");

                    Portal p = new Portal(name, server, fill, type, dest, new Location(server, world, xmax, ymax, zmax), new Location(server, world, xmin, ymin, zmin));
                    DatabaseManager.portals.insertPortal(p);
                }

                res.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }  finally {
                connectionHandler.release();
            }
        }

        @Override
        public String[] getTable() {
            return new String[]{"BungeePortals"};
        }

        @Override
        public void registerPreparedStatements(ConnectionHandler connection) {
            connection.addPreparedStatement("selectPortals", "SELECT * FROM BungeePortals");
        }

        @Override
        public void checkUpdate() {

        }
    }

    private class Bans implements IRepository {
        public void convert() {
            ConnectionHandler connectionHandler = connectionPool.getConnection();

            try {
                PreparedStatement selectBans = connectionHandler.getPreparedStatement("selectBans");

                ResultSet res = selectBans.executeQuery();
                while (res.next()) {
                    DatabaseManager.bans.insertBanConvert(res.getString("banned_by"), res.getString("player"), (FeatureDetector.canUseUUID()) ? Utilities.getUUID(res.getString("player")) : null, null, res.getString("reason"), res.getString("type"), res.getDate("banned_on"), res.getDate("banned_until"));
                }

                res.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }  finally {
                connectionHandler.release();
            }
        }

        @Override
        public String[] getTable() {
            return new String[]{"BungeeBans"};
        }

        @Override
        public void registerPreparedStatements(ConnectionHandler connection) {
            connection.addPreparedStatement("selectBans", "SELECT * FROM BungeeBans");
        }

        @Override
        public void checkUpdate() {

        }
    }

    private class Spawns implements IRepository {
        public void convert() {
            ConnectionHandler connectionHandler = connectionPool.getConnection();

            try {
                PreparedStatement selectSpawns = connectionHandler.getPreparedStatement("selectSpawns");

                ResultSet res = selectSpawns.executeQuery();
                while (res.next()) {
                    Location location = new Location(res.getString("server"), res.getString("world"), res.getDouble("x"), res.getDouble("y"), res.getDouble("z"), res.getFloat("yaw"), res.getFloat("pitch"));
                    DatabaseManager.spawns.insertSpawn(new Spawn(res.getString("spawnname"), location));
                }

                res.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connectionHandler.release();
            }
        }

        @Override
        public String[] getTable() {
            return new String[]{"BungeeSpawns"};
        }

        @Override
        public void registerPreparedStatements(ConnectionHandler connection) {
            connection.addPreparedStatement("selectSpawns", "SELECT * FROM BungeeSpawns");
        }

        @Override
        public void checkUpdate() {

        }
    }

    private class Warps implements IRepository {
        public void convert() {
            ConnectionHandler connectionHandler = connectionPool.getConnection();

            try {
                PreparedStatement selectWarps = connectionHandler.getPreparedStatement("selectWarps");

                ResultSet res = selectWarps.executeQuery();
                while (res.next()) {
                    Location location = new Location(res.getString("server"), res.getString("world"), res.getDouble("x"), res.getDouble("y"), res.getDouble("z"), res.getFloat("yaw"), res.getFloat("pitch"));
                    DatabaseManager.warps.insertWarp(new Warp(res.getString("warpname"), location, res.getBoolean("hidden"), res.getBoolean("global")));
                }

                res.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connectionHandler.release();
            }
        }

        @Override
        public String[] getTable() {
            return new String[]{"BungeeWarps"};
        }

        @Override
        public void registerPreparedStatements(ConnectionHandler connection) {
            connection.addPreparedStatement("selectWarps", "SELECT * FROM BungeeWarps");
        }

        @Override
        public void checkUpdate() {

        }
    }

    public void convert() {
        new Thread(){
            public void run() {
                Players players = new Players();
                Homes homes = new Homes();
                Portals portals = new Portals();
                Bans bans = new Bans();
                Spawns spawns = new Spawns();
                Warps warps = new Warps();

                connectionPool.addRepository(players);
                connectionPool.addRepository(homes);
                connectionPool.addRepository(portals);
                connectionPool.addRepository(bans);
                connectionPool.addRepository(spawns);
                connectionPool.addRepository(warps);

                connectionPool.initialiseConnections(ConfigManager.main.BungeeSuiteDatabase);

                players.convert();
                homes.convert();
                portals.convert();
                bans.convert();
                spawns.convert();
                warps.convert();

                ConfigManager.main.ConvertFromBungeeSuite = false;
                try {
                    ConfigManager.main.save();
                } catch (InvalidConfigurationException e) {

                }
            }
        }.start();
    }
}
