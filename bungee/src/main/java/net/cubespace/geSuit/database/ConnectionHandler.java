package net.cubespace.geSuit.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class ConnectionHandler {
    private Connection connection;
    private boolean used;
    private long lastUsed;
    private Map<StatementKey, PreparedStatement> preparedStatements;

    public ConnectionHandler(Connection connection) {
        this.connection = connection;
        
        preparedStatements = Maps.newIdentityHashMap();
    }

    public Connection getConnection() {
        lastUsed = System.currentTimeMillis();
        used = true;
        return connection;
    }

    public boolean isOldConnection() {
        return (System.currentTimeMillis() - lastUsed) > 30000;
    }

    void registerStatementKey(StatementKey key) {
        try {
            PreparedStatement statement = key.createPreparedStatement(connection);
            preparedStatements.put(key, statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public ResultSet executeQuery(StatementKey key, Object... arguments) throws SQLException {
        PreparedStatement statement = preparedStatements.get(key);
        Preconditions.checkNotNull(statement, "Statement was never registered (or failed)");
        
        used = true;
        applyArguments(statement, arguments);
        return statement.executeQuery();
    }
    
    public int executeUpdate(StatementKey key, Object... arguments) throws SQLException {
        PreparedStatement statement = preparedStatements.get(key);
        Preconditions.checkNotNull(statement, "Statement was never registered (or failed)");
        
        used = true;
        applyArguments(statement, arguments);
        return statement.executeUpdate();
    }
    
    public ResultSet executeUpdateWithResults(StatementKey key, Object... arguments) throws SQLException {
        Preconditions.checkArgument(key.returnsGeneratedKeys(), "Statement does not return generated keys");
        
        PreparedStatement statement = preparedStatements.get(key);
        Preconditions.checkNotNull(statement, "Statement was never registered (or failed)");
        
        used = true;
        applyArguments(statement, arguments);
        statement.executeUpdate();
        return statement.getGeneratedKeys();
    }
    
    private void applyArguments(PreparedStatement statement, Object[] arguments) throws SQLException {
        for (int i = 0; i < arguments.length; ++i) {
            statement.setObject(i+1, arguments[i]);
        }
    }
    
    public void release() {
        used = false;
    }

    public boolean isUsed() {
        return used;
    }

    public void closeConnection() {
        used = true;
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
