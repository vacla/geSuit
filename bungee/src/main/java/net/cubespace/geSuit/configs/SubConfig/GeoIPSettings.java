package net.cubespace.geSuit.configs.SubConfig;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.YamlConfig;

public class GeoIPSettings extends YamlConfig {
    
    public boolean ShowOnLogin = true;
    
    public boolean ShowCity = true;
    public boolean DownloadIfMissing = true;

    @Comment("URL for the database that provides country level lookups only")
    public String DownloadURL = "http://geolite.maxmind.com/download/geoip/database/GeoLiteCountry/GeoIP.dat.gz";
    @Comment("URL for the database that provides city level lookups")
    public String CityDownloadURL = "http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz";
}
