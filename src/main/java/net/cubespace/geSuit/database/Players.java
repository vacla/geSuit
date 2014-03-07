package net.cubespace.geSuit.database;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.FeatureDetector;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.GSPlayer;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Players implements IRepository {
    public boolean playerExists(String player) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement playerExists = connectionHandler.getPreparedStatement("playerExists");
            playerExists.setString(1, player);
            playerExists.setString(2, player);

            return playerExists.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return true;
    }

    public String getPlayerIP(String player) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement getPlayerIP = connectionHandler.getPreparedStatement("getPlayerIP");
            getPlayerIP.setString(1, player);
            getPlayerIP.setString(2, player);

            String ip = null;
            ResultSet res = getPlayerIP.executeQuery();
            while (res.next()) {
                ip = res.getString("ipaddress");
            }
            res.close();

            return ip;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return null;
    }

    public boolean getPlayerTPS(String player) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement getPlayerTPS = connectionHandler.getPreparedStatement("getPlayerTPS");
            getPlayerTPS.setString(1, player);
            getPlayerTPS.setString(2, player);

            boolean tps = true;
            ResultSet res = getPlayerTPS.executeQuery();
            while (res.next()) {
                tps = res.getBoolean("tps");
            }
            res.close();

            return tps;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return true;
    }

    public void insertPlayer(GSPlayer player, String ip) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement insertPlayer = connectionHandler.getPreparedStatement("insertPlayer");
            insertPlayer.setString(1, player.getName());
            insertPlayer.setString(2, player.getUuid());
            insertPlayer.setString(3, ip);

            insertPlayer.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public void insertPlayerConvert(String player, Date lastonline, String ip, boolean tps) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement insertPlayerConvert = connectionHandler.getPreparedStatement("insertPlayerConvert");
            insertPlayerConvert.setString(1, player);
            insertPlayerConvert.setString(2, (FeatureDetector.canUseUUID()) ? Utilities.getUUID(player) : null);
            insertPlayerConvert.setDate(3, lastonline);
            insertPlayerConvert.setString(4, ip);
            insertPlayerConvert.setBoolean(5, tps);

            insertPlayerConvert.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public void updatePlayer(GSPlayer gsPlayer) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement updatePlayer = connectionHandler.getPreparedStatement("updatePlayer");
            updatePlayer.setString(1, gsPlayer.getUuid());
            updatePlayer.setString(2, gsPlayer.getName());
            updatePlayer.setString(3, gsPlayer.getName());
            updatePlayer.setString(4, gsPlayer.getUuid());

            updatePlayer.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public GSPlayer loadPlayer(String player) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        GSPlayer player1 = null;
        try {
            PreparedStatement getPlayer = connectionHandler.getPreparedStatement("getPlayer");
            getPlayer.setString(1, player);
            getPlayer.setString(2, player);

            ResultSet res = getPlayer.executeQuery();
            while (res.next()) {
                player1 = new GSPlayer(res.getString("playername"), res.getString("uuid"), res.getBoolean("tps"));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return player1;
    }

    @Override
    public String[] getTable() {
        return new String[]{"players", "playername VARCHAR(100), " +
                "`uuid` VARCHAR(100) NULL UNIQUE," +
                "lastonline DATETIME NOT NULL, " +
                "ipaddress VARCHAR(100), " +
                "tps TINYINT(1) DEFAULT 1," +
                ((FeatureDetector.canUseUUID()) ?
                        "CONSTRAINT pk_uuid PRIMARY KEY (uuid)" :
                        "CONSTRAINT pk_playername PRIMARY KEY (playername)")};
    }

    @Override
    public void registerPreparedStatements(ConnectionHandler connection) {
        connection.addPreparedStatement("getPlayerIP", "SELECT ipaddress FROM players WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("playerExists", "SELECT playername FROM players WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("getPlayer", "SELECT playername,uuid,tps FROM players WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("getPlayerTPS", "SELECT tps FROM players WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("insertPlayer", "INSERT INTO players (playername,uuid,lastonline,ipaddress) VALUES (?, ?, NOW(), ?)");
        connection.addPreparedStatement("insertPlayerConvert", "INSERT INTO players (playername,uuid,lastonline,ipaddress,tps) VALUES (?, ?, ?, ?, ?)");
        connection.addPreparedStatement("getPlayers", "SELECT * FROM players");
        connection.addPreparedStatement("setUUID", "UPDATE players SET uuid = ? WHERE playername = ?");
        connection.addPreparedStatement("updatePlayer", "UPDATE players SET uuid = ?, playername = ?, lastonline = NOW() WHERE playername = ? OR uuid = ?");
    }

    @Override
    public void checkUpdate() {
        int installedVersion = ConfigManager.main.Version_Database_Players;

        System.out.println("Current Version of the Players Database: " + installedVersion);

        if (installedVersion < 2) {
            // Version 2 adds UUIDs as Field
            ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

            try {
                connectionHandler.getConnection().createStatement().execute("ALTER TABLE `players` ADD `uuid` VARCHAR(100) NULL AFTER `playername`, ADD UNIQUE (`uuid`) ;");
            } catch (SQLException e) {
                System.out.println("Could not update the Player Database to version 2");
                e.printStackTrace();
                return;
            } finally {
                connectionHandler.release();
            }

            if (FeatureDetector.canUseUUID()) {
                connectionHandler = DatabaseManager.connectionPool.getConnection();

                // Convert all Names to UUIDs
                PreparedStatement getPlayers = connectionHandler.getPreparedStatement("getPlayers");
                try {
                    ResultSet res = getPlayers.executeQuery();
                    while(res.next()) {
                        String playername = res.getString("playername");
                        String uuid = Utilities.getUUID(playername);

                        if (uuid != null) {
                            ConnectionHandler connectionHandler1 = DatabaseManager.connectionPool.getConnection();

                            try {
                                PreparedStatement preparedStatement = connectionHandler1.getPreparedStatement("setUUID");
                                preparedStatement.setString(1, uuid);
                                preparedStatement.setString(2, playername);
                                preparedStatement.executeUpdate();
                            } catch (SQLException e) {
                                System.out.println("Could not update Player for update to version 2");
                                e.printStackTrace();
                            } finally {
                                connectionHandler1.release();
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.out.println("Could not get Players for update to version 2");
                    e.printStackTrace();
                    return;
                } finally {
                    connectionHandler.release();
                }
            }
        }

        ConfigManager.main.Version_Database_Players = 2;
        try {
            ConfigManager.main.save();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
