package net.cubespace.geSuit.moderation;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.ModuleManager;

import org.bukkit.plugin.java.JavaPlugin;

public class Bootstrap extends JavaPlugin {
    @Override
    public void onEnable() {
        GSPlugin plugin = JavaPlugin.getPlugin(GSPlugin.class);
        ModuleManager manager = plugin.getModuleManager();
        
        manager.registerModule(ModerationModule.class);
    }
}
