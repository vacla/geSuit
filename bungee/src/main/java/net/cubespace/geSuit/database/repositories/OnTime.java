package net.cubespace.geSuit.database.repositories;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.TimeRecord;
import net.cubespace.geSuit.core.util.Utilities;
import net.cubespace.geSuit.database.BaseRepository;
import net.cubespace.geSuit.database.ConnectionHandler;
import net.cubespace.geSuit.database.StatementKey;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.md_5.bungee.api.ChatColor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class OnTime extends BaseRepository {
    private StatementKey timeDay;
    private StatementKey timeWeek;
    private StatementKey timeMonth;
    private StatementKey timeYear;
    private StatementKey timeTotal;
    
    private StatementKey ontimeTop;
    
    public OnTime(String name) {
        super(name);
    }
    
    @Override
    protected String getTableDeclaration() {
        return "`uuid` char(32) NOT NULL, `timeslot` datetime NOT NULL, `time` int(11) NOT NULL, UNIQUE KEY `pair` (`uuid`,`timeslot`)";
    }
    
    @Override
    public void registerStatements() {
        timeDay = registerStatement("getOnTimeToday", "SELECT SUM(`time`) FROM " + getName() + " WHERE `uuid`=? AND `timeslot` >= CURRENT_DATE()");
        timeWeek = registerStatement("getOnTimeWeek",  "SELECT SUM(`time`) FROM " + getName() + " WHERE `uuid`=? AND `timeslot` >= STR_TO_DATE(CONCAT(YEARWEEK(NOW()), ' Sunday'), '%X%V %W')");
        timeMonth = registerStatement("getOnTimeMonth", "SELECT SUM(`time`) FROM " + getName() + " WHERE `uuid`=? AND `timeslot` >= DATE_FORMAT(NOW(), '%Y-%m-01')");
        timeYear = registerStatement("getOnTimeYear",  "SELECT SUM(`time`) FROM " + getName() + " WHERE `uuid`=? AND `timeslot` > DATE_FORMAT(NOW(), '%Y-01-01')");
        timeTotal = registerStatement("getOnTimeTotal", "SELECT SUM(`time`) FROM " + getName() + " WHERE `uuid`=?");
        ontimeTop = registerStatement("getOnTimeTop",   "SELECT `uuid`, SUM(`time`) AS `totaltime` FROM `%ontime%` GROUP BY `uuid` ORDER BY `totaltime` DESC LIMIT ? OFFSET ?".replace("%ontime%", getName()));
    }
    
    public void updatePlayerOnTime(GlobalPlayer player, long tsStart, long tsEnd) {
        String uuid = Utilities.toString(player.getUniqueId());
        // Get start/end in calendar objects (required to handle day/month/year changes properly)
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(tsStart);
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(tsEnd);
        
        if (geSuit.getPlugin().isDebugEnabled())
        	geSuit.getPlugin().DebugMsg("OnTime (" +player+ "): " + Utilities.formatDate(start.getTimeInMillis()) + " -> " + Utilities.formatDate(end.getTimeInMillis()));

        // Set up the initial slots
        Calendar slot = Calendar.getInstance();
        Calendar next   = Calendar.getInstance();
        slot.setTimeInMillis(tsStart);
        slot.set(Calendar.MINUTE, 0);
        slot.set(Calendar.SECOND, 0);
        slot.set(Calendar.MILLISECOND, 0);
        next.setTimeInMillis(slot.getTimeInMillis());
        next.add(Calendar.HOUR, 1);

        int loop = 0;
        List<String> values = new ArrayList<String>();

        // Loop through all the hourly time slots that the player was online
        while ((loop == 0) || (end.after(next))) {
        	long time, from, to;
        	if (loop == 0) {
        		from = start.getTimeInMillis();  // On the first loop, measure from the start time, not the beginning of the slot
        	} else {
        		slot.setTimeInMillis(next.getTimeInMillis());
                next.add(Calendar.HOUR, 1);
        		from = slot.getTimeInMillis();  // Every other loop iteration, we measure from the beginning of the slot
        	}

        	if (end.after(next)) {
        		to = next.getTimeInMillis();
        		time = (to - from);  // This is not the last slot, measure time to the end of this slot
        	} else {
        		to = end.getTimeInMillis();
        		time = (to - from);   // This is the last slot, measure time to the "end" time (not the end of the slot)
        	}

        	// Only record positive online times  
        	if (time > 0) {
        		time = (time / 1000);  // Convert from milliseconds to seconds

                if (geSuit.getPlugin().isDebugEnabled())
                	geSuit.getPlugin().DebugMsg("   " +
	        			"Row " + loop + ": " +
	        			"Slot: " + Utilities.formatDate(slot.getTimeInMillis()) + " = " +
	        			Utilities.formatDate(from) + " -> " +
	        			Utilities.formatDate(to) + ": " +
	        			time + " secs"
	        	);
	
	        	values.add("('" +uuid+ "', '" +Utilities.formatDate(slot.getTimeInMillis())+ "', " +time+ ")");
        	}

        	loop++;

        	if (loop == 100) {
        		LoggingManager.log(ChatColor.RED + "WARNING! OnTime slot checking exceeded 100 loops for " +player+ ", this should never hapen!");
        		break;
        	}
        }
        
        StringBuilder sqlvalues = new StringBuilder();
        for (int x = 0; x < values.size(); x++) {
        	if (x > 0)
        		sqlvalues.append(", ");

        	sqlvalues.append(values.get(x));
        }

        ConnectionHandler connectionHandler = getConnection();
        Statement stmt = null;
        try {
        	// Sadly, we can't use prepared statements here because the statement is dynamic
            // TODO: This could probably be a prepared statement if we use batching
            stmt = connectionHandler.getConnection().createStatement();
        	stmt.executeUpdate("INSERT DELAYED INTO "+ ConfigManager.main.Table_OnTime + " " +
        			"(uuid,timeslot,time) VALUES " + sqlvalues + " " +
        			"ON DUPLICATE KEY UPDATE time=time+VALUES(time)"
        	);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
			try {
				if (stmt != null) { stmt.close(); stmt = null; }
			} catch (SQLException e) {
				System.out.println("ERROR: Failed to close SQL Statement!");
				e.printStackTrace();
			}
    		connectionHandler.release();
        }
    }

    public TimeRecord getPlayerOnTime(UUID id) throws SQLException {
        TimeRecord record = new TimeRecord(id);

        ConnectionHandler handler = getConnection();
        ResultSet res = null;
        try {
            String uuid = Utilities.toString(id);
        	// Time today
            res = handler.executeQuery(timeDay, uuid);
            if (res.next()) {
                record.setTimeToday(res.getLong(1) * 1000);
            }
            res.close();

        	// Time this week
            res = handler.executeQuery(timeWeek, uuid);
            if (res.next()) {
                record.setTimeWeek(res.getLong(1) * 1000);
            }
            res.close();

        	// Time this month
            res = handler.executeQuery(timeMonth, uuid);
            if (res.next()) {
                record.setTimeMonth(res.getLong(1) * 1000);
            }
            res.close();

        	// Time this year
            res = handler.executeQuery(timeYear, uuid);
            if (res.next()) {
                record.setTimeYear(res.getLong(1) * 1000);
            }
            res.close();

        	// Total time
            res = handler.executeQuery(timeTotal, uuid);
            if (res.next()) {
                record.setTimeTotal(res.getLong(1) * 1000);
            }
            res.close();
            
            return record;
        } finally {
            if (res != null && !res.isClosed()) {
                res.close();
            }
            
            handler.release();
        }
    }    

    public Map<UUID, Long> getOnTimeTop(int offset, int size) throws SQLException {
        ConnectionHandler handler = getConnection();
        ResultSet results = null;
        try {
            Map<UUID, Long> top = Maps.newLinkedHashMap();
            
            results = handler.executeQuery(ontimeTop, size, offset);
            while (results.next()) {
                top.put(Utilities.makeUUID(results.getString("uuid")), results.getLong("totaltime"));
            }
            
            return top;
        } finally {
            if (results != null) {
                results.close();
            }
            
            handler.release();
        }
    }
}
