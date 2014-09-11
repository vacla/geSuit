package net.cubespace.geSuit.database;

import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;

import java.sql.PreparedStatement;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Tracking implements IRepository {

    public void insertTracking(String player, String uuid, String ip) {
        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();

        try {
            PreparedStatement insertPlayer = connectionHandler.getPreparedStatement("insertTracking");
            insertPlayer.setString(1, player);
            insertPlayer.setString(2, uuid);
            insertPlayer.setString(3, ip);

            insertPlayer.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }
    }

    @Override
    public String[] getTable() {
    	return new String[]{ConfigManager.main.Table_Tracking, "player varchar(20) NOT NULL, "
    			  + "`uuid` varchar(32) NOT NULL,"
    			  + "`ip` varchar(15) NOT NULL,"
    			  + "`firstseen` datetime NOT NULL,"
    			  + "`lastseen` datetime NOT NULL,"
    			  + "UNIQUE KEY `player` (`player`,`uuid`,`ip`)"};
    }

    @Override
    public void registerPreparedStatements(ConnectionHandler connection) {
        connection.addPreparedStatement("insertTracking", "INSERT INTO "+ ConfigManager.main.Table_Tracking +" (player,uuid,ip,firstseen,lastseen) VALUES (?, ?, ?, NOW(), NOW()) ON DUPLICATE KEY UPDATE lastseen=NOW()");
    }

	@Override
	public void checkUpdate() {

	}
}
