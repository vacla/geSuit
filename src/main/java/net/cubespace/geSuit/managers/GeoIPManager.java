package net.cubespace.geSuit.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import net.cubespace.geSuit.geSuit;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import com.maxmind.geoip.regionName;

public class GeoIPManager {
    private static LookupService mLookup;
    private static File mDatabaseFile;
    
    public static void initialize() {
        if (ConfigManager.bans.GeoIP.ShowCity) {
            mDatabaseFile = new File(geSuit.instance.getDataFolder(), "GeoIPCity.dat");
        } else {
            mDatabaseFile = new File(geSuit.instance.getDataFolder(), "GeoIP.dat");
        }
        
        if (!mDatabaseFile.exists()) {
            if (ConfigManager.bans.GeoIP.DownloadIfMissing) {
                if (!updateDatabase()) {
                    return;
                }
            } else {
                geSuit.instance.getLogger().warning("[GeoIP] No GeoIP database is available locally and updating is off. Lookups will be unavailable");
                return;
            }
        }
        
        try {
            mLookup = new LookupService(mDatabaseFile);
        } catch(IOException e) {
            geSuit.instance.getLogger().warning("[GeoIP] Unable to read GeoIP database, if this No GeoIP database is available locally and updating is off. Lookups will be unavailable");
        }
    }
    
    public static String lookup(InetAddress address)
    {
        if (mLookup == null) { 
            return null;
        }
        
        if (ConfigManager.bans.GeoIP.ShowCity) {
            Location loc = mLookup.getLocation(address);
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
            return mLookup.getCountry(address).getName();
        }
    }
    
    private static boolean updateDatabase() {
        String url;
        if (ConfigManager.bans.GeoIP.ShowCity) {
            url = ConfigManager.bans.GeoIP.CityDownloadURL;
        } else {
            url = ConfigManager.bans.GeoIP.DownloadURL;
        }
        
        if (url == null || url.trim().isEmpty()) {
            geSuit.instance.getLogger().severe("[GeoIP] There is no configured update url!");
            return false;
        }
        
        try {
            geSuit.instance.getLogger().info("[GeoIP] Downloading GeoIP database... This may take a while");
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
            
            FileOutputStream output = new FileOutputStream(mDatabaseFile);
            byte[] buffer = new byte[2048];
            int length = input.read(buffer);
            while (length >= 0) {
                output.write(buffer, 0, length);
                
                current += length;
                if (System.currentTimeMillis() - lastNotify > 2000) {
                    if (total == -1) {
                        geSuit.instance.getLogger().info(String.format("[GeoIP] Downloading GeoIP database... %d bytes", current));
                    } else {
                        double percent = current / (double)total;
                        percent *= 100;
                        if (percent > 100) {
                            percent = 100;
                        }
                        geSuit.instance.getLogger().info(String.format("[GeoIP] Downloading GeoIP database... %.0f%%", percent));
                    }
                    lastNotify = System.currentTimeMillis();
                }
                
                length = input.read(buffer);
            }
            output.close();
            input.close();
            geSuit.instance.getLogger().info("[GeoIP] Download complete");
            return true;
        } catch(IOException e) {
            geSuit.instance.getLogger().severe("[GeoIP] Download failed. IOException: " + e.getMessage());
            return false;
        }
    }
}
