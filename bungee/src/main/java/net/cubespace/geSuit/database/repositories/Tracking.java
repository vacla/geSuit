package net.cubespace.geSuit.database.repositories;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.Track;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.database.BaseRepository;
import net.cubespace.geSuit.database.ConnectionHandler;
import net.cubespace.geSuit.database.StatementKey;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Tracking extends BaseRepository {
    private StatementKey insertTracking;
    
    private StatementKey nameTracking;
    private StatementKey uuidTracking;
    private StatementKey ipTracking;
    
    private StatementKey nameHistory;
    private StatementKey checkNameChange;
    
    private String bansTable;
    
    public Tracking(String name, String bansName) {
        super(name);
        bansTable = bansName;
    }
    
    @Override
    protected String getTableDeclaration() {
        return "name varchar(20) NOT NULL, nickname varchar(20), `uuid` varchar(32) NOT NULL, `ip` varchar(15) NOT NULL, `firstseen` datetime NOT NULL, `lastseen` datetime NOT NULL, UNIQUE KEY `player` (`name`,`nickname`,`uuid`,`ip`)";
    }
    
    @Override
    public void registerStatements() {
        insertTracking = registerStatement("insertTracking", "INSERT INTO " + getName() + " VALUES (?,?,?,?,NOW(),NOW()) ON DUPLICATE KEY UPDATE lastseen=NOW()");
        nameTracking = registerStatement("nameTracking", "SELECT t2.`name`, t2.`nickname`, t2.`uuid`, t2.`ip`, t2.`firstseen`, t2.`lastseen`, IF(b1.`date` IS NOT NULL, IF (b1.`until` IS NULL,1,2), 0) as `name_ban`, IF(b2.`date` IS NOT NULL, IF (b2.`until` IS NULL,1,2), 0) as `ip_ban` FROM `%tracking%` AS t1 JOIN `%tracking%` AS t2 ON (t1.`ip`=t2.`ip` OR t1.`uuid`=t2.`uuid`) LEFT JOIN `%bans%` AS b1 ON (t2.`uuid`=b1.`who`) AND b1.`action`='ban' AND b1.`unban_id` IS NULL AND (b1.`until` IS NULL OR b1.`until` > NOW())LEFT JOIN `%bans%` AS b2 ON (t2.`ip`=b2.`who`) AND b2.`action`='ban' AND b2.`unban_id` IS NULL AND (b2.`until` IS NULL OR b2.`until` > NOW()) WHERE t1.`name`=? GROUP BY t2.`name`,t2.`nickname`,t2.`uuid`,t2.`ip` ORDER BY t2.`lastseen`;".replace("%tracking%", getName()).replace("%bans%", bansTable));
        uuidTracking = registerStatement("uuidTracking", "SELECT t2.`name`, t2.`nickname`, t2.`uuid`, t2.`ip`, t2.`firstseen`, t2.`lastseen`, IF(b1.`date` IS NOT NULL, IF (b1.`until` IS NULL,1,2), 0) as `name_ban`, IF(b2.`date` IS NOT NULL, IF (b2.`until` IS NULL,1,2), 0) as `ip_ban` FROM `%tracking%` AS t1 JOIN `%tracking%` AS t2 ON (t1.`ip`=t2.`ip`) LEFT JOIN `%bans%` AS b1 ON (t2.`uuid`=b1.`who`) AND b1.`action`='ban' AND b1.`unban_id` IS NULL AND (b1.`until` IS NULL OR b1.`until` > NOW()) LEFT JOIN `%bans%` AS b2 ON (t2.`ip`=b2.`who`) AND b2.`action`='ban' AND b2.`unban_id` IS NULL AND (b2.`until` IS NULL OR b2.`until` > NOW()) WHERE t1.`uuid`=? GROUP BY t2.`name`,t2.`nickname`,t2.`uuid`,t2.`ip` ORDER BY t2.`lastseen`;".replace("%tracking%", getName()).replace("%bans%", bansTable));
        ipTracking = registerStatement("ipTracking", "SELECT t.`name`, t.`nickname`, t.`uuid`, t.`ip`, t.`firstseen`, t.`lastseen`, IF(b1.`date` IS NOT NULL, IF (b1.`until` IS NULL,1,2), 0) as `name_ban`, IF(b2.`date` IS NOT NULL, IF (b2.`until` IS NULL,1,2), 0) as `ip_ban` FROM `%tracking%` AS t LEFT JOIN `%bans%` AS b1 ON (t.`uuid`=b1.`who`) AND b1.`action`='ban' AND b1.`unban_id` IS NULL AND (b1.`until` IS NULL OR b1.`until` > NOW()) LEFT JOIN `%bans%` AS b2 ON (t.`ip`=b2.`who`) AND b2.`action`='ban' AND b2.`unban_id` IS NULL AND (b2.`until` IS NULL OR b2.`until` > NOW()) WHERE t.`ip`=? GROUP BY t.`name`,t.`nickname`,t.`uuid`,t.`ip` ORDER BY t.`lastseen`;".replace("%tracking%", getName()).replace("%bans%", bansTable));
        nameHistory = registerStatement("nameHistory", "SELECT p1.`name`, p1.`nickname`, p1.`uuid`, p1.`ip`, p1.`firstseen`, p1.`lastseen`, 0 as `name_ban`, 0 as `ip_ban` FROM `%tracking%` p1 INNER JOIN (SELECT max(lastseen) lastseen, name FROM `%tracking%` WHERE uuid=? GROUP BY name) p2 ON p1.name = p2.name AND p1.lastseen = p2.lastseen WHERE p1.uuid=? ORDER BY p1.lastseen DESC;".replace("%tracking%", getName()));
        checkNameChange = registerStatement("checkNameChange", "SELECT `name`, `nickname`, `uuid`, `ip`, `firstseen`, `lastseen`, 0 AS `name_ban`, 0 AS `ip_ban` FROM " + getName() + " WHERE `uuid`=? AND `player`!=? ORDER BY `lastseen` DESC LIMIT 1;");
    }
    
    public void insertTracking(GlobalPlayer player) throws SQLException {
        ConnectionHandler handler = getConnection();
        
        try {
            handler.executeUpdate(insertTracking,
                    player.getName(),
                    player.getNickname(),
                    Utilities.toString(player.getUniqueId()),
                    player.getAddress().getHostAddress()
                    );
        } finally {
            handler.release();
        }
    }
    
    public List<Track> getTrackingForName(String name) throws SQLException {
        ConnectionHandler handler = getConnection();
        ResultSet results = null;
        try {
            results = handler.executeQuery(nameTracking, name);
            return parseTracking(results);
        } finally {
            if (results != null) {
                results.close();
            }
            
            handler.release();
        }
    }
    
    public List<Track> getTrackingForIP(InetAddress ip) throws SQLException {
        ConnectionHandler handler = getConnection();
        ResultSet results = null;
        try {
            results = handler.executeQuery(ipTracking, ip.getHostAddress());
            return parseTracking(results);
        } finally {
            if (results != null) {
                results.close();
            }
            
            handler.release();
        }
    }
    
    public List<Track> getTrackingForUUID(UUID id) throws SQLException {
        ConnectionHandler handler = getConnection();
        ResultSet results = null;
        try {
            results = handler.executeQuery(uuidTracking, Utilities.toString(id));
            return parseTracking(results);
        } finally {
            if (results != null) {
                results.close();
            }
            
            handler.release();
        }
    }
    
    private List<Track> parseTracking(ResultSet results) throws SQLException {
        List<Track> tracking = Lists.newArrayList();

        while(results.next()) {
            tracking.add(readTracking(results));
        }
        
        return tracking;
    }
    
    private Track readTracking(ResultSet results) throws SQLException {
        return new Track(
                results.getString("name"), 
                results.getString("nickname"), 
                Utilities.makeUUID(results.getString("uuid")),
                InetAddresses.forString(results.getString("ip")), 
                results.getTimestamp("firstseen").getTime(),
                results.getTimestamp("lastseen").getTime(), 
                results.getInt("name_ban"), 
                results.getInt("ip_ban")
        );
    }
    
    public List<Track> getNameHistory(UUID id) throws SQLException {
        ConnectionHandler handler = getConnection();
        ResultSet results = null;
        try {
            results = handler.executeQuery(nameHistory, Utilities.toString(id), Utilities.toString(id));
            return parseTracking(results);
        } finally {
            if (results != null) {
                results.close();
            }
            
            handler.release();
        }
    }

    public Track checkNameChange(UUID id, String currentName) throws SQLException {
        ConnectionHandler handler = getConnection();
        ResultSet result = null;
        try {
            result = handler.executeQuery(checkNameChange, Utilities.toString(id), currentName);
            if (result.next()) {
                return readTracking(result);
            } else {
                return null;
            }
        } finally {
            if (result != null) {
                result.close();
            }
            handler.release();
        }
    }
}
