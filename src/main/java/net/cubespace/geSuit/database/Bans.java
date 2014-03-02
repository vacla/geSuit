package net.cubespace.geSuit.database;

import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.Ban;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Bans implements IRepository {
    public boolean isPlayerBanned(String player) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement isPlayerBanned = connectionHandler.getPreparedStatement("isPlayerBanned");
            isPlayerBanned.setString(1, player);

            return isPlayerBanned.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return true;
    }

    public void banPlayer(String display, String bannedBy, String bannedEntity, String reason, String type) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement banPlayer = connectionHandler.getPreparedStatement("banPlayer");
            banPlayer.setString(1, display);
            banPlayer.setString(2, bannedEntity);
            banPlayer.setString(3, bannedBy);
            banPlayer.setString(4, reason);
            banPlayer.setString(5, type);

            banPlayer.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public void tempBanPlayer(String display, String bannedBy, String bannedEntity, String reason, String till) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement tempBanPlayer = connectionHandler.getPreparedStatement("tempBanPlayer");
            tempBanPlayer.setString(1, display);
            tempBanPlayer.setString(2, bannedEntity);
            tempBanPlayer.setString(3, bannedBy);
            tempBanPlayer.setString(4, reason);
            tempBanPlayer.setString(5, till);

            tempBanPlayer.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public Ban getBanInfo(String player) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        Ban b = null;

        try {
            PreparedStatement banInfo = connectionHandler.getPreparedStatement("banInfo");
            banInfo.setString(1, player);

            ResultSet res = banInfo.executeQuery();
            while (res.next()) {
                b = new Ban(res.getInt("id"), res.getString("display"), res.getString("banned_by"), res.getString("reason"), res.getString("type"), res.getTimestamp("banned_on"), res.getTimestamp("banned_until"));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return b;
    }

    public void unbanPlayer(int id) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement unbanPlayer = connectionHandler.getPreparedStatement("unbanPlayer");
            unbanPlayer.setInt(1, id);
            unbanPlayer.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    public void insertBanConvert(String bannedBy, String player, String reason, String type, Date bannedOn, Date bannedUntil) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement insertBanConvert = connectionHandler.getPreparedStatement("insertBanConvert");
            insertBanConvert.setString(1, player);
            insertBanConvert.setString(2, player);
            insertBanConvert.setString(3, bannedBy);
            insertBanConvert.setString(4, reason);
            insertBanConvert.setString(5, type);
            insertBanConvert.setDate(6, bannedOn);
            insertBanConvert.setDate(7, bannedUntil);

            insertBanConvert.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    @Override
    public String[] getTable() {
        return new String[]{"bans", "id INT(11) NOT NULL AUTO_INCREMENT," +
                "display VARCHAR(100), " +
                "banned_entity VARCHAR(100), " +
                "banned_by VARCHAR(100), " +
                "reason VARCHAR(255), " +
                "type VARCHAR(100), " +
                "banned_on DATETIME NOT NULL," +
                "banned_until DATETIME, " +
                "CONSTRAINT pk_banid PRIMARY KEY (id)"};
    }

    @Override
    public void registerPreparedStatements(ConnectionHandler connection) {
        connection.addPreparedStatement("isPlayerBanned", "SELECT id FROM bans WHERE banned_entity = ? AND type <> 'unban'");
        connection.addPreparedStatement("banPlayer", "INSERT INTO bans (display,banned_entity,banned_by,reason,type,banned_on) VALUES (?,?,?,?,?,NOW());");
        connection.addPreparedStatement("unbanPlayer", "UPDATE bans SET type = 'unban' WHERE id = ?");
        connection.addPreparedStatement("banInfo", "SELECT * FROM bans WHERE banned_entity = ? AND type <> 'unban'");
        connection.addPreparedStatement("tempBanPlayer", "INSERT INTO bans (display,banned_entity,banned_by,reason,type,banned_on,banned_until) VALUES(?,?,?,?,'tempban',NOW(),?)");
        connection.addPreparedStatement("insertBanConvert", "INSERT INTO bans (display,banned_entity,banned_by,reason,type,banned_on,banned_until) VALUES(?,?,?,?,?,?,?)");
    }

    @Override
    public void checkUpdate() {
        //What current Version of the Database is this ?
        int installedVersion = ConfigManager.main.Version_Database_Ban;
    }
}
