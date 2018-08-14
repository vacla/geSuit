package net.cubespace.geSuit.database;

import net.cubespace.geSuit.configs.SubConfig.Database;
import net.cubespace.geSuit.managers.ConfigManager;

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
        ConfigManager manager = new ConfigManager();
        config = new Database();
        config.Database = "geSuit";
        config.Port = "3306";
        config.Username = "root";
        config.Password = "password";
        config.Host = "localhost";
    }

    @Test
    public void ConnectionPoolIntegrationTest() {
        ConnectionPool pool = new ConnectionPool();
        pool.initialiseConnections(config);
        try {
            assert (pool.getConnection() != null);
        } catch (SQLException e) {
        }
    }

}