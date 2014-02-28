package net.cubespace.getSuit.managers;

import net.cubespace.getSuit.configs.Announcements;
import net.cubespace.getSuit.configs.BansConfig;
import net.cubespace.getSuit.configs.MainConfig;
import net.cubespace.getSuit.configs.Messages;
import net.cubespace.getSuit.configs.SpawnConfig;
import net.cubespace.getSuit.configs.TeleportConfig;
import net.cubespace.Yamler.Config.InvalidConfigurationException;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class ConfigManager {
    public static Announcements announcements = new Announcements();
    public static BansConfig bans = new BansConfig();
    public static MainConfig main = new MainConfig();
    public static SpawnConfig spawn = new SpawnConfig();
    public static TeleportConfig teleport = new TeleportConfig();
    public static Messages messages = new Messages();

    static {
        try {
            messages.init();
            announcements.init();
            bans.init();
            main.init();
            spawn.init();
            teleport.init();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
