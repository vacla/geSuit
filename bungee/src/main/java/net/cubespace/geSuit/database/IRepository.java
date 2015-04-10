package net.cubespace.geSuit.database;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public interface IRepository {
    public String[] getTable();
    public void registerPreparedStatements(ConnectionHandler connection);
    public void checkUpdate();
}
