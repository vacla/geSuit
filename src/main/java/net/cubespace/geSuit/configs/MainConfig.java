package net.cubespace.geSuit.configs;

import net.cubespace.geSuit.geSuit;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.geSuit.configs.SubConfig.Database;

import java.io.File;

public class MainConfig extends Config {
    public MainConfig() {
        CONFIG_FILE = new File(geSuit.instance.getDataFolder(), "config.yml");
    }

    public Database Database = new Database();
    public Boolean ConvertFromBungeeSuite = false;
    public Database BungeeSuiteDatabase = new Database();

    public Boolean MOTD_Enabled = true;
    public Boolean NewPlayerBroadcast = true;
    public Boolean BroadcastProxyConnectionMessages = true;
    public Integer PlayerDisconnectDelay = 10;
}
