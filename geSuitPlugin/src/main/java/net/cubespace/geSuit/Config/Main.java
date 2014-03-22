package net.cubespace.geSuit.Config;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.geSuit.Config.Sub.Database;
import net.cubespace.lib.CubespacePlugin;

import java.io.File;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Main extends Config {
    public Main(CubespacePlugin plugin) {
        CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
    }

    public Database Database = new Database();
    public Boolean ConvertFromBungeeSuite = false;
    public Database BungeeSuiteDatabase = new Database();
}
