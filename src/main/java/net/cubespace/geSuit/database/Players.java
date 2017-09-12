package net.cubespace.geSuit.database;

import com.google.common.collect.Maps;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.GSPlayer;

import java.sql.*;
import java.util.*;

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

    public String[] getAltPlayer(String uuid, String ip, boolean ignoreSelf) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement getAltPlayer = connectionHandler.getPreparedStatement("getAltPlayer");
            getAltPlayer.setString(1, ip);
            
            String altname = "";
            String altuuid = uuid;
            
            ResultSet res = getAltPlayer.executeQuery();

            if (ignoreSelf) {
            	// Skip the first result when a new player joins (because it's always themselves)
            	res.next();
            }

            if (res.next()) {
                altname = res.getString("playername");
                altuuid = res.getString("uuid");
            }
            res.close();

            if (!uuid.equals(altuuid)) {
            	return new String[]{altname, altuuid};
            } else {
            	return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return null;
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

    public void insertPlayerConvert(String player, String uuid, Timestamp lastonline, String ip, boolean tps) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
        try {
            PreparedStatement insertPlayerConvert = connectionHandler.getPreparedStatement("insertPlayerConvert");
            insertPlayerConvert.setString(1, player);
            insertPlayerConvert.setString(2, uuid);
            insertPlayerConvert.setTimestamp(3, lastonline);
            insertPlayerConvert.setTimestamp(4, lastonline);
            insertPlayerConvert.setString(5, ip);
            insertPlayerConvert.setBoolean(6, tps);

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
            PreparedStatement updatePlayer = connectionHandler.getPreparedStatement("updatePlayerByUUID");
            updatePlayer.setString(1, gsPlayer.getUuid());
            updatePlayer.setString(2, gsPlayer.getIp());
            updatePlayer.setBoolean(3, gsPlayer.acceptingTeleports());
            updatePlayer.setBoolean(4, gsPlayer.isNewSpawn());
            updatePlayer.setString(5, gsPlayer.getName());

            int result = updatePlayer.executeUpdate();
            if (result > 1) {
                geSuit.instance.getLogger().warning("PLAYER HAS MULTIPLE UUID ENTRIES WHICH HAVE BEEN UPDATED: " + gsPlayer.toString());
            }
            if (result == 0) {
                geSuit.instance.getLogger().warning("PLAYER IS BEING UPDATED BY NAME: " + gsPlayer.toString());

                PreparedStatement updatePlayerbyName = connectionHandler.getPreparedStatement("updatePlayerbyName");
                updatePlayerbyName.setString(1, gsPlayer.getName());
                updatePlayerbyName.setString(2, gsPlayer.getIp());
                updatePlayerbyName.setBoolean(3, gsPlayer.acceptingTeleports());
                updatePlayerbyName.setBoolean(4, gsPlayer.isNewSpawn());
                updatePlayerbyName.setString(5, gsPlayer.getUuid());
                if (updatePlayerbyName.executeUpdate() != 1) {
                    geSuit.instance.getLogger().warning("PLAYER COULD NOT BE UPDATED: " + gsPlayer.toString());

                }
            }
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
                player1 = new GSPlayer(res.getString("playername"), res.getString("uuid"), res.getBoolean("tps"), res.getBoolean("newspawn"), res.getString("ipaddress"), res.getTimestamp("lastonline"), res.getTimestamp("firstonline"));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return player1;
    }

    public List<String> matchPlayers(String player) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        List<String> players = new ArrayList<>();
        try {
            PreparedStatement getPlayer = connectionHandler.getPreparedStatement("matchPlayers");
            getPlayer.setString(1, "%" + player + "%");     // Player Name
            getPlayer.setString(2, player);                 // UUID

            ResultSet res = getPlayer.executeQuery();
            while (res.next()) {
                players.add(res.getString("playername"));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return players;
    }
    
    public Map<String, UUID> resolvePlayerNames(Collection<String> names) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        Map<String, UUID> resolved = Maps.newHashMapWithExpectedSize(names.size());
        try {
            int maxBatch = 40;
            int count = 0;
            StringBuilder builder = new StringBuilder();
            for (String name : names) {
                ++count;
                if (builder.length() != 0) {
                    builder.append(",");
                }
                
                builder.append(name);
                
                if (count >= maxBatch) {
                    PreparedStatement statement = connectionHandler.getPreparedStatement("resolvePlayerName");
                    statement.setString(1, builder.toString());
                    
                    ResultSet results = statement.executeQuery();
                    while(results.next()) {
                        resolved.put(results.getString("playername"), Utilities.makeUUID(results.getString("uuid")));
                    }
                    results.close();
                    
                    builder.setLength(0);
                    count = 0;
                }
            }
            
            if (count > 0) {
                PreparedStatement statement = connectionHandler.getPreparedStatement("resolvePlayerName");
                statement.setString(1, builder.toString());
                
                ResultSet results = statement.executeQuery();
                while(results.next()) {
                    resolved.put(results.getString("playername"), Utilities.makeUUID(results.getString("uuid")));
                }
                results.close();
            }
            
            return resolved;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        } finally {
            connectionHandler.release();
        }
    }
    
    /**
     * Resolves a player name using the tracking table instead of the players table. This allows it to resolve old names for players
     * @param names the names
     * @return Map
     */
    public Map<String, UUID> resolvePlayerNamesHistoric(Collection<String> names) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        Map<String, UUID> resolved = Maps.newHashMapWithExpectedSize(names.size());
        try {
            int maxBatch = 40;
            int count = 0;
            StringBuilder builder = new StringBuilder();
            for (String name : names) {
                ++count;
                if (builder.length() != 0) {
                    builder.append(",");
                }
                
                builder.append(name);
                
                if (count >= maxBatch) {
                    PreparedStatement statement = connectionHandler.getPreparedStatement("resolveOldPlayerName");
                    statement.setString(1, builder.toString());
                    
                    ResultSet results = statement.executeQuery();
                    while(results.next()) {
                        resolved.put(results.getString("player"), Utilities.makeUUID(results.getString("uuid")));
                    }
                    results.close();
                    
                    builder.setLength(0);
                    count = 0;
                }
            }
            
            if (count > 0) {
                PreparedStatement statement = connectionHandler.getPreparedStatement("resolveOldPlayerName");
                statement.setString(1, builder.toString());
                
                ResultSet results = statement.executeQuery();
                while(results.next()) {
                    resolved.put(results.getString("player"), Utilities.makeUUID(results.getString("uuid")));
                }
                results.close();
            }
            
            return resolved;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        } finally {
            connectionHandler.release();
        }
    }
    
    public Map<UUID, String> resolveUUIDs(Collection<UUID> ids) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        Map<UUID, String> resolved = Maps.newHashMapWithExpectedSize(ids.size());
        try {
            int maxBatch = 40;
            int count = 0;
            StringBuilder builder = new StringBuilder();
            for (UUID id : ids) {
                ++count;
                if (builder.length() != 0) {
                    builder.append(",");
                }
                
                builder.append(id.toString().replace("-", ""));
                
                if (count >= maxBatch) {
                    PreparedStatement statement = connectionHandler.getPreparedStatement("resolveUUID");
                    statement.setString(1, builder.toString());
                    
                    ResultSet results = statement.executeQuery();
                    while(results.next()) {
                        resolved.put(Utilities.makeUUID(results.getString("uuid")), results.getString("playername"));
                    }
                    results.close();
                    
                    builder.setLength(0);
                    count = 0;
                }
            }
            
            if (count > 0) {
                PreparedStatement statement = connectionHandler.getPreparedStatement("resolveUUID");
                statement.setString(1, builder.toString());
                
                ResultSet results = statement.executeQuery();
                while(results.next()) {
                    resolved.put(Utilities.makeUUID(results.getString("uuid")), results.getString("playername"));
                }
                results.close();
            }
            
            return resolved;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        } finally {
            connectionHandler.release();
        }
    }

    @Override
    public String[] getTable() {
        return new String[]{ConfigManager.main.Table_Players, "playername VARCHAR(100), "
                + "uuid VARCHAR(100) NOT NULL,"
                + "firstonline DATETIME NOT NULL, "
                + "lastonline DATETIME NOT NULL, "
                + "ipaddress VARCHAR(100), "
                + "tps TINYINT(1) DEFAULT 1,"
                + "newspawn TINYINT(1) DEFAULT 0,"
                + "CONSTRAINT pk_uuid PRIMARY KEY (uuid)"};
    }

    @Override
    public void registerPreparedStatements(ConnectionHandler connection) {
        connection.addPreparedStatement("getPlayerIP", "SELECT ipaddress FROM "+ ConfigManager.main.Table_Players +" WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("playerExists", "SELECT playername FROM "+ ConfigManager.main.Table_Players +" WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("getPlayerTPS", "SELECT tps FROM "+ ConfigManager.main.Table_Players +" WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("getPlayer", "SELECT * FROM "+ ConfigManager.main.Table_Players +" WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("getAltPlayer", "SELECT playername, uuid FROM "+ ConfigManager.main.Table_Players +" WHERE ipaddress = ? ORDER BY lastonline DESC LIMIT 2");
        // Show the 20 most recent players whose name matches the search string
        connection.addPreparedStatement("matchPlayers", "SELECT playername,uuid FROM (SELECT playername,uuid,lastonline FROM "+ ConfigManager.main.Table_Players +" WHERE playername like ? OR uuid like ? ORDER BY lastonline desc LIMIT 20) AS FilterQ ORDER BY lastonline");
        connection.addPreparedStatement("insertPlayer", "INSERT INTO "+ ConfigManager.main.Table_Players +" (playername,uuid,firstonline,lastonline,ipaddress) VALUES (?, ?, NOW(), NOW(), ?)");
        connection.addPreparedStatement("insertPlayerConvert", "INSERT INTO "+ ConfigManager.main.Table_Players +" (playername,uuid,firstonline,lastonline,ipaddress,tps) VALUES (?, ?, ?, ?, ?, ?)");
        connection.addPreparedStatement("getPlayers", "SELECT * FROM "+ ConfigManager.main.Table_Players);
        connection.addPreparedStatement("setUUID", "UPDATE "+ ConfigManager.main.Table_Players +" SET uuid = ? WHERE playername = ?");
        connection.addPreparedStatement("updatePlayerByUUID", "UPDATE " + ConfigManager.main.Table_Players + " SET playername = ?, lastonline = NOW(), ipaddress = ?, tps = ?, newspawn = ? WHERE uuid = ?");
        connection.addPreparedStatement("updatePlayerByName", "UPDATE " + ConfigManager.main.Table_Players + " SET uuid = ?, lastonline = NOW(), ipaddress = ?, tps = ?, newspawn = ? WHERE playername = ?");
        connection.addPreparedStatement("resolvePlayerName", "SELECT playername,uuid FROM "+ ConfigManager.main.Table_Players +" WHERE FIND_IN_SET(playername, ?)");
        connection.addPreparedStatement("resolveOldPlayerName", "SELECT player,uuid FROM "+ ConfigManager.main.Table_Tracking +" WHERE FIND_IN_SET(player, ?) GROUP BY player");
        connection.addPreparedStatement("resolveUUID", "SELECT playername,uuid FROM "+ ConfigManager.main.Table_Players +" WHERE FIND_IN_SET(uuid, ?)");
    }

    @Override
    public void checkUpdate() {
        int installedVersion = ConfigManager.main.Version_Database_Players;

        System.out.println("Current Version of the Players Database: " + installedVersion);

        if (installedVersion < 2) {
            // Version 2 adds UUIDs as Field
            ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

            try {
                connectionHandler.getConnection().createStatement().execute("ALTER TABLE `"+ ConfigManager.main.Table_Players +"` ADD `uuid` VARCHAR(100) NULL AFTER `playername`, ADD UNIQUE (`uuid`) ;");
            } catch (SQLException e) {
                System.out.println("Could not update the Player Database to version 2");
                e.printStackTrace();
                return;
            } finally {
                connectionHandler.release();
            }

            connectionHandler = DatabaseManager.connectionPool.getConnection();

            // Convert all Names to UUIDs
            PreparedStatement getPlayers = connectionHandler.getPreparedStatement("getPlayers");
            try {
                ResultSet res = getPlayers.executeQuery();
                while (res.next()) {
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
        if (installedVersion < 3) {
            // Version 3 adds "firstonline" field
            ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
            try {
                System.out.println("Upgrading Player Database to version 3...");
                connectionHandler.getConnection().createStatement().execute("ALTER TABLE `"+ ConfigManager.main.Table_Players +"` ADD `firstonline` DATETIME NOT NULL AFTER `uuid`;");
            } catch (SQLException e) {
                System.out.println("Could not update the Player Database to version 3");
                e.printStackTrace();
                return;
            } finally {
                connectionHandler.release();
            }

            // Convert any existing "firstonline" values to the current "lastonline" values
            connectionHandler = DatabaseManager.connectionPool.getConnection();
            Statement stmt = null; 
            try {
            	stmt = connectionHandler.getConnection().createStatement();
            	stmt.executeUpdate("UPDATE `"+ ConfigManager.main.Table_Players +"` SET firstonline=lastonline");
            	stmt.close();
            } catch (SQLException e) {
                System.out.println("Could not upgrade firstonline values of existing players");
                e.printStackTrace();
                return;
            } finally {
                connectionHandler.release();
            }
        }

        ConfigManager.main.Version_Database_Players = 3;
        try {
            ConfigManager.main.save();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

}
