package net.cubespace.geSuit.database;

import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.Track;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

    public List<Track> getPlayerTracking(String search, String type) {
        List<Track> tracking = new ArrayList<>();

        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
        try {
        	PreparedStatement trackInfo;
        	
        	if (type == "ip") {
        		// Lookup by IP
            	trackInfo = connectionHandler.getPreparedStatement("getIPTracking");
                trackInfo.setString(1, search);
        	}
        	else if (type == "uuid") {
        		// Lookup by IP
            	trackInfo = connectionHandler.getPreparedStatement("getUUIDTracking");
                trackInfo.setString(1, search);
        	} else {
        		// Lookup by player name
            	trackInfo = connectionHandler.getPreparedStatement("getPlayerTracking");
                trackInfo.setString(1, search);
        	}

            ResultSet res = trackInfo.executeQuery();
            while (res.next()) {
                tracking.add(new Track(
                		res.getString("player"),
                		res.getString("uuid"),
                		res.getString("ip"),
                		res.getTimestamp("firstseen"),
                		res.getTimestamp("lastseen")
                ));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionHandler.release();
        }

        return tracking;
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
        connection.addPreparedStatement("getPlayerTracking", "SELECT t2.ip, t2.player, t2.uuid, t2.firstseen, t2.lastseen FROM "+ ConfigManager.main.Table_Tracking +" AS t1 JOIN tracking AS t2 ON t1.ip=t2.ip WHERE t1.player=? ORDER BY t2.lastseen");
        connection.addPreparedStatement("getUUIDTracking", "SELECT t2.ip, t2.player, t2.uuid, t2.firstseen, t2.lastseen FROM "+ ConfigManager.main.Table_Tracking +" AS t1 JOIN tracking AS t2 ON t1.ip=t2.ip WHERE t1.uuid=? ORDER BY t2.lastseen");
        connection.addPreparedStatement("getIPTracking", "SELECT ip, player, uuid, firstseen, lastseen FROM "+ ConfigManager.main.Table_Tracking +" WHERE ip=? ORDER BY lastseen");
    }

	@Override
	public void checkUpdate() {

	}
}
