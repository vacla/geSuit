package net.cubespace.geSuit.configs;

import net.cubespace.Yamler.Config.YamlConfig;
import net.cubespace.geSuit.geSuit;

import java.io.File;

public class SpawnConfig extends YamlConfig {
    public SpawnConfig() {
        CONFIG_FILE = new File(geSuit.instance.getDataFolder(), "spawns.yml");
    }

	public Boolean SpawnNewPlayerAtNewspawn = false;
	public Boolean ForceAllPlayersToProxySpawn = false;
}
