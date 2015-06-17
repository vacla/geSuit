package net.cubespace.geSuit.general;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.managers.ConfigManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import com.maxmind.geoip.regionName;

public class GeoIPLookup {
    private LookupService lookup;
    private File databaseFile;
    
    public void initialize() {
        if (ConfigManager.bans.GeoIP.ShowCity) {
            databaseFile = geSuit.getFile("GeoIPCity.dat");
        } else {
            databaseFile = geSuit.getFile("GeoIP.dat");
        }
        
        if (!databaseFile.exists()) {
            if (ConfigManager.bans.GeoIP.DownloadIfMissing) {
                if (!updateDatabase()) {
                    return;
                }
            } else {
                geSuit.getLogger().warning("[GeoIP] No GeoIP database is available locally and updating is off. Lookups will be unavailable");
                return;
            }
        }
        
        try {
            lookup = new LookupService(databaseFile);
        } catch(IOException e) {
            geSuit.getLogger().warning("[GeoIP] Unable to read GeoIP database, if this No GeoIP database is available locally and updating is off. Lookups will be unavailable");
        }
    }
    
    public String lookup(InetAddress address)
    {
        if (lookup == null) { 
            return null;
        }
        
        if (ConfigManager.bans.GeoIP.ShowCity) {
            Location loc = lookup.getLocation(address);
            if (loc == null) {
                return null;
            }
            
            String region = regionName.regionNameByCode(loc.countryCode, loc.region);
            
            String result = "";
            if (loc.city != null) {
                result += loc.city + ", ";
            }
            
            if (region != null) {
                result += region + ", ";
            }
            
            result += loc.countryName;
            return result;
        } else {
            return lookup.getCountry(address).getName();
        }
    }
    
    private boolean updateDatabase() {
        String url;
        if (ConfigManager.bans.GeoIP.ShowCity) {
            url = ConfigManager.bans.GeoIP.CityDownloadURL;
        } else {
            url = ConfigManager.bans.GeoIP.DownloadURL;
        }
        
        if (url == null || url.trim().isEmpty()) {
            geSuit.getLogger().severe("[GeoIP] There is no configured update url!");
            return false;
        }
        
        try {
            geSuit.getLogger().info("[GeoIP] Downloading GeoIP database... This may take a while");
            long lastNotify = System.currentTimeMillis();
            URLConnection con = new URL(url).openConnection();
            con.setConnectTimeout(10000);
            con.connect();
            
            long total = con.getContentLengthLong();
            long current = 0;
            InputStream input = con.getInputStream();
            if (url.endsWith(".gz")) {
                input = new GZIPInputStream(input);
            }
            
            FileOutputStream output = new FileOutputStream(databaseFile);
            byte[] buffer = new byte[2048];
            int length = input.read(buffer);
            while (length >= 0) {
                output.write(buffer, 0, length);
                
                current += length;
                if (System.currentTimeMillis() - lastNotify > 2000) {
                    if (total == -1) {
                        geSuit.getLogger().info(String.format("[GeoIP] Downloading GeoIP database... %d bytes", current));
                    } else {
                        double percent = current / (double)total;
                        percent *= 100;
                        if (percent > 100) {
                            percent = 100;
                        }
                        geSuit.getLogger().info(String.format("[GeoIP] Downloading GeoIP database... %.0f%%", percent));
                    }
                    lastNotify = System.currentTimeMillis();
                }
                
                length = input.read(buffer);
            }
            output.close();
            input.close();
            geSuit.getLogger().info("[GeoIP] Download complete");
            return true;
        } catch(IOException e) {
            geSuit.getLogger().severe("[GeoIP] Download failed. IOException: " + e.getMessage());
            return false;
        }
    }
    
    public void addPlayerInfo(ProxiedPlayer player, GlobalPlayer gPlayer) {
        // Show Geo location notifications for player (if enabled)
        if (ConfigManager.bans.GeoIP.ShowOnLogin) {
            String location = lookup(player.getAddress().getAddress());
            if (location != null) {
                String msg = Global.getMessages().get("connect.geoip", "player", gPlayer.getDisplayName(), "location", location);
                Utilities.doBungeeChatMirror("StaffNotice", msg);
            }
        }
    }
}
