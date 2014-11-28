package net.cubespace.geSuit.configs;

import java.io.File;
import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.geSuit.configs.SubConfig.Database;
import net.cubespace.geSuit.geSuit;

public class MainConfig extends Config {
    public MainConfig() {
        CONFIG_FILE = new File(geSuit.instance.getDataFolder(), "config.yml");
    }

    public Database Database = new Database();

    @Comment("This can be used if you have multiple Proxies to seperate the Homes in it")
    public String Table_Homes = "homes";
    @Comment("This can be used if you have multiple Proxies to seperate the Players in it")
    public String Table_Players = "players";
    @Comment("This can be used if you have multiple Proxies to seperate the Warps in it")
    public String Table_Warps = "warps";
    @Comment("This can be used if you have multiple Proxies to seperate the Bans in it")
    public String Table_Bans = "bans";
    @Comment("This can be used if you have multiple Proxies to seperate the Portals in it")
    public String Table_Portals = "portals";
    @Comment("This can be used if you have multiple Proxies to seperate the Spawns in it")
    public String Table_Spawns = "spawns";
    @Comment("This can be used if you have multiple Proxies to seperate the Tracking in it")
    public String Table_Tracking = "tracking";
    @Comment("This can be used if you have multiple Proxies to seperate the Ontime in it")
    public String Table_OnTime = "ontime";

    public Boolean ConvertFromBungeeSuite = false;
    public Database BungeeSuiteDatabase = new Database();

    @Comment("Turn this to false if you want to use your regular /motd comand (requires restart)")
    public Boolean MOTD_Enabled = true;
    @Comment("Turn this to false if you want to use your your regular /seen comand (requires restart)")
    public Boolean Seen_Enabled = false;
    
    @Comment()
    public Boolean NewPlayerBroadcast = true;
    public Boolean BroadcastProxyConnectionMessages = true;
    public Integer PlayerDisconnectDelay = 10;
    @Comment("This should be true on offline Mode Server since they can't use UUIDs provided by Mojang")
    public Boolean OverwriteUUID = false;

    @Comment("Do not alter this. It will be used automaticly.")
    public Boolean Inited = false;

    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Ban = 3;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Homes = 2;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Players = 2;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Portals = 1;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Spawns = 1;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Warps = 1;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Tracking = 1;
}
