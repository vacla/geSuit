package net.cubespace.geSuit.config;

import net.cubespace.Yamler.Config.Config;

import java.io.File;
import java.util.ArrayList;

public class TeleportsConfig extends Config {
    public TeleportsConfig(File file) {
        CONFIG_FILE = file;
    }

    public Integer TeleportRequestExpireTime = 10;

    public ArrayList<String> TPAWhitelist = new ArrayList<String>();
    
    public Boolean SpawnNewPlayerAtNewspawn = false;
    public Boolean ForceAllPlayersToProxySpawn = false;
}
