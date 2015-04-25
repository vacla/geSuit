package net.cubespace.geSuit.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementKey {
    private String name;
    private String sql;
    private boolean returnGeneratedKeys;
    private boolean valid;
    
    StatementKey(String name, String sql, boolean returnGeneratedKeys) {
        this.name = name;
        this.sql = sql;
        this.returnGeneratedKeys = returnGeneratedKeys;
        this.valid = true;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSQL() {
        return sql;
    }
    
    public boolean returnsGeneratedKeys() {
        return returnGeneratedKeys;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
        try {
            return connection.prepareStatement(sql, (returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS));
        } catch (SQLException e) {
            valid = false;
            throw e;
        }
    }
}
