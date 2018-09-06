package net.cubespace.geSuit.database;

import net.cubespace.geSuit.configs.SubConfig.Database;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.md_5.bungee.api.ProxyServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.logging.Logger;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 8/08/2018.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ProxyServer.class, geSuit.class, ConfigManager.class})
public class ConnectionPoolIntegrationTest {
    
    private static final ProxyServer server = mock(ProxyServer.class);
    private static final Logger log = Logger.getLogger("Test");
    private final geSuit plugin = new geSuit();
    private ConfigManager cManager = mock(ConfigManager.class);
    
    private Database config;
    
    @Before
    public void Setup() {
        PowerMockito.mockStatic(geSuit.class);
        PowerMockito.mockStatic(ProxyServer.class);
        PowerMockito.mockStatic(ConfigManager.class);
        when(ProxyServer.getInstance()).thenReturn(server);
        when(server.getLogger()).thenReturn(log);
        when(plugin.getDataFolder()).thenReturn(new File("./"));
        when(geSuit.getInstance()).thenReturn(plugin);
        when(server.getLogger()).thenReturn(log);
        config = new Database();
        config.Database = "geSuit";
        config.Port = "3306";
        config.Username = "gesuit";
        config.Password = "gesuit";
        config.Host = "localhost";
        config.useMetrics = false;
    }
    
    @Test
    public void ConnectionPoolTest() {
        ConnectionPool pool = new ConnectionPool();
        pool.configureDataSource(config);
        assert (pool.getConnection() != null);
    }
}