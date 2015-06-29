package net.cubespace.geSuit.database;

import java.sql.SQLException;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public abstract class BaseRepository {
    private String name;
    private Map<String, StatementKey> keys;
    private ConnectionPool pool;
    
    public BaseRepository(String name, ConnectionPool pool) {
        this.name = name;
        this.pool = pool;
        
        keys = Maps.newLinkedHashMap();
    }
    
    public final String getName() {
        return name;
    }
    
    protected abstract String getTableDeclaration();
    
    protected final StatementKey registerStatement(String name, String sql) {
        return registerStatement(name, sql, false);
    }
    
    protected final StatementKey registerStatement(String name, String sql, boolean returnGeneratedKeys) {
        Preconditions.checkArgument(!keys.containsKey(name));
        
        StatementKey key = new StatementKey(name, sql, returnGeneratedKeys);
        keys.put(name, key);
        return key;
    }
    
    protected final StatementKey getStatement(String name) {
        return keys.get(name);
    }
    
    public abstract void registerStatements();
    
    protected final ConnectionHandler getConnection() throws SQLException {
        return pool.getConnection();
    }
}
