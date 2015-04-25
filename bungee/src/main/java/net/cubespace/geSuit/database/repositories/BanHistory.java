package net.cubespace.geSuit.database.repositories;

import java.net.InetAddress;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.database.BaseRepository;
import net.cubespace.geSuit.database.ConnectionHandler;
import net.cubespace.geSuit.database.StatementKey;

public class BanHistory extends BaseRepository {
    private StatementKey insertBan;
    private StatementKey insertUnban;
    private StatementKey updateBan;
    private StatementKey history;
    
    public BanHistory(String name) {
        super(name);
    }
    
    @Override
    protected String getTableDeclaration() {
        return "`id` INTEGER AUTO INCREMENT PRIMARY KEY, `who` VARCHAR(32) NOT NULL, `type` ENUM('uuid', 'ip') NOT NULL, `reason` VARCHAR(255), `by_name` VARCHAR(20) NOT NULL, `by_uuid` CHAR(32), `action` ENUM('ban', 'unban') NOT NULL, `date` DATETIME NOT NULL, `until` DATETIME, `unban_id` INTEGER, INDEX (`who`, `type`)";
    }

    @Override
    public void registerStatements() {
        insertBan = registerStatement("insertBan", "INSERT INTO `" + getName() + "` VALUES (DEFAULT,?,?,?,?,?,'ban',?,?,NULL);", true);
        insertUnban = registerStatement("insertUnban", "INSERT INTO `" + getName() + "` VALUES (DEFAULT,?,?,?,?,?,'unban',?,NULL,NULL);", true);
        updateBan = registerStatement("updateBan", "UPDATE `" + getName() + "` SET `unban_id`=? WHERE `id`=?;");
        history = registerStatement("getHistory", "SELECT * FROM `" + getName() + "` WHERE `who`=? AND `type`=? ORDER BY `date` ASC;");
    }

    public void recordBan(BanInfo<?> ban) throws SQLException {
        ConnectionHandler con = getConnection();
        
        try {
            ResultSet result;
            if (ban.getWho() instanceof GlobalPlayer) {
                result = con.executeUpdateWithResults(insertBan, 
                        Utilities.toString(((GlobalPlayer)ban.getWho()).getUniqueId()),
                        "uuid",
                        ban.getReason(),
                        ban.getBannedBy(),
                        (ban.getBannedById() != null ? Utilities.toString(ban.getBannedById()) : null),
                        new Date(ban.getDate()),
                        (ban.isTemporary() ? new Date(ban.getUntil()) : null)
                        );
            } else if (ban.getWho() instanceof InetAddress) {
                result = con.executeUpdateWithResults(insertBan,
                        ((InetAddress)ban.getWho()).getHostAddress(),
                        "ip",
                        ban.getReason(),
                        ban.getBannedBy(),
                        (ban.getBannedById() != null ? Utilities.toString(ban.getBannedById()) : null),
                        new Date(ban.getDate()),
                        (ban.isTemporary() ? new Date(ban.getUntil()) : null)
                        );
            } else {
                throw new IllegalArgumentException("Ban is of a type that is not supported");
            }
            
            if (result.next()) {
                ban.setDatabaseKey(result.getInt(1));
            }
            
            result.close();
        } finally {
            con.release();
        }
    }
    
    public void recordUnban(BanInfo<?> ban, String reason, String by, UUID byId) throws SQLException {
        Preconditions.checkArgument(ban.getDatabaseKey() >= 0, "Database key not set. We cannot unban without it.");
        ConnectionHandler con = getConnection();
        
        try {
            ResultSet result;
            if (ban.getWho() instanceof GlobalPlayer) {
                result = con.executeUpdateWithResults(insertUnban, 
                        Utilities.toString(((GlobalPlayer)ban.getWho()).getUniqueId()),
                        "uuid",
                        reason,
                        ban.getBannedBy(),
                        (ban.getBannedById() != null ? Utilities.toString(ban.getBannedById()) : null),
                        new Date(ban.getDate())
                        );
            } else if (ban.getWho() instanceof InetAddress) {
                result = con.executeUpdateWithResults(insertUnban,
                        ((InetAddress)ban.getWho()).getHostAddress(),
                        "ip",
                        reason,
                        ban.getBannedBy(),
                        (ban.getBannedById() != null ? Utilities.toString(ban.getBannedById()) : null),
                        new Date(ban.getDate())
                        );
            } else {
                throw new IllegalArgumentException("Ban is of a type that is not supported");
            }
            
            if (result.next()) {
                con.executeUpdate(updateBan, result.getInt(1), ban.getDatabaseKey());
            }
        } finally {
            con.release();
        }
    }
    
    public List<BanInfo<GlobalPlayer>> getBanHistory(GlobalPlayer player) throws SQLException {
        ConnectionHandler con = getConnection();
        
        try {
            ResultSet results = con.executeQuery(history, 
                    Utilities.makeUUID(player.getUniqueId().toString()),
                    "uuid"
                    );
            
            List<BanInfo<GlobalPlayer>> bans = Lists.newArrayList();
            
            while(results.next()) {
                int id = results.getInt("id");
                String reason = results.getString("reason");
                String byName = results.getString("by_name");
                UUID byId = null;
                if (results.getString("by_id") == null) {
                    byId = Utilities.makeUUID(results.getString("by_id"));
                }
                
                long date = results.getDate("date").getTime();
                long until = 0;
                if (results.getDate("until") != null) {
                    until = results.getDate("until").getTime();
                }
                
                boolean isUnban = results.getString("action").equals("unban");
                
                bans.add(new BanInfo<GlobalPlayer>(player, id, reason, byName, byId, date, until, isUnban));
            }
            
            results.close();
            return bans;
        } finally {
            con.release();
        }
    }
}
