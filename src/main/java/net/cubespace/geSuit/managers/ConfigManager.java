package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.configs.*;
import net.cubespace.Yamler.Config.InvalidConfigurationException;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class ConfigManager {
    public static Announcements announcements = new Announcements();
    public static BansConfig bans = new BansConfig();
    public static LockDownConfig lockdown = new LockDownConfig();
    public static MainConfig main = new MainConfig();
    public static SpawnConfig spawn = new SpawnConfig();
    public static TeleportConfig teleport = new TeleportConfig();
    public static Messages messages = new Messages();
    public static MOTDFile motd = new MOTDFile("motd.txt");
    public static MOTDFile motdNew = new MOTDFile("motd-new.txt");

    static {
        try {
            messages.init();
            announcements.init();
            bans.init();
            lockdown.init();
            main.init();
            spawn.init();
            teleport.init();
            motd.init();
            motdNew.init();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
