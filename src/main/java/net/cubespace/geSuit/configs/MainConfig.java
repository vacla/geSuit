package net.cubespace.geSuit.configs;

import net.cubespace.Yamler.Config.Comment;
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
    @Comment("This should be true on offline Mode Server since they can't use UUIDs provided by Mojang")
    public Boolean OverwriteUUID = false;

    @Comment("Do not alter this. It will be used automaticly.")
    public Boolean Inited = false;

    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Ban = 1;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Homes= 1;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Players = 1;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Portals = 1;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Spawns = 1;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Warps = 1;
}
