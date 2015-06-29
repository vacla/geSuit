package net.cubespace.geSuit.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.cubespace.geSuit.config.MainConfig.Database;
import net.cubespace.geSuit.database.repositories.BanHistory;
import net.cubespace.geSuit.database.repositories.OnTime;
import net.cubespace.geSuit.database.repositories.Tracking;
import net.cubespace.geSuit.database.repositories.WarnHistory;

public class DatabaseManager {
    private BanHistory banHistory;
    private WarnHistory warnHistory;
    private OnTime ontime;
    private Tracking tracking;
    
    private ConnectionPool pool;
    
    public DatabaseManager(ConnectionPool pool, Database config) {
        this.pool = pool;
        
        banHistory = new BanHistory(config.NameBanhistory, pool);
        warnHistory = new WarnHistory(config.NameWarnhistory, pool);
        ontime = new OnTime(config.NameOntime, pool);
        tracking = new Tracking(config.NameTracking, config.NameBanhistory, pool);
    }
    
    public void shutdown() {
        pool.closeConnections();
    }
    
    public void initialize() throws SQLException {
        BaseRepository[] repositories = new BaseRepository[] {banHistory, warnHistory, ontime, tracking};
        
        ConnectionHandler handler = pool.getConnection();
        try {
            Connection con = handler.getConnection();
            
            Statement statement = con.createStatement();
            
            for (BaseRepository repository : repositories) {
                try {
                    statement.executeQuery(String.format("SELECT * FROM `%s` LIMIT 0;", repository.getName()));
                    // Table exists, do nothing
                } catch (SQLException e) {
                    // Table does not exist
                    try {
                        setupRepository(repository, statement);
                    } catch (SQLException ex) {
                        // Repackage with repo name
                        throw new SQLException("Failed to create the " + repository.getName() + " table", ex);
                    }
                }
                
                repository.registerStatements();
            }
        } finally {
            handler.release();
        }
    }
    
    private void setupRepository(BaseRepository repo, Statement statement) throws SQLException {
        statement.executeUpdate(String.format("CREATE TABLE `%s` (%s);", repo.getName(), repo.getTableDeclaration()));
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
