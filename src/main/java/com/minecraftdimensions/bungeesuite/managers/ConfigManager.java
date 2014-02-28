package com.minecraftdimensions.bungeesuite.managers;

import com.minecraftdimensions.bungeesuite.configs.Announcements;
import com.minecraftdimensions.bungeesuite.configs.BansConfig;
import com.minecraftdimensions.bungeesuite.configs.MainConfig;
import com.minecraftdimensions.bungeesuite.configs.Messages;
import com.minecraftdimensions.bungeesuite.configs.SpawnConfig;
import com.minecraftdimensions.bungeesuite.configs.TeleportConfig;

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
}
