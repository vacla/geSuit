package net.cubespace.geSuit.database;

import net.cubespace.geSuit.managers.DatabaseManager;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Players implements IRepository {
    public boolean playerExists(String player) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement playerExists = connectionHandler.getPreparedStatement("playerExists");
            playerExists.setString(1, player);

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
            PreparedStatement getPlayerIP = connectionHandler.getPreparedStatement("getPlayerTPS");
            getPlayerIP.setString(1, player);

            boolean tps = true;
            ResultSet res = getPlayerIP.executeQuery();
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

    public void insertPlayer(String player, String ip) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement insertPlayer = connectionHandler.getPreparedStatement("insertPlayer");
            insertPlayer.setString(1, player);
            insertPlayer.setString(2, ip);

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
            insertPlayerConvert.setDate(2, lastonline);
            insertPlayerConvert.setString(3, ip);
            insertPlayerConvert.setBoolean(4, tps);

            insertPlayerConvert.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    @Override
    public String[] getTable() {
        return new String[]{"players", "playername VARCHAR(100), " +
                "lastonline DATETIME NOT NULL, " +
                "ipaddress VARCHAR(100), " +
                "tps TINYINT(1) DEFAULT 1," +
                "CONSTRAINT pk_playername PRIMARY KEY (playername)"};
    }

    @Override
    public void registerPreparedStatements(ConnectionHandler connection) {
        connection.addPreparedStatement("getPlayerIP", "SELECT ipaddress FROM players WHERE playername = ?");
        connection.addPreparedStatement("playerExists", "SELECT playername FROM players WHERE playername = ?");
        connection.addPreparedStatement("getPlayerTPS", "SELECT tps FROM players WHERE playername = ?");
        connection.addPreparedStatement("insertPlayer", "INSERT INTO players (playername,lastonline,ipaddress) VALUES (?, NOW(), ?)");
        connection.addPreparedStatement("insertPlayerConvert", "INSERT INTO players (playername,lastonline,ipaddress,tps) VALUES (?, ?, ?, ?)");
    }
}
