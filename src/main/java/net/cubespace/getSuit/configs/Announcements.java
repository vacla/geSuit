package net.cubespace.getSuit.configs;

import net.cubespace.getSuit.BungeeSuite;
import net.cubespace.getSuit.configs.SubConfig.AnnouncementEntry;
import net.cubespace.Yamler.Config.Config;

import java.io.File;
import java.util.HashMap;

public class Announcements extends Config {
    public Announcements() {
        CONFIG_FILE = new File(BungeeSuite.instance.getDataFolder(), "announcements.yml");
    }

    public Boolean Enabled = true;
    public HashMap<String, AnnouncementEntry> Announcements = new HashMap<>();
}


