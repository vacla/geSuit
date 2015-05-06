package net.cubespace.geSuit.database;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.configs.SubConfig.Database;
import net.md_5.bungee.api.ProxyServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.google.common.collect.Lists;

public class ConnectionPool {
    private Database dbConfig;
    private String connectionString;
    private List<BaseRepository> repositories;
    private List<ConnectionHandler> connections;

    public ConnectionPool() {
        repositories = Lists.newArrayList();
        connections = Lists.newArrayList();
        
        // Load the JDBC driver if not already
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Mysql jdbc driver missing. This is should not happen");
        }
    }
    public void addRepository(BaseRepository repository) {
        repositories.add(repository);
        repository.initialize(this);
        repository.registerStatements();
    }

    public boolean initialiseConnections(Database database) {
        this.dbConfig = database;
        
        connectionString = String.format("jdbc:mysql://%s:%s/%s", dbConfig.Host, dbConfig.Port, dbConfig.Database);
        
        try {
            for (int i = 0; i < database.Threads; i++) {
                createConnection();
            }
        } catch (SQLException e) {
            geSuit.getLogger().severe("Unable to connect to MySQL.");
            throw new IllegalArgumentException("Unable to connect to MySQL");
        }

        ProxyServer.getInstance().getScheduler().schedule(geSuit.getPlugin(), new Runnable() {
            public void run() {
                Iterator<ConnectionHandler> cons = connections.iterator();
                while (cons.hasNext()) {
                    ConnectionHandler con = cons.next();

                    if (!con.isUsed() && con.isOldConnection()) {
                        con.closeConnection();
                        cons.remove();
                    }
                }
            }
        }, 10, 10, TimeUnit.SECONDS);

        doSetupCheck();

        return true;
    }
    
    private void doSetupCheck() {
        ConnectionHandler handler = getConnection();
        Connection con = handler.getConnection();
        
        try {
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
                        geSuit.getLogger().log(Level.SEVERE, "There was an SQLException while attempting to create the " + repository.getName() + " table", ex);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void setupRepository(BaseRepository repo, Statement statement) throws SQLException {
        statement.executeUpdate(String.format("CREATE TABLE `%s` (%s);", repo.getName(), repo.getTableDeclaration()));
    }

    /**
     * @return Returns a free connection from the pool of connections. Creates a new connection if there are none available
     */
    public synchronized ConnectionHandler getConnection() {
        for (ConnectionHandler c : connections) {
            if (!c.isUsed()) {
                return c;
            }
        }
        
        try {
            return createConnection();
        } catch (SQLException e) {
            geSuit.getLogger().log(Level.SEVERE, "Unable to create new MySQL connection", e);
            return null;
        }
    }
    
    private ConnectionHandler createConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(connectionString, dbConfig.Username, dbConfig.Password);

        ConnectionHandler handler = new ConnectionHandler(connection);
        for(BaseRepository repository : repositories) {
            repository.createStatements(handler);
        }
        
        connections.add(handler);
        return handler;
    }

    public void closeConnections() {
        for (ConnectionHandler c : connections) {
            c.closeConnection();
        }
    }
}