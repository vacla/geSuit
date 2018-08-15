package net.cubespace.geSuit.database;

import net.cubespace.geSuit.configs.SubConfig.Database;

import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 8/08/2018.
 */
public class ConnectionPoolIntegrationTest {
    private Database config;

    @Before
    public void Setup() {
        config = new Database();
        config.Database = "geSuit";
        config.Port = "3306";
        config.Username = "root";
        config.Password = "password";
        config.Host = "localhost";
        config.useMetrics = false;
    }

    @Test
    public void ConnectionPoolTest() {
        ConnectionPool pool = new ConnectionPool();
        pool.configureDataSource(config);
        try {
            assert (pool.getConnection() != null);
        } catch (SQLException e) {
        }
    }

}