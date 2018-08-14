package net.cubespace.geSuit.configs.SubConfig;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.YamlConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Database extends YamlConfig {
    @Comment("Your Database Host")
    public String Host = "localhost";
    @Comment("Your Database name")
    public String Database = "minecraft";
    @Comment("Your Database Port")
    public String Port = "3306";
    @Comment("Database Username")
    public String Username = "username";
    @Comment("Database Password")
    public String Password = "password";
    @Comment("Database Threads - now deprecated")
    public Integer Threads = 5;
    @Comment("Database useSSL")
    public Boolean useSSL = false;
    @Comment("Set true if you use DripCordReporter")
    public Boolean useMetrics = false;
    @Comment("A list of optional properties for the Hikari Connection Pool see https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby")
    public Map<String, String> properties = new HashMap<>();
}
