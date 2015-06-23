package net.cubespace.geSuit.config;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;

import java.io.File;
import java.util.HashMap;

public class ModerationConfig extends Config {
    public ModerationConfig(File file) {
        CONFIG_FILE = file;
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
    
    public static class GeoIPSettings extends Config {
        public boolean ShowOnLogin = true;
        
        public boolean ShowCity = true;
        public boolean DownloadIfMissing = true;

        @Comment("URL for the database that provides country level lookups only")
        public String DownloadURL = "http://geolite.maxmind.com/download/geoip/database/GeoLiteCountry/GeoIP.dat.gz";
        @Comment("URL for the database that provides city level lookups")
        public String CityDownloadURL = "http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz";
    }
}
