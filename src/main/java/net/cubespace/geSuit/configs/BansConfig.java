package net.cubespace.geSuit.configs;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.configs.SubConfig.GeoIPSettings;
import net.cubespace.Yamler.Config.Config;

import java.io.File;
import java.util.HashMap;

public class BansConfig extends Config {
    public BansConfig() {
        CONFIG_FILE = new File(geSuit.instance.getDataFolder(), "bans.yml");
    }

    public Boolean Enabled = true;
    public Boolean BroadcastBans = true;
    public Boolean BroadcastUnbans = false;
    public Boolean BroadcastWarns = true;
    public Boolean BroadcastKicks = true;
    public Boolean ShowAltAccounts = true;
    public Boolean ShowBannedAltAccounts = true;
    public Boolean TrackOnTime = true;
    public Boolean RecordKicks = false;  //record kicks to the database.
    public int NameChangeNotifyTime = 20;
    public int WarningExpiryDays = 180;
    public int KickExpiryDays = 10; //this is only used if record kicks is true;
    public int KickLimit = 0; // if 0 temp banning after kicks is off.
    public long KicksTimeOut = 600000; //time in ms that kicks are considered active
    public long TempBanTime = 300000; // Time in ms that a player is autobanned for
    public HashMap<Integer, String> Actions = new HashMap<>();
    
    public GeoIPSettings GeoIP = new GeoIPSettings();
}
