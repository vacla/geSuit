package net.cubespace.geSuit.database;

import net.cubespace.geSuit.configs.SubConfig.Database;
import net.cubespace.geSuit.database.repositories.BanHistory;
import net.cubespace.geSuit.database.repositories.OnTime;
import net.cubespace.geSuit.database.repositories.Tracking;
import net.cubespace.geSuit.database.repositories.WarnHistory;

public class DatabaseManager {
    private BanHistory banHistory;
    private WarnHistory warnHistory;
    private OnTime ontime;
    private Tracking tracking;
    
    private Database config;
    private ConnectionPool pool;
    
    public DatabaseManager(Database config) {
        this.config = config;
        
        pool = new ConnectionPool();
    }
    
    public boolean initialize() {
        banHistory = new BanHistory(config.NameBanhistory);
        pool.addRepository(banHistory);
        warnHistory = new WarnHistory(config.NameWarnhistory);
        pool.addRepository(warnHistory);
        ontime = new OnTime(config.NameOntime);
        pool.addRepository(ontime);
        tracking = new Tracking(config.NameTracking, config.NameBanhistory);
        pool.addRepository(tracking);
        
        return pool.initialiseConnections(config);
    }
    
    public void shutdown() {
        pool.closeConnections();
    }
    
    // Repositories
    
    public BanHistory getBanHistory() {
        return banHistory;
    }
    
    public WarnHistory getWarnHistory() {
        return warnHistory;
    }
    
    public Tracking getTracking() {
        return tracking;
    }
    
    public OnTime getOntime() {
        return ontime;
    }
}
