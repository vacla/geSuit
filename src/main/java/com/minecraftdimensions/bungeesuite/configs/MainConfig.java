package com.minecraftdimensions.bungeesuite.configs;

import com.minecraftdimensions.bungeesuite.BungeeSuite;
import net.cubespace.Yamler.Config.Config;

import java.io.File;

public class MainConfig extends Config {
    public MainConfig() {
        CONFIG_FILE = new File(BungeeSuite.instance.getDataFolder(), "config.yml");
    }

    public String Database_Host = "localhost";
    public String Database_Database = "minecraft";
    public String Database_Port = "3306";
    public String Database_Username = "username";
    public String Database_Password = "password";
    public Integer Database_Threads = 5;

    public Boolean MOTD_Enabled = true;
    public Boolean NewPlayerBroadcast = true;
    public Boolean BroadcastProxyConnectionMessages = true;
    public Integer PlayerDisconnectDelay = 10;
}
