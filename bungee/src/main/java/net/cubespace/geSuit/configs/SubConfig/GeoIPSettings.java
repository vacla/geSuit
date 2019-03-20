package net.cubespace.geSuit.configs.SubConfig;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.YamlConfig;

public class GeoIPSettings extends YamlConfig {
    
    public boolean ShowOnLogin = true;
    
    public boolean ShowCity = true;
    public boolean DownloadIfMissing = true;

    @Comment("URL for the database that provides country level lookups only")
    public String DownloadURL = "https://geolite.maxmind.com/download/geoip/database/GeoLite2-Country.tar.gz";
    @Comment("URL for the database that provides city level lookups")
    public String CityDownloadURL = "https://geolite.maxmind.com/download/geoip/database/GeoLite2-City.tar.gz";
}
