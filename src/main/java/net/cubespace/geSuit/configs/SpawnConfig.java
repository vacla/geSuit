package net.cubespace.geSuit.configs;

import net.cubespace.geSuit.geSuit;
import net.cubespace.Yamler.Config.Config;

import java.io.File;

public class SpawnConfig extends Config {
    public SpawnConfig() {
        CONFIG_FILE = new File(geSuit.instance.getDataFolder(), "spawns.yml");
    }

	public Boolean SpawnNewPlayerAtNewspawn = false;
	public Boolean ForceAllPlayersToProxySpawn = false;
}
