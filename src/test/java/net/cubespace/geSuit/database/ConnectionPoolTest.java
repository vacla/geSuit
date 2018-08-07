package net.cubespace.geSuit.database;

import net.cubespace.geSuit.configs.SubConfig.Database;

import org.junit.Test;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 8/08/2018.
 */
public class ConnectionPoolTest {

    @Test
    public void initialiseConnectionsTest() {
        ClassLoader.getSystemClassLoader();
        Database config = new Database();
        config.Host = "localhost";
        config.Username = "username";
        config.Password = "password";
        config.Port = "3306";
        config.useSSL = false;
        ConnectionPool pool = new ConnectionPool();
        pool.initialiseConnections(config);

    }
}