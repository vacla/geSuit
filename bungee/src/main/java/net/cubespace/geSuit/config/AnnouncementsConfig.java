package net.cubespace.geSuit.config;

import net.cubespace.Yamler.Config.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class AnnouncementsConfig extends Config {
    public AnnouncementsConfig(File file) {
        CONFIG_FILE = file;
    }

    public Boolean Enabled = true;
    public HashMap<String, AnnouncementEntry> Announcements = new HashMap<>();
    
    /**
     * @author geNAZt (fabian.fassbender42@googlemail.com)
     */
    public static class AnnouncementEntry extends Config {
        public Integer Interval = 150;
        public ArrayList<String> Messages = new ArrayList<>();
    }
}


