package net.cubespace.geSuit.configs;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.geSuitPlugin;
import net.cubespace.Yamler.Config.Config;

import java.io.File;
import java.util.ArrayList;

public class TeleportConfig extends Config {
    public TeleportConfig() {
        CONFIG_FILE = geSuit.getFile("teleport.yml");
    }

    public Integer TeleportRequestExpireTime = 10;

    public ArrayList<String> TPAWhitelist = new ArrayList<String>();
}
