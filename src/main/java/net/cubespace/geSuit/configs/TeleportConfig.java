package net.cubespace.geSuit.configs;

import net.cubespace.geSuit.geSuit;
import net.cubespace.Yamler.Config.Config;

import java.io.File;

public class TeleportConfig extends Config {
    public TeleportConfig() {
        CONFIG_FILE = new File(geSuit.instance.getDataFolder(), "teleport.yml");
    }

    public Integer TeleportRequestExpireTime = 10;
}
