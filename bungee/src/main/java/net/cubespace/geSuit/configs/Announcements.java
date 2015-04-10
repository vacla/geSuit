package net.cubespace.geSuit.configs;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.configs.SubConfig.AnnouncementEntry;
import net.cubespace.Yamler.Config.Config;

import java.util.HashMap;

public class Announcements extends Config {
    public Announcements() {
        CONFIG_FILE = geSuit.getFile("announcements.yml");
    }

    public Boolean Enabled = true;
    public HashMap<String, AnnouncementEntry> Announcements = new HashMap<>();
}


