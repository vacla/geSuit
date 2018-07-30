package net.cubespace.geSuit.configs.SubConfig;

import net.cubespace.Yamler.Config.YamlConfig;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Database extends YamlConfig {
    public String Host = "localhost";
    public String Database = "minecraft";
    public String Port = "3306";
    public String Username = "username";
    public String Password = "password";
    public Integer Threads = 5;
    public Boolean useSSL = false;
}
