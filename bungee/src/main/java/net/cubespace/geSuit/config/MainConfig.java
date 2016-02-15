package net.cubespace.geSuit.config;

import java.io.File;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Config;

public class MainConfig extends Config {
    public MainConfig(File file) {
        CONFIG_FILE = file;
    }

    public Database Database = new Database();
    public Redis Redis = new Redis();
    
    @Comments({"The language file to use. This file can either be in the jar or inside",
            "the folder 'lang/' in the plugins. Lang files must end in '.lang'"})
    public String Lang = "en_US";

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
    public Integer PlayerDisconnectDelay = 0;
    @Comment("This should be true on offline Mode Server since they can't use UUIDs provided by Mojang")
    public Boolean OverwriteUUID = false;
    
    @Comment("Enable this if you want to use BungeeChat with geSuit.")
    public Boolean BungeeChatIntegration = false;

    @Comment("Do not alter this. It will be used automatically.")
    public Boolean Inited = false;

    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Ban = 3;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Homes = 2;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Players = 3;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Portals = 1;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Spawns = 1;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Warps = 1;
    @Comment("Stored version informations. If you alter this you can damage your Database")
    public Integer Version_Database_Tracking = 1;
    
    public static class Redis extends Config {
        public String host = "localhost";
        public String password = "";
        
        public int port = 6379;
    }
    
    /**
     * @author geNAZt (fabian.fassbender42@googlemail.com)
     */
    public static class Database extends Config {
        public String Host = "localhost";
        public String Database = "minecraft";
        public String Port = "3306";
        public String Username = "username";
        public String Password = "password";
        public Integer Threads = 5;
        
        public String NameBanhistory = "banhistory";
        public String NameWarnhistory = "warnhistory";
        public String NameOntime = "ontime";
        public String NameTracking = "tracking";
    }
}
