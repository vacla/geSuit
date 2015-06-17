package net.cubespace.geSuit.configs;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.geSuitPlugin;
import net.cubespace.geSuit.configs.SubConfig.GeoIPSettings;
import net.cubespace.Yamler.Config.Config;

import java.io.File;
import java.util.HashMap;

public class BansConfig extends Config {
    public BansConfig() {
        CONFIG_FILE = geSuit.getFile("bans.yml");
    }

    public Boolean Enabled = true;
    public Boolean BroadcastBans = true;
    public Boolean BroadcastUnbans = false;
    public Boolean BroadcastWarns = true;
    public Boolean BroadcastKicks = true;
    public Boolean ShowAltAccounts = true;
    public Boolean ShowBannedAltAccounts = true;
    public Boolean TrackOnTime = true;
    public int NameChangeNotifyTime = 20;

    public int WarningExpiryDays = 180;
    
    public String DefaultBanReason = "Unknown";
    public String DefaultWarnReason = "Unknown";
    public String DefaultKickReason = "Unknown";

    public HashMap<Integer, String> Actions = new HashMap<Integer, String>();
    
    public GeoIPSettings GeoIP = new GeoIPSettings();
}
